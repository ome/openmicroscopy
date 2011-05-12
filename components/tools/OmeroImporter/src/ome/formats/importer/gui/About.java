/*
 * ome.formats.importer.gui.About
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

 // This was an original package from LOCI Bio-Formats which has since been
 // depreciated and is Copyright (C) 2005 Melissa Linkert, Curtis Rueden, 
 // Chris Allan. Brian Loranger, and Eric Kjellman.

package ome.formats.importer.gui;

import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import loci.formats.FormatTools;
import ome.formats.importer.ImportConfig;

/**
 * About is a small program for displaying version information in a dialog box.
 * It is intended to be used as a main class for JAR libraries to easily
 * determine library version and build date.
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 */
public abstract class About
{

    private static String title;

    private static String msg;

    /**
     * @param parent - parent frame
     * @param config - importer config
     * @param useSplashScreen - boolean to use splash screen
     */
    public static void show(JFrame parent, ImportConfig config, boolean useSplashScreen)
    {
        
        if (useSplashScreen == true)
        {
            //SplashWindow.splash(Splasher.class.getResource(Main.splash));
        } else
        {
            if (config.getAppTitle() != null)
                title = "About " + config.getAppTitle();
            else
            	title = "About";
            
            ResourceBundle bundle = ResourceBundle.getBundle("omero");
            String omeroVersion = bundle.getString("omero.version");
            
            msg = "Version: " + omeroVersion;
            msg = msg + "\n Bio-Formats " + FormatTools.VERSION + 
            " (SVN " + FormatTools.VCS_REVISION + ", " + FormatTools.DATE + ")";
            JOptionPane.showMessageDialog(parent, msg, title,
                    JOptionPane.INFORMATION_MESSAGE); 
            parent.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    /**
     * internal test main (only for debugging)
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        show(null, new ImportConfig(), false);
        System.exit(0);
    }

}
