/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.eclipse.pde.api.tools.internal.BundleApiComponent;
import org.eclipse.pde.api.tools.internal.PluginProjectApiComponent;
import org.eclipse.pde.api.tools.internal.model.infos.ApiBaselineInfo;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.eclipse.pde.core.plugin.IPluginModelBase;

/**
 * Implementation of an API profile.
 * 
 * @since 1.0
 */
public class ApiBaseline extends ApiElement implements IApiBaseline, IVMInstallChangedListener {
	/**
	 * Constant used for controlling tracing in the example class
	 */
	private static boolean DEBUG = Util.DEBUG;
	
	/**
	 * Method used for initializing tracing in the example class
	 */
	public static void setDebug(boolean debugValue) {
		DEBUG = debugValue || Util.DEBUG;
	}

	private IApiComponent[] EMPTY_COMPONENTS = new IApiComponent[0];
	
	/**
	 * Next available bundle id
	 */
	private long fNextId = 0L; 
	
	/**
	 * Cache of resolved packages. 
	 * <p>Map of <code>PackageName -> Map(componentName -> IApiComponent[])</code></p>
	 * For each package the cache contains a map of API components that provide that package,
	 * by source component name (including the <code>null</code> component name).
	 */
	private HashMap fComponentsCache = null;
	
	/**
	 * Maps bundle descriptions to components.
	 * <p>Map of <code>BundleDescription.getSymbolicName() + BundleDescription.getVersion().toString() -> {@link IApiComponent}</code></p>
	 */
	private Map fComponents = null;
	
	/**
	 * Maps component id's to components.
	 * <p>Map of <code>componentId -> {@link IApiComponent}</code></p>
	 */
	private Map fComponentsById = null;
	
	/**
	 * Cache of system package names
	 */
	private Set fSystemPackageNames = null;
	
	/**
	 * The EE file to use for EE resolution
	 */
	private File fEeFile = null;
	/**
	 * Whether an execution environment should be automatically resolved 
	 * as API components are added.
	 */
	private boolean fAutoResolve = false;
	/**
	 * The VM install this profile is bound to for system libraries or <code>null</code>.
	 * Only used in the IDE when OSGi is running.
	 */
	private IVMInstall fVMBinding = null;
	
	/**
	 * Constructs a new API profile with the given name.
	 * 
	 * @param name profile name
	 */
	public ApiBaseline(String name) {
		super(null, name, IApiElement.BASELINE);
		fAutoResolve = true;
	}	
		
	/**
	 * Constructs a new API profile with the given attributes.
	 * 
	 * @param name profile name
	 * @param eeFile execution environment description file
	 */
	public ApiBaseline(String name, File eeFile) {
		this(name);
		fEeFile = eeFile;
	}

	/**
	 * Clears the package -> components cache and sets it to <code>null</code>
	 */
	private void clearComponentsCache() {
		if(fComponentsCache != null) {
			fComponentsCache.clear();
			fComponentsCache = null;
		}
	}
	
	/**
	 * Adds an {@link IApiComponent} to the fComponentsById mapping
	 * @param component
	 */
	private void addComponent(IApiComponent component) {
		if(component == null) {
			return;
		}
		if(fComponentsById == null) {
			fComponentsById = new HashMap();
		}
		fComponentsById.put(component.getId(), component);
	}
	
	/* (non-Javadoc)
	 * @see IApiBaseline#addApiComponents(org.eclipse.pde.api.tools.model.component.IApiComponent[], boolean)
	 */
	public void addApiComponents(IApiComponent[] components) {
		HashSet ees = new HashSet();
		ApiBaselineInfo info = getInfo();
		State state = info.getState();
		for (int i = 0; i < components.length; i++) {
			BundleApiComponent component = (BundleApiComponent) components[i];
			if (component.isSourceComponent()) {
				continue;
			}
			BundleDescription description = component.getBundleDescription();
			state.addBundle(description);
			storeBundleDescription(description, component);
			addComponent(component);
			ees.addAll(Arrays.asList(component.getExecutionEnvironments()));
		}
		info.resolveSystemLibrary(ees);
		state.resolve();
	}

