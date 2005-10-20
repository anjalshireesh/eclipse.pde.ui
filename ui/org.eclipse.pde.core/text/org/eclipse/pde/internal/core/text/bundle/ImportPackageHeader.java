/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.text.bundle;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.ibundle.IBundle;

public class ImportPackageHeader extends BasePackageHeader {

    private static final long serialVersionUID = 1L;

    public ImportPackageHeader(String name, String value, IBundle bundle, String lineDelimiter) {
		super(name, value, bundle, lineDelimiter);
	}

    protected PDEManifestElement createElement(ManifestElement element) {
    	return new ImportPackageObject(this, element, getVersionAttribute());
    }
    
    public ImportPackageObject[] getPackages() {
        return (ImportPackageObject[])getElements();
    }
    


}
