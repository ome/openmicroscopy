/*
 *  ome.formats.importer.StatusBar
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

package ome.formats.importer;

// Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

// Third-party libraries

/**
 * Presents the progress of the data retrieval.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @author <br>
 *         Brian Loranger &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:brian.loranger@lifesci.dundee.ac.uk"> 
 *         brian.loranger@lifesci.dundee.ac.uk</a> *         
 */
public class StatusBar extends JPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar progressBar;

    /** Displays the status message. */
    private JLabel       status;

    /**
     * Initializes the components.
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    private void initComponents()
    {
        progressBar = new JProgressBar();
        status = new JLabel();
        status.setFont(getFont().deriveFont(11.0f));
    }

    /** Build and lay out the UI. */
    private void buildUI()
    {
        Border compoundBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory
                        .createEmptyBorder(0, 4, 0, 15));

        setPreferredSize(new Dimension(10, 28));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(0, -2, -2, -2), compoundBorder));
        add(status);
        add(buildComponentPanelRight(progressBar));
    }

    /**
     * Creates a new instance.
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    public StatusBar()
    {
        initComponents();
        buildUI();
    }

    /**
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    public void setStatus(String s)
    {
        status.setText(s);
    }

    /**
     * Sets the value of the progress bar.
     * 
     * @param visible Pass <code>false</code> to hide the progress bar,
     *            <code>true</otherwise>
     * @param perc  The value to set at the % for the progress bar.
     * @param string A string value to set for the progress bar instead of perc
     */
    public void setProgress(boolean visible, int perc, String string)
    {
        progressBar.setVisible(visible);
        progressBar.setFont(getFont().deriveFont(11.0f));
        progressBar.setPreferredSize(new Dimension(220, 20));
        if (perc < 0) progressBar.setIndeterminate(true);
        else
        {
            progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
        if (string.length() > 0) 
        {
            progressBar.setStringPainted(true);
            progressBar.setString("   " + string + "   ");
        }
    }

    public void setProgressMaximum(int max)
    {
        progressBar.setMaximum(max);
    }
    
    public void setProgressValue(int val)
    {
        progressBar.setValue(val);
    }
     
    public JPanel buildComponentPanelRight(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(component);
        return p;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected void setStatusIcon(String path, String description)
    {
        if (path != null)
        {
            java.net.URL imgURL = Main.class.getResource(path);
            if (imgURL != null)
            {
                status.setIcon(new ImageIcon(imgURL, description));
            } else
            {
                System.err.println("Couldn't find icon: " + path);
            }
        } else
        {
            status.setIcon(null);
        }
        status.setText(description);
    }
}
