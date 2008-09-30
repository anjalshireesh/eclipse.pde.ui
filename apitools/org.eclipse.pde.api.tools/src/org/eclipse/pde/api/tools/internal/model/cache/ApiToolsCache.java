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
package org.eclipse.pde.api.tools.internal.model.cache;

import java.util.HashMap;

import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiDescription;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;

/**
 * Cache of {@link IApiElement} infos, API descriptions and {@link IApiBaseline}s
 * 
 * @since 1.0.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ApiToolsCache {
	
	public static final int DEFAULT_CACHE_SIZE = 20;
	public static final int DEFAULT_MEMBER_CACHE_SIZE = 100;
	
	/**
	 * The cache of {@link IApiDescription}s
	 */
	private ApiElementCache fDescriptionCache = new ApiElementCache(DEFAULT_CACHE_SIZE);
	
	/**
	 * The cache of {@link IApiComponent} infos
	 */
	private ApiElementCache fComponentCache = new ApiElementCache(DEFAULT_CACHE_SIZE);
	
	/**
	 * The cache of {@link IApiMember} infos
	 */
	private HashMap fMemberCache = new HashMap(DEFAULT_MEMBER_CACHE_SIZE);

	/**
	 * Returns the cached {@link IApiDescription} for the given component or <code>null</code>
	 * if there is no {@link IApiDescription} cached for the given component.
	 * 
	 * @param component
	 * @return the cached {@link IApiDescription} for the given component or <code>null</code> if none.
	 */
	public IApiDescription getApiDescription(IApiComponent component) {
		return (IApiDescription) fDescriptionCache.get(component);
	}
	
	/**
	 * Adds a new cached {@link IApiDescription} for the given component
	 * @param component
	 * @param description
	 */
	public void addApiDescription(IApiComponent component, IApiDescription description) {
		fDescriptionCache.put(component, description);
	}
	
	/**
	 * Removes the cached {@link IApiDescription} for the given component and returns 
	 * the cached description, or <code>null</code> if nothing was removed
	 * @param component
	 * @return the removed {@link IApiDescription} or <code>null</code> if nothing was removed
	 */
	public IApiDescription removeApiDescription(IApiComponent component) {
		return (IApiDescription) fDescriptionCache.remove(component);
	}
	
	/**
	 * Returns the {@link ApiElementInfo} for the given {@link IApiElement} or <code>null</code>
	 * if the info was not in the cache
	 * @param element
	 * @return the {@link ApiElementInfo} or <code>null</code>
	 */
	public ApiElementInfo getInfo(IApiElement element) {
		switch(element.getType()) {
			case IApiElement.COMPONENT: {
				return (ApiElementInfo) fComponentCache.get(element);
			}
			case IApiElement.PACKAGE_ROOT: {
				break;
			}
			case IApiElement.PACKAGE: {
				break;
			}
			case IApiElement.TYPE: {
				break;
			}
			case IApiElement.INITIALIZER:
			case IApiElement.FIELD: 
			case IApiElement.METHOD: {
				return (ApiElementInfo) fMemberCache.get(element);
			}
			case IApiElement.RESOURCE: {
				break;
			}
		}
		return null;
	}
	
	/**
	 * Adds new {@link ApiElementInfo}s to the cache
	 * @param element
	 * @param info
	 */
	public void putInfo(IApiElement element, ApiElementInfo info) {
		switch(element.getType()) {
			case IApiElement.COMPONENT: {
				fComponentCache.put(element, info);
				break;
			}
			case IApiElement.PACKAGE_ROOT: {
				break;
			}
			case IApiElement.PACKAGE: {
				break;
			}
			case IApiElement.TYPE: {
				break;
			}
			case IApiElement.INITIALIZER:
			case IApiElement.FIELD: 
			case IApiElement.METHOD: {
				fMemberCache.put(element, info);
				break;
			}
			case IApiElement.RESOURCE: {
				break;
			}
		}
	}
	
	/**
	 * Removes the {@link ApiElementInfo} from the cache for the specified 
	 * {@link IApiElement}. Returns the removed info or <code>null</code> if there 
	 * was nothing removed.
	 * 
	 * @param element
	 * @return the removed {@link ApiElementInfo} or <code>null</code> if nothing was removed
	 */
	public ApiElementInfo removeInfo(IApiElement element) {
		switch(element.getType()) {
			case IApiElement.COMPONENT: {
				return (ApiElementInfo) fComponentCache.remove(element);
			}
			case IApiElement.PACKAGE_ROOT: {
				break;
			}
			case IApiElement.PACKAGE: {
				break;
			}
			case IApiElement.TYPE: {
				break;
			}
			case IApiElement.INITIALIZER:
			case IApiElement.FIELD: 
			case IApiElement.METHOD: {
				return (ApiElementInfo) fMemberCache.remove(element);
			}
			case IApiElement.RESOURCE: {
				break;
			}
		}
		return null;
	}
}
