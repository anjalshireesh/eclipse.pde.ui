/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.site;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.internal.core.isite.ISite;
import org.eclipse.pde.internal.core.isite.ISiteArchive;
import org.eclipse.pde.internal.core.isite.ISiteCategory;
import org.eclipse.pde.internal.core.isite.ISiteCategoryDefinition;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.ui.editor.FormOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;

public class SiteOutlinePage extends FormOutlinePage {
	private LabelProvider fLabelProvider;

	/**
	 * @param editor
	 */
	public SiteOutlinePage(PDEFormEditor editor) {
		super(editor);
	}

	protected Object[] getChildren(Object parent) {
		if (parent instanceof PDEFormPage) {
			PDEFormPage page = (PDEFormPage) parent;
			ISiteModel model = (ISiteModel) page.getModel();
			if (model.isValid()) {
				ISite site = model.getSite();
				if (page.getId().equals(FeaturesPage.PAGE_ID)) {
					ArrayList result = new ArrayList();
					ISiteCategoryDefinition[] catDefs = site
							.getCategoryDefinitions();
					for (int i = 0; i < catDefs.length; i++) {
						result.add(catDefs[i]);
					}
					ISiteFeature[] features = site.getFeatures();
					for (int i = 0; i < features.length; i++) {
						if (features[i].getCategories().length == 0)
							result
									.add(new SiteFeatureAdapter(null,
											features[i]));
					}
					return result.toArray();
				}
				if (page.getId().equals(ArchivePage.PAGE_ID))
					return site.getArchives();
			}
		}
		if (parent instanceof ISiteCategoryDefinition) {
			ISiteCategoryDefinition catDef = (ISiteCategoryDefinition) parent;
			ISiteModel model = catDef.getModel();
			if (model.isValid()) {
				ISite site = model.getSite();
				ISiteFeature[] features = site.getFeatures();
				HashSet result = new HashSet();
				for (int i = 0; i < features.length; i++) {
					ISiteCategory[] cats = features[i].getCategories();
					for (int j = 0; j < cats.length; j++) {
						if (cats[j].getDefinition() != null
								&& cats[j].getDefinition().equals(catDef)) {
							result.add(new SiteFeatureAdapter(
									cats[j].getName(), features[i]));
						}
					}
				}
				return result.toArray();
			}
		}
		return new Object[0];
	}

	protected String getParentPageId(Object item) {
		String pageId = null;
		if (item instanceof ISiteCategoryDefinition
				|| item instanceof SiteFeatureAdapter)
			pageId = FeaturesPage.PAGE_ID;
		else if (item instanceof ISiteArchive)
			pageId = ArchivePage.PAGE_ID;
		if (pageId != null)
			return pageId;
		return super.getParentPageId(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.FormOutlinePage#createLabelProvider()
	 */
	protected ILabelProvider createLabelProvider() {
		fLabelProvider = new SiteLabelProvider();
		return fLabelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.FormOutlinePage#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fLabelProvider != null)
			fLabelProvider.dispose();
	}
}
