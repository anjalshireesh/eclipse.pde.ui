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
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;

/**
 * Base implementation of {@link IApiProject}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiComponent extends ApiElement implements IApiComponent {

	/**
	 * Constructor
	 * @param parent
	 * @param name
	 * @param type
	 */
	public ApiComponent(IApiElement parent, String name) {
		super(parent, name, IApiElement.COMPONENT);
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
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getPath()
	 */
	public IPath getPath() {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
	}
}
