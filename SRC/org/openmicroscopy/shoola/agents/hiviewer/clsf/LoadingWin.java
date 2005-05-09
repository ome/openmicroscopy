/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.LoadingWin
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;


//Java imports
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.twindow.TinyWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Loading window.
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
class LoadingWin
    extends TinyWindow
{

    /** Dimension of the loading window. */
    private static final Dimension WIN_DIMENSION = new Dimension(300, 150);
    
    LoadingWin(Frame owner)
    {
        super(owner, "Loading");
        buildGUI();
    }

    /** Brings up the window on screen and centers it. */
    void setOnScreen()
    {
        setSize(WIN_DIMENSION);
        UIUtilities.centerAndShow(this);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        p. setBorder(BorderFactory.createEtchedBorder());
        add(UIUtilities.buildComponentPanelRight(progressBar));
        uiDelegate.setCanvas(p);
    }

}
