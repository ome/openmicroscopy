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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.events.hiviewer.DownloadEvent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class DownloadAction
    extends DataBrowserAction
{

    /** The default name of the action. */
    private static final String NAME = "Download...";

    /** The description of the action. */
    private static final String DESCRIPTION = "Download the selected files.";

    /**
     * Sets the action enabled depending on the currently selected display
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
        if (node == null) {
            setEnabled(false);
            return;
        }
        Collection<DataObject> list = model.getBrowser().getSelectedDataObjects();
        boolean enabled = false;
        if (CollectionUtils.isNotEmpty(list)) {
            Iterator<DataObject> i = list.iterator();
            DataObject d;
            while (i.hasNext()) {
                d = i.next();
                if (d instanceof ImageData) {
                    ImageData data = (ImageData) d;
                    if (data.isArchived()) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        setEnabled(enabled);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public DownloadAction(DataBrowser model)
    {
        super(model);
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DOWNLOAD));
        putValue(Action.NAME, NAME);
    }

    /**
     * Downloads the selected files.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        ImageDisplay node = model.getBrowser().getLastSelectedDisplay();
        if (node == null)
            return;
        
        JFrame f = DataBrowserAgent.getRegistry().getTaskBar().getFrame();

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
                    EventBus bus = DataBrowserAgent.getRegistry().getEventBus();
                    bus.post(new DownloadEvent(new File(path), src.isOverride()));
                }
            }
        });
        chooser.centerDialog();
    }
}
