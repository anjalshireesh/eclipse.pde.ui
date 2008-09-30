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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.model.ApiResource;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiResource;

/**
 * {@link IApiResource} element info.
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiResourceInfo extends ApiElementInfo {

	private IResource fResource = null;
	
	/**
	 * Constructor
	 */
	public ApiResourceInfo(IApiResource owner, IResource resource) {
		super(owner);
		fResource = resource;
	}
	
	/**
	 * @return if the resource has children
	 */
	public boolean hasChildren() throws CoreException {
		switch(fResource.getType()) {
			case IResource.PROJECT: {
				return ((IProject)fResource).members().length > 0;
			}
			case IResource.FOLDER: {
				return ((IFolder)fResource).members().length > 0;
			}
		}
		return false;
	}
	
	private IResource[] getMembers() throws CoreException {
		switch(fResource.getType()) {
			case IResource.PROJECT: {
				return ((IProject)fResource).members();
			}
			case IResource.FOLDER: {
				return ((IFolder)fResource).members();
			}
		}
		return null;
	}
	
	/**
	 * @return the children of this resource
	 */
	public IApiElement[] getChildren() throws CoreException {
		IApiElement[] children = null;
		if(hasChildren()) {
			IResource[] members = getMembers();
			if(members == null) {
				return null;
			}
			children = new IApiElement[members.length];
			IApiResource owner = (IApiResource) getOwner();
			for(int i = 0; i < members.length; i++) {
				//TODO should we cache the children of a resource element?
				children[i] = new ApiResource(owner, members[i]);
			}
		}
		return children;
	}
}
