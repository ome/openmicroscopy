/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2016 University of Dundee & Open Microscopy Environment.
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
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class Setup {

    /** The default port.*/
    public static final int DEFAULT_PORT = 4064;
    
    /** The name space used during the training.*/
    public static final String TRAINING_NS = "omero.training.demo";
    
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
	Setup(String[] args)
	{
        long imageId = 1;
        long datasetId = 1;
        long projectId = 1;
        long plateId = 1;

        Properties p = loadConfig(resolveFilePath(CONFIG_FILE, CONFIG_DIR));
        if (p != null) {
            imageId = Long.parseLong(p.getProperty("omero.imageid", "1"));
            datasetId = Long.parseLong(p.getProperty("omero.datasetid", "1"));
            projectId = Long.parseLong(p.getProperty("omero.projectid", "1"));
            plateId = Long.parseLong(p.getProperty("omero.plateid", "1"));
        }
        
        if (args == null || args.length == 0) {
            String ice_config = System.getenv().get("ICE_CONFIG");
            if (ice_config != null && !ice_config.isEmpty()) {
                p = loadConfig(new File(ice_config).getAbsolutePath());
                if (p != null)
                    args = new String[] {
                            "--omero.host=" + p.getProperty("omero.host"),
                            "--omero.pass=" + p.getProperty("omero.pass"),
                            "--omero.user=" + p.getProperty("omero.user"),
                            "--omero.port=" + p.getProperty("omero.port") };
            }

            if (args == null || args.length == 0)
                throw new RuntimeException(
                        "Login credentials missing, neither arguments nor valid ICE_CONFIG provided.");
        }
		
		new CreateImage(args, imageId, datasetId);
		new DeleteData(args);
		new HowToUseTables(args);
		new LoadMetadataAdvanced(args, imageId);
		new RawDataAccess(args, imageId);
		new ReadData(args, datasetId, plateId, imageId);
		new ReadDataAdvanced(args);
		new RenderImages(args, imageId);
		new ROIs(args, imageId);
		new WriteData(args, imageId, projectId);
		new ImportImage(args);
	}
	
	/**
	 * Entry point. Runs all the examples.
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Setup(args);
		System.exit(0);
	}

}
