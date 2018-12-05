package Kasa;

import Client.Client;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;

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
            }catch(ArrayIndexOutOfBounds ignored){ }
        }

        /*if(interactionClass == najkrotszaKolejkaHandle) {
            try {
                fed.numberQueue = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                builder.append(" NajkrotszaKolejka , number=" + fed.numberQueue);
            } catch (ArrayIndexOutOfBounds ignored) {
            }
        }
        log( builder.toString() );*/
    }
}
