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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureImport;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.isite.ISiteCategory;
import org.eclipse.pde.internal.core.isite.ISiteCategoryDefinition;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.build.BuildSiteJob;
import org.eclipse.pde.internal.ui.editor.ModelDataTransfer;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.TreeSection;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.pde.internal.ui.parts.TreePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;

public class CategorySection extends TreeSection {
	private static final int BUTTON_ADD_CATEGORY = 0;

	private static final int BUTTON_ADD_FEATURE = 1;

	// private static final int BUTTON_SEPARATOR = 2;

	private static final int BUTTON_BUILD_FEATURE = 3;

	private static final int BUTTON_BUILD_ALL = 4;
	
	private static int newCategoryCounter;

	private ISiteModel fModel;

	private TreePart fCategoryTreePart;

	private TreeViewer fCategoryViewer;
	
	private LabelProvider fSiteLabelProvider;

	class CategoryContentProvider extends DefaultContentProvider implements
			ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			// model = (ISite) inputElement;
			ArrayList result = new ArrayList();
			ISiteCategoryDefinition[] catDefs = fModel.getSite()
					.getCategoryDefinitions();
			for (int i = 0; i < catDefs.length; i++) {
				result.add(catDefs[i]);
			}
			ISiteFeature[] features = fModel.getSite().getFeatures();
			for (int i = 0; i < features.length; i++) {
				if (features[i].getCategories().length == 0)
					result.add(new SiteFeatureAdapter(null, features[i]));
			}
			return result.toArray();
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof ISiteCategoryDefinition) {
				ISiteCategoryDefinition catDef = (ISiteCategoryDefinition) parent;
				ISiteFeature[] features = fModel.getSite().getFeatures();
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
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ISiteCategoryDefinition) {
				ISiteCategoryDefinition catDef = (ISiteCategoryDefinition) element;
				ISiteFeature[] features = fModel.getSite().getFeatures();
				for (int i = 0; i < features.length; i++) {
					ISiteCategory[] cats = features[i].getCategories();
					for (int j = 0; j < cats.length; j++) {
						if (cats[j].getDefinition() != null
								&& cats[j].getDefinition().equals(catDef)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	public CategorySection(PDEFormPage formPage, Composite parent) {
		super(formPage, parent, Section.DESCRIPTION, new String[] {
				PDEPlugin.getResourceString("CategorySection.new"), //$NON-NLS-1$
				PDEPlugin.getResourceString("CategorySection.add"), //$NON-NLS-1$
				null, PDEPlugin.getResourceString("CategorySection.build"), //$NON-NLS-1$
				PDEPlugin.getResourceString("CategorySection.buildAll") }); //$NON-NLS-1$
		getSection().setText(
				PDEPlugin.getResourceString("CategorySection.title")); //$NON-NLS-1$
		getSection().setDescription(
				PDEPlugin.getResourceString("CategorySection.desc")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.update.ui.forms.internal.FormSection#createClient(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.update.ui.forms.internal.FormWidgetFactory)
	 */
	public void createClient(Section section, FormToolkit toolkit) {
		fModel = (ISiteModel) getPage().getModel();
		fModel.addModelChangedListener(this);

		Composite container = createClientContainer(section, 2, toolkit);
		createViewerPartControl(container, SWT.SINGLE, 2, toolkit);
		fCategoryTreePart = getTreePart();
		fCategoryViewer = fCategoryTreePart.getTreeViewer();
		fCategoryViewer.setContentProvider(new CategoryContentProvider());
		fSiteLabelProvider = new SiteLabelProvider();
		fCategoryViewer.setLabelProvider(fSiteLabelProvider);

		fCategoryViewer.setInput(fModel.getSite());
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { ModelDataTransfer.getInstance() };
		if (isEditable()) {
			fCategoryViewer.addDropSupport(ops, transfers,
					new ViewerDropAdapter(fCategoryViewer) {
						public void dragEnter(DropTargetEvent event) {
							Object target = determineTarget(event);
							if (target == null && event.detail == DND.DROP_COPY) {
								event.detail = DND.DROP_MOVE;
							}
							super.dragEnter(event);
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
						 */
						public void dragOperationChanged(DropTargetEvent event) {
							Object target = determineTarget(event);
							if (target == null && event.detail == DND.DROP_COPY) {
								event.detail = DND.DROP_MOVE;
							}
							super.dragOperationChanged(event);
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
						 */
						public void dragOver(DropTargetEvent event) {
							Object target = determineTarget(event);
							if (target == null && event.detail == DND.DROP_COPY) {
								event.detail = DND.DROP_MOVE;
							}
							super.dragOver(event);
						}

						/**
						 * Returns the position of the given event's coordinates
						 * relative to its target. The position is determined to
						 * be before, after, or on the item, based on some
						 * threshold value.
						 * 
						 * @param event
						 *            the event
						 * @return one of the <code>LOCATION_* </code>
						 *         constants defined in this class
						 */
						protected int determineLocation(DropTargetEvent event) {
							if (!(event.item instanceof Item)) {
								return LOCATION_NONE;
							}
							Item item = (Item) event.item;
							Point coordinates = new Point(event.x, event.y);
							coordinates = getViewer().getControl().toControl(
									coordinates);
							if (item != null) {
								Rectangle bounds = getBounds(item);
								if (bounds == null) {
									return LOCATION_NONE;
								}
							}
							return LOCATION_ON;
						}

						public boolean performDrop(Object data) {
							if (!(data instanceof Object[]))
								return false;
							Object target = getCurrentTarget();

							int op = getCurrentOperation();
							Object[] objects = (Object[]) data;
							if (objects.length > 0
									&& objects[0] instanceof SiteFeatureAdapter) {
								if (op == DND.DROP_COPY && target != null) {
									copyFeature(
											(SiteFeatureAdapter) objects[0],
											target);
								} else {
									moveFeature(
											(SiteFeatureAdapter) objects[0],
											target);
								}
								return true;
							}
							return false;
						}

						public boolean validateDrop(Object target,
								int operation, TransferData transferType) {
							return (target instanceof ISiteCategoryDefinition || target == null);
						}

					});
		}

		fCategoryViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY,
				transfers, new DragSourceListener() {
					public void dragStart(DragSourceEvent event) {
						IStructuredSelection ssel = (IStructuredSelection) fCategoryViewer
								.getSelection();
						if (ssel == null
								|| ssel.isEmpty()
								|| !(ssel.getFirstElement() instanceof SiteFeatureAdapter)) {
							event.doit = false;
						}
					}

					public void dragSetData(DragSourceEvent event) {
						IStructuredSelection ssel = (IStructuredSelection) fCategoryViewer
								.getSelection();
						event.data = ssel.toArray();
					}

					public void dragFinished(DragSourceEvent event) {
					}
				});
		
		fCategoryTreePart.setButtonEnabled(BUTTON_ADD_CATEGORY, isEditable());
		fCategoryTreePart.setButtonEnabled(BUTTON_ADD_FEATURE, isEditable());
		fCategoryTreePart.setButtonEnabled(BUTTON_BUILD_FEATURE, isEditable());
		fCategoryTreePart.setButtonEnabled(BUTTON_BUILD_ALL, isEditable());

		// fCategoryViewer.expandAll();
		toolkit.paintBordersFor(container);
		section.setClient(container);
		initialize();
	}

	private boolean categoryExists(String name) {
		ISiteCategoryDefinition [] defs = fModel.getSite().getCategoryDefinitions();
		for (int i=0; i<defs.length; i++) {
			ISiteCategoryDefinition def = defs[i];
			String dname = def.getName();
			if (dname!=null && dname.equals(name))
				return true;
		}
		return false;
	}
	
	private void copyFeature(SiteFeatureAdapter adapter, Object target) {
		ISiteFeature feature = findRealFeature(adapter);
		if (feature == null) {
			return;
		}
		/*
		 * if (adapter.category == null) { moveFeature(adapter, target); } else
		 */if (target != null && target instanceof ISiteCategoryDefinition) {
			addCategory(feature, ((ISiteCategoryDefinition) target).getName());
		}
	}

	private void addCategory(ISiteFeature aFeature, String catName) {
		try {
			if (aFeature == null)
				return;
			ISiteCategory[] cats = aFeature.getCategories();
			for (int j = 0; j < cats.length; j++) {
				if (cats[j].getName().equals(catName))
					return;
			}
			ISiteCategory cat = fModel.getFactory().createCategory(aFeature);
			cat.setName(catName);
			expandCategory(catName);
			aFeature.addCategories(new ISiteCategory[] { cat });
		} catch (CoreException e) {
		}
	}

	private void moveFeature(SiteFeatureAdapter adapter, Object target) {
		ISiteFeature feature = findRealFeature(adapter);
		if (feature == null) {
			return;
		}
		if (adapter.category != null) {
			removeCategory(feature, adapter.category);
		}
		if (target != null && target instanceof ISiteCategoryDefinition) {
			addCategory(feature, ((ISiteCategoryDefinition) target).getName());
		}
	}

	protected void buttonSelected(int index) {
		switch (index) {
		case BUTTON_ADD_CATEGORY:
			handleAddCategoryDefinition();
			break;
		case BUTTON_ADD_FEATURE:
			handleNewFeature();
			break;
		case BUTTON_BUILD_FEATURE:
			handleBuild();
			break;
		case BUTTON_BUILD_ALL:
			handleBuild(fModel.getSite().getFeatures());
		}
	}

	protected void handleDoubleClick(IStructuredSelection ssel) {
		Object selected = ssel.getFirstElement();
		if (selected instanceof SiteFeatureAdapter) {
			IFeature feature = getFeature(((SiteFeatureAdapter) selected).feature);
			if (feature != null) {
				IFile file = (IFile) feature.getModel().getUnderlyingResource();
				if (file != null && file.exists()) {
					IWorkbenchPage page = PDEPlugin.getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	protected void selectionChanged(IStructuredSelection selection) {
		getPage().getPDEEditor().setSelection(selection);
		updateButtons();
	}

	private void handleAddCategoryDefinition() {
		String name = PDEPlugin.getFormattedMessage(
				"CategorySection.newCategoryName", //$NON-NLS-1$
				Integer .toString(++newCategoryCounter));
		while (categoryExists(name)) {
			name = PDEPlugin.getFormattedMessage(
					"CategorySection.newCategoryName", //$NON-NLS-1$
					Integer .toString(++newCategoryCounter));
		}
		String label = PDEPlugin.getFormattedMessage(
				"CategorySection.newCategoryLabel", //$NON-NLS-1$
				Integer .toString(newCategoryCounter));
		ISiteCategoryDefinition categoryDef = fModel.getFactory()
				.createCategoryDefinition();
		try {
			categoryDef.setName(name);
			categoryDef.setLabel(label);
			fModel.getSite().addCategoryDefinitions(
					new ISiteCategoryDefinition[] { categoryDef });
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}

	private boolean handleRemove() {
		IStructuredSelection ssel = (IStructuredSelection) fCategoryViewer
				.getSelection();
		Object object = ssel.getFirstElement();
		if (object == null)
			return true;
		if (object instanceof ISiteCategoryDefinition) {
			return handleRemoveCategoryDefinition((ISiteCategoryDefinition) object);
		}
		return handleRemoveSiteFeatureAdapter((SiteFeatureAdapter) object);
	}

	private boolean handleRemoveCategoryDefinition(
			ISiteCategoryDefinition catDef) {
		try {
			Object[] children = ((CategoryContentProvider) fCategoryViewer
					.getContentProvider()).getChildren(catDef);
			for (int i = 0; i < children.length; i++) {
				SiteFeatureAdapter adapter = (SiteFeatureAdapter) children[i];
				ISiteCategory[] cats = adapter.feature.getCategories();
				for (int j = 0; j < cats.length; j++) {
					if (adapter.category.equals(cats[j].getName()))
						adapter.feature
								.removeCategories(new ISiteCategory[] { cats[j] });
				}
				if (adapter.feature.getCategories().length == 0) {
					fModel.getSite().removeFeatures(
							new ISiteFeature[] { adapter.feature });
				}
			}
			fModel.getSite().removeCategoryDefinitions(
					new ISiteCategoryDefinition[] { catDef });
			return true;
		} catch (CoreException e) {
		}
		return false;
	}

	private boolean handleRemoveSiteFeatureAdapter(SiteFeatureAdapter adapter) {
		try {
			ISiteFeature feature = adapter.feature;
			if (adapter.category == null) {
				fModel.getSite().removeFeatures(new ISiteFeature[] { feature });
			} else {
				removeCategory(feature, adapter.category);
				if (feature.getCategories().length == 0)
					fModel.getSite().removeFeatures(
							new ISiteFeature[] { feature });
			}
			return true;
		} catch (CoreException e) {
		}
		return false;
	}

	private void removeCategory(ISiteFeature aFeature, String catName) {
		try {
			if (aFeature == null)
				return;
			ISiteCategory[] cats = aFeature.getCategories();
			for (int i = 0; i < cats.length; i++) {
				if (catName.equals(cats[i].getName()))
					aFeature.removeCategories(new ISiteCategory[] { cats[i] });
			}
		} catch (CoreException e) {
		}
	}

	private ISiteFeature findRealFeature(SiteFeatureAdapter adapter) {
		ISiteFeature featureCopy = adapter.feature;
		ISiteFeature[] features = fModel.getSite().getFeatures();
		for (int i = 0; i < features.length; i++) {
			if (features[i].getId().equals(featureCopy.getId())
					&& features[i].getVersion()
							.equals(featureCopy.getVersion())) {
				return features[i];
			}
		}
		return null;
	}

	public void dispose() {
		super.dispose();
		fModel.removeModelChangedListener(this);
		if(fSiteLabelProvider!=null)
			fSiteLabelProvider.dispose();
	}

	protected void fillContextMenu(IMenuManager manager) {
		Action removeAction = new Action(PDEPlugin
				.getResourceString("CategorySection.remove")) { //$NON-NLS-1$
			public void run() {
				doGlobalAction(ActionFactory.DELETE.getId());
			}
		};
		removeAction.setEnabled(isEditable());
		manager.add(removeAction);
		getPage().getPDEEditor().getContributor().contextMenuAboutToShow(
				manager);

		ISelection selection = fCategoryViewer.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			final Object o = ((IStructuredSelection) selection)
					.getFirstElement();
			if (o instanceof SiteFeatureAdapter) {
				manager.add(new Separator());
				Action buildAction = new Action(PDEPlugin
						.getResourceString("CategorySection.build")) { //$NON-NLS-1$
							public void run() {
								handleBuild(new ISiteFeature[] { ((SiteFeatureAdapter) o).feature });
							}
						};
				manager.add(buildAction);
				buildAction.setEnabled(isEditable());

			}
		}

	}

	public boolean doGlobalAction(String actionId) {
		if (actionId.equals(ActionFactory.CUT.getId())) {
			handleRemove();
			return false;
		}
		if (actionId.equals(ActionFactory.PASTE.getId())) {
			doPaste();
			return true;
		}
		if (actionId.equals(ActionFactory.DELETE.getId())) {
			return handleRemove();
		}
		return false;
	}

	public void refresh() {
		fCategoryViewer.refresh();
		updateButtons();
		super.refresh();
	}

	private void updateButtons() {
		if(!isEditable()){
			return;
		}
		IStructuredSelection sel = (IStructuredSelection) fCategoryViewer
				.getSelection();
		fCategoryTreePart.setButtonEnabled(BUTTON_BUILD_FEATURE,
				!sel.isEmpty()
						&& sel.getFirstElement() instanceof SiteFeatureAdapter
						&& getFeature(((SiteFeatureAdapter) sel
								.getFirstElement()).feature) != null);
		int featureCount = fModel.getSite().getFeatures().length;
		fCategoryTreePart.setButtonEnabled(BUTTON_BUILD_ALL, featureCount > 0);
	}

	public void modelChanged(IModelChangedEvent e) {
		markStale();
	}

	public void initialize() {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste(java.lang.Object,
	 *      java.lang.Object[])
	 */
	protected void doPaste(Object target, Object[] objects) {
		try {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof SiteFeatureAdapter) {
					copyFeature((SiteFeatureAdapter) objects[i], target);
				} else if (objects[i] instanceof ISiteCategoryDefinition) {
					fModel
							.getSite()
							.addCategoryDefinitions(
									new ISiteCategoryDefinition[] { (ISiteCategoryDefinition) objects[i] });
				}
			}
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#canPaste(java.lang.Object,
	 *      java.lang.Object[])
	 */
	protected boolean canPaste(Object target, Object[] objects) {
		if (target == null || target instanceof ISiteCategoryDefinition) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof SiteFeatureAdapter)
					return true;
				if (objects[i] instanceof ISiteCategoryDefinition) {
					String name = ((ISiteCategoryDefinition) objects[i])
							.getName();
					ISiteCategoryDefinition[] defs = fModel.getSite()
							.getCategoryDefinitions();
					for (int j = 0; j < defs.length; j++) {
						ISiteCategoryDefinition def = defs[j];
						String dname = def.getName();
						if (dname != null && dname.equals(name))
							return false;
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private void handleBuild() {
		IStructuredSelection sel = (IStructuredSelection) fCategoryViewer
				.getSelection();
		if (!sel.isEmpty()
				&& sel.getFirstElement() instanceof SiteFeatureAdapter) {
			ISiteFeature feature = ((SiteFeatureAdapter) sel.getFirstElement()).feature;
			handleBuild(new ISiteFeature[] { feature });
		}
	}

	private void handleBuild(ISiteFeature[] sFeatures) {
		if (sFeatures.length == 0)
			return;
		IFeatureModel[] models = getFeatureModels(sFeatures);
		if (models.length == 0)
			return;
		BuildSiteJob job = new BuildSiteJob(models, fModel
				.getUnderlyingResource().getParent());
		job.setUser(true);
		job.schedule();
	}

	/**
	 * 
	 * @param siteFeature
	 * @return IFeatureModel or null
	 */
	private IFeature getFeature(ISiteFeature siteFeature) {
		IFeatureModel[] models = PDECore.getDefault()
				.getWorkspaceModelManager().getFeatureModels();
		for (int i = 0; i < models.length; i++) {
			IFeatureModel model = models[i];
			IFeature feature = model.getFeature();
			if (feature.getId().equals(siteFeature.getId())
					&& feature.getVersion().equals(siteFeature.getVersion())) {
				return feature;
			}
		}
		return null;
	}

	private IFeatureModel[] getFeatureModels(ISiteFeature[] sFeatures) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < sFeatures.length; i++) {
			IFeature feature = getFeature(sFeatures[i]);
			if (feature == null)
				continue;
			IFeatureModel model = feature.getModel();
			if (model != null && model.getUnderlyingResource() != null)
				list.add(model);
		}
		return (IFeatureModel[]) list.toArray(new IFeatureModel[list.size()]);
	}

	private void handleNewFeature() {
		final Control control = fCategoryViewer.getControl();
		BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
			public void run() {
				BuiltFeaturesWizard wizard = new BuiltFeaturesWizard(
						CategorySection.this);
				WizardDialog dialog = new WizardDialog(control.getShell(),
						wizard);
				if (dialog.open() == Window.OK) {
					markDirty();
				}
			}
		});
	}

	public static ISiteFeature createSiteFeature(ISiteModel model,
			IFeatureModel featureModel) throws CoreException {
		IFeature feature = featureModel.getFeature();
		ISiteFeature sfeature = model.getFactory().createFeature();
		sfeature.setId(feature.getId());
		sfeature.setVersion(feature.getVersion());
		// sfeature.setURL(model.getBuildModel().getSiteBuild().getFeatureLocation()
		// + "/" + feature.getId() + "_" + feature.getVersion() + ".jar");
		// //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sfeature
				.setURL("features/" + feature.getId() + "_" + feature.getVersion() + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sfeature.setOS(feature.getOS());
		sfeature.setWS(feature.getWS());
		sfeature.setArch(feature.getArch());
		sfeature.setNL(feature.getNL());
		sfeature.setIsPatch(isFeaturePatch(feature));
		return sfeature;
	}

	private static boolean isFeaturePatch(IFeature feature) {
		IFeatureImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			if (imports[i].isPatch())
				return true;
		}
		return false;
	}

	public ISiteModel getModel() {
		return fModel;
	}

	/**
	 * 
	 * @param candidates
	 *            Array of IFeatureModel
	 * @param monitor
	 * @throws CoreException
	 */
	public void doAdd(Object[] candidates, IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask(
				PDEPlugin.getResourceString("CategorySection.adding"), //$NON-NLS-1$
				candidates.length + 1);
		// Category to add features to
		String categoryName = null;
		ISelection selection = fCategoryViewer.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			if (element instanceof ISiteCategoryDefinition) {
				categoryName = ((ISiteCategoryDefinition) element).getName();
			} else if (element instanceof SiteFeatureAdapter) {
				categoryName = ((SiteFeatureAdapter) element).category;
			}
		}
		//
		ISiteFeature[] added = new ISiteFeature[candidates.length];
		for (int i = 0; i < candidates.length; i++) {
			IFeatureModel candidate = (IFeatureModel) candidates[i];
			String name = candidate.getFeature().getLabel();
			monitor.subTask(candidate.getResourceString(name));
			ISiteFeature child = createSiteFeature(fModel, candidate);
			if (categoryName != null) {
				addCategory(child, categoryName);
			}
			added[i] = child;
			monitor.worked(1);
		}

		// Update model
		monitor.subTask(""); //$NON-NLS-1$
		monitor.setTaskName(PDEPlugin
				.getResourceString("CategorySection.updating")); //$NON-NLS-1$
		fModel.getSite().addFeatures(added);
		monitor.worked(1);
	}

	void fireSelection() {
		fCategoryViewer.setSelection(fCategoryViewer.getSelection());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#setFormInput(java.lang.Object)
	 */
	public boolean setFormInput(Object input) {
		if (input instanceof ISiteCategoryDefinition){
			fCategoryViewer.setSelection(new StructuredSelection(input), true);
			return true;
		}
		if (input instanceof SiteFeatureAdapter ) {
			// first, expand the category, otherwise tree will not find the feature
			String category = ((SiteFeatureAdapter)input).category;
			if(category!=null){
				expandCategory(category);
			}
			fCategoryViewer.setSelection(new StructuredSelection(input), true);
			return true;
		}
		return super.setFormInput(input);
	}
	private void expandCategory(String category){
		if(category!=null){
			ISiteCategoryDefinition[] catDefs = fModel.getSite().getCategoryDefinitions();
			for (int i = 0; i < catDefs.length; i++) {
				if (category.equals(catDefs[i].getName())){
					fCategoryViewer.expandToLevel(catDefs[i], 1);
					break;
				}
			}
		}
		
	}
}