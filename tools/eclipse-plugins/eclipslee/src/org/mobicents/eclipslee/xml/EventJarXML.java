/**
 *   Copyright 2005 Open Cloud Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.mobicents.eclipslee.xml;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.SAXException;

/**
 * @author cath
 */
public class EventJarXML extends org.mobicents.eclipslee.util.slee.xml.components.EventJarXML {
	
	/** The jar file location*/
	private String jarLocation = null;

	public EventJarXML(IFile file) throws SAXException, IOException, ParserConfigurationException, CoreException {		
		super(file.getContents(), new SLEEEntityResolver(), SLEEEntityResolver.getEmptyXML(PUBLIC_ID, SYSTEM_ID));
		this.osPath = file.getFullPath().toOSString();
	}
	
	public EventJarXML(JarFile file, JarEntry entry, String location) throws SAXException, IOException, ParserConfigurationException {		
		super(file.getInputStream(entry), new SLEEEntityResolver(), SLEEEntityResolver.getEmptyXML(PUBLIC_ID, SYSTEM_ID));
		String fname = "jar:" + file.getName() + "!" + entry.getName();
		jarLocation = location;
		this.osPath = fname;
	}

	public EventJarXML() throws ParserConfigurationException, IOException {
		super(new SLEEEntityResolver(), SLEEEntityResolver.getEmptyXML(PUBLIC_ID, SYSTEM_ID));
		this.osPath = null;
	}

	public String getPath() {
		return osPath;
	}

	public final String osPath;

	/**
	 * @return Returns the jarLocation.
	 */
	public String getJarLocation() {
		return jarLocation;
	}

	/**
	 * @param jarLocation The jarLocation to set.
	 */
	public void setJarLocation(String jarLocation) {
		this.jarLocation = jarLocation;
	}
	
}
