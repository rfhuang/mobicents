/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mobicents.media.server.testsuite.general;

import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.apache.log4j.Logger;
import org.mobicents.media.server.testsuite.general.rtp.RtpPacket;
import org.mobicents.media.server.testsuite.general.rtp.RtpSocket;
import org.mobicents.media.server.testsuite.general.rtp.RtpSocketFactory;
import org.mobicents.media.server.testsuite.general.rtp.RtpSocketListener;
import org.mobicents.mgcp.stack.JainMgcpExtendedListener;
import org.mobicents.mgcp.stack.JainMgcpStackProviderImpl;

/**
 * 
 * @author baranowb
 */
public abstract class AbstractCall implements JainMgcpExtendedListener, Serializable, RtpSocketListener {

	protected transient Logger logger = Logger.getLogger(AbstractCall.class);

	// Some static vals:
	protected transient static final AtomicLong _GLOBAL_SEQ = new AtomicLong(-1);
	protected transient static final long _DATA_DUMP_DELAY = 2500;
	protected transient static final long _DATA_DUMP_RATE = 2500;

	protected final long sequence;
	protected CallState state = CallState.INITIAL;
	protected int avgJitter;
	protected int peakJitter;
	protected long lastDeliverTimeStamp;

	// Data dump
	// protected transient java.io.File dataFileName;
	// protected transient java.io.File graphDataFileName;
	// protected transient java.io.File pureRtpDataFileName;
	// protected transient FileOutputStream fos;
	// protected transient ObjectOutputStream dataDumpChannel;
	// protected transient FileOutputStream graphDataDumpChannel;
	protected transient List<RtpPacket> rtpTraffic = new ArrayList<RtpPacket>();

	// Media part
	protected String endpointName = ""; // endpoint name with wildcard - used to
	// send to mms to get actual EI
	protected EndpointIdentifier endpointIdentifier;
	protected CallIdentifier callIdentifier;

	// Below is part we dont want to propagate to other side :)
	protected transient AbstractTestCase testCase;

	// RTP part
	protected transient RtpSocket socket;

	// MGCP part
	protected transient JainMgcpStackProviderImpl provider;

	protected transient ScheduledFuture<?> timeoutHandle;
	protected transient ScheduledFuture<?> dataDumpFuture;

	public static void resetSequence() {
		_GLOBAL_SEQ.set(-1);
	}

	public AbstractCall(AbstractTestCase testCase) throws IOException {
		this.testCase = testCase;
		this.callIdentifier = testCase.getProvider().getUniqueCallIdentifier();

		try {

			this.sequence = _GLOBAL_SEQ.incrementAndGet();
			setTestCase(testCase);

		} finally {

		}
	}

	void setTestCase(AbstractTestCase testCase) {
		this.testCase = testCase;
		this.setDumpDir(this.testCase.getTestDumpDirectory());
		this.provider = testCase.getProvider();
	}

	void setDumpDir(File testDumpDirectory) {
		// this.dataFileName = new File(testDumpDirectory, this.sequence +
		// ".mrtp");
		// this.pureRtpDataFileName = new File(testDumpDirectory, this.sequence
		// + ".rtp");
		// this.graphDataFileName = new File(testDumpDirectory, this.sequence +
		// "_graph.txt");
	}

	// public java.io.File getGraphDataFileName() {
	// return graphDataFileName;
	// }

	protected void initSocket() throws IOException {
		if (this.testCase != null && this.testCase.getSocketFactory() != null) {
			RtpSocketFactory factory = this.testCase.getSocketFactory();
			this.socket = factory.createSocket();
			socket.addListener(this);

		}
	}

