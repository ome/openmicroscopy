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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import omero.gateway.model.AnnotationData;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * An empty {@link AnnotationTaskPaneUI}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 */
public class DummyTaskPaneUI extends AnnotationTaskPaneUI {

    /**
     * Creates a new instance
     * 
     * @param model
     *            Reference to the {@link EditorModel}
     * @param view
     *            Reference to the {@link EditorUI}
     * @param controller
     *            Reference to the {@link EditorControl}r
     */
    DummyTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);
        JLabel l = new JLabel("Not implemented yet");
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        add(l);
    }

    @Override
    void refreshUI() {

    }

    @Override 
    void clearDisplay() {
        
    }

    @Override
    List<AnnotationData> getAnnotationsToSave() {
        return Collections.EMPTY_LIST;
    }

    @Override
    List<Object> getAnnotationsToRemove() {
        return Collections.EMPTY_LIST;
    }

    @Override
    void onRelatedNodesSet() {
        
    }
    
    
}
