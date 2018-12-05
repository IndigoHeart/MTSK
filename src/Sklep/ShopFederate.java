package Sklep;

import Client.Client;
import Client.ClientAmbassador;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ShopFederate {
    private RTIambassador rtiamb;
    private ShopAmbassador fedamb;

    public LinkedList<Client> klienciWsklepie = new LinkedList<Client>();

    public void runFederate() throws RTIexception, InterruptedException {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
        try{
            File fom = new File( "CashDesk.xml" );
            rtiamb.createFederationExecution( "CashDeskClientFederation",
                    fom.toURI().toURL() );
            log( "Created Federation" );
        }
        catch( FederationExecutionAlreadyExists exists ){
            log( "Didn't create federation, it already existed" );
        }
        catch( MalformedURLException urle ){
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        fedamb = new ShopAmbassador(this);
        rtiamb.joinFederationExecution( "ShopFederate", "CashDeskClientFederation", fedamb );
        log( "Joined Federation as ShopFederate");

        rtiamb.registerFederationSynchronizationPoint( ClientAmbassador.READY_TO_RUN, null );

        while( fedamb.isAnnounced == false ){
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( ClientAmbassador.READY_TO_RUN );
        log( "Achieved sync point: " +ClientAmbassador.READY_TO_RUN+ ", waiting for federation..." );

        while( fedamb.isReadyToRun == false ){
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();



        while (fedamb.running) {
            advanceTime(randomTime());
            System.out.println(" klientów w sklepie jest "+klienciWsklepie.size());
            //Shop Shop=new Shop(randomizePrivileg(20), (int)(25*randomTime()));
            //publishShop(Shop);
            advanceTime(50);
            sendInteractionprzejdzDoKolejki(fedamb.federateTime + fedamb.federateLookahead);

        }
    }

    private void log( String message ){
        System.out.println( "ShopFederate   : " + message );
    }

    void zarzadzaj(){

    }
    private void waitForUser(){
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try{
            reader.readLine();
        }
        catch( Exception e ){
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    boolean randomizePrivileg(int p){
        Random random = new Random();
        int x = random.nextInt()*100;
        if(x<p){
            return true;
        }
        else
            return false;
    }

    private LogicalTime convertTime(double time ){
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time ){
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    private void enableTimePolicy() throws RTIexception{
        LogicalTime currentTime = convertTime( fedamb.federateTime );
        LogicalTimeInterval lookahead = convertInterval( fedamb.federateLookahead );

        this.rtiamb.enableTimeRegulation( currentTime, lookahead );

        while( fedamb.isRegulating == false ){
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while( fedamb.isConstrained == false ){
            rtiamb.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception{
        int klientHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Client");
        int przywilejHandle    = rtiamb.getAttributeHandle( "przywilej", klientHandle );
        int timeHandle = rtiamb.getAttributeHandle("time", klientHandle);

        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( przywilejHandle );
        attributes.add(timeHandle);

        rtiamb.subscribeObjectClassAttributes(klientHandle,attributes);

        //przejdz do kolejki publish interaction
        int przejdzDoKolejkiHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.przejdzDoKolejki" );
        rtiamb.publishInteractionClass(przejdzDoKolejkiHandle);
        //int privilegedHandle = rtiamb.getParameterHandle(przejdzDoKolejkiHandle, "InteractionRoot.")

    }

    public Boolean goToQueue(){
            return klienciWsklepie.remove().getPrivileged();
    }

    private void sendInteractionprzejdzDoKolejki(double timeStep) throws RTIexception {
        try{
            SuppliedParameters parameters =
                    RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

            byte[] privileged = EncodingHelpers.encodeBoolean(goToQueue());

            int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.przejdzDoKolejki");
            int privilegedHandle = rtiamb.getParameterHandle( "privileged", interactionHandle );

            parameters.add(privilegedHandle, privileged);

            LogicalTime time = convertTime( timeStep );
            rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
        }catch (NoSuchElementException e){
        }

    }

    private void advanceTime( double timestep ) throws RTIexception
    {
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime( fedamb.federateTime + timestep );
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.tick();
        }
    }

    private double randomTime(){
        Random r = new Random();
        return (double)(300 + r.nextInt(10));
    }

    private int randomProductNumber(){
        Random r = new Random();
        return 1 + r.nextInt(9);
    }



    public static void main( String[] args ){
        try{
            new ShopFederate().runFederate();
        }
        catch( RTIexception rtie ){
            rtie.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
