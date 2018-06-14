/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import omero.gateway.model.TagAnnotationData;

import org.apache.commons.io.FileUtils;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI;
import org.openmicroscopy.shoola.agents.fsimporter.util.LightFileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Component displaying an import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ImporterUIElementLight extends ImporterUIElement {

    /** The message to display in the header. */
    private static final String MESSAGE = "When upload is complete, the import"
            + CommonsLangUtils.LINE_SEPARATOR
            + "window and OMERO session can be closed."
            + CommonsLangUtils.LINE_SEPARATOR
            + "Reading will continue on the server.";

    @Override
    FileImportComponentI buildComponent(ImportableFile importable,
            boolean browsable, boolean singleGroup, int index,
            Collection<TagAnnotationData> tags) {
        return new LightFileImportComponent(importable, getID(),
                object.getTags());
    }

    /**
     * Creates a new instance.
     * 
     * @param controller
     *            Reference to the control. Mustn't be <code>null</code>.
     * @param model
     *            Reference to the model. Mustn't be <code>null</code>.
     * @param view
     *            Reference to the model. Mustn't be <code>null</code>.
     * @param id
     *            The identifier of the component.
     * @param index
     *            The index of the component.
     * @param name
     *            The name of the component.
     * @param object
     *            the object to handle. Mustn't be <code>null</code>.
     */
    ImporterUIElementLight(ImporterControl controller, ImporterModel model,
            ImporterUI view, int id, int index, String name,
            ImportableObject object) {
        super(controller, model, view, id, index, name, object);
        buildGUI();
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {
        setLayout(new BorderLayout(0, 0));
        
        add(buildHeader(), BorderLayout.NORTH);
        
        
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(new LineBorder(Color.LIGHT_GRAY));
        add(info, BorderLayout.CENTER);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (
            FileImportComponentI.IMPORT_FILES_NUMBER_PROPERTY.equals(
                    name)) {
            //-1 to remove the entry for the folder.
            Integer v = (Integer) evt.getNewValue()-1;
            totalToImport += v;
            setNumberOfImport();
        } 
    }

}