	/* (non-Javadoc)
	 * @see IApiBaseline#getApiComponents()
	 */
	public IApiComponent[] getApiComponents() {
		if(fComponentsById == null) {
			return EMPTY_COMPONENTS;
		}
		Collection values = fComponentsById.values();
		return (IApiComponent[]) values.toArray(new IApiComponent[values.size()]);
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline#resolvePackage(org.eclipse.pde.api.tools.internal.provisional.IApiComponent, java.lang.String)
	 */
	public synchronized IApiComponent[] resolvePackage(IApiComponent sourceComponent, String packageName) throws CoreException {
		HashMap componentsForPackage = null;
		if(fComponentsCache != null){
			componentsForPackage = (HashMap) fComponentsCache.get(packageName);
		}
		else {
			fComponentsCache = new HashMap(8);
		}
		IApiComponent[] cachedComponents = null;
		if (componentsForPackage != null) {
			cachedComponents = (IApiComponent[]) componentsForPackage.get(sourceComponent);
			if (cachedComponents != null) {
				return cachedComponents;
			}
		} else {
			componentsForPackage = new HashMap(8);
			fComponentsCache.put(packageName, componentsForPackage);
		}
		// check system packages first
		if (isSystemPackage(packageName)) {
			if (fSystemLibraryComponent != null) {
				cachedComponents = new IApiComponent[] { fSystemLibraryComponent };
			} else {
				return EMPTY_COMPONENTS;
			}
		} else {
			if (sourceComponent != null) {
				List componentsList = new ArrayList();
				resolvePackage0(sourceComponent, packageName, componentsList);
				if (componentsList.size() != 0) {
					cachedComponents = new IApiComponent[componentsList.size()];
					componentsList.toArray(cachedComponents);
				}
			}
		}
		if (cachedComponents == null) {
			cachedComponents = EMPTY_COMPONENTS;
		}
		componentsForPackage.put(sourceComponent, cachedComponents);
		return cachedComponents;
	}

	/**
	 * Resolves the listing of {@link IApiComponent}s that export the given package name. The collection 
	 * of {@link IApiComponent}s is written into the specified list <code>componentList</code> 
	 * @param component
	 * @param packageName
	 * @param componentsList
	 * @throws CoreException
	 */
	private void resolvePackage0(IApiComponent component, String packageName, List componentsList) throws CoreException {
		if (component instanceof BundleApiComponent) {
			BundleDescription bundle = ((BundleApiComponent)component).getBundleDescription();
			if (bundle != null) {
				StateHelper helper = getInfo().getState().getStateHelper();
				ExportPackageDescription[] visiblePackages = helper.getVisiblePackages(bundle);
				for (int i = 0; i < visiblePackages.length; i++) {
					ExportPackageDescription pkg = visiblePackages[i];
					if (packageName.equals(pkg.getName())) {
						BundleDescription bundleDescription = pkg.getExporter();
						IApiComponent exporter = getBundleDescription(bundleDescription);
						if (exporter != null) {
							componentsList.add(exporter);
						}
					}
				}
				// check for package within the source component
				String[] packageNames = component.getPackageNames();
				// TODO: would be more efficient to have containsPackage(...) or something
				for (int i = 0; i < packageNames.length; i++) {
					if (packageName.equals(packageNames[i])) {
						componentsList.add(component);
					}
				}
			}
		}
	}
	
	/**
	 * Returns whether the specified package is supplied by the system
	 * library.
	 * 
	 * @param packageName package name
	 * @return whether the specified package is supplied by the system
	 * 	library 
	 */
	private boolean isSystemPackage(String packageName) {
		if (packageName.startsWith("java.")) { //$NON-NLS-1$
			return true;
		}
		if (fSystemPackageNames == null) {
			ExportPackageDescription[] systemPackages = getInfo().getState().getSystemPackages();
			fSystemPackageNames = new HashSet(systemPackages.length);
			for (int i = 0; i < systemPackages.length; i++) {
				fSystemPackageNames.add(systemPackages[i].getName());
			}
		}
		return fSystemPackageNames.contains(packageName);
	}

	
	/* (non-Javadoc)
	 * @see IApiBaseline#newApiComponent(java.lang.String)
	 */
	public IApiComponent newApiComponent(String location) throws CoreException {
		BundleApiComponent component = new BundleApiComponent(this, location);
		if(component.isValidBundle()) {
			component.init(getInfo().getState(), nextId());
			return component;
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see IApiBaseline#newApiComponent(IPluginModelBase)
	 */
	public IApiComponent newApiComponent(IPluginModelBase model) throws CoreException {
		BundleDescription bundleDescription = model.getBundleDescription();
		if (bundleDescription == null) {
			return null;
		}
		String location = bundleDescription.getLocation();
		if (location == null) {
			return null;
		}
		IPath pathForLocation = new Path(location);
		BundleApiComponent component = null;
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if (path != null && path.isPrefixOf(pathForLocation)) {
			if(isValidProject(location)) {
				component = new PluginProjectApiComponent(this, location, model);
			}
		} else {
			component = new BundleApiComponent(this, location);
		}
		if(component != null && component.isValidBundle()) {
			component.init(getInfo().getState(), nextId());
			return component;
		}
		return null;
	}

	/**
	 * Returns if the specified location is a valid API project or not.
	 * <p>
	 * We accept projects that are plug-ins even if not API enabled (i.e.
	 * with API nature), as we still need them to make a complete
	 * API profile without resolution errors.
	 * </p> 
	 * @param location
	 * @return true if the location is valid, false otherwise
	 * @throws CoreException
	 */
	private boolean isValidProject(String location) throws CoreException {
		IPath path = new Path(location);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
		return project != null && project.exists();
	}
	
	/**
	 * Returns the next available bundle identifier.
	 * 
	 * @return next available bundle identifier
	 */
	private long nextId() {
		return ++fNextId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiProfile#getApiComponent(java.lang.String)
	 */
	public IApiComponent getApiComponent(String id) {
		if(fComponentsById == null) {
			return null;
		}
		return (IApiComponent) fComponentsById.get(id);
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline#getExecutionEnvironment()
	 */
	public String getExecutionEnvironment() {
		return getInfo().getExecutionEnvironment();
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline#getResolutionErrors()
	 */
	public ResolverError[] getResolutionErrors() {
		return getInfo().getResolutionErrors();
	}

	/* (non-Javadoc)
	 * @see IApiBaseline#setName(java.lang.String)
	 */
	public void setName(String name) {
		if(name != null) {
			fName = name;
		}
	}

	/* (non-Javadoc)
	 * @see IApiBaseline#dispose()
	 */
	public void dispose() {
		if (ApiPlugin.isRunningInFramework()) {
			JavaRuntime.removeVMInstallChangedListener(this);
		}
		getInfo().dispose();
		
		IApiComponent[] components = getApiComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].dispose();
		}
		if(fComponents != null) {
			fComponents.clear();
			fComponents = null;
		}
		clearComponentsCache();
		if(fComponentsById != null) {
			fComponentsById.clear();
			fComponentsById = null;
		}
		if (fSystemPackageNames != null) {
			fSystemPackageNames.clear();
		}
		if(fSystemLibraryComponent != null) {
			fSystemLibraryComponent.dispose();
			fSystemLibraryComponent = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiProfile#close()
	 */
	public void close() throws CoreException {
		IApiComponent[] components = getApiComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.IApiProfile#getDependentComponents(org.eclipse.pde.api.tools.IApiComponent[])
	 */
	public IApiComponent[] getDependentComponents(IApiComponent[] components) {
		ArrayList bundles = getBundleDescriptions(components);
		BundleDescription[] bundleDescriptions = getInfo().getState().getStateHelper().getDependentBundles((BundleDescription[]) bundles.toArray(new BundleDescription[bundles.size()]));
		return getApiComponents(bundleDescriptions);
	}

	/**
	 * Returns an array of API components corresponding to the given bundle descriptions.
	 * 
	 * @param bundles bundle descriptions
	 * @return corresponding API components
	 */
	private IApiComponent[] getApiComponents(BundleDescription[] bundles) {
		ArrayList dependents = new ArrayList(bundles.length);
		for (int i = 0; i < bundles.length; i++) {
			BundleDescription bundle = bundles[i];
			IApiComponent component = getApiComponent(bundle.getSymbolicName());
			if (component != null) {
				dependents.add(component);
			}
		}
		return (IApiComponent[]) dependents.toArray(new IApiComponent[dependents.size()]);
	}

	/**
	 * Returns an array of bundle descriptions corresponding to the given API components.
	 * 
	 * @param components API components
	 * @return corresponding bundle descriptions
	 */
	private ArrayList getBundleDescriptions(IApiComponent[] components) {
		ArrayList bundles = new ArrayList(components.length);
		for (int i = 0; i < components.length; i++) {
			IApiComponent component = components[i];
			if (component instanceof BundleApiComponent) {
				bundles.add(((BundleApiComponent)component).getBundleDescription());
			}
		}
		return bundles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.IApiProfile#getPrerequisiteComponents(org.eclipse.pde.api.tools.IApiComponent[])
	 */
	public IApiComponent[] getPrerequisiteComponents(IApiComponent[] components) {
		ArrayList bundles = getBundleDescriptions(components);
		BundleDescription[] bundlesDescriptions = getInfo().getState().getStateHelper().getPrerequisites((BundleDescription[]) bundles.toArray(new BundleDescription[bundles.size()]));
		return getApiComponents(bundlesDescriptions);
	}
	
	/**
	 * Gets the {@link BundleDescription} from the cache, if present
	 * @param bundleDescription
	 * @return the {@link BundleDescription} or <code>null</code>
	 */
	private IApiComponent getBundleDescription(BundleDescription bundleDescription) {
		if(fComponents == null) {
			return null;
		}
		return (IApiComponent) fComponents.get(bundleDescription.getSymbolicName() + bundleDescription.getVersion().toString());
	}
	
	/**
	 * Stores the given component in the cache keyed by {@link BundleDescription} symbolic name + version 
	 * @param bundleDescription
	 * @param component
	 */
	private void storeBundleDescription(BundleDescription bundleDescription, IApiComponent component) {
		if(fComponents == null) {
			fComponents = new HashMap(8);
		}
		fComponents.put(bundleDescription.getSymbolicName() + bundleDescription.getVersion().toString(), component);
	}
	
	/**
	 * Clear cached settings for the given package.
	 * 
	 * @param packageName
	 */
	public synchronized void clearPackage(String packageName) {
		if(fComponentsCache != null) {
			fComponentsCache.remove(packageName);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
		if (!(vm instanceof VMStandin)) {
			// there may be a better fit for VMs/EEs
			rebindVM();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	public void vmChanged(PropertyChangeEvent event) {
		if (!(event.getSource() instanceof VMStandin)) {
			String property = event.getProperty();
			if (IVMInstallChangedListener.PROPERTY_INSTALL_LOCATION.equals(property) ||
					IVMInstallChangedListener.PROPERTY_LIBRARY_LOCATIONS.equals(property)) {
				rebindVM();
			}
		}
	}

	/**
	 * Re-binds the VM this profile is bound to.
	 */
	private void rebindVM() {
		fVMBinding = null;
		IApiComponent[] components = getApiComponents();
		HashSet ees = new HashSet();
		for (int i = 0; i < components.length; i++) {
			ees.addAll(Arrays.asList(components[i].getExecutionEnvironments()));
		}
		getInfo().resolveSystemLibrary(ees);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
	 */
	public void vmRemoved(IVMInstall vm) {
		if (vm.equals(fVMBinding)) {
			rebindVM();
		}
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiParent#getChildren()
	 */
	public IApiElement[] getChildren() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiParent#hasChildren()
	 */
	public boolean hasChildren() throws CoreException {
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return new ApiBaselineInfo(this, fEeFile, fAutoResolve);
	}
	
	/**
	 * @return the info for this element
	 */
	private ApiBaselineInfo getInfo() {
		return (ApiBaselineInfo) getElementInfo();
	}
}
