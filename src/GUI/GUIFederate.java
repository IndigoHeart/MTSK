package GUI;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.Random;

public class GUIFederate {
    private RTIambassador rtiamb;
    private GUIAmbassador fedamb;

    int liczbaWkolejkach;
    int liczbaOtwartychKas;

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

        fedamb = new GUIAmbassador(this);
        rtiamb.joinFederationExecution( "GUIFederate", "CashDeskClientFederation", fedamb );
        log( "Joined Federation as GUIFederate");

        rtiamb.registerFederationSynchronizationPoint( GUIAmbassador.READY_TO_RUN, null );

        while( fedamb.isAnnounced == false ){
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( GUIAmbassador.READY_TO_RUN );
        log( "Achieved sync point: " + GUIAmbassador.READY_TO_RUN+ ", waiting for federation..." );

        while( fedamb.isReadyToRun == false ){
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();



        while (fedamb.running) {
            advanceTime(1);
        }
    }

    private void log( String message ){
        System.out.println( "GUIFederate   : " + message );
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
        //przejdz do kolejki publish interaction
        int liczbaWkolejkachHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.liczbaWkolejkach" );
        rtiamb.subscribeInteractionClass(liczbaWkolejkachHandle);

        int liczbaOtwartychKasHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.liczbaOtwartychKas" );
        rtiamb.subscribeInteractionClass(liczbaOtwartychKasHandle);
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

    public static void main( String[] args ){
        try{
            new GUIFederate().runFederate();
        }
        catch( RTIexception rtie ){
            rtie.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}