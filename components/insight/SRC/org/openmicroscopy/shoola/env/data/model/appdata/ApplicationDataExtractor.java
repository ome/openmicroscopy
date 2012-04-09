package org.openmicroscopy.shoola.env.data.model.appdata;

import java.io.File;

import org.openmicroscopy.shoola.env.data.model.ApplicationData;

public interface ApplicationDataExtractor {
	String getDefaultAppDirectory();
	ApplicationData extractAppData(File file) throws Exception;
}
