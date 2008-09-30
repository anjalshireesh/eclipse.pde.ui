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
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Base implementation of {@link IApiMethod}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiMethod extends ApiMember implements IApiMethod {

	private String fSignature = null;
	
	/**
	 * Constructor
	 * @param parent
	 * @param name
	 * @param signature
	 * @param flags
	 */
	protected ApiMethod(IApiElement parent, String name, String signature, int flags) {
		super(parent, name, IApiElement.METHOD, flags);
		fSignature = signature;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod#getSignature()
	 */
	public String getSignature() throws CoreException {
		return fSignature;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod#isConstructor()
	 */
	public boolean isConstructor() throws CoreException {
		return getName().equals("<init>"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod#isSimilar(org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod)
	 */
	public boolean isSimilar(IApiMethod method) {
		try {
			return Util.matchesSignatures(getSignature(), method.getSignature());
		}
		catch(CoreException ce) {}
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
		return fSignature;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
	}
}