	protected void releaseSocket() {
		try {
			if (this.socket != null) {
				this.socket.release();
				this.socket = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<RtpPacket> getRtp() throws IOException {
		List<RtpPacket> ll = this.loadRtp();

		return ll;
	}

	private List<RtpPacket> loadRtp() throws IOException {
		ArrayList<RtpPacket> list = new ArrayList();
		String startString = this.sequence + ":";
		InputStreamReader isw = this.testCase.getRtpISR();

		BufferedReader br = new BufferedReader(isw);
		String line = null;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith(startString))
				continue;

			line = line.trim();
			RtpPacket p = new RtpPacket();
			p.deserializeFromString(line);
			list.add(p);
		}
		br.close();
		return list;
	}

	public CallState getState() {
		return state;
	}

	public int getAvgJitter() {
		return this.avgJitter;
	}

	public int getPeakJitter() {
		return this.peakJitter;
	}

	public EndpointIdentifier getEndpoint() {
		return this.endpointIdentifier;
	}

	public CallIdentifier getCallID() {
		return this.callIdentifier;
	}

	public long getSequence() {
		return this.sequence;
	}

	protected void setState(CallState state) {
		if (logger.isDebugEnabled()) {
			logger.debug("Dumping data to file. State = " + state);
		}
		if (state == this.state) {
			return;
		}

		this.state = state;

		switch (this.state) {
		case ENDED:
		case IN_ERROR:
		case TIMED_OUT:
			this.releaseSocket();

			performDataDumps(true);

			break;

		default:
			break;
		}

		this.testCase.callStateChanged(this);

	}

	void performDataDumps(boolean isFinal) {

		try {
			if (rtpTraffic == null)
				return;
			int currentSize = rtpTraffic.size() - 1;
			OutputStreamWriter osw = testCase.getRtpOSW();
			for (int i = 0; i < currentSize; i++) {
				RtpPacket p1 = rtpTraffic.remove(0);
				RtpPacket p2 = rtpTraffic.get(0);
				int localJitter = (int) (p2.getTime().getTime() - p1.getTime().getTime());
				if (localJitter > this.peakJitter)
					this.peakJitter = localJitter;
				// Aprox
				this.avgJitter += localJitter;
				this.avgJitter /= 2;
				try {
					osw.write(p1.serializeToString());
					osw.write("\n");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (isFinal) {
				this.rtpTraffic.clear();
				this.rtpTraffic = null;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			// we dont sync, this is penalty :)
		}
	
	}

	protected String getLocalDescriptor(int port) {

		SessionDescription localSDP = null;
		String userName = "Mobicents-Call-Generator";
		long sessionID = System.currentTimeMillis() & 0xffffff;
		long sessionVersion = sessionID;

		String networkType = javax.sdp.Connection.IN;
		String addressType = javax.sdp.Connection.IP4;

		SdpFactory sdpFactory = testCase.getSdpFactory();

		try {
			localSDP = sdpFactory.createSessionDescription();
			localSDP.setVersion(sdpFactory.createVersion(0));
			localSDP.setOrigin(sdpFactory.createOrigin(userName, sessionID, sessionVersion, networkType, addressType, this.testCase.getClientTestNodeAddress().getHostAddress()));
			localSDP.setSessionName(sdpFactory.createSessionName("session"));
			localSDP.setConnection(sdpFactory.createConnection(networkType, addressType, this.testCase.getClientTestNodeAddress().getHostAddress()));

			Vector<Attribute> attributes = testCase.getSDPAttributes();
			int[] audioMap = new int[attributes.size()];
			for (int index = 0; index < audioMap.length; index++) {
				String m = attributes.get(index).getValue().split(" ")[0];
				audioMap[index] = Integer.valueOf(m);
			}
			// generate media descriptor
			MediaDescription md = sdpFactory.createMediaDescription("audio", port, 1, "RTP/AVP", audioMap);

			// set attributes for formats

			md.setAttributes(attributes);
			Vector descriptions = new Vector();
			descriptions.add(md);

			localSDP.setMediaDescriptions(descriptions);
		} catch (SdpException e) {
			e.printStackTrace();
		}

		return localSDP.toString();
	}

	public abstract void start();

	public abstract void timeOut();

	public abstract void stop();

	protected void onStart() {
		this.dataDumpFuture = this.testCase.getExecutors().scheduleAtFixedRate(new Runnable() {

			public void run() {
				performDataDumps(false);

			}
		}, _DATA_DUMP_DELAY, _DATA_DUMP_RATE, TimeUnit.MILLISECONDS);
	}

	protected void onStop() {
		if (this.dataDumpFuture != null) {
			this.dataDumpFuture.cancel(false);
			this.dataDumpFuture = null;
		}
	}

	public ScheduledFuture<?> getTimeoutHandle() {
		return timeoutHandle;
	}

	public void setTimeoutHandle(ScheduledFuture<?> timeoutHandle) {
		this.timeoutHandle = timeoutHandle;
	}

	// //////////////////
	// SOCKET METHODS //
	// //////////////////

	public void error(Exception e) {
		// FIXME?

	}

	public void receive(RtpPacket packet) {
		if (this.rtpTraffic != null) {
			this.rtpTraffic.add(packet);
			packet.minimize();
			packet.setCallSequence(this.sequence);

		}

	}

	/**
	 * @return
	 */
	public String getCallSpecificData() {

		StringBuffer sb = new StringBuffer();
		sb.append(AbstractTestCase._LINE_SEPARATOR);
		//sb.append((sequence + AbstractTestCase._LINE_SEPARATOR));
		//sb.append((avgJitter + AbstractTestCase._LINE_SEPARATOR));
		//sb.append((peakJitter + AbstractTestCase._LINE_SEPARATOR));
		try {
			this.rtpTraffic = loadRtp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < rtpTraffic.size() - 1; i++) {
			RtpPacket p1 = rtpTraffic.get(i);
			RtpPacket p2 = rtpTraffic.get(i + 1);

			int localJitter = (int) (p2.getTime().getTime() - p1.getTime().getTime());

			sb.append((localJitter + AbstractTestCase._LINE_SEPARATOR));

		}

		this.rtpTraffic.clear();
		this.rtpTraffic = null;
		return sb.toString();
	}

}
