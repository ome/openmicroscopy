/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ROIToolAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.actions;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Brings up the Measurement tool.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIToolAction
    extends ViewerAction
    implements MouseListener
{

    /** The name of the action. */
    private static final String NAME = "ROI Tool...";

    /** The description of the action. */
    private static final String DESCRIPTION = "Bring up the ROI tool.";

    /** The location of the mouse pressed. */
    private Point point;

    /** Flag indicating the component has been pressed.*/
    private boolean pressed;

    /** Flag to disable the action by all means */
    private boolean forceDisable = false;
    
    /**
     * Sets the enabled flag depending on the selected tab.
     * @see ViewerAction#onTabSelection()
     */
    protected void onTabSelection()
    {
        if (ImViewerAgent.isRunAsPlugin()) {
            setEnabled(false);
        } else {
            setEnabled(model.getSelectedIndex() != ImViewer.PROJECTION_INDEX && !forceDisable);
        }
    }

    /**
     * Disposes and closes the movie player when the {@link ImViewer} is
     * discarded.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
        if (model.getState() == ImViewer.READY)
            onTabSelection();
        else setEnabled(false);
    }

    /**
     * Forces the action to stay disabled
     * 
     * @param b
     *            Pass <code>true</code> to make sure the action will be
     *            disabled
     */
    public void forceDisable(boolean b) {
        this.forceDisable = b;
    }
    
    /**
     * Creates a new instance.
     *
     * @param model The model. Mustn't be <code>null</code>.
     */
    public ROIToolAction(ImViewer model)
    {
        super(model, NAME);
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, 
                icons.getIcon(IconManager.MEASUREMENT_TOOL));
        if (ImViewerAgent.isRunAsPlugin()) {
            setEnabled(false);
        }
    }

    /**
     * Brings up on screen the {@link ImgSaver} window.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (isEnabled() && !pressed) model.showMeasurementTool(point);
    }

    /**
     * Sets the location of the point where the <code>mousePressed</code>
     * event occurred.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {
        pressed = true;
        point = me.getPoint();
    }

    /**
     * Brings up the menu.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
        pressed = false;
        Object source = me.getSource();
        if (point == null) point = me.getPoint();
        if (source instanceof Component && isEnabled()) {
            SwingUtilities.convertPointToScreen(point, (Component) source);
            model.showMeasurementTool(point);
        }
    }

    /**
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {}
}
