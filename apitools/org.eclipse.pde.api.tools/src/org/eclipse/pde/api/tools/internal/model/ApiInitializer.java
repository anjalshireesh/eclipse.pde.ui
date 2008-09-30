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

import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiInitializer;

/**
 * Base implementation of {@link IApiInitializer}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiInitializer extends ApiMember implements IApiInitializer {

	/**
	 * Constructor
	 * @param parent
	 * @param name
	 * @param flags
	 */
	protected ApiInitializer(IApiElement parent, String name, int flags) {
		super(parent, name, IApiElement.INITIALIZER, flags);
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
	}
}
