package Kasa;

import Client.Client;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.LinkedList;

public class CashDeskAmbassador extends NullFederateAmbassador {

    public static final String READY_TO_RUN = "ReadyToRun";
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    protected boolean running 			 = true;

    protected int przejdzDoKolejkiHandle;
    public static int queueMaxSize = 10;
    private CashDeskFederate fed;


    public CashDeskAmbassador(CashDeskFederate fed){
        this.fed = fed;
    }

    private double convertTime( LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    private void log( String message )
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(CashDeskAmbassador.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(CashDeskAmbassador.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.federateTime = convertTime( theTime );
        this.isAdvancing = false;
    }
    public void discoverObjectInstance( int theObject,
                                        int theObjectClass,
                                        String objectName )
    {
        log( "Discoverd Object: handle=" + theObject + ", classHandle=" +
                theObjectClass + ", name=" + objectName );
    }

    public void reflectAttributeValues( int theObject,
                                        ReflectedAttributes theAttributes,
                                        byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        reflectAttributeValues( theObject, theAttributes, tag, null, null );
    }

    public void reflectAttributeValues( int theObject,
                                        ReflectedAttributes theAttributes,
                                        byte[] tag,
                                        LogicalTime theTime,
                                        EventRetractionHandle retractionHandle )
    {

    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag,
                                    LogicalTime theTime,
                                    EventRetractionHandle eventRetractionHandle )
    {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        if(interactionClass == przejdzDoKolejkiHandle){
            try{
                Boolean privileged = EncodingHelpers.decodeBoolean(theInteraction.getValue(0));
                manageCashDeskQueue(fed.cashdeskList ,privileged);
            }catch(ArrayIndexOutOfBounds ignored){ }
        }

    }

    private void manageCashDeskQueue(LinkedList<CashDesk> cashdeskList, Boolean privileged) {
        if(cashdeskList.size()*queueMaxSize < fed.CountAllClients(cashdeskList)){
            addClientToQueue(cashdeskList, privileged);
        }
        else{
            openNewCashDesk(cashdeskList, privileged);
        }
    }

    private void addClientToQueue(LinkedList<CashDesk> cdList, Boolean priv){
        if(priv==true){
            int smallestQueueNr = findSmallestPrivilegedQueue(cdList);
            addPrivClient(cdList.get(smallestQueueNr));
        }else{
            int smallestQueueNr = findSmallestNonPrivilegedQueue(cdList);
            addNonPrivClient(cdList.get(smallestQueueNr));
        }
    }

    //sprawdz + dodawanie tylko do otwartych kas
    private int findSmallestPrivilegedQueue(LinkedList<CashDesk> cdList){
        int queueNr=0;
        LinkedList<CashDesk> openCashDeskList = getOpenCashDesk(cdList);
        for(int i=0; i<cdList.size()-1;i++){
            if(openCashDeskList.get(i).getPrivilegedQueue() < openCashDeskList.get(i+1).getPrivilegedQueue()){
                queueNr=i;
            }else{
                queueNr=i+1;
            }
        }
        return queueNr;
    }

    //sprawdz + dodawanie tylko do otwartych kas
    public int findSmallestNonPrivilegedQueue(LinkedList<CashDesk> cdList){
        int queueNr=0;
        LinkedList<CashDesk> openCashDeskList = getOpenCashDesk(cdList);
        for(int i=0; i<openCashDeskList.size()-1;i++){
            if(openCashDeskList.get(i).getSuma() < openCashDeskList.get(i+1).getSuma()){
                queueNr=i;
            }else{
                queueNr=i+1;
            }
    }
        return queueNr;
    }

    private void openNewCashDesk(LinkedList<CashDesk> cdList, Boolean priv){
        cdList.add(new CashDesk());
        if(priv==true){
            cdList.getLast().addPrivilegedQueue();
        }else{
            cdList.getLast().addNonPrivilegedQueue();
        }
    }

    public LinkedList<CashDesk> getOpenCashDesk(LinkedList<CashDesk> cdList){
        LinkedList<CashDesk> openCashDeskList = new LinkedList<CashDesk>();
        for(int i=0; i<cdList.size();i++){
            if(cdList.get(i).isOpen==true)
                openCashDeskList.add(cdList.get(i));
        }
        return openCashDeskList;
    }

    private void addPrivClient(CashDesk cd){
        cd.addPrivilegedQueue();
    }

    private void addNonPrivClient(CashDesk cd){
        cd.addNonPrivilegedQueue();
    }
}
