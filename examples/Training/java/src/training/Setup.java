/*
 * training.Setup
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


/**
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class Setup {


	/** The name of the configuration file in the configuration directory. */
	private static final String CONFIG_FILE = "training.config";
	
	/** 
	 * Points to the configuration directory.
	 * The path is relative to the installation directory.
	 */
	public static final String CONFIG_DIR = "config";
	
	/** Absolute path to the installation directory. */
	private String homeDir;
	
    /**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occurred.
	 */
	private Properties loadConfig(String file)
	{
		Properties config = new Properties();
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			config.load(fis);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {}
		}
		return config;
	}
	
	/**
	 * Resolves <code>fileName</code> against the configuration directory.
	 * 
	 * @param fileName The name of a configuration file.
	 * @param directory The directory of reference.
	 * @return	Returns the absolute path to the specified file.
	 */
	private String resolveFilePath(String fileName, String directory)
	{
		//if (fileName == null)	throw new NullPointerException();
        StringBuffer relPath = new StringBuffer(directory);
        relPath.append(File.separatorChar);
        relPath.append(fileName);
		File f = new File(homeDir, relPath.toString());
		return f.getAbsolutePath();
	}
	
	/**
	 * Creates a new instance.
	 */
	Setup()
	{
		//homeDir = "";
		//Read the configuration file.
		Properties p = loadConfig(resolveFilePath(CONFIG_FILE, CONFIG_DIR));
		ConfigurationInfo info = null;
		try {
			info = new ConfigurationInfo();
			info.setHostName(p.getProperty("omero.host"));
			info.setPassword(p.getProperty("omero.pass"));
			info.setUserName(p.getProperty("omero.user"));
			info.setPort(Integer.parseInt(p.getProperty("omero.port")));
			info.setImageId(Long.parseLong(p.getProperty("omero.imageid")));
			info.setDatasetId(Long.parseLong(p.getProperty("omero.datasetid")));
			info.setProjectId(Long.parseLong(p.getProperty("omero.projectid")));
			info.setScreenId(Long.parseLong(p.getProperty("omero.screenid")));
			info.setPlateId(Long.parseLong(p.getProperty("omero.plateid")));
			info.setPlateAcquisitionId(Long.parseLong(
					p.getProperty("omero.plateAcquisitionid")));
		} catch (Exception e) {
			e.printStackTrace();
			//wrong configuration
			info = null;
		}
		new Connector(info);
		new CreateImage(info);
		new DeleteData(info);
		new HowToUseTables(info);
		new LoadMetadataAdvanced(info);
		new RawDataAccess(info);
		new ReadData(info);
		new ReadDataAdvanced(info);
		new RenderImages(info);
		new ROIs(info);
		new WriteData(info);
	}
	
	/**
	 * Entry point. Runs all the examples.
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Setup();
		System.exit(0);
	}

}
