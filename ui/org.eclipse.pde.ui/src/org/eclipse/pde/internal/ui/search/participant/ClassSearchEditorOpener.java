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
package org.eclipse.pde.internal.ui.search.participant;

import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.internal.ui.model.bundle.Bundle;
import org.eclipse.pde.internal.ui.model.bundle.ManifestHeader;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;


public class ClassSearchEditorOpener {

	public static IEditorPart open(Match match) throws PartInitException {
		IEditorPart editorPart = null;
		Object element = match.getElement();
		if (element instanceof SearchHit)
			element = ((SearchHit)element).getHitElement();
		if (element instanceof IPluginObject) {
			ISharedPluginModel model = ((IPluginObject)element).getModel();
			if (model instanceof WorkspaceExtensionsModel)
				model = ((WorkspaceExtensionsModel)model).getBundlePluginModel();
			if (model instanceof IPluginModelBase)
				editorPart = ManifestEditor.openPluginEditor(((IPluginModelBase)model).getPluginBase());
		} else if (element instanceof Bundle) {	
			String id = null;
			try {
				Bundle bundle = (Bundle)element;
				ManifestHeader header = bundle.getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
				ManifestElement[] elements = ManifestElement.parseHeader(header.getName(), header.getValue());
				String[] values = elements[0].getValueComponents();
				id = values[0];
			} catch (BundleException e) {
			}
			if (id != null)
				editorPart = ManifestEditor.openPluginEditor(id);
		}
		if (editorPart instanceof ManifestEditor) {
			ManifestEditor editor = (ManifestEditor)editorPart;
			IDocument doc = editor.getDocument(match);
			if (doc != null) {
				editor.openToSourcePage(match.getElement(), match.getOffset(), match.getLength());
			}
		}
		return editorPart;
	}
}