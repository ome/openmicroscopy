/*
 * org.openmicroscopy.shoola.env.Environment
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env;


//Java imports
import java.io.File;
import java.net.URL;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Lets agents access information about the container's runtime environment.
 * Agents can retrieve an <code>Environment</code> object from their registries:
 * <p><code>
 * Environment env = (Environment) registry.lookup(LookupNames.ENV);
 * </code></p> 
 * 
 * @see LookupNames
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * @version 2.2.1
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Environment 
{

	/** Reference to the container, not to be leaked. */
	private Container	container;
	
	
	/**
	 * Creates a new instance.
	 * This constructor is only meant to be used by the container.
	 * 
	 * @param c	Reference to the container.
	 */
	Environment(Container c) 
	{
		container = c;
	}
	
	/**
	 * Returns the absolute path to the installation directory.
	 * 
	 * @return	See above.
	 */
	public String getHomeDir()
	{
		return container.getHomeDir();
	}
    
    /**
     * Returns the graphical mode of the application.  If this returns
     * true, then the application is currently running in taskbar (multi-
     * frame) mode.  If not, the application is running in internal frame
     * mode.  The TaskBar will be the central locus of UI control in the
     * first; the TopFrame in the latter.  This value is determined by
     * the /services/TASKBAR/on parameter in container.xml.
     * 
     * NB: This was the cleanest way for me to signal to the agent.  If
     * we come up with a mechanism that is Shoola-wide for assigning
     * windows, we can get rid of this; but for now, in order to make
     * both on/off work, this is the simplest way.  If we decide on/off
     * isn't necessary, then screw it.
     * 
     * @return Whether or not the application is running with a taskbar.
     */
    public boolean getTaskbarMode()
    {
        Boolean taskbarMode =
            (Boolean)container.getRegistry().lookup("/services/TASKBAR/on");
        return taskbarMode.booleanValue();
    }
	
	/**
	 * Resolves the specified pathname against the installation directory.
	 * 
	 * @param relPathName	The pathname to resolve.
	 * @return	The absolute pathname obtained by resolving
	 * 			<code>relPathName</code> against the installation directory.
	 */
	public String resolvePathName(String relPathName)
	{
		File f = new File(getHomeDir(), relPathName);
		return f.getAbsolutePath();
	}
	
	/**
	 * Returns the current <code>OMEDS</code> address used by the container.
	 * 
	 * @return	See above.
	 */
	public URL getOMEDSAddress()
	{
		Registry r = container.getRegistry();
		OMEDSInfo i = (OMEDSInfo) r.lookup(LookupNames.OMEDS);
		return (i == null) ? null : i.getServerAddress();
	}

}
