/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.TreePopupMenuMng
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * 
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreePopupMenuMng
    implements ActionListener
{
    
    /** Indicates that the <code>Properties</code> menu item is selected. */
    private static final int    PROPERTIES = 0;
    
    /** Indicates that the <code>Annotate</code> menu item is selected. */
    private static final int    ANNOTATE = 1;
    
    /** Indicates that the <code>Classify</code> menu item is selected. */
    private static final int    CLASSIFY = 2;
    
    /** Indicates that the <code>Declassify</code> menu item is selected. */
    private static final int    DECLASSIFY = 3;
    
    /** Indicates that the <code>View</code> menu item is selected. */
    private static final int    VIEW = 4;
    
    /** The view this class controls. */
    private ClipBoardUI clipBoard;
    
    /**
     * Adds an {@link ActionListener} to the specified component.
     * 
     * @param item The component.
     * @param id The action command ID.
     */
    private void attachItemListener(JMenuItem item, int id)
    {
        item.addActionListener(this);
        item.setActionCommand(""+id);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view The View this class is for. Mustn't be <code>null</code>.
     * @param clipBoard The {@link ClipBoardUI}. Mustn't be <code>null</code>.
     */
    TreePopupMenuMng(TreePopupMenu view, ClipBoardUI clipBoard)
    {
        if (view == null) throw new IllegalArgumentException("No view.");
        if (clipBoard == null)
            throw new IllegalArgumentException("No clipBoard.");
        this.clipBoard = clipBoard;
        attachItemListener(view.properties, PROPERTIES);
        attachItemListener(view.annotate, ANNOTATE);
        attachItemListener(view.classify, CLASSIFY);
        attachItemListener(view.declassify, DECLASSIFY);
        attachItemListener(view.view, VIEW);
    }

    /**
     * Handles actions.
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        DataObject object;
        try {
            index = Integer.parseInt(e.getActionCommand());
            object = clipBoard.getDataObject();
            switch (index) { 
                case PROPERTIES:
                    if (object != null) new PropertiesCmd(object).execute();
                    break;
                case ANNOTATE:
                    if (object != null) new AnnotateCmd(object).execute();
                    break;
                case CLASSIFY:
                    if (object instanceof ImageSummary) {
                        ClassifyCmd cmd = new ClassifyCmd((ImageSummary) object, 
                                Classifier.CLASSIFICATION_MODE, null);
                        cmd.execute();
                    }
                    break;
                case DECLASSIFY:
                    if (object instanceof ImageSummary) {
                        ClassifyCmd cmd = new ClassifyCmd((ImageSummary) object, 
                                Classifier.DECLASSIFICATION_MODE, null);
                        cmd.execute();
                    }
                    break;
                case VIEW:
                    if (object != null) new ViewCmd(object).execute();
                    break;
            } 
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }
    
}
