/*
 * org.openmicroscopy.shoola.Main
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

package org.openmicroscopy.shoola;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.init.StartupException;

/** 
 * Application entry point.
 * This class implements the main method, which gets only one optional
 * argument to specify the path to the installation directory.
 * <p>If this argument doesn't specify an absolute path, then it'll be 
 * translated into an absolute path.  Translation is system dependent -- in
 * many cases, the path is resolved against the user directory (typically the
 * directory in which the JVM was invoked).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Main 
{
	/**
	 * Main method.
	 * 
	 * @param args	Optional path to the installation directory.  If not 
	 * 				specified, then the user directory is assumed.
	 */
	public static void main(String[] args) 
	{
		String homeDir = "";
		if (0 < args.length)	homeDir = args[0];
		try {
			Container.startup(homeDir);
		} catch (StartupException se) {
			System.out.println();
			System.out.println("---------------------------------------------");
			System.out.println("An error occurred during initialization.");
			System.out.println("Error message: "+se.getMessage());
			System.out.println("Originated by: "+se.getOriginator());
			System.out.println("---------------------------------------------");
			System.out.println();
			System.out.println("Details as follows.");
			se.printStackTrace();
		}
	}
}
