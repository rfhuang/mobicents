package org.mobicents.ss7.isup.impl.message.parameter;

import org.mobicents.ss7.isup.ISUPComponent;
import org.mobicents.ss7.isup.impl.message.parameter.CallDiversionTreatmentIndicatorsImpl;
/**
 * 
 * Start time:13:47:44 2009-04-23<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski
 *         </a>
 */
public class CallDiversionTreatmentIndicatorsTest extends ParameterHarness {

	
	public CallDiversionTreatmentIndicatorsTest() {
		super();
		// TODO Auto-generated constructor stub
		//super.badBodies.add(new byte[0]);
		
		super.goodBodies.add(getBody1());
		
	}
	private byte[] getBody1() {
		byte[] b = new byte[10];
		b[9] = (byte) (b[9] | (0x01 << 7));
		return b;
	}
	@Override
	public ISUPComponent getTestedComponent() {
		return new CallDiversionTreatmentIndicatorsImpl();
	}

	
	
	
}
