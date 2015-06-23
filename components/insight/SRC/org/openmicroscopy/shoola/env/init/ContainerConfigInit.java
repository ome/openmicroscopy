/*
 * org.openmicroscopy.shoola.env.init.ContainerConfigInit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.init;

//Java imports
import ij.IJ;
import java.io.File;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.ConfigException;
import org.openmicroscopy.shoola.env.config.PluginInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;

/** 
 * Fills up the {@link Container}'s registry with the entries from its
 * configuration file.
 *
 * @see	InitializationTask
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public final class ContainerConfigInit
	extends InitializationTask 
{

	/** Indicates the fiji title.*/
	private static final String FIJI = "fiji";
	
	/** Indicates the ImageJ2 title.*/
	private static final String IMAGE_J2 = "imagej2";

	/**
	 * Handles the plugin dependencies. Returns <code>true</code> if the
	 * dependencies are found, <code>false</code> otherwise.
	 * 
	 * @param info The information about the plugin.
	 * @return See above.
	 */
	private boolean handlePluginDependencies(PluginInfo info)
	{
		String[] values = info.getDependenciesAsArray();
		if (values == null || values.length == 0) return true;
		//Check if plugin is there
		int count = 0;
		try {
			//Plugin folder
		    File parent = new File(container.getHomeDir());
		    File pp = parent.getParentFile();
		    if (pp == null) pp = parent;
			File dir;
			if (pp.getName().equals(info.getDirectory())) dir = pp;
			else dir = new File(pp, info.getDirectory());
			File[] l = dir.listFiles();
			String value;
			for (int j = 0; j < values.length; j++) {
				value = values[j];
				if (value != null) value = value.trim();
				if (l == null) continue;
				int idx = value.indexOf(".jar");
				if (idx==-1){
				    idx = value.length();
				}
				value = value.substring(0, idx);
				for (int i = 0; i < l.length; i++) {
					if (l[i].getName().startsWith(value)) {
						count++;
					}
				}
			}

		} catch (Exception e) {
			if ((info.getId() == LookupNames.IMAGE_J ||
			        info.getId() == LookupNames.IMAGE_J_IMPORT)
			        && IJ.debugMode) {
				String msg = "An error occurred while checking if " +
						"the dependencies are installed."+e.toString();
				IJ.log(msg);
			}
		}
		switch (info.getConjunction()) {
			case PluginInfo.AND:
				return (values.length == count);
			case PluginInfo.OR:
			default:
				return count > 0;
		}
	}
	
	/** Constructor required by superclass. */
	public ContainerConfigInit() {}
	
	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName() { return "Loading Container configuration"; }

	/** 
	 * Does nothing, as this task requires no set up.
	 * @see InitializationTask#configure()
	 */
	void configure() {}

	/** 
	 * Carries out this task.
	 * @see InitializationTask#execute()
	 */
	void execute() 
		throws StartupException
	{
		String file = container.getConfigFileRelative();
		Registry reg = container.getRegistry();
		try {
			RegistryFactory.fillFromFile(file, reg);
            String name = (String) reg.lookup(LookupNames.OMERO_HOME);
    		String omeroDir = System.getProperty("user.home")
    							+File.separator+name;
            reg.bind(LookupNames.USER_HOME_OMERO, omeroDir);
            String tmp = (String) reg.lookup(LookupNames.OMERO_FILES_HOME);
            File home = new File(omeroDir);
    		if (!home.exists()) home.mkdir();
    		File files;
            if (home.isDirectory()) files = new File(home, tmp);
            else files = new File(container.getHomeDir(), tmp);
            if (!files.exists()) {
            	 files.mkdir();
                 files.deleteOnExit();
            }
            
            List<PluginInfo> infos = 
            	(List<PluginInfo>) reg.lookup(LookupNames.PLUGINS);
            Integer v = (Integer) reg.lookup(LookupNames.PLUGIN);
    		if (infos == null || infos.size() == 0
    			|| v == null || v.intValue() < 0)
    			return;
    		boolean b = false;
    		Iterator<PluginInfo> i = infos.iterator();
    		PluginInfo info;
    		switch (v.intValue()) {
    			case LookupNames.IMAGE_J:
    			case LookupNames.IMAGE_J_IMPORT:
    				String title = IJ.getInstance().getTitle();
    				title = title.toLowerCase();
    				if (title != null) {
    					if (title.contains(FIJI) || title.equals(IMAGE_J2))
        					break;
    				}
    				
    				while (i.hasNext()) {
						info = i.next();
						b = handlePluginDependencies(info);
						if (!b) {
	    					StartupException ex = new StartupException(
	    							"OMERO.insight as ImageJ plugin");
	    					ex.setPluginInfo(info);
	    					throw ex;
	    				}
					}
    				break;
    			default:
    				break;
    		}
		} catch (ConfigException ce) {
			throw new StartupException("Unable to load Container configuration",
										ce);
		}
	}
	
	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}
	
}
