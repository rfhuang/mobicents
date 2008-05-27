package net.java.slee.resource.mgcp;

import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;

/**
 * This activity for Mgcp Resource Adaptor type can be used as the context of
 * Mgcp commands related with a specific Mgcp connection.
 * 
 * @author eduardomartins
 * 
 */
public interface MgcpConnectionActivity {

	/**
	 * Retrieves the mgcp {@link ConnectionIdentifier} associated with this
	 * activity type.
	 * 
	 * @return null if the {@link ConnectionIdentifier} is not known yet.
	 */
	public String getConnectionIdentifier();

	/**
	 * Forces the end of this activity.
	 */
	public void release();
	
}
