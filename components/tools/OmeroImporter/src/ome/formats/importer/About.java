/*
 * ome.formats.testclient.About
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

package ome.formats.importer;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

/**
 * About is a small program for displaying version information in a dialog box.
 * It is intended to be used as a main class for JAR libraries to easily
 * determine library version and build date.
 */
public abstract class About
{

    private static String title;

    private static String msg;

    public static void show(Component c, boolean useSplashScreen)
    {

        if (useSplashScreen == true)
        {
            //SplashWindow.splash(Splasher.class.getResource(Main.splash));
        } else
        {

            if (title == null)
            {
                StringBuffer sb = new StringBuffer();
                try
                {
                    InputStream is = About.class.getResourceAsStream("about.txt");
                    if (is == null)
                    {
                        title = "About";
                        msg = "Error: version information not found";
                    } else
                    {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(is));
                        while (true)
                        {
                            String line = in.readLine();
                            if (line == null) break;
                            if (title == null) title = "About " + line;
                            else
                                sb.append("\n");
                            sb.append(line);
                        }
                        in.close();
                        msg = sb.toString();
                        msg = msg + "\n\n Version: " + Main.versionText;
                    }
                } catch (IOException exc)
                {
                    if (title == null) title = "About";
                    msg = "Error: could not read version information";
                }
            }
            if (Main.TITLE != null)
                title = "About " + Main.TITLE;
            JOptionPane.showMessageDialog(c, msg, title,
                    JOptionPane.INFORMATION_MESSAGE);    
        }
    }

    public static void main(String[] args)
    {
        show(null, false);
        System.exit(0);
    }

}
