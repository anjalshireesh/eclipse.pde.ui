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
import org.eclipse.jdt.core.Flags;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiField;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;

/**
 * Base implementation of {@link IApiField}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiField extends ApiMember implements IApiField {

	/**
	 * Constructor
	 * @param parent
	 * @param name
	 * @param flags
	 */
	protected ApiField(IApiElement parent, String name, int flags) {
		super(parent, name, IApiElement.FIELD, flags);
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiField#isEnumConstant()
	 */
	public boolean isEnumConstant() throws CoreException {
		return (Flags.isEnum(getFlags()));
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMember#getDeclaringType()
	 */
	public IApiType getDeclaringType() throws CoreException {
		return (IApiType) getParent();
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getPath()
	 */
	public IPath getPath() {
		return getParent().getPath();
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getResource()
	 */
	public IResource getResource() {
		return getParent().getResource();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		IApiType type = null;
		try {
			type = getDeclaringType();
		}
		catch(CoreException ce) {}
		return getName().hashCode() + (type == null ? 0 : type.hashCode());
	}	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IApiField) {
			IApiField field = (IApiField) obj;
			IApiType type = null, ftype = null;
			try {
				type = getDeclaringType();
				ftype = field.getDeclaringType();
			}
			catch(CoreException ce) {}
			return getName().equals(field.getName()) && (type == null ? true : type.equals(ftype));
		}
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
	}
}
