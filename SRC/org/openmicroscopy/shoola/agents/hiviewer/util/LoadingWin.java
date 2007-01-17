/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.LoadingWin
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

package org.openmicroscopy.shoola.agents.hiviewer.util;


//Java imports
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.tdialog.TinyDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Loading window brought on screen during data retrieval.
 *
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
public class LoadingWin
    extends TinyDialog
{

    /** Dimension of the loading window. */
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
    public LoadingWin(JFrame owner)
    {
        super(owner, "Loading...", false);
        setModal(true);
        buildGUI();
    }
    
    /** Brings up the window on screen and centers it. */
    public void setOnScreen()
    {
        setSize(WIN_DIMENSION);
        setClosed(false);
        UIUtilities.centerAndShow(this);
    }

}
