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
 * Describes a Java type (class, interface, enum or annotation).
 * 
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiType extends IApiMember {

	/**
	 * Returns whether this type represents a class.
	 * <p>
	 * Note that a class can neither be an interface, an enumeration class, nor an annotation type.
	 * </p>
	 * @throws CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 * @return true if this type represents a class, false otherwise
	 */
	public boolean isClass() throws CoreException;
	
	/**
	 * Returns whether this type represents an interface.
	 * <p>
	 * Note that an interface can also be an annotation type, but it can neither be a class nor an enumeration class.
	 * </p>
	 * @return true if this type represents an interface, false otherwise
	 * @throws CoreException
	 */
	public boolean isInterface() throws CoreException;
	
	/**
	 * Returns whether this type represents an enumeration class.
	 * <p>
	 * Note that an enumeration class can neither be a class, an interface, nor an annotation type.
	 * </p>
	 * @throws CoreExeption if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 * @return true if this type represents an enumeration class, false otherwise
	 */
	public boolean isEnum() throws CoreException;

	/**
	 * Returns whether this type represents an annotation type.
	 * <p>
	 * Note that an annotation type is also an interface, but it can neither be a class nor an enumeration class.
	 * </p>
	 * @return true if this type represents an annotation type, false otherwise
	 * @throws CoreException
	 */
	public boolean isAnnotation() throws CoreException;
	
	/**
	 * Returns whether this type represents a member type.
	 * 
	 * @return true if this type represents a member type, false otherwise
	 * @throws CoreException
	 */
	public boolean isMember() throws CoreException;
	
	/**
	 * Returns the fully qualified name of this type.
	 * Member types are denoted using '$'. For example a.b.c.C$InnerC
	 * 
	 * @return the fully qualified name of the type
	 */
	public String getFullyQualifiedName();
	
	/**
	 * Returns the immediate member types declared by this type.
	 * The results are listed in the order in which they appear in the source or class file.
	 *
	 * @throws CoreException if this element does not exist or if an exception occurs while accessing its corresponding resource.
	 * @return the immediate member types declared by this type
	 */
	public IApiType[] getTypes() throws CoreException;
	
	/**
	 * Returns all of the {@link IApiField}s directly enclosed by this type.
	 * 
	 * @return all of the {@link IApiField}s of this type
	 * @throws CoreException if the element does not exist or an exception occurs while accessing its corresponding resource.
	 */
	public IApiField[] getFields() throws CoreException;
	
	/**
	 * Returns all of the {@link IApiMethod}s directly enclosed by this type.
	 * 
	 * @return all of the {@link IApiMethod}s of this type
	 * @throws CoreException if the element does not exist or an exception occurs while accessing its corresponding resource.
	 */
	public IApiMethod[] getMethods() throws CoreException;
	
	/**
	 * Returns the immediate enclosing {@link IApiPackageFragment} that this type resides in.
	 * 
	 * @return the immediate parent {@link IApiPackageFragment} this type resides in
	 * @throws CoreException if the element does not exist or an exception occurs while accessing its corresponding resource. 
	 */
	public IApiPackageFragment getPackageFragment() throws CoreException;
	
	/**
	 * Returns the super-class of this type if there is one, <code>null</code> otherwise.
	 * 
	 * @return the super-class of this type or <code>null</code> for types that do not have super-classes
	 * @throws CoreException if the element does not exist or an exception occurs while accessing its corresponding resource. 
	 */
	public IApiType getSuperClass() throws CoreException;
	
	/**
	 * Returns the super-interface set of this type if there is one, <code>null</code> otherwise.
	 * 
	 * @return the super-interface set of this type or <code>null</code> if there isn't one
	 * @throws CoreException if the element does not exist or an exception occurs while accessing its corresponding resource. 
	 */
	public IApiType[] getSuperInterfaces() throws CoreException;
}
