/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.actions;

//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.util.SaveResultsDialog;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.event.SaveEvent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Action to save some results from ImageJ back to OMERO.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */

public class SaveResultsAction
    extends TreeViewerAction
{

    /** The name of the action. */
    private static final String NAME = "Save ImageJ Results";

    /** The description of the action. */
    private static final String DESCRIPTION = "Save Results back to OMERO.";

    /** The plugin this action is for.*/
    private int plugin;

    /** Indicates what to save.*/
    private int saveIndex;

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param plugin Identifies the plug-in the action is for.
     */
    public SaveResultsAction(TreeViewer model, int plugin)
    {
        super(model);
        setEnabled(true);
        this.plugin = plugin;
        saveIndex = -1;
        if (plugin == LookupNames.IMAGE_J) {
            putValue(Action.NAME, NAME);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION));
            saveIndex = SaveEvent.ALL;
        }
    }

    /**
     * Sets the index indicating the data to save.
     *
     * @param index The indicate the data to save.
     */
    public void setSaveIndex(int index) { saveIndex = index; }

    /**
     * Brings up the dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (plugin == LookupNames.IMAGE_J) { 
            SaveResultsDialog d = new SaveResultsDialog(model.getUI(), saveIndex);
            UIUtilities.centerAndShow(d);
        }
    }
}
