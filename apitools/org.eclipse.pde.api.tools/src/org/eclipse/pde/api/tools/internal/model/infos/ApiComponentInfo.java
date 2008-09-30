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
 * {@link IApiComponent} specific info.
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiComponentInfo extends ApiElementInfo {

	/**
	 * Constructor
	 * @param owner
	 */
	public ApiComponentInfo(IApiElement owner) {
		super(owner);
	}

}
