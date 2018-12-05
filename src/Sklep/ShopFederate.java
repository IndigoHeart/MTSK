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
import java.util.Random;

public class ShopFederate {
    private RTIambassador rtiamb;
    private ShopAmbassador fedamb;

LinkedList<Client> klienciWsklepie =new LinkedList<Client>();

    public void runFederate() throws RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
        try{
            File fom = new File( "CashDesk.xml" );
            rtiamb.createFederationExecution( "CashdeskCilentFederation",
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
        rtiamb.joinFederationExecution( "ShopFederate", "CashdeskCilentFederation", fedamb );
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
        }
    }
}
