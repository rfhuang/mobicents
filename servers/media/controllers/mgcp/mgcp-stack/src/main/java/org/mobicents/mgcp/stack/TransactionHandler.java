/*
 * File Name     : TransactionHandle.java
 *
 * The JAIN MGCP API implementaion.
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */
package org.mobicents.mgcp.stack;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.Notify;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Implements the base gateway control interface.
 * 
 * The MGCP implements the media gateway control interface as a set of
 * transactions. The transactions are composed of a command and a mandatory
 * response. There are eight types of command:
 * 
 * <li> CreateConnection ModifyConnection DeleteConnection NotificationRequest
 * Notify AuditEndpoint AuditConnection RestartInProgress </li>
 * 
 * The first four commands are sent by the Call Agent to a gateway. The Notify
 * command is sent by the gateway to the Call Agent. The gateway may also send a
 * DeleteConnection. The Call Agent may send either of the Audit commands to the
 * gateway. The Gateway may send a RestartInProgress command to the Call Agent.
 * 
 * All commands are composed of a Command header, optionally followed by a
 * session description.
 * 
 * All responses are composed of a Response header, optionally followed by a
 * session description.
 * 
 * Headers and session descriptions are encoded as a set of text lines,
 * separated by a line feed character. The headers are separated from the
 * session description by an empty line.
 * 
 * MGCP uses a transaction identifier to correlate commands and responses. The
 * transaction identifier is encoded as a component of the command header and
 * repeated as a component of the response header.
 * 
 * Transaction identifiers have values between 1 and 999999999. An MGCP entity
 * shall not reuse a transaction identifier sooner than 3 minutes after
 * completion of the previous command in which the identifier was used.
 * 
 * @author Oleg Kulikov
 * @author Pavel Mitrenko
 */
public abstract class TransactionHandler {

	private static int GENERATOR = 1;

	public final static int TIMEOUT = 5000; // 5secs
	/** Is this a transaction on a command sent or received? */
	private boolean sent;
	/** Transaction handle sent from application to the MGCP provider. */
	private int remoteTID;
	/** Transaction handle sent from MGCP provider to MGCP listener */
	private int localTID;
	protected JainMgcpStackImpl stack;
	/** Holds the address from wich request was originaly received by provider */
	private InetAddress remoteAddress;
	/**
	 * Holds the port number from wich request was originaly received by
	 * provider
	 */
	private int remotePort;
	/** Used to hold parsed command event */
	protected JainMgcpCommandEvent commandEvent;
	/** Logger instance */
	private Logger logger = Logger.getLogger(TransactionHandler.class);
	/** Expiration timer */
	private static Timer timer = new Timer();
	private TransactionTimerTask timerTask;

	private class TransactionTimerTask extends TimerTask {

		public void run() {
			if (logger.isDebugEnabled()) {
				logger.debug("Transaction localID=" + localTID + " timeout");
			}
			try {
				// the try ensures the static timer will not get a runtime
				// exception
				// process tx timeout
				if (sent) {
					stack.provider.processTxTimeout(commandEvent);
				} else {
					stack.provider.processRxTimeout(commandEvent);
				}
				// releases the tx
				release();
			} catch (Exception e) {
				logger.error("Failed to release mgcp transaction localID=" + localTID, e);
			}
		}
	}

	/**
	 * Check whether the given return code is a provisional response.
	 * 
	 * @param rc
	 *            the return code
	 * @return true when the code is provisional
	 */
	private static boolean isProvisional(ReturnCode rc) {
		final int rval = rc.getValue();

		return ((99 < rval) && (rval < 200));
	}

	/**
	 * Creates a new instance of TransactionHandle
	 * 
	 * Used by provider to prepare origination transaction for sending command
	 * message from an application to the stack.
	 * 
	 * @param stack
	 *            the reference to the MGCP stack.
	 */
	public TransactionHandler(JainMgcpStackImpl stack) {
		this.stack = stack;
		this.localTID = GENERATOR++;
		stack.transactions.put(Integer.valueOf(localTID), this);
		if (logger.isDebugEnabled()) {
			logger.debug("New mgcp transaction with id localID=" + localTID);
		}
	}

	/**
	 * Creates a new instance of TransactionHandle.
	 * 
	 * Used by stack to prepare transaction for transmitting message from
	 * provider to the application.
	 * 
	 * @param stack
	 *            the reference to the MGCP stack.
	 * @remoteAddress the address from wich command message was received.
	 * @port the number of the port from wich command received.
	 */
	public TransactionHandler(JainMgcpStackImpl stack, InetAddress remoteAddress, int port) {
		this(stack);
		this.remoteAddress = remoteAddress;
		this.remotePort = port;
	}

