package org.mobicents.ipbx.session.configuration;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.mobicents.ipbx.entity.GlobalProperty;
import org.mobicents.ipbx.entity.PstnGatewayAccount;

@Name("pbxConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
public class PbxConfiguration {
	@In EntityManager sipEntityManager;
	
	private List<PstnGatewayAccount> pstnAccounts;
	private HashMap<String,GlobalProperty> globalProperties;
	
	public List<PstnGatewayAccount> getPstnAccounts() {
		if(pstnAccounts == null) pstnAccounts = loadPstnAccounts();
		return pstnAccounts;
	}
	
	@Create
	public HashMap<String,GlobalProperty> getGlobalProperties() {
		if(globalProperties == null) globalProperties = loadGlobalProperties();
		return globalProperties;
	}
	
	public String getProperty(String property) {
		return getGlobalProperties().get(property).getValue();
	}

	
	public void updateAll() {
		pstnAccounts = loadPstnAccounts();
		globalProperties = loadGlobalProperties();
	}
	
	public HashMap<String,GlobalProperty> loadGlobalProperties() {
        List<GlobalProperty> props = sipEntityManager.createQuery("SELECT global FROM GlobalProperty global").getResultList();
        HashMap<String,GlobalProperty> globalProperties = new HashMap<String, GlobalProperty>();
		for(GlobalProperty p : props) {
			globalProperties.put(p.getName(), p);
		}
		return globalProperties;
	}
	
	public List loadPstnAccounts() {
		return sipEntityManager.createQuery("SELECT pstn FROM PstnGatewayAccount pstn").getResultList();
	}
}