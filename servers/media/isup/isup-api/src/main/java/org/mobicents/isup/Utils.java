/**
 * Start time:16:56:29 2009-07-17<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
package org.mobicents.isup;

/**
 * Start time:16:56:29 2009-07-17<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class Utils {

	
	public static String toHex(byte[] b)
	{
		
		String out = "";
		
		for(int index = 0;index<b.length;index++)
		{
		
			
		
			out+="b["+Integer.toHexString(b[index])+"]";
			
			
			//out+="\n";
		}
		
		return out;
		
	}
	
}