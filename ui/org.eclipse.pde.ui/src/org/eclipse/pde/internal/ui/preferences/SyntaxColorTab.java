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
package org.eclipse.pde.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.text.ChangeAwareSourceViewerConfiguration;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class SyntaxColorTab {

	protected IColorManager fColorManager;
	private TableViewer fElementViewer;
	private SourceViewer fPreviewViewer;
	private ChangeAwareSourceViewerConfiguration fSourceViewerConfiguration;

	class ColorElement {
		private String fDisplayName;
		private String fColorKey;
		private RGB fColorValue;
		private Color fColor;
		
		public ColorElement(String displayName, String colorKey, RGB colorValue) {
			fDisplayName = displayName;
			fColorKey = colorKey;
			fColorValue = colorValue;
		}
		public String getColorKey() {
			return fColorKey;
		}
		public String getDisplayName() {
			return fDisplayName;
		}
		public RGB getColorValue() {
			return fColorValue;
		}		
		public Color getItemColor() {
			if (fColor != null && !fColor.getRGB().equals(fColorValue))
				disposeColor();
			if (fColor == null) 
				fColor = new Color(PDEPlugin.getActiveWorkbenchShell().getDisplay(), getColorValue());
			return fColor;
		}
		public void setColorValue(RGB rgb) {
			if (fColorValue.equals(rgb))
				return;
			RGB oldrgb = fColorValue;
			fColorValue = rgb;
			fSourceViewerConfiguration.adaptToPreferenceChange(new PropertyChangeEvent(this, fColorKey, oldrgb, rgb));
			fPreviewViewer.invalidateTextPresentation();
		}
		public void disposeColor() {
			if (fColor != null) {
				fColor.dispose();
				fColor = null;
			}
		}
		public String toString() { 
			return getDisplayName();
		}
	}
	
	class ColorListLabelProvider extends LabelProvider implements IColorProvider {
		public Color getForeground(Object element) {
			return ((ColorElement)element).getItemColor();
		}
		public Color getBackground(Object element) {
			return null;
		}
	}
	
	public SyntaxColorTab(IColorManager manager) {
		fColorManager = manager;
	}

	protected ArrayList loadColorData(String[][] colors) {
		IPreferenceStore store = PDEPlugin.getDefault().getPreferenceStore();
		ArrayList list = new ArrayList(colors.length);
		for (int i = 0; i < colors.length; i++) {
			String displayName = colors[i][0];
			String key = colors[i][1];
			RGB setting = PreferenceConverter.getColor(store, key);
			list.add(new ColorElement(displayName, key, setting));	
		}
		return list;
	}
	
	public Control createContents(Composite parent) {	
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		createElementTable(container);
		createPreviewer(container);
		return container;
	}
	
	private void createElementTable(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
		Label label = new Label(container, SWT.LEFT);
		label.setText("Elements:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(container, SWT.LEFT);
		label.setText("Properties:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
		fElementViewer = new TableViewer(container, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fElementViewer.setLabelProvider(new ColorListLabelProvider());
		fElementViewer.setContentProvider(new ArrayContentProvider());
		fElementViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		fElementViewer.getControl().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		Composite colorComposite = new Composite(container, SWT.NONE);
		colorComposite.setLayout(new GridLayout(2, false));
		colorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label = new Label(colorComposite, SWT.LEFT);
		label.setText("&Color:");
		
		final ColorSelector colorSelector = new ColorSelector(colorComposite);
		Button colorButton = colorSelector.getButton();
		colorButton.setLayoutData(new GridData(GridData.BEGINNING));

		colorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorElement item = getColorElement(fElementViewer);
				item.setColorValue(colorSelector.getColorValue());
				fElementViewer.update(item, null);
			}
		});
		
		fElementViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ColorElement item = getColorElement(fElementViewer);
				colorSelector.setColorValue(item.getColorValue());
			}
		});
		fElementViewer.setInput(getViewerInput());
		fElementViewer.setSorter(new ViewerSorter());
		fElementViewer.setSelection(new StructuredSelection(fElementViewer.getElementAt(0)));
	}	
	
	private void createPreviewer(Composite parent) {
		Composite previewComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		previewComp.setLayout(layout);
		previewComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(previewComp, SWT.NONE);
		label.setText("Preview:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fPreviewViewer = new SourceViewer(previewComp, null, SWT.BORDER|SWT.V_SCROLL);	
		fSourceViewerConfiguration = getSourceViewerConfiguration();
		
		if (fSourceViewerConfiguration != null)
			fPreviewViewer.configure(fSourceViewerConfiguration);
	
		fPreviewViewer.setEditable(false);	
		fPreviewViewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));	
		fPreviewViewer.setDocument(getDocument());
		
		Control control = fPreviewViewer.getControl();
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	protected abstract ChangeAwareSourceViewerConfiguration getSourceViewerConfiguration();
	
	public void performOk() {
		IPreferenceStore store = PDEPlugin.getDefault().getPreferenceStore();
		int count = fElementViewer.getTable().getItemCount();
		for (int i = 0; i < count; i++) {
			ColorElement item = (ColorElement)fElementViewer.getElementAt(i);
			PreferenceConverter.setValue(store, item.getColorKey(), item.getColorValue());
		}
	}
	
	public void performDefaults() {
		IPreferenceStore store = PDEPlugin.getDefault().getPreferenceStore();
		int count = fElementViewer.getTable().getItemCount();
		for (int i = 0; i < count; i++) {
			ColorElement item = (ColorElement)fElementViewer.getElementAt(i);
			RGB rgb = PreferenceConverter.getDefaultColor(store, item.getColorKey());		
			item.setColorValue(rgb);
			fElementViewer.update(item, null);
		}		
	}
	
	public void dispose() {
		int count = fElementViewer.getTable().getItemCount();
		for (int i = 0; i < count; i++) {
			ColorElement item = (ColorElement)fElementViewer.getElementAt(i);
			item.disposeColor();
		}				
	}
	
	protected abstract IDocument getDocument();
	
	protected abstract ArrayList getViewerInput();
	
	private ColorElement getColorElement(TableViewer viewer) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		return (ColorElement) selection.getFirstElement();
	}

}
