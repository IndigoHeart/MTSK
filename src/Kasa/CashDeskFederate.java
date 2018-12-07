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
import java.util.NoSuchElementException;
import java.util.Random;

public class CashDeskFederate {
    private RTIambassador rtiamb;
    private CashDeskAmbassador fedamb;

    public LinkedList<CashDesk> cashdeskList;

    public void runFederate() throws RTIexception {
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

        fedamb = new CashDeskAmbassador(this);
        rtiamb.joinFederationExecution( "CashDeskFederate", "CashDeskClientFederation", fedamb );
        log( "Joined Federation as CashDeskFederate");

        rtiamb.registerFederationSynchronizationPoint( CashDeskAmbassador.READY_TO_RUN, null );

        cashdeskList = new LinkedList<CashDesk>();
        cashdeskList.add(new CashDesk());

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
            advanceTime(30);
            sendInteractionliczbaWkolejkach(fedamb.federateTime+fedamb.federateLookahead);
            sendInteractionliczbaOtwartychKas(fedamb.federateTime+fedamb.federateLookahead);
            for(int i=0; i < cashdeskList.size();i++){
                System.out.println("suma w kasie " + cashdeskList.get(i).suma + " nr "
                        + cashdeskList.get(i).getCashdeskNumber()
                        + " isOpen " + cashdeskList.get(i).getOpen());
            }

            for(int i=0; i < cashdeskList.size();i++){
                manageCashDesk(cashdeskList.get(i));
            }
            for(int i=0; i < cashdeskList.size();i++){
                if(cashdeskList.get(i).suma==0 && cashdeskList.size() > 1){
                    cashdeskList.remove(i);
                }
            }

            //sprawdzamy czy trzeba zablokować kasę bo jest ich za dużo
            // (x-1)*y>=sumAllOpen
            // x - liczba otwartych kas
            // y - limit kolejki do kasy
            // sumAllOpen - suma klientów we wszystkich kolejkach otwartych kas
            if(cashdeskList.size()>1) {
                while (fedamb.queueMaxSize * (fedamb.getOpenCashDesk(cashdeskList).size() - 1) >
                        CountAllClients(fedamb.getOpenCashDesk(cashdeskList))) {
                    LinkedList<CashDesk> openCashDesks = fedamb.getOpenCashDesk(cashdeskList);
                    int queueNr = fedamb.findSmallestNonPrivilegedQueue(openCashDesks);
                    Boolean flag = true;
                    while (flag) {
                        int i = 0;
                        if (cashdeskList.get(i).getCashdeskNumber() == openCashDesks.get(queueNr).getCashdeskNumber()) {
                            cashdeskList.get(i).setOpen(false);
                            System.out.println("Zablokowano kolejkę nr " + cashdeskList.get(i).getCashdeskNumber());
                            flag = false;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
    }

    private void manageCashDesk(CashDesk cd){
        cd.decrementServiceTime();
    }

    private void log( String message ){
        System.out.println( "CashDeskFederate   : " + message );
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

    private void sendInteractionliczbaWkolejkach(double timeStep) throws RTIexception {
        try{
            SuppliedParameters parameters =
                    RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

            byte[] liczbaWkolejkach = EncodingHelpers.encodeInt(CountAllClients(cashdeskList));

            int liczbaWkolejkachHandle = rtiamb.getInteractionClassHandle("InteractionRoot.liczbaWkolejkach");
            int liczbaHandle = rtiamb.getParameterHandle( "liczbaWkolejkach", liczbaWkolejkachHandle );

            parameters.add(liczbaHandle, liczbaWkolejkach);

            LogicalTime time = convertTime( timeStep );
            rtiamb.sendInteraction( liczbaWkolejkachHandle, parameters, "tag".getBytes(), time );
        }catch (NoSuchElementException e){
        }

    }

    private void sendInteractionliczbaOtwartychKas(double timeStep) throws RTIexception {
        try{
            SuppliedParameters parameters =
                    RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

            byte[] liczbaOtwartychKas = EncodingHelpers.encodeInt(cashdeskList.size());

            int liczbaOtwartychKasHandle = rtiamb.getInteractionClassHandle("InteractionRoot.liczbaOtwartychKas");
            int liczbaHandle = rtiamb.getParameterHandle( "liczbaOtwartychKas", liczbaOtwartychKasHandle );

            parameters.add(liczbaHandle, liczbaOtwartychKas);

            LogicalTime time = convertTime( timeStep );
            rtiamb.sendInteraction( liczbaOtwartychKasHandle, parameters, "tag".getBytes(), time );
        }catch (NoSuchElementException e){
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


        //przejdz do kolejki subscribe interaction
        int przejdzDoKolejkiHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.przejdzDoKolejki" );
        rtiamb.subscribeInteractionClass(przejdzDoKolejkiHandle);
        fedamb.przejdzDoKolejkiHandle=przejdzDoKolejkiHandle;

        //liczba w kolejkach publish interaction
        int liczbaWkolejkachHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.liczbaWkolejkach" );
        rtiamb.publishInteractionClass(liczbaWkolejkachHandle);
        //liczba liczba otwartych kas publish interaction
        int liczbaOtwartychKasHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.liczbaOtwartychKas" );
        rtiamb.publishInteractionClass(liczbaOtwartychKasHandle);


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
