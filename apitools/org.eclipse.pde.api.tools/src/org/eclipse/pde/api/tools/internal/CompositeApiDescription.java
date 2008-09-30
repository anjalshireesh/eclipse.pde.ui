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
package org.eclipse.pde.api.tools.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.api.tools.internal.model.ApiElement;
import org.eclipse.pde.api.tools.internal.model.infos.ApiElementInfo;
import org.eclipse.pde.api.tools.internal.provisional.ApiDescriptionVisitor;
import org.eclipse.pde.api.tools.internal.provisional.IApiAnnotations;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiDescription;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;

/**
 * A host API description combines descriptions of a host and all its
 * fragments.
 * 
 * @since 1.0
 */
public class CompositeApiDescription extends ApiElement implements IApiDescription {
	
	private IApiDescription[] fDescriptions;
	
	/**
	 * Constructs a composite API description out of the given descriptions.
	 * 
	 * @param descriptions
	 */
	public CompositeApiDescription(IApiDescription[] descriptions) {
		super(null, "composite description", IApiElement.COMPONENT);
		fDescriptions = descriptions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiDescription#accept(org.eclipse.pde.api.tools.internal.provisional.ApiDescriptionVisitor)
	 */
	public void accept(ApiDescriptionVisitor visitor) {
		for (int i = 0; i < fDescriptions.length; i++) {
			fDescriptions[i].accept(visitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiDescription#resolveAnnotations(java.lang.String, org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor)
	 */
	public IApiAnnotations resolveAnnotations(IElementDescriptor element) {
		for (int i = 0; i < fDescriptions.length; i++) {
			IApiAnnotations ann = fDescriptions[i].resolveAnnotations(element);
			if (ann != null) {
				return ann;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiDescription#setRestrictions(java.lang.String, org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor, int)
	 */
	public IStatus setRestrictions(IElementDescriptor element, int restrictions) {
		for (int i = 0; i < fDescriptions.length; i++) {
			IStatus status = fDescriptions[i].setRestrictions(element, restrictions);
			if (status.isOK() || i == (fDescriptions.length - 1)) {
				return status;
			}
		}
		return Status.CANCEL_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IApiDescription#setVisibility(java.lang.String, org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor, int)
	 */
	public IStatus setVisibility(IElementDescriptor element, int visibility) {
		for (int i = 0; i < fDescriptions.length; i++) {
			IStatus status = fDescriptions[i].setVisibility(element, visibility);
			if (status.isOK() || i == (fDescriptions.length - 1)) {
				return status;
			}
		}
		return Status.CANCEL_STATUS;
	}	

	/**
	 * Disconnects an underlying API description from the given bundle.
	 * 
	 * @param bundle
	 */
	void disconnect(BundleDescription bundle) {
		for (int i = 0; i < fDescriptions.length; i++) {
			IApiDescription description = fDescriptions[i];
			if (description instanceof ProjectApiDescription) {
				ProjectApiDescription pad = (ProjectApiDescription) description;
				if (bundle.equals(pad.getConnection())) {
					pad.disconnect(bundle);
					return;
				}
			}
		}
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
	 * @see org.eclipse.pde.api.tools.internal.model.ApiElement#createElementInfo()
	 */
	public ApiElementInfo createElementInfo() {
		return null;
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
}
