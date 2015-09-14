/*
 * org.openmicroscopy.shoola.utils.ui.TinyLoadingWin
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.ui;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

/** 
 * Tiny window displayed during data loading.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TinyLoadingWin
    extends TinyDialog
{

    /** The dimension of the loading window. */
    private static final Dimension WIN_DIMENSION = new Dimension(200, 30);

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        setCanvas(progressBar);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this frame.
     */
    public TinyLoadingWin(JFrame owner)
    {
    	this(owner, "Loading...");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this frame.
     * @param title	The title of the frame.
     */
    public TinyLoadingWin(JFrame owner, String title)
    {
        super(owner, title, BOTH);
        setModal(true);
        //setAlwaysOnTop(true);
        buildGUI();
    }

    /** Brings the window up on screen and centers it. */
    public void setOnScreen()
    {
        setSize(WIN_DIMENSION);
        setClosed(false);
        UIUtilities.centerAndShow(this);
    }

}
