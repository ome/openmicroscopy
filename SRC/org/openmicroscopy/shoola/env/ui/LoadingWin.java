/*
 * org.openmicroscopy.shoola.env.ui.LoadingWin
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.tdialog.TinyDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Tiny window displayed during data loading.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LoadingWin
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
        uiDelegate.setCanvas(progressBar);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this frame.
     */
    public LoadingWin(JFrame owner)
    {
        super(owner, "Loading...", true);
        setModal(true);
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
