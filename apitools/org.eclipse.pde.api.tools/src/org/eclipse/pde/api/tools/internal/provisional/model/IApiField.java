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
 * Describes a field with an {@link IApiType}
 * 
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiField extends IApiMember {

	/**
	 * Returns if this field is an enum constant.
	 * 
	 * @return true if this field is an enum constant, false otherwise
	 * @throws CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 */
	public boolean isEnumConstant() throws CoreException;
}
