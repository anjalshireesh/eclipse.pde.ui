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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiField;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiPackageFragment;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;

/**
 * Base implementation of {@link IApiType}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiType extends ApiMember implements IApiType {

	/**
	 * Constructor
	 * @param parent
	 * @param name
	 * @param flags
	 */
	protected ApiType(IApiElement parent, String name, int flags) {
		super(parent, name, IApiElement.TYPE, flags);
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getFields()
	 */
	public IApiField[] getFields() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getFullyQualifiedName()
	 */
	public String getFullyQualifiedName() {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getMethods()
	 */
	public IApiMethod[] getMethods() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getPackageFragment()
	 */
	public IApiPackageFragment getPackageFragment() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getSuperClass()
	 */
	public IApiType getSuperClass() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getSuperInterfaces()
	 */
	public IApiType[] getSuperInterfaces() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#getTypes()
	 */
	public IApiType[] getTypes() throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#isAnnotation()
	 */
	public boolean isAnnotation() throws CoreException {
		return Flags.isAnnotation(getFlags());
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#isClass()
	 */
	public boolean isClass() throws CoreException {
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#isEnum()
	 */
	public boolean isEnum() throws CoreException {
		return Flags.isEnum(getFlags());
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#isInterface()
	 */
	public boolean isInterface() throws CoreException {
		return Flags.isInterface(getFlags());
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiType#isMember()
	 */
	public boolean isMember() throws CoreException {
		IApiElement parent = getParent();
		return (parent != null && parent.getType() == IApiElement.TYPE);
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
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getDescriptor()
	 */
	public String getDescriptor() {
		return getFullyQualifiedName();
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
	}
}
