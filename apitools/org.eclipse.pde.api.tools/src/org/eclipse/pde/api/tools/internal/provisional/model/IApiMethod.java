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
package org.eclipse.pde.api.tools.internal.provisional.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Describes a method.
 * 
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiMethod extends IApiMember {

	/**
	 * Returns if this method is a constructor or not.
	 * 
	 * @return is this method is a constructor
	 * @throws CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 */
	public boolean isConstructor() throws CoreException;
	
	/**
	 * Returns the signature of this method.
	 * 
	 * @return the signature of this method
	 * @throws CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 */
	public String getSignature() throws CoreException;
	
	/**
	 * Returns whether this method is similar to the given method.
	 * Two methods are similar if:
	 * <ul>
	 * <li>their element names are equal</li>
	 * <li>they have the same number of parameters</li>
	 * <li>the simple names of their parameter types are equal</li>
	 * </ul>
	 * This is a handle-only method.
	 *
	 * @param method the given method
	 * @return true if this method is similar to the given method.
	 */
	public boolean isSimilar(IApiMethod method);
}
