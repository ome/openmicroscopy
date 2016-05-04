/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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

import java.awt.Container;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import omero.gateway.model.AnnotationData;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.editor.AnnotationTaskPaneUI.Filter;
import org.openmicroscopy.shoola.env.data.model.AnnotationType;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link JXTaskPane} for displaying a certain type of annotation
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AnnotationTaskPane extends JXTaskPane {

    /** The {@link JXTaskPane} property fired on collapse/expansion of the pane */
    private static final String COLLAPSED_PROPERTY = "collapsed";
    
    /** Reference to the {@link EditorUI} */
    private EditorUI view;

    /** Reference to the {@link EditorModel} */
    private EditorModel model;

    /** Reference to the {@link EditorControl} */
    private EditorControl controller;

    /** The component hosting the UI elements */
    private AnnotationTaskPaneUI ui;

    /** The {@link AnnotationType} this taskpane should display */
    private AnnotationType type;

    /**
     * Creates a new instance
     * 
     * @param type
     *            The {@link AnnotationType} this taskpane should display
     * @param view
     *            Reference to the {@link EditorUI}
     * @param model
     *            Reference to the {@link EditorModel}
     * @param controller
     *            Reference to the {@link EditorControl}
     */
    AnnotationTaskPane(AnnotationType type, EditorUI view,
            EditorModel model, EditorControl controller) {
        setTitle(type.getName());
        this.type = type;
        this.view = view;
        this.model = model;
        this.controller = controller;

        setAnimated(false);
        setCollapsed(true);

        Container c = getContentPane();
        c.setBackground(UIUtilities.BACKGROUND_COLOR);
        setBackground(UIUtilities.BACKGROUND_COLOR);

        Font font = getFont();
        setFont(font.deriveFont(font.getSize2D() - 2));

        if (c instanceof JComponent)
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1,
                    1));

        buildUI();
        refreshUI();
    }

    /**
     * Sets the annotation count, displays the number of annotations in the
     * header
     * 
     * @param n
     *            The number of annotations available
     */
    void setAnnotationCount(int n) {
        setTitle(type.getName() + " (" + n + ")");
    }

    /**
     * Checks if there is data which has to be saved
     * 
     * @return <code>true</code> if there is data which has to be saved,
     *         <code>false</code> otherwise
     */
    boolean hasDataToSave() {
        return !ui.getAnnotationsToSave().isEmpty()
                || !ui.getAnnotationsToRemove().isEmpty();
    }

    /**
     * Get the annotations which have to be saved
     * 
     * @return See above
     */
    List<AnnotationData> getAnnotationsToSave() {
        return ui.getAnnotationsToSave();
    }

    /**
     * Get the annotations which have to be deleted
     * 
     * @return See above
     */
    List<Object> getAnnotationsToRemove() {
        return ui.getAnnotationsToRemove();
    }

    /**
     * Wipes the display
     */
    void clearDisplay() {
        ui.clearDisplay();
    }

    /**
     * Refreshes the display
     */
    void refreshUI() {
        ui.refreshUI();
        setAnnotationCount(ui.getUnfilteredAnnotationCount());
    }

    /**
     * Apply a {@link Filter}
     * 
     * @param filter
     */
    void filter(Filter filter) {
        ui.filter(filter);
    }

    void onRelatedNodesSet() {
        ui.onRelatedNodesSet();
    }
    
    /**
     * Get a reference to the {@link AnnotationTaskPaneUI}
     * 
     * @return See above
     */
     AnnotationTaskPaneUI getTaskPaneUI() {
        return ui;
    }

    /**
     * Build the UI component
     */
    private void buildUI() {
        switch (type) {
        case TAG:
            ui = new TagsTaskPaneUI(model, view, controller);
            break;
        case MAP:
            ui = new MapTaskPaneUI(model, view, controller);
            break;
        case ATTACHMENT:
            ui = new AttachmentsTaskPaneUI(model, view, controller);
            break;
        case COMMENT:
            ui = new CommentsTaskPaneUI(model, view, controller);
            break;
        case OTHER:
            ui = new OtherTaskPaneUI(model, view, controller);
            break;
        case RATING:
            ui = new RatingTaskPaneUI(model, view, controller);
            break;
        case ROI:
        default:
            ui = new DummyTaskPaneUI(model, view, controller);
            MetadataViewerAgent
                    .getRegistry()
                    .getLogger()
                    .warn(this,
                            "UI for displaying " + type.toString()
                                    + " annotations not implemented yet!");
        }

        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (COLLAPSED_PROPERTY.equals(evt.getPropertyName())) {
                    ui.onCollapsed((Boolean) evt.getNewValue());
                }
            }
        });
        
        add(ui);
    }

}
