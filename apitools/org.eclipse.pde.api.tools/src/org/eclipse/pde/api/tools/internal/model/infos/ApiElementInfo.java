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

import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;

/**
 * Describes backing non-handle information for an {@link IApiElement}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ApiElementInfo {

	private IApiElement fOwner = null;
	
	/**
	 * Constructor
	 */
	public ApiElementInfo(IApiElement owner) {
		fOwner = owner;
	}
	
	/**
	 * @return the {@link IApiElement} that owns this info
	 */
	protected IApiElement getOwner() {
		return fOwner;
	}
}
