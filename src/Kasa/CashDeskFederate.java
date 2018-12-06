package Kasa;

import Client.Client;
import Sklep.ShopAmbassador;
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

public class CashDeskFederate {
    private RTIambassador rtiamb;
    private CashDeskAmbassador fedamb;

    LinkedList<CashDesk> cashdeskList;

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

        fedamb = new CashDeskAmbassador(this);
        rtiamb.joinFederationExecution( "CashDeskFederate", "CashdeskCilentFederation", fedamb );
        log( "Joined Federation as CashDeskFederate");

        rtiamb.registerFederationSynchronizationPoint( CashDeskAmbassador.READY_TO_RUN, null );

        cashdeskList = new LinkedList<>();

        while( fedamb.isAnnounced == false ){
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( CashDeskAmbassador.READY_TO_RUN );
        log( "Achieved sync point: " + CashDeskAmbassador.READY_TO_RUN+ ", waiting for federation..." );

        while( fedamb.isReadyToRun == false ){
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();



        while (fedamb.running) {
            advanceTime(randomTime());

        }
    }

    private void log( String message ){
        System.out.println( "CashDeskFederate   : " + message );
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
        rtiamb.subscribeInteractionClass(przejdzDoKolejkiHandle);
        fedamb.przejdzDoKolejkiHandle=przejdzDoKolejkiHandle;


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
            new CashDeskFederate().runFederate();
        }
        catch( RTIexception rtie ){
            rtie.printStackTrace();
        }
    }

    public int CountAllClients(LinkedList<CashDesk> list){
        int sumAll=0;
        for(int i=0; i<list.size();i++){
            sumAll += list.get(i).suma;
        }
        return sumAll;
    }

}
