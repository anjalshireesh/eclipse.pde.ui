/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.shared.target;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.provisional.frameworkadmin.BundleInfo;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.internal.core.target.provisional.IResolvedBundle;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.util.SharedLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides text and image labels for BundleInfo and IResolveBundle objects.
 */
public class BundleInfoLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof BundleInfo) {
			StringBuffer buf = new StringBuffer();
			buf.append(((BundleInfo) element).getSymbolicName());
			String version = ((BundleInfo) element).getVersion();
			if (version != null) {
				buf.append(' ');
				buf.append('(');
				buf.append(version);
				buf.append(')');
			}
			return buf.toString();
		} else if (element instanceof IResolvedBundle) {
			return getText(((IResolvedBundle) element).getBundleInfo());
		}
		return super.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof IResolvedBundle) {
			IResolvedBundle bundle = (IResolvedBundle) element;
			int flag = 0;
			if (bundle.getStatus().getSeverity() == IStatus.WARNING) {
				flag = SharedLabelProvider.F_WARNING;
			} else if (bundle.getStatus().getSeverity() == IStatus.ERROR) {
				flag = SharedLabelProvider.F_ERROR;
			}

			if (bundle.isFragment()) {
				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FRAGMENT_OBJ, flag);
			} else if (bundle.isSourceBundle()) {
				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_MF_OBJ, flag);
			} else {
				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ, flag);
			}
		} else if (element instanceof BundleInfo) {
			return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ);
		}
		return super.getImage(element);
	}

}