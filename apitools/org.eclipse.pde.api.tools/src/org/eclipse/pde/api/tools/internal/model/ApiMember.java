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
package org.eclipse.pde.api.tools.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMember;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;

/**
 * Base implementation of {@link IApiMember}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ApiMember extends ApiElement implements IApiMember {

	private int fFlags = -1;
	
	protected ApiMember(IApiElement parent, String name, int type, int flags) {
		super(parent, name, type);
		fFlags = flags;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMember#getDeclaringType()
	 */
	public IApiType getDeclaringType() throws CoreException {
		IApiElement tparent = getParent();
		while(tparent.getParent() != null && tparent.getType() == IApiElement.TYPE) {
			tparent = tparent.getParent();
		}
		return (IApiType)tparent;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMember#getFlags()
	 */
	public int getFlags() throws CoreException {
		return fFlags;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiParent#getChildren()
	 */
	public IApiElement[] getChildren() throws CoreException {
		return new IApiElement[0];
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiParent#hasChildren()
	 */
	public boolean hasChildren() throws CoreException {
		return false;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getPath()
	 */
	public IPath getPath() {
		return null;
	}
}
