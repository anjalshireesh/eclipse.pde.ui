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
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.api.tools.internal.ApiModelManager;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;

/**
 * Base implementation of {@link IApiElement}
 * 
 * @since 1.0.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ApiElement implements IApiElement {

	/**
	 * The name of the element
	 */
	private String fName = null;
	/**
	 * The parent {@link IApiElement}
	 */
	private IApiElement fParent = null;
	/**
	 * The type of the element
	 * @see IApiElement for complete listing of element types
	 */
	private int fType = -1;
	
	/**
	 * Constructor
	 * @param parent the parent element
	 * @param name the name of the element
	 * @param type the type of the element
	 * @see IApiElement for type constants
	 */
	protected ApiElement(IApiElement parent, String name, int type) {
		fParent = parent;
		fName = name;
		fType = type;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getParent()
	 */
	public IApiElement getParent() {
		return fParent;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getAncestor(int)
	 */
	public IApiElement getAncestor(int ancestorType) {
		IApiElement element = this;
		while(element != null && element.getType() != ancestorType) {
			element  = element.getParent();
		}
		return element;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getType()
	 */
	public int getType() {
		return fType;
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.provisional.model.IApiElement#getDescriptor()
	 */
	public String getDescriptor() {
		return fName;
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
	 * Returns the current {@link ApiElementInfo} for this element. If there
	 * is no info currently in the cache, a call to createElementInfo() is made
	 * to lazily create an info object. If new infos are created they will automatically
	 * be added to the cache.
	 * @return the {@link ApiElementInfo} for this element
	 */
	public ApiElementInfo getElementInfo() {
		ApiModelManager manager = ApiModelManager.getManager();
		ApiElementInfo info = manager.getInfo(this);
		if(info != null) {
			return info;
		}
		info = createElementInfo();
		manager.addInfo(this, info);
 		return info;
	}
	
	/**
	 * Creates and return the new element info for this element.
	 * This method is called if a call to getElementInfo(..) returns <code>null</code>
	 * 
	 * @return the new {@link ApiElementInfo} for this element
	 */
	public abstract ApiElementInfo createElementInfo();
}
