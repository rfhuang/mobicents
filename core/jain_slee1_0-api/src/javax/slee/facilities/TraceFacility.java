package javax.slee.facilities;

import javax.slee.ComponentID;
import javax.slee.UnrecognizedComponentException;

/**
 * The Trace Facility is used by SBBs (and other components as determined by
 * the SLEE vendor) to generate trace notifications intended for consumption by
 * management clients external to the SLEE.  For example, an SCE may attach a trace
 * listener in order to deliver trace message output to the Service developer.
 * Management clients register register interest in receiving trace notifictions using
 * the SLEE's {@link javax.slee.management.TraceMBean} object.
 * <p>
 * The Trace Facility is non-transactional.  The effects of operations invoked on this
 * facility occur regardless of the state or outcome of any enclosing transaction.
 * <p>
 * <dl>
 *   <dt><b>SBB JNDI Location:</b>
 *   <dd><code>java:comp/env/slee/facilities/trace</code>
 * </dl>
 *
 * @see javax.slee.management.TraceMBean
 * @see javax.slee.management.TraceNotification
 */
public interface TraceFacility {
    /**
     * Get the current trace filter level set for a component identifier.
     * <p>
     * This method is a non-transactional method.
     * @param messageSource the component identifier.
     * @return the current trace filter level for the identified component.
     * @throws NullPointerException if <code>id</code> is <code>null</code>.
     * @throws UnrecognizedComponentException if <code>id</code> is not a recognizable
     *        <code>ComponentID</code> object for the SLEE or it does not correspond
     *        with a component installed in the SLEE.
     * @throws FacilityException if the trace level could not be obtained due to a
     *        system-level failure.
     */
    public Level getTraceLevel(ComponentID messageSource)
        throws NullPointerException, UnrecognizedComponentException, FacilityException;

    /**
     * Emit a trace notification containing the specified trace message.  The trace
     * notification will only be generated by the TraceMBean if the trace filter level
     * of the specified component identifer does not cause the trace message to be filtered.
     * <p>
     * This method is a non-transactional method.
     * @param messageSource the identifer of the component that is emitting the trace message.
     * @param traceLevel the logging level of the trace message.
     * @param messageType a string denoting the type of the trace message.
     * @param message the trace message.
     * @param timeStamp the time (in ms since January 1, 1970 UTC) that the message was generated.
     * @throws NullPointerException if any parameter is <code>null</code>.
     * @throws IllegalArgumentException if <code>traceLevel == </code>{@link Level#OFF}.
     * @throws UnrecognizedComponentException if <code>messageSource</code> is not a recognizable
     *        <code>ComponentID</code> object for the SLEE or it does not correspond
     *        with a component installed in the SLEE.
     * @throws FacilityException if the trace could not be generated due to a system-level failure.
     */
    public void createTrace(ComponentID messageSource, Level traceLevel, String messageType, String message, long timeStamp)
        throws NullPointerException, IllegalArgumentException, UnrecognizedComponentException, FacilityException;

    /**
     * Emit a trace notification containing the specified trace message and cause throwable.
     * The trace notification will only be generated by the TraceMBean if the trace filter level
     * of the specified component identifer does not cause the trace message to be filtered.
     * <p>
     * This method is a non-transactional method.
     * @param messageSource the identifer of the component that is emitting the trace message.
     * @param traceLevel the logging level of the trace message.
     * @param messageType a string denoting the type of the trace message.
     * @param message the trace message.
     * @param cause the reason (if any) this trace message was generated.
     * @param timeStamp the time (in ms since January 1, 1970 UTC) that the message was generated.
     * @throws NullPointerException if any parameter is <code>null</code>.
     * @throws IllegalArgumentException if <code>traceLevel == </code>{@link Level#OFF}.
     * @throws UnrecognizedComponentException if <code>messageSource</code> is not a recognizable
     *        <code>ComponentID</code> object for the SLEE or it does not correspond
     *        with a component installed in the SLEE.
     * @throws FacilityException if the trace could not be generated due to a system-level failure.
     */
    public void createTrace(ComponentID messageSource, Level traceLevel, String messageType, String message, Throwable cause, long timeStamp)
        throws NullPointerException, IllegalArgumentException, UnrecognizedComponentException, FacilityException;
}

