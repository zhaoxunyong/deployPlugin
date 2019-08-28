package com.aeasycredit.deployplugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aeasycredit.deployplugin.DeployPluginLauncherPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DeployPluginLauncherPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.GIT_SCRIPTS_URL, "http://gitlab.aeasycredit.net/dave.zhao/deployPlugin/raw/master");
		store.setDefault(PreferenceConstants.GIT_SHOWTAG_IN_DROPDOWN, false);
		store.setDefault(PreferenceConstants.GIT_RELEASE_WITH_TAG, false);
		store.setDefault(PreferenceConstants.GIT_BASH_DEBUG, false);
//		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
//		store.setDefault(PreferenceConstants.P_STRING, "Defaults value");
	}

}
