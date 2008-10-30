package org.mobicents.mgcp.stack;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Notify;
import jain.protocol.ip.mgcp.message.NotifyResponse;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.EventName;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.RequestIdentifier;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;

import org.apache.log4j.Logger;
import org.mobicents.mgcp.stack.parser.MgcpContentHandler;
import org.mobicents.mgcp.stack.parser.MgcpMessageParser;
import org.mobicents.mgcp.stack.parser.Utils;

public class NotifyHandler extends TransactionHandler {

	private Notify command;
	private NotifyResponse response;

	private Logger logger = Logger.getLogger(NotifyHandler.class);
	
	public NotifyHandler(JainMgcpStackImpl stack) {
		super(stack);
	}

	public NotifyHandler(JainMgcpStackImpl stack, InetAddress address, int port) {
		super(stack, address, port);
	}

	@Override
	public JainMgcpCommandEvent decodeCommand(String message) throws ParseException {
		MgcpMessageParser parser = new MgcpMessageParser(new CommandContentHandle());
		try {
			parser.parse(message);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), -1);
		}

		return command;
	}

	@Override
	public JainMgcpResponseEvent decodeResponse(String message) throws ParseException {
		if (logger.isDebugEnabled()) {
			logger.debug("Decoding Notify response = "+message);
		}
		MgcpMessageParser parser = new MgcpMessageParser(new ResponseContentHandle());
		try {
			parser.parse(message);
		} catch (IOException e) {
			logger.error("Something wrong while parsing the NOTIFY Response received",e);
		}

		return response;
	}

	@Override
	public String encode(JainMgcpCommandEvent event) {
		Notify notify = (Notify) event;
		StringBuffer message = new StringBuffer();
		message.append("NTFY " + event.getTransactionHandle() + " " + notify.getEndpointIdentifier() + " MGCP 1.0\n");

		if (notify.getNotifiedEntity() != null) {
			message.append("N:" + notify.getNotifiedEntity() + "\n");
		}

		message.append("X:" + notify.getRequestIdentifier() + "\n");

		if (notify.getObservedEvents() != null) {
			message.append("O:" + utils.encodeEventNames(notify.getObservedEvents()) + "\n");
		}
		return message.toString();
	}

	@Override
	public String encode(JainMgcpResponseEvent event) {
		return event.getReturnCode().getValue() + " " + event.getTransactionHandle() + " "
				+ event.getReturnCode().getComment() + "\n";
	}

	private class CommandContentHandle implements MgcpContentHandler {

		public void header(String header) throws ParseException {
			String[] tokens = header.split("\\s");

			String verb = tokens[0].trim();
			String transactionID = tokens[1].trim();
			String version = tokens[3].trim() + " " + tokens[4].trim();

			int tid = Integer.parseInt(transactionID);
			EndpointIdentifier endpoint = utils.decodeEndpointIdentifier(tokens[2].trim());

			command = new Notify(getObjectSource(tid), endpoint, new RequestIdentifier("0"), new EventName[] {});
			command.setTransactionHandle(tid);
		}

		public void param(String name, String value) throws ParseException {
			if (name.equalsIgnoreCase("N")) {
				command.setNotifiedEntity(utils.decodeNotifiedEntity(value));
			} else if (name.equalsIgnoreCase("X")) {
				command.setRequestIdentifier(new RequestIdentifier(value));
			} else if (name.equalsIgnoreCase("O")) {
				command.setObservedEvents(utils.decodeEventNames(value));
			}
		}

		public void sessionDescription(String sd) throws ParseException {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	private class ResponseContentHandle implements MgcpContentHandler {

		public void header(String header) throws ParseException {
			String[] tokens = header.split("\\s");

			int tid = Integer.parseInt(tokens[1]);
			response = new NotifyResponse(stack, utils.decodeReturnCode(Integer.parseInt(tokens[0])));
			response.setTransactionHandle(tid);
		}

		public void param(String name, String value) throws ParseException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void sessionDescription(String sd) throws ParseException {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	@Override
	public JainMgcpResponseEvent getProvisionalResponse() {
		NotifyResponse provisionalresponse = null;
		if (!sent) {
			provisionalresponse = new NotifyResponse(commandEvent.getSource(), ReturnCode.Transaction_Being_Executed);
		}
		return provisionalresponse;
	}
}
