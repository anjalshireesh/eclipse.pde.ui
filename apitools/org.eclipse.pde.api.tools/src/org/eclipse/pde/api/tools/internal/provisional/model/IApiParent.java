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
 * Describes an {@link IApiElement} that can have other {@link IApiElement}s as children
 * 
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiParent {

	/**
	 * Returns the immediate children of this element.
	 * Unless otherwise specified by the implementing element the children are in no particular order.
	 *
	 * @exception CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource
	 * @return the immediate children of this element
	 */
	public IApiElement[] getChildren() throws CoreException;
	
	/**
	 * Returns whether this element has one or more immediate children.
	 *
	 * @exception CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource
	 * @return true if the immediate children of this element, false otherwise
	 */
	public boolean hasChildren() throws CoreException;	
}
