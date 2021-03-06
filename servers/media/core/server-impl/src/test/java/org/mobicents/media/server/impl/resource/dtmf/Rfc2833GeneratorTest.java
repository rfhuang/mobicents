package org.mobicents.media.server.impl.resource.dtmf;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.server.EndpointImpl;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.dtmf.Rfc2833DetectorImpl;
import org.mobicents.media.server.impl.resource.dtmf.Rfc2833GeneratorImpl;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.Timer;

/**
 * 
 * @author amit bhayani
 * 
 */
public class Rfc2833GeneratorTest {

    private Timer timer = null;
    private Endpoint endpoint = null;
    private Rfc2833GeneratorImpl generator = null;
    private Semaphore semaphore;
    private volatile boolean isFormatCorrect = true;
    private volatile boolean isSizeCorrect = false;
    private volatile boolean isDurationCorrect = false;
    private volatile boolean isSeqCorrect = false;
    private volatile boolean isCorrectTimestamp = false;
    private volatile boolean isEndEventReceived = false;
    private volatile boolean isCorrectDigit = false;
    private volatile boolean isCorrectNoOfPkt = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        semaphore = new Semaphore(0);

        timer = new TimerImpl();

        endpoint = new EndpointImpl();
        endpoint.setTimer(timer);

        generator = new Rfc2833GeneratorImpl("Rfc2833DetectorTest", timer);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGenerator() throws Exception {
        TestSink sink = new TestSink("TestSink");
        sink.start();
        
        generator.connect(sink);
        generator.setToneDuration(100); // 100 ms
        generator.setVolume(0);
        generator.setDigit("9");
        generator.setEndpoint(endpoint);

        generator.start();

        semaphore.tryAcquire(150, TimeUnit.MILLISECONDS);

        assertEquals(true, isFormatCorrect);
        assertEquals(true, isSizeCorrect);
	assertEquals(true, isDurationCorrect);
        assertEquals(true, isSeqCorrect);
//		assertEquals(true, isCorrectTimestamp);
//		assertEquals(true, isEndEventReceived);
        assertEquals(true, isCorrectDigit);
        assertEquals(true, isCorrectNoOfPkt);

    }

    //Since duration set is 100, we need to get 7 packets
    private class TestSink extends AbstractSink {

        private long lastDuration = 0;
        private long lastSeqNo = 0;
        private long timeStamp = 0;
        private int packetsReceived = 0;

        private TestSink(String name) {
            super(name);
        }

        public Format[] getFormats() {
            return new Format[0];
        }

        public boolean isAcceptable(Format format) {
            return true;
        }

        @Override
        public void onMediaTransfer(Buffer buffer) {
            isFormatCorrect &= buffer.getFormat().matches(AVProfile.DTMF);
            isSizeCorrect = ((buffer.getLength()) == 4);

            byte[] data = (byte[]) buffer.getData();

            int high = data[2] & 0xff;
            int low = data[3] & 0xff;

            int theDuration = (int) ((high << 8) | low);

            if (lastDuration > 0) {
                isDurationCorrect = theDuration - lastDuration == 160;
            }
            lastDuration = theDuration;

            if (lastSeqNo > 0) {
                isSeqCorrect = (buffer.getSequenceNumber() - lastSeqNo) == 1;

            }
            lastSeqNo = buffer.getSequenceNumber();

            if (timeStamp > 0) {
                isCorrectTimestamp = (buffer.getTimeStamp() == timeStamp);
            }
            timeStamp = buffer.getTimeStamp();

            isEndEventReceived = ((data[1] & 0x80) != 0);

            isCorrectDigit = ("9".equals(Rfc2833DetectorImpl.TONE[data[0]]));

            packetsReceived++;
            if (packetsReceived == 7) {
                isCorrectNoOfPkt = true;
                semaphore.release();
            }
        }
    }
}
