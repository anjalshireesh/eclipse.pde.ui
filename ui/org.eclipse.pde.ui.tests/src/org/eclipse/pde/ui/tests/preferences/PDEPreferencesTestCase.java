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
package org.eclipse.pde.ui.tests.preferences;

import org.eclipse.pde.internal.ui.IPDEUIConstants;

import junit.framework.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.builders.CompilerFlags;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.ui.IPreferenceConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;

/**
 * Tests to ensure that the PDE Preferences manager, added in 3.5, is working
 * correctly and is compatable with the existing preference story.
 */
public class PDEPreferencesTestCase extends TestCase {

	private static final String PLUGIN_ID = "org.eclipse.pde.core"; 
	
	public PDEPreferencesTestCase(){
		initPreferences();
	}
	
	private static void initPreferences(){
		PDEPreferencesManager preferences = new PDEPreferencesManager(PLUGIN_ID);
		preferences.setValue("stringKey", "stringValue");
		preferences.setValue("booleanKey", true);
		preferences.setValue("intKey", 0);
		preferences.savePluginPreferences();

		preferences.setDefault("stringKey", "defaultValue");
		preferences.setDefault("booleanKey", false);
		preferences.setDefault("intKey", -1);
	}
	public static Test suite() {
		return new TestSuite(PDEPreferencesTestCase.class);
	}
	
	public void testInstanceScopePDEPreferences(){
		PDEPreferencesManager preferences = new PDEPreferencesManager(PLUGIN_ID);
		assertEquals(preferences.getString("stringKey"), "stringValue");
		assertEquals(preferences.getBoolean("booleanKey"), true);
		assertEquals(preferences.getInt("intKey"), 0);
	}
	
	public void testDefaultPDEPreferences(){
		PDEPreferencesManager preferences = new PDEPreferencesManager(PLUGIN_ID);
		assertEquals(preferences.getDefaultString("stringKey"), "defaultValue");
		assertEquals(preferences.getDefaultBoolean("booleanKey"), false);
		assertEquals(preferences.getDefaultInt("intKey"), -1);
	}
	
	public void testPreferenceChangeListener1(){
		IEclipsePreferences preferences = new InstanceScope().getNode(PLUGIN_ID);
		IPreferenceChangeListener listener = new IPreferenceChangeListener(){
		
			public void preferenceChange(PreferenceChangeEvent event) {
				assertEquals(event.getKey(), "stringKey");
				assertEquals(event.getNewValue(), "stringValue");				
			}
		};
		preferences.addPreferenceChangeListener(listener);
		preferences.put("stringKey", "stringValue");
		preferences.removePreferenceChangeListener(listener);
	}
	
	public void testPreferenceChangeListner2(){
		IEclipsePreferences preferences = new InstanceScope().getNode(PLUGIN_ID);
		preferences.put("stringKey", "oldStringValue");
		
		IPreferenceChangeListener listener = new IPreferenceChangeListener(){
		
			public void preferenceChange(PreferenceChangeEvent event) {
				assertEquals(event.getKey(), "stringKey");
				assertEquals(event.getOldValue(), "oldStringValue");
				assertEquals(event.getNewValue(), "newStringValue");			
			}
		};
		preferences.put("stringKey", "newStringValue");
		preferences.removePreferenceChangeListener(listener);				
	}
	
	public void testPDECoreDefaultPreferences(){
		// TODO The target preferences are now deprecated, we should choose new ones to test
		PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
		assertEquals(preferences.getDefaultString(ICoreConstants.TARGET_MODE), ICoreConstants.VALUE_USE_THIS);
		assertEquals(preferences.getDefaultString(ICoreConstants.CHECKED_PLUGINS), ICoreConstants.VALUE_SAVED_ALL);
		assertEquals(preferences.getDefaultString(ICoreConstants.OS), Platform.getOS());
		assertEquals(preferences.getDefaultBoolean(ICoreConstants.TARGET_PLATFORM_REALIZATION), TargetPlatform.getDefaultLocation().equals(TargetPlatform.getLocation()));
	}
	
	public void testCompilerPreferences(){
		// Testing the compiler preferences set by PDECore in org.eclipse.pde
		PDEPreferencesManager preferences = new PDEPreferencesManager(PDE.PLUGIN_ID);
		assertEquals(preferences.getDefaultInt(CompilerFlags.P_UNRESOLVED_IMPORTS), CompilerFlags.ERROR);
		assertEquals(preferences.getDefaultInt(CompilerFlags.P_DEPRECATED), CompilerFlags.WARNING);
		assertEquals(preferences.getDefaultInt(CompilerFlags.P_MISSING_VERSION_EXP_PKG), CompilerFlags.IGNORE);
	}
	
	public void testPreferencesCompatability(){
		// TODO The target preferences are now deprecated, we should choose new ones to test
		Preferences preferences = PDECore.getDefault().getPluginPreferences();		
		PDEPreferencesManager preferencesManager = PDECore.getDefault().getPreferencesManager();
		assertEquals(preferences.getString(ICoreConstants.TARGET_MODE), preferencesManager.getString(ICoreConstants.TARGET_MODE));
		assertEquals(preferences.getString(ICoreConstants.CHECKED_PLUGINS), preferencesManager.getString(ICoreConstants.CHECKED_PLUGINS));
		assertEquals(preferences.getBoolean(ICoreConstants.TARGET_PLATFORM_REALIZATION), preferencesManager.getBoolean(ICoreConstants.TARGET_PLATFORM_REALIZATION));		
	}
	
	public void testCompatibilityWithPreferenceStore(){
		IPreferenceStore store = PDEPlugin.getDefault().getPreferenceStore();
		PDEPreferencesManager preferencesManager = new PDEPreferencesManager(IPDEUIConstants.PLUGIN_ID);
		assertEquals(store.getString(IPreferenceConstants.PROP_SHOW_OBJECTS),preferencesManager.getString(IPreferenceConstants.PROP_SHOW_OBJECTS));
		assertEquals(store.getBoolean(IPreferenceConstants.EDITOR_FOLDING_ENABLED),preferencesManager.getBoolean(IPreferenceConstants.EDITOR_FOLDING_ENABLED));
		assertEquals(store.getBoolean(IPreferenceConstants.PROP_AUTO_MANAGE),preferencesManager.getBoolean(IPreferenceConstants.PROP_AUTO_MANAGE));
	}
	
}