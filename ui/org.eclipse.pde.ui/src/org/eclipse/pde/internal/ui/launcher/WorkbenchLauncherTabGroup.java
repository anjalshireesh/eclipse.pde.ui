/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.ui.launcher.ConfigurationTab;
import org.eclipse.pde.ui.launcher.MainTab;
import org.eclipse.pde.ui.launcher.PluginsTab;
import org.eclipse.pde.ui.launcher.TracingTab;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class WorkbenchLauncherTabGroup extends AbstractLaunchConfigurationTabGroup {

		/**
		 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog,
		 *      String)
		 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = null;
		if (PDECore.getDefault().getModelManager().isOSGiRuntime()) {
			tabs = new ILaunchConfigurationTab[]{new MainTab(),
					new JavaArgumentsTab(),
					new PluginsTab(), new ConfigurationTab(),
					new TracingTab(), new EnvironmentTab(),
					new SourceLookupTab(), new CommonTab()};
		} else {
			tabs = new ILaunchConfigurationTab[]{new MainTab(),
					new JavaArgumentsTab(),
					new PluginsTab(), new TracingTab(),
					new EnvironmentTab(), new SourceLookupTab(), 
					new CommonTab()};
		}
		setTabs(tabs);
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		final ILaunchConfiguration config = configuration;
		final ILaunchConfigurationTab[] tabs = getTabs();
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				try {
					if (config instanceof ILaunchConfigurationWorkingCopy) {
						checkBackwardCompatibility(
							(ILaunchConfigurationWorkingCopy) config);
					}
				} catch (CoreException e) {
				}
				for (int i = 0; i < tabs.length; i++) {
					tabs[i].initializeFrom(config);
				}
			}
		});
	}
	
	private void checkBackwardCompatibility(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		String id = wc.getAttribute(
						IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
						(String) null);
		if (id == null) {
			wc.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
				"org.eclipse.pde.ui.workbenchClasspathProvider"); //$NON-NLS-1$
		}
		
		String args = wc.getAttribute("vmargs", (String)null);
		if (args != null) {
			wc.setAttribute("vmargs", (String)null);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, args);
		}
		
		args = wc.getAttribute("progargs", (String)null);
		if (args != null) {
			wc.setAttribute("progargs", (String)null);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		}		
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(
			IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
			"org.eclipse.pde.ui.workbenchClasspathProvider"); //$NON-NLS-1$
	}

}
