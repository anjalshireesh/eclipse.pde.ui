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
package org.eclipse.pde.api.tools.internal;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.pde.api.tools.internal.model.ApiBaseline;
import org.eclipse.pde.api.tools.internal.model.ApiComponent;
import org.eclipse.pde.api.tools.internal.model.ApiResource;
import org.eclipse.pde.api.tools.internal.model.cache.ApiToolsCache;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiResource;

/**
 * Manager to help with model elements / infos caching
 * 
 * @since 1.0.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ApiModelManager {

	/**
	 * The singleton instance of this manager
	 */
	private static ApiModelManager fInstance = null;
	
	/**
	 * The cache of {@link ApiElementInfo}s for the workspace
	 */
	private ApiToolsCache fCache = new ApiToolsCache();
	
	/**
	 * Constructor, made private for no instantiation
	 */
	private ApiModelManager() {}
	
	/**
	 * Returns the available instance of this manager
	 * @return the manager instance
	 */
	public static synchronized ApiModelManager getManager() {
		if(fInstance == null) {
			fInstance = new ApiModelManager();
		}
		return fInstance;
	}
	
	/**
	 * Returns the cached {@link ApiElementInfo} for the given {@link IApiElement}
	 * or <code>null</code> if there is no entry in the cache.
	 * This method does not create new element infos.
	 * @param element
	 * @return the {@link ApiElementInfo} for the given {@link IApiElement} or <code>null</code>
	 */
	public synchronized ApiElementInfo getInfo(IApiElement element) {
		return fCache.getInfo(element);
	}
	
	/**
	 * Adds the given {@link ApiElementInfo} to the cache for the given {@link IApiElement}
	 * @param element
	 * @param info
	 */
	public synchronized void addInfo(IApiElement element, ApiElementInfo info) {
		fCache.putInfo(element, info);
	}
	
	/**
	 * Removes the cached {@link ApiElementInfo} for the given {@link IApiElement} and returns
	 * the removed info, or <code>null</code> if there was no entry in the cache for the given element.
	 * @param element
	 * @return the removed {@link ApiElementInfo} or <code>null</code> if nothing was removed
	 */
	public synchronized ApiElementInfo removeInfo(IApiElement element) {
		return fCache.removeInfo(element);
	}
	
	/**
	 * Creates and returns a new {@link IApiComponent} handle or <code>null</code>
	 * if the given project is <code>null</code>.
	 * The returned handle is not guaranteed to exist. 
	 * @param baseline
	 * @param project
	 * @return a new {@link IApiComponent} handle or <code>null</code>
	 */
	public IApiComponent create(IApiBaseline baseline, IProject project) {
		if(project == null) {
			return null;
		}
		return new ApiComponent(baseline, project.getName());
	}
	
	/**
	 * Creates and returns a new {@link IApiComponent} handle or <code>null</code>
	 * if the given project is <code>null</code>.
	 * The returned handle is not guaranteed to exist. 
	 * @param baseline
	 * @param name
	 * @return a new {@link IApiComponent} handle or <code>null</code>
	 */
	public IApiComponent create(IApiBaseline baseline, String name) {
		if(name == null) {
			return null;
		}
		return new ApiComponent(baseline, name);
	}
	
	/**
	 * Creates and returns a new {@link IApiComponent} handle or <code>null</code>
	 * if the given project is <code>null</code>.
	 * The returned handle is not guaranteed to exist. 
	 * @param project
	 * @return a new {@link IApiComponent} handle or <code>null</code>
	 */
	public IApiComponent create(IProject project) {
		return create((IApiBaseline)null, project);
	}
	
	/**
	 * Creates and returns a new {@link IApiComponent} handle or <code>null</code>
	 * if the given project is <code>null</code>.
	 * The returned handle is not guaranteed to exist. 
	 * @param name
	 * @return a new {@link IApiComponent} handle or <code>null</code>
	 */
	public IApiComponent create(String name) {
		return create(null, name);
	}
	
	/**
	 * Creates and returns a new {@link IApiResource} handle or <code>null</code> 
	 * if the given resource is <code>null</code>.
	 * The returned handle is not guaranteed to exist.
	 * @param resource
	 * @param project
	 * @return a new {@link IApiResource} handle or <code>null</code>
	 */
	public IApiResource create(IResource resource, IProject project) {
		if(resource == null) {
			return null;
		}
		return new ApiResource(create(project), resource);
	}
	
	/**
	 * Creates and returns a new {@link IApiResource} handle with
	 * an undetermined parent project or <code>null</code> if the
	 * given resource is <code>null</code>
	 * @param resource
	 * @return a new {@link IApiResource} handle or <code>null</code>
	 */
	public IApiResource create(IResource resource) {
		return create(resource, null);
	}
	
	/**
	 * Creates and returns a new {@link IApiBaseline} handle
	 * or <code>null</code> if the given name is <code>null</code>
	 * @param name
	 * @param eefile the optional execution environment description file to initialize
	 * resulting infos from.
	 * @return a new {@link IApiBaseline} handle or <code>null</code>
	 */
	public IApiBaseline create(String name, File eefile) {
		if(name == null) {
			return null;
		}
		return new ApiBaseline(name, eefile);
	}
}
