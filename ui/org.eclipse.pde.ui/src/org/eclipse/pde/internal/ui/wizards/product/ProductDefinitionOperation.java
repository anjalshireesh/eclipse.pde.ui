/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.product;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatform;
import org.eclipse.pde.internal.core.iproduct.IAboutInfo;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.ISplashInfo;
import org.eclipse.pde.internal.core.iproduct.IWindowImages;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModelBase;
import org.eclipse.pde.internal.core.product.SplashInfo;
import org.eclipse.pde.internal.core.text.plugin.PluginElementNode;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;

public class ProductDefinitionOperation extends BaseManifestOperation {

	private String fProductId;
	private String fApplication;
	private IProduct fProduct;

	public ProductDefinitionOperation(IProduct product, String pluginId, String productId, String application, Shell shell) {
		super(shell, pluginId);
		fProductId = productId;
		fApplication = application;
		fProduct = product;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			IFile file = getFile();
			if (!file.exists()) {
				createNewFile(file);
			} else {
				modifyExistingFile(file, monitor);
			}
			updateSingleton(monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	private void createNewFile(IFile file) throws CoreException {
		WorkspacePluginModelBase model = (WorkspacePluginModelBase)getModel(file);
		IPluginBase base = model.getPluginBase();
		base.setSchemaVersion(TargetPlatform.getTargetVersion() < 3.2 ? "3.0" : "3.2"); //$NON-NLS-1$ //$NON-NLS-2$
		base.add(createExtension(model));
		model.save();
	}
	
	private IPluginExtension createExtension(IPluginModelBase model) throws CoreException{
		IPluginExtension extension = model.getFactory().createExtension();
		extension.setPoint("org.eclipse.core.runtime.products"); //$NON-NLS-1$
		extension.setId(fProductId);
		extension.add(createExtensionContent(extension));
		return extension;
	}
	
	private IPluginElement createExtensionContent(IPluginExtension extension) throws CoreException  {
		IPluginElement element = extension.getModel().getFactory().createElement(extension);
		element.setName("product"); //$NON-NLS-1$
		element.setAttribute("name", fProduct.getName()); //$NON-NLS-1$
		element.setAttribute("application", fApplication); //$NON-NLS-1$

		IPluginElement child = createElement(element, IProductConstants.WINDOW_IMAGES, getWindowImagesString());
		if (child != null)
			element.add(child);
		
		child = createElement(element, IProductConstants.ABOUT_TEXT, getAboutText());
		if (child != null)
			element.add(child);
			
		child = createElement(element, IProductConstants.ABOUT_IMAGE, getAboutImage());
		if (child != null)
			element.add(child);		
		
		child = createElement(element, IProductConstants.STARTUP_FOREGROUND_COLOR, getForegroundColor());
		if (child != null)
			element.add(child);	
		
		child = createElement(element, IProductConstants.STARTUP_PROGRESS_RECT, getProgressRect());
		if (child != null)
			element.add(child);	
		
		child = createElement(element, IProductConstants.STARTUP_MESSAGE_RECT, getMessageRect());
		if (child != null)
			element.add(child);	
		
		return element;
	}
	
	private IPluginElement createElement(IPluginElement parent, String name, String value) throws CoreException {
		IPluginElement element = null;
		if (value != null && value.length() > 0) {
			element = parent.getModel().getFactory().createElement(parent);
			element.setName("property"); //$NON-NLS-1$
			element.setAttribute("name", name); //$NON-NLS-1$ 
			element.setAttribute("value", value); //$NON-NLS-1$ 
		}
		return element;
	}
	
	private String getAboutText() {
		IAboutInfo info = fProduct.getAboutInfo();
		if (info != null) {
			String text = info.getText();
			return text == null || text.length() == 0 ? null : text;
		}
		return null;
	}
	
	private String getAboutImage() {
		IAboutInfo info = fProduct.getAboutInfo();
		return info != null ? getURL(info.getImagePath()) : null;
	}
	
	private String getURL(String location) {
		if (location == null || location.trim().length() == 0)
			return null;
		IPath path = new Path(location);
		if (!path.isAbsolute())
			return location;
		String projectName = path.segment(0);
		IProject project = PDEPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(project);
			if (model != null) {
				String id = model.getPluginBase().getId();
				if (fPluginId.equals(id))
					return path.removeFirstSegments(1).toString();
				return "platform:/plugin/" + id + "/" + path.removeFirstSegments(1); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return location;
	}
	
	private String getWindowImagesString() {
		IWindowImages images = fProduct.getWindowImages();
		StringBuffer buffer = new StringBuffer();
		if (images != null) {
			for (int i = 0; i < IWindowImages.TOTAL_IMAGES; i++) {
				String image = getURL(images.getImagePath(i));
				if (image != null) {
					if (buffer.length() > 0)
						buffer.append(","); //$NON-NLS-1$
					buffer.append(image);
				}
				
			}
		}
		return buffer.length() == 0 ? null : buffer.toString(); //$NON-NLS-1$
	}
	
	private String getForegroundColor() {
		ISplashInfo info = fProduct.getSplashInfo();
		return info != null ? info.getForegroundColor() : null;
	}
	
	private String getProgressRect() {
		ISplashInfo info = fProduct.getSplashInfo();
		return info != null ? SplashInfo.getGeometryString(info.getProgressGeometry()) : null;
	}
	
	private String getMessageRect() {
		ISplashInfo info = fProduct.getSplashInfo();
		return info != null ? SplashInfo.getGeometryString(info.getMessageGeometry()) : null;
	}
	
	private void modifyExistingFile(IFile file, IProgressMonitor monitor) throws CoreException {
		IStatus status = PDEPlugin.getWorkspace().validateEdit(new IFile[] {file}, getShell());
		if (status.getSeverity() != IStatus.OK)
			throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.pde.ui", IStatus.ERROR, NLS.bind(PDEUIMessages.ProductDefinitionOperation_readOnly, fPluginId), null)); //$NON-NLS-1$ 
		
		ModelModification mod = new ModelModification(file) {
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				if (!(model instanceof IPluginModelBase))
					return;
				IPluginExtension extension = findProductExtension((IPluginModelBase)model);
				if (extension == null)
					insertNewExtension((IPluginModelBase)model);
				else
					modifyExistingExtension(extension);
			}
		};
		PDEModelUtility.modifyModel(mod, monitor);
	}
	
	private IPluginExtension findProductExtension(IPluginModelBase model) {
		IPluginExtension[] extensions = model.getPluginBase().getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			String point = extensions[i].getPoint();
			String id = extensions[i].getId();
			if (fProductId.equals(id) && "org.eclipse.core.runtime.products".equals(point)) { //$NON-NLS-1$
				return extensions[i];
			}
		}
		return null;
	}
	
	private void insertNewExtension(IPluginModelBase model) throws CoreException {
		IPluginExtension extension = createExtension(model);
		model.getPluginBase().add(extension);
	}
	
	private void modifyExistingExtension(IPluginExtension extension) throws CoreException {
		if (extension.getChildCount() == 0) {
			insertNewProductElement(extension);
			return;
		}
		
		PluginElementNode element = (PluginElementNode)extension.getChildren()[0];
		
		if (!"product".equals(element.getName())) { //$NON-NLS-1$
			insertNewProductElement(extension);
			return;
		}
		
		element.setAttribute("application", fApplication); //$NON-NLS-1$
		element.setAttribute("name", fProduct.getName()); //$NON-NLS-1$
		
		synchronizeChild(element, IProductConstants.ABOUT_IMAGE, getAboutImage());
		synchronizeChild(element, IProductConstants.ABOUT_TEXT, getAboutText());
		synchronizeChild(element, IProductConstants.WINDOW_IMAGES, getWindowImagesString());
		synchronizeChild(element, IProductConstants.STARTUP_FOREGROUND_COLOR, getForegroundColor());
		synchronizeChild(element, IProductConstants.STARTUP_MESSAGE_RECT, getMessageRect());
		synchronizeChild(element, IProductConstants.STARTUP_PROGRESS_RECT, getProgressRect());
	}
	
	private void synchronizeChild(IPluginElement element, String propertyName, String value) throws CoreException {
		IPluginElement child = null;
		IPluginObject[] children = element.getChildren();
		for (int i = 0; i < children.length; i++) {
			IPluginElement candidate = (IPluginElement)children[i];
			if (candidate.getName().equals("property")) { //$NON-NLS-1$
				IPluginAttribute attr = candidate.getAttribute("name"); //$NON-NLS-1$
				if (attr != null && attr.getValue().equals(propertyName)) {
					child = candidate;
					break;
				}
			}
		}
		if (child != null && value == null)
			element.remove(child);
		
		if (value == null)
			return;
		
		if (child == null) {
			child = element.getModel().getFactory().createElement(element);
			child.setName("property"); //$NON-NLS-1$
			element.add(child);
		}
		child.setAttribute("value", value); //$NON-NLS-1$
		child.setAttribute("name", propertyName); //$NON-NLS-1$
	}
	
	private void insertNewProductElement(IPluginExtension extension) throws CoreException {
		IPluginElement element = createExtensionContent(extension);
		extension.add(element);
	}
	
}
