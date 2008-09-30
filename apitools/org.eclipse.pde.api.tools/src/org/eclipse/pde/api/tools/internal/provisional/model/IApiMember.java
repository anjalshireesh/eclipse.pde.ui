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
 * Description of elements that can be members of {@link IApiType}s
 * <p>
 * This set consists of:
 * <ul>
 * <li>{@link IApiType}</li>
 * <li>{@link IApiField}</li>
 * <li>{@link IApiMethod}</li>
 * </ul>
 * </p>
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiMember extends IApiParent, IApiElement {

	/**
	 * Returns the smallest enclosing {@link IApiType} that encloses this element or 
	 * <code>null</code> if the enclosing type is a top-level type.
	 * 
	 * @return the smallest enclosing {@link IApiType} of this element or <code>null</code>
	 * @throws CoreException
	 */
	public IApiType getDeclaringType() throws CoreException;
	
	/**
	 * Returns the modifier flags for this member. The flags can be examined using the class
	 * <code>Flags</code>.
	 * <p>
	 * Note that only flags as indicated in the source are returned. Thus if an interface
	 * defines a method <code>void myMethod();</code> the flags don't include the
	 * 'public' flag.
	 *
	 * @exception CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 * @return the modifier flags for this member
	 * @see Flags
	 */
	public int getFlags() throws CoreException;
}
