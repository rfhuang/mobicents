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

package org.mobicents.eclipslee.servicecreation.wizards.event;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.mobicents.eclipslee.servicecreation.util.FileUtil;
import org.mobicents.eclipslee.servicecreation.wizards.generic.BaseWizard;
import org.mobicents.eclipslee.util.Utils;
import org.mobicents.eclipslee.util.slee.xml.ant.AntBuildTargetXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntCleanTargetXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntInitTargetXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntJavacXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntPathXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntProjectXML;
import org.mobicents.eclipslee.util.slee.xml.ant.AntTargetXML;
import org.mobicents.eclipslee.util.slee.xml.components.ComponentNotFoundException;
import org.mobicents.eclipslee.xml.EventJarXML;


public class EventWizard extends BaseWizard {

	private static final String EVENT_TEMPLATE = "/templates/Event.template";
	
	/**
	 * Constructor for EventWizard.
	 */
	public EventWizard() {
		super();
		setNeedsProgressMonitor(true);
		WIZARD_TITLE = "JAIN SLEE Event Wizard";
		ENDS = "Event.java";
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	public void doFinish(IProgressMonitor monitor) throws CoreException {

		String eventClassName = Utils.getSafePackagePrefix(getPackageName()) + getFileName().substring(0, getFileName().indexOf(".java"));
		String baseName = getFileName().substring(0, getFileName().indexOf(ENDS));
		
		// Create the Event Java file.
		monitor.beginTask("Creating JAIN SLEE Event " + getComponentName(), 4);
		HashMap map = new HashMap();
		map.put("__PACKAGE__", Utils.getPackageTemplateValue(getPackageName()));
		map.put("__NAME__", getFileName().substring(0, getFileName().length() - ENDS.length()));

		IFolder folder = getSourceContainer().getFolder(new Path(this.getPackageName().replaceAll("\\.", "/")));

		final IFile javaFile;
		try {
			javaFile = FileUtil.createFromTemplate(folder, new Path(getFileName()), new Path(EVENT_TEMPLATE), map, monitor);		
		} catch (IOException e) {
			e.printStackTrace();
			throw newCoreException("Failed to create JAIN SLEE Event Java: ", e);			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		monitor.worked(1);
		
		// Create the event-jar.xml file.
		monitor.setTaskName("Creating JAIN SLEE Event XML");		
		try {
			EventJarXML eventXML = new EventJarXML();
			eventXML.addEvent(getComponentName(), getComponentVendor(), getComponentVersion(),
					getComponentDescription(), eventClassName);

			FileUtil.createFromInputStream(folder, new Path(baseName + "-event-jar.xml"), eventXML.getInputStreamFromXML(), monitor);
		} catch (IOException e) {
				throwCoreException("Failed to create JAIN SLEE Event XML: ", e);
		} catch (Exception e) {
			throwCoreException("Failed to create JAIN SLEE Event XML: ", e);
		}
		monitor.worked(1);

		monitor.setTaskName("Creating Ant Build File");
		// Load the ant build file from the root of the project.
		try {
			IPath antBuildPath = new Path("/build.xml");
						
			String sourceDir = getSourceContainer().getName();
			
			IFile projectFile = getSourceContainer().getProject().getFile(antBuildPath);
			AntProjectXML projectXML = new AntProjectXML(projectFile.getContents());
			AntInitTargetXML initXML = (AntInitTargetXML) projectXML.getTarget("init");
			AntBuildTargetXML buildXML = projectXML.addBuildTarget();
			AntCleanTargetXML cleanXML = projectXML.addCleanTarget();
			AntTargetXML allXML = projectXML.getTarget("all");
			AntTargetXML cleanAllXML = projectXML.getTarget("clean");			
			AntPathXML sleePathXML = initXML.getPathID("slee");
		
			String shortName = baseName + "-event";
			String classesDir = "classes/" + shortName;
			String jarDir = "jars/";
			String jarName = jarDir + shortName + ".jar";
			
			buildXML.setName("build-" + shortName);
			cleanXML.setName("clean-" + shortName);
			allXML.addAntTarget(buildXML);
			cleanAllXML.addAntTarget(cleanXML);
			
			cleanXML.addFile(jarName);
			cleanXML.addDir(classesDir);
			
			buildXML.addMkdir(classesDir);
			buildXML.addMkdir(jarDir);
			buildXML.setDepends(new String[] { "init" });
			AntJavacXML javacXML = buildXML.createJavac();
			javacXML.setDestdir(classesDir);
			javacXML.setSrcdir(sourceDir);
			// Just include the event java files
			
			String packageDir = getPackageName().replaceAll("\\.", "/");
			
			javacXML.setIncludes(new String[] { Utils.getSafePackageDir(packageDir) + getFileName() });
			javacXML.addPathXML(sleePathXML);
			
			
			try {
				AntPathXML externalComponentsPath = initXML.getPathID("ExternalComponents");
				javacXML.addPathXML(externalComponentsPath);;
			} catch (ComponentNotFoundException e) {
			}
			
			org.mobicents.eclipslee.util.slee.xml.ant.AntEventJarXML eventXML = buildXML.addEventJar();
			eventXML.setDestfile(jarName);
			eventXML.setClasspath(classesDir);
			eventXML.setXML(sourceDir + "/" + Utils.getSafePackageDir(packageDir) + baseName + "-event-jar.xml");
			
			FileUtil.createFromInputStream(getSourceContainer().getProject(), antBuildPath, projectXML.getInputStreamFromXML(), monitor);
			monitor.worked(1);
		} catch (Exception e) {
			throwCoreException("Unable to modify Ant Build file '/build.xml'", e);
		}		
		
		// Open the .java file for editing
		monitor.setTaskName("Opening JAIN SLEE Event Java file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, javaFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
}