	/** Release this transaction and frees all allocated resources. */
	protected void release() {
		if (logger.isDebugEnabled()) {
			logger.debug("Released transaction (local id=" + localTID + "), stop timer");
		}

		stack.transactions.remove(Integer.valueOf(localTID));

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	/**
	 * Returns the transaction handle sent from application to the MGCP
	 * provider.
	 * 
	 * @return the int value wich identifiers the transaction handle.
	 */
	public int getRemoteTID() {
		return remoteTID;
	}

	/**
	 * Returns the transaction handle sent from MGCP provider to listener.
	 * 
	 * @return the int value wich identifiers the transaction handle.
	 */
	public int getLocalTID() {
		return localTID;
	}

	/**
	 * Encodes command event object into MGCP command message.
	 * 
	 * All descendant classes should implement this method with accordance of
	 * the command type.
	 * 
	 * @param event
	 *            the command event object.
	 * @return the encoded MGCP message.
	 */
	protected abstract String encode(JainMgcpCommandEvent event);

	/**
	 * Encodes response event object into MGCP response message.
	 * 
	 * All descendant classes should implement this method with accordance of
	 * the response type.
	 * 
	 * @param event
	 *            the response event object.
	 * @return the encoded MGCP message.
	 */
	protected abstract String encode(JainMgcpResponseEvent event);

	/**
	 * Decodes MGCP command message into jain mgcp command event object.
	 * 
	 * All descendant classes should implement this method with accordance of
	 * the command type.
	 * 
	 * @param MGCP
	 *            message
	 * @return jain mgcp command event object.
	 */
	protected abstract JainMgcpCommandEvent decodeCommand(String message) throws ParseException;

	/**
	 * Decodes MGCP response message into jain mgcp response event object.
	 * 
	 * All descendant classes should implement this method with accordance of
	 * the command type.
	 * 
	 * @param MGCP
	 *            message
	 * @return jain mgcp response event object.
	 */
	protected abstract JainMgcpResponseEvent decodeResponse(String message) throws ParseException;

	/**
	 * Sends MGCP command from the application to the endpoint specified in the
	 * message.
	 * 
	 * @param event
	 *            the jain mgcp command event object.
	 */
	public void send(JainMgcpCommandEvent event) {

		sent = true;

		String host = null;
		int port = 0;

		// save command to later link it with response
		this.commandEvent = event;

		switch (event.getObjectIdentifier()) {
		case Constants.CMD_NOTIFY:
			Notify notifyCommand = (Notify) event;
			NotifiedEntity notifiedEntity = notifyCommand.getNotifiedEntity();
			port = notifiedEntity.getPortNumber();
			host = notifiedEntity.getLocalName();
			break;

		default:
			// determite destination address and port to send request to
			// from endpoint identifier parameter.
			String domainName = event.getEndpointIdentifier().getDomainName();

			// now checks does port number is specified in the domain name
			// if port number is not specified use 2427 by default
			int pos = domainName.indexOf(':');
			if (pos > 0) {
				port = Integer.parseInt(domainName.substring(pos + 1));
				host = domainName.substring(0, pos);
			} else {
				port = 2427;
				host = domainName;
			}
			break;
		}

		// construct the destination as InetAddress object
		InetAddress address = null;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Unknown endpoint " + host);
		}

		// save this tx in stack and start timer
		remoteTID = event.getTransactionHandle();
		event.setTransactionHandle(localTID);

		// encode event object as MGCP command and send over UDP.
		String msg = encode(event);
		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

		resetTimer();

		if (logger.isDebugEnabled()) {
			logger.debug("Send command event to " + address + ", message\n" + msg);
		}
		stack.send(packet);
	}

	/**
	 * Sends MGCP response message from the application to the host from wich
	 * origination command was received.
	 * 
	 * @param event
	 *            the jain mgcp response event object.
	 */
	public void send(JainMgcpResponseEvent event) {

		if (timerTask != null) {
			timerTask.cancel();
		}

		// to send response we already should know the address and port
		// number from which the original request was received
		if (remoteAddress == null) {
			throw new IllegalArgumentException("Unknown orinator address");
		}

		// restore the original transaction handle parameter
		// and encode event objet into MGCP response message
		event.setTransactionHandle(remoteTID);

		// encode event object into MGCP response message
		String msg = encode(event);

		// send response message to the originator
		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);

		if (logger.isDebugEnabled()) {
			logger.debug("LocalID=" + localTID + ", Send response event to " + remoteAddress + ":" + remotePort
					+ ", message\n" + msg);
		}
		stack.send(packet);

		/*
		 * Just reset timer in case of provisional response. Otherwise, release
		 * tx.
		 */
		if (isProvisional(event.getReturnCode())) {
			// reset timer.
			resetTimer();
		} else {
			release();
		}
	}

	/**
	 * Used by stack for transmitting received MGCP command message to the
	 * application.
	 * 
	 * @param message
	 *            receive MGCP command message.
	 */
	public void receiveCommand(String message) {

		JainMgcpCommandEvent event = null;
		try {
			event = decodeCommand(message);
			if (logger.isDebugEnabled()) {
				logger.debug("Event decoded: " + event);
			}
		} catch (ParseException e) {
			logger.error("Coud not parse message: ", e);
			return;
		}

		sent = false;
		commandEvent = event;

		// store original transaction handle parameter
		// and populate with local value
		remoteTID = event.getTransactionHandle();
		event.setTransactionHandle(localTID);

		resetTimer();

		// fire event
		stack.provider.processMgcpCommandEvent(event);

	}

	/**
	 * Used by stack for relaying received MGCP response messages to the
	 * application.
	 * 
	 * @param message
	 *            receive MGCP response message.
	 */
	public void receiveResponse(String message) {

		JainMgcpResponseEvent event = null;

		if (timerTask != null) {
			timerTask.cancel();
		}

		try {
			event = decodeResponse(message);
		} catch (Exception e) {
			logger.error("Could not decode message: ", e);
		}

		// restore original transaction handle parameter
		event.setTransactionHandle(remoteTID);
		// fire event
		stack.provider.processMgcpResponseEvent(event, commandEvent);

		/*
		 * Just reset timer in case of provisional response. Otherwise, release
		 * tx.
		 */
		if (isProvisional(event.getReturnCode())) {
			// reset timer. TODO: increment delays according RFC
			resetTimer();
		} else {
			release();
		}
	}

	private void resetTimer() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		timerTask = new TransactionTimerTask();
		timer.schedule(timerTask, TIMEOUT);
	}

	/**
	 * constructs the object source for a command
	 * 
	 * @param tid
	 * @return
	 */
	protected Object getObjectSource(int tid) {
		if (sent) {
			return stack;
		} else {
			return new ReceivedTransactionID(tid, remoteAddress.toString(), remotePort);
		}
	}
}
