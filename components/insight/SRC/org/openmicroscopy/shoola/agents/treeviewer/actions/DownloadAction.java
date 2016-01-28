/*
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;

/** 
 * Action to download the selected file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DownloadAction 
	extends TreeViewerAction
{

	/** The default name of the action. */
    private static final String NAME = "Download...";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Download the selected files.";

    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) return;
        switch (browser.getState()) {
        case Browser.LOADING_DATA:
        case Browser.LOADING_LEAVES:
            setEnabled(false);
            break;
        default:
            onDisplayChange(browser.getLastSelectedDisplay());
            break;
        }
    }

    /**
     * Sets the enabled flag of the action depending on the selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        setEnabled(false);
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof FileAnnotationData) {
            setEnabled(true);
        } else if (ho instanceof ImageData) {
            Browser browser = model.getSelectedBrowser();
            List<DataObject> list = browser.getSelectedDataObjects();
            Iterator<DataObject> i = list.iterator();
            DataObject data;
            boolean enabled = false;
            while (i.hasNext()) {
                data = i.next();
                if (data instanceof ImageData) {
                    ImageData img = (ImageData) data;
                    if (img.isArchived()) enabled = true;
                }
            }
            setEnabled(enabled);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public DownloadAction(TreeViewer model)
    {
        super(model);
        name = NAME;
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
        description = (String) getValue(Action.SHORT_DESCRIPTION);
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.DOWNLOAD));
    }

    /**
     * Downloads the selected files.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Browser browser = model.getSelectedBrowser();
        if (browser == null) 
            return;
        
        TreeImageDisplay node = browser.getLastSelectedDisplay();
        if (node == null) 
            return;
        
        JFrame f = TreeViewerAgent.getRegistry().getTaskBar().getFrame();
        
        int type = FileChooser.FOLDER_CHOOSER;

        FileChooser chooser = new FileChooser(f, type,
                FileChooser.DOWNLOAD_TEXT, FileChooser.DOWNLOAD_DESCRIPTION);

        IconManager icons = IconManager.getInstance();
        chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
        chooser.setApproveButtonText(FileChooser.DOWNLOAD_TEXT);
        chooser.setCheckOverride(true);
        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                FileChooser src = (FileChooser) evt.getSource();
                if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
                    String path = (String) evt.getNewValue();
                    model.download(new File(path), src.isOverride());
                }
            }
        });
        chooser.centerDialog();
    }

}
