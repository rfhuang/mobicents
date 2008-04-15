package javax.servlet.sip;
/**
 * Causes applications to be notified of various error conditions occurring during regular SIP transaction processing.
 */
public interface SipErrorListener extends java.util.EventListener{
    /**
     * Invoked by the servlet container to notify an application that no ACK was received for an INVITE transaction for which a final response has been sent upstream.
     * This method is invoked for UAS applications only and not for applications that proxied the INVITE.
     */
    void noAckReceived(javax.servlet.sip.SipErrorEvent ee);

    /**
     * Invoked by the servlet container for applications acting as a UAS when no PRACK was received for a previously sent reliable provisional response. It is then up to the application to generate the 5xx response reccommended by RFC 3262 for the INVITE transaction. The original INVITE request as well as the unacknowledged reliable response is available from the SipErrorEvent argument.
     */
    void noPrackReceived(javax.servlet.sip.SipErrorEvent ee);

}
