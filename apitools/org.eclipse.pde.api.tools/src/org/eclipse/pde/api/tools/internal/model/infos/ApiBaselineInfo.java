/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.model.infos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.pde.api.tools.internal.AnyValue;
import org.eclipse.pde.api.tools.internal.CoreMessages;
import org.eclipse.pde.api.tools.internal.SystemLibraryApiComponent;
import org.eclipse.pde.api.tools.internal.model.ApiBaseline;
import org.eclipse.pde.api.tools.internal.model.cache.ApiElementCache;
import org.eclipse.pde.api.tools.internal.model.cache.ApiToolsCache;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.ibm.icu.text.MessageFormat;

/**
 * {@link IApiBaseline} specific info object
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiBaselineInfo extends ApiElementInfo {

	/**
	 * OSGi bundle state
	 */
	private State fState = null;
	/**
	 * Execution environment identifier
	 */
	private String fExecutionEnvironment = null;
	/**
	 * Component representing the system library
	 */
	private IApiComponent fSystemLibraryComponent = null;
	/**
	 * The ee description file to use to init the ee
	 */
	private File fEeFile = null;
	/**
	 * if the profile should be auto-resolving or not
	 */
	private boolean fAutoresolve = false;
	/**
	 * The current collection of status from the profile
	 */
	private MultiStatus fStatus = null;
	
	/**
	 * The current set of cached {@link IApiComponent}s
	 */
	private ApiElementCache fComponents = null;
	
	/**
	 * The current set of cached {@link IApiPackageFragment}s
	 */
	private ApiElementCache fPackages = null;
	
	/**
	 * Constant to match any value for ws, os, arch.
	 */
	private AnyValue ANY_VALUE = new AnyValue("*"); //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public ApiBaselineInfo(IApiBaseline owner, File eefile, boolean autoresolve) {
		super(owner);
		fEeFile = eefile;
		fAutoresolve = autoresolve;
		fState = StateObjectFactory.defaultFactory.createState(true);
		if(!fAutoresolve) {
			addStatus(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, CoreMessages.ApiProfile_0));
		}
		fComponents = new ApiElementCache(ApiToolsCache.DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Initializes this profile info to resolve in the execution environment
	 * associated with the given symbolic name.
	 * 
	 * @param environmentId execution environment symbolic name
	 * @param eeFile execution environment description file
	 * @throws CoreException if unable to initialize based on the given id
	 */
	private void initialize(String environmentId, File eeFile) {
		try {
			EEVMType.clearProperties(eeFile);
			String profile = EEVMType.getProperty(EEVMType.PROP_CLASS_LIB_LEVEL, eeFile);
			Properties properties = null;
			if (ApiPlugin.isRunningInFramework()) {
				properties = getJavaProfileProperties(environmentId);
			} else {
				properties = Util.getEEProfile(eeFile);
			}
			if (properties == null) {
				addStatus(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, MessageFormat.format("Unknown execution environment: ", new String[] {environmentId})));
			} else {
				initialize(properties, eeFile);
			}
			addStatus(new Status(IStatus.OK, ApiPlugin.PLUGIN_ID, MessageFormat.format(CoreMessages.ApiProfile_1, new String[]{profile})));
		}
		catch(CoreException ce) {}
	}
	
	/**
	 * Initializes this profile from the given properties.
	 * 
	 * @param profile OGSi profile properties
	 * @param description execution environment description file
	 * @throws CoreException if unable to initialize
	 */
	private void initialize(Properties profile, File description) throws CoreException {
		String value = profile.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES);
		Dictionary dictionary = new Hashtable();
		String[] systemPackages = null;
		if (value != null) {
			systemPackages = value.split(","); //$NON-NLS-1$
			dictionary.put(Constants.FRAMEWORK_SYSTEMPACKAGES, value);
		}
		value = profile.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT);
		if (value != null) {
			dictionary.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, value);
		}
		fExecutionEnvironment = profile.getProperty("osgi.java.profile.name"); //$NON-NLS-1$
		if (fExecutionEnvironment == null) {
			addStatus(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, "Profile file missing 'osgi.java.profile.name'"));
		}
		dictionary.put("osgi.os", ANY_VALUE); //$NON-NLS-1$
		dictionary.put("osgi.arch", ANY_VALUE); //$NON-NLS-1$
		dictionary.put("osgi.ws", ANY_VALUE); //$NON-NLS-1$
		dictionary.put("osgi.nl", ANY_VALUE); //$NON-NLS-1$
		fState.setPlatformProperties(dictionary);
		// clean up previous system library
		if (fSystemLibraryComponent != null && fComponentsById != null) {
			fComponentsById.remove(fSystemLibraryComponent.getId());
		}
		if(fSystemPackageNames != null) {
			fSystemPackageNames.clear();
			fSystemPackageNames = null;
		}
		clearComponentsCache();
		// set new system library
		fSystemLibraryComponent = new SystemLibraryApiComponent((IApiBaseline) getOwner(), description, systemPackages);
		addComponent(fSystemLibraryComponent);
	}
	
	/**
	 * Returns the property file for the given environment or <code>null</code>.
	 * 
	 * @param ee execution environment symbolic name
	 * @return properties file or <code>null</code> if none
	 */
	public Properties getJavaProfileProperties(String ee) throws CoreException {
		Bundle osgiBundle = Platform.getBundle("org.eclipse.osgi"); //$NON-NLS-1$
		if (osgiBundle == null) 
			return null;
		URL profileURL = osgiBundle.getEntry(ee.replace('/', '_') + ".profile"); //$NON-NLS-1$
		if (profileURL != null) {
			InputStream is = null;
			try {
				profileURL = FileLocator.resolve(profileURL);
				URLConnection openConnection = profileURL.openConnection();
				openConnection.setUseCaches(false);
				is = openConnection.getInputStream();
				if (is != null) {
					Properties profile = new Properties();
					profile.load(is);
					return profile;
				}
			} catch (IOException e) {
				addStatus(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, MessageFormat.format("Unable to read profile: ", new String[] {ee})));
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					ApiPlugin.log(e);
				}
			}
		}
		return null;
	}	
	
	/**
	 * Resolves and initializes the system library to use based on API component requirements.
	 * Only works when running in the framework. Has no effect if not running in the framework.
	 */
	public void resolveSystemLibrary(HashSet ees) {
		if (ApiPlugin.isRunningInFramework() && fAutoresolve) {
			IStatus error = null;
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			Iterator iterator = ees.iterator();
			Map VMsToEEs = new HashMap();
			while (iterator.hasNext()) {
				String ee = (String) iterator.next();
				IExecutionEnvironment environment = manager.getEnvironment(ee);
				if (environment != null) {
					IVMInstall[] compatibleVMs = environment.getCompatibleVMs();
					for (int i = 0; i < compatibleVMs.length; i++) {
						IVMInstall vm = compatibleVMs[i];
						Set EEs = (Set) VMsToEEs.get(vm);
						if (EEs == null) {
							EEs = new HashSet();
							VMsToEEs.put(vm, EEs);
						}
						EEs.add(ee);
					}
				}
			}
			// select VM that is compatible with most required environments
			iterator = VMsToEEs.entrySet().iterator();
			IVMInstall bestFit = null;
			int bestCount = 0;
			while (iterator.hasNext()) {
				Entry entry = (Entry) iterator.next();
				Set EEs = (Set)entry.getValue();
				if (EEs.size() > bestCount) {
					bestCount = EEs.size();
					bestFit = (IVMInstall)entry.getKey();
				}
			}
			String systemEE = null;
			if (bestFit != null) {
				// find the EE this VM is strictly compatible with
				IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
				for (int i = 0; i < environments.length; i++) {
					IExecutionEnvironment environment = environments[i];
					if (environment.isStrictlyCompatible(bestFit)) {
						systemEE = environment.getId();
						break;
					}
				}
				if (systemEE == null) {
					// a best fit, but not strictly compatible with any environment (e.g.
					// a 1.7 VM for which there is no profile yet). This is a bit of a hack
					// until an OSGi profile exists for 1.7.
					if (bestFit instanceof IVMInstall2) {
			            String javaVersion = ((IVMInstall2)bestFit).getJavaVersion();
			            if (javaVersion != null) {
			            	if (javaVersion.startsWith(JavaCore.VERSION_1_7)) {
			            		// set EE to 1.6 when 1.7 is detected
			            		systemEE = "JavaSE-1.6"; //$NON-NLS-1$
			            	}
			            }
					}
				}
				if (systemEE != null) {
					// only update if different from current or missing VM binding
					if (!systemEE.equals(getExecutionEnvironment()) || fVMBinding == null) {
						try {
							File file = Util.createEEFile(bestFit, systemEE);
							JavaRuntime.addVMInstallChangedListener((ApiBaseline)getOwner());
							fVMBinding = bestFit;
							initialize(systemEE, file);
						}
						catch (IOException e) {
							error = new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, CoreMessages.ApiProfile_2, e);
						}
					}
				} else {
					// VM is not strictly compatible with any EE
					error = new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, CoreMessages.ApiProfile_4);
				}
			} else {
				// no VMs match any required EE
				error = new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, CoreMessages.ApiProfile_4);
			}
			if (error == null) {
				// build status for unbound required EE's
				Set missing = new HashSet(ees);
				Set covered = new HashSet((Set)VMsToEEs.get(bestFit));
				missing.removeAll(covered);
				if (missing.isEmpty()) {
					addStatus(new Status(IStatus.OK, ApiPlugin.PLUGIN_ID, MessageFormat.format(CoreMessages.ApiProfile_1, new String[]{systemEE})));
				} else {
					iterator = missing.iterator();
					MultiStatus multi = new MultiStatus(ApiPlugin.PLUGIN_ID, 0, CoreMessages.ApiProfile_7, null);
					while (iterator.hasNext()) {
						String id = (String) iterator.next();
						multi.add(new Status(IStatus.WARNING, ApiPlugin.PLUGIN_ID,
								MessageFormat.format(CoreMessages.ApiProfile_8, new String[]{id})));
					}
					addStatus(multi);
				}
			} else {
				addStatus(error);
			}
		}
	}
	
	/**
	 * cleans up residents of this info
	 */
	public void dispose() {
		fState = null;
	}
	
	/**
	 * @return the id of the currently bound EE
	 */
	public String getExecutionEnvironment() {
		return fExecutionEnvironment;
	}
	
	/**
	 * @return the current OSGi state in use in this info
	 */
	public State getState() {
		return fState;
	}
	
	/**
	 * @return the complete listing of errors from attempted resolution of the backing OSGi state
	 */
	public ResolverError[] getResolutionErrors() {
		List errs = new ArrayList();
		BundleDescription[] bundles = fState.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			ResolverError[] errors = fState.getResolverErrors(bundles[i]);
			for (int j = 0; j < errors.length; j++) {
				errs.add(errors[j]);
			}
		}
		return (ResolverError[]) errs.toArray(new ResolverError[errs.size()]);
	}
	
	/**
	 * Adds a new status to the profiles' overall status
	 * @param newstatus
	 */
	private void addStatus(IStatus newstatus) {
		if(fStatus == null) {
			fStatus = new MultiStatus(ApiPlugin.PLUGIN_ID, IStatus.ERROR, "API Baseline Problems", null);
		}
		fStatus.add(newstatus);
	}
	
	/**
	 * @return the complete status of the profile at the time this method is called
	 */
	public MultiStatus getStatus() {
		return fStatus;
	}
	
}
