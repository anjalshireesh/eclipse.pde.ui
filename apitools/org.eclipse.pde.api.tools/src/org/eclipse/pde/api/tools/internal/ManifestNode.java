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

import java.util.HashMap;

import org.eclipse.pde.api.tools.internal.descriptors.ElementDescriptorImpl;
import org.eclipse.pde.api.tools.internal.provisional.RestrictionModifiers;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IFieldDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IMemberDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IMethodDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IPackageDescriptor;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a single node in the tree of mapped manifest items
 */
public class ManifestNode implements Comparable {
	public IElementDescriptor element = null;
	public int visibility, restrictions;
	public ManifestNode parent = null;
	public HashMap children = new HashMap(1);
	
	public ManifestNode(ManifestNode parent, IElementDescriptor element, int visibility, int restrictions) {
		this.element = element;
		this.visibility = visibility;
		this.restrictions = restrictions;
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * This method is safe to override, as the name of an element is unique within its branch of the tree
	 * and does not change over time.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof ManifestNode) {
			return ((ManifestNode)obj).element.equals(element);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 * This method is safe to override, as the name of an element is unique within its branch of the tree
	 * and does not change over time.
	 */
	public int hashCode() {
		return element.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String type = null;
		String name = null;
		switch(element.getElementType()) {
			case IElementDescriptor.T_FIELD: {
				type = "Field"; //$NON-NLS-1$
				name = ((IMemberDescriptor) element).getName();
				break;
			}
			case IElementDescriptor.T_METHOD: {
				type = "Method"; //$NON-NLS-1$
				name = ((IMemberDescriptor) element).getName();
				break;
			}
			case IElementDescriptor.T_PACKAGE: {
				type = "Package"; //$NON-NLS-1$
				name = ((IPackageDescriptor) element).getName();
				break;
			}
			case IElementDescriptor.T_REFERENCE_TYPE: {
				type = "Type"; //$NON-NLS-1$
				name = ((IMemberDescriptor) element).getName();
				break;
			}
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(type == null ? "Unknown" : type).append(" Node: ").append(name == null ? "Unknown Name" : name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buffer.append("\nVisibility: ").append(Util.getVisibilityKind(visibility)); //$NON-NLS-1$
		buffer.append("\nRestrictions: ").append(Util.getRestrictionKind(restrictions)); //$NON-NLS-1$
		if(parent != null) {
			String pname = parent.element.getElementType() == IElementDescriptor.T_PACKAGE ? 
					((IPackageDescriptor)parent.element).getName() : ((IMemberDescriptor)parent.element).getName();
			buffer.append("\nParent: ").append(parent == null ? null : pname); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof ManifestNode) {
			ManifestNode node = (ManifestNode) o;
			return ((ElementDescriptorImpl)element).compareTo(node.element);
		}
		return -1;
	}
	
	/**
	 * Ensure this node is up to date. Default implementation does
	 * nothing. Subclasses should override as required.
	 * 
	 * Returns the resulting node if the node is valid, or <code>null</code>
	 * if the node no longer exists.
	 * 
	 * @return up to date node, or <code>null</code> if no longer exists
	 */
	public ManifestNode refresh() {
		return this;
	}
	
	/**
	 * Persists this node as a child of the given element.
	 * 
	 * @param document XML document
	 * @param parent parent element in the document
	 * @param component component the description is for or <code>null</code>
	 */
	void persistXML(Document document, Element parent) {
		if(restrictions == RestrictionModifiers.NO_RESTRICTIONS) { 
			return;
		}
		switch (element.getElementType()) {
		case IElementDescriptor.T_METHOD:
			IMethodDescriptor md = (IMethodDescriptor) element;
			Element method = document.createElement(IApiXmlConstants.ELEMENT_METHOD);
			method.setAttribute(IApiXmlConstants.ATTR_NAME, md.getName());
			method.setAttribute(IApiXmlConstants.ATTR_SIGNATURE, md.getSignature());
			persistAnnotations(method);
			parent.appendChild(method);
			break;
		case IElementDescriptor.T_FIELD:
			IFieldDescriptor fd = (IFieldDescriptor) element;
			Element field = document.createElement(IApiXmlConstants.ELEMENT_FIELD);
			field.setAttribute(IApiXmlConstants.ATTR_NAME, fd.getName());
			persistAnnotations(field);
			parent.appendChild(field);
			break;
		}
	}
	
	/**
	 * Adds visibility and restrictions to the XML element.
	 * 
	 * @param element XML element to annotate
	 * @param component the component the description is for or <code>null</code>
	 */
	void persistAnnotations(Element element) {
		element.setAttribute(IApiXmlConstants.ATTR_VISIBILITY, Integer.toString(visibility));
		element.setAttribute(IApiXmlConstants.ATTR_RESTRICTIONS, Integer.toString(restrictions));
	}
}
