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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import omero.gateway.model.AnnotationData;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Parent class for the UI components displaying annotations
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public abstract class AnnotationTaskPaneUI extends JPanel {

    /**
     * Different kinds of filtering annotations
     * 
     * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
     */
    public enum Filter {
        /** Show all annotations */
        SHOW_ALL("Show all"),

        /** Show only annotations added by the user */
        ADDED_BY_ME("Show added by me"),

        /** Show only annotations added by other users */
        ADDED_BY_OTHERS("Show added by others");

        /** Human readable name for the filter */
        String name = "";

        /**
         * Creates a new Filter enumeration instance
         * 
         * @param name
         *            Human readable name for the filter
         */
        Filter(String name) {
            this.name = name;
        }
    }

    /** Reference to the {@link EditorModel} */
    EditorModel model;

    /** Reference to the {@link EditorUI} */
    EditorUI view;

    /** Reference to the {@link EditorControl} */
    EditorControl controller;

    /** The default {@link Filter}, set to 'show all' */
    Filter filter = Filter.SHOW_ALL;

    /** The panel holding the actual content */
    private JPanel contentPane;

    /**
     * Creates a new instance
     * 
     * @param model
     *            Reference to the {@link EditorModel}
     * @param view
     *            Reference to the {@link EditorUI}
     * @param controller
     *            Reference to the {@link EditorControl}
     */
    AnnotationTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        this.model = model;
        this.view = view;
        this.controller = controller;

        this.contentPane = new JPanel();
        this.contentPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        super.setLayout(new BorderLayout());
        super.add(getToolbar(), BorderLayout.NORTH);
        super.add(contentPane, BorderLayout.CENTER);
    }

    /**
     * Apply a filter and refresh the UI
     * 
     * @param filter
     *            The {@link Filter} to apply
     */
    void filter(Filter filter) {
        this.filter = filter;
        refreshUI();
    }

    /**
     * Creates the toolbar, if needed (see {@link #getToolbarButtons()}
     */
    private JPanel getToolbar() {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        List<JButton> buttons = getToolbarButtons();
        if (CollectionUtils.isNotEmpty(buttons)) {
            for (JButton b : buttons) {
                p.add(b);
            }
        }
        return p;
    }

    /**
     * Get a reference to the content pane
     */
    JPanel getContentPane() {
        return contentPane;
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (contentPane == null)
            super.setLayout(mgr);
        else
            contentPane.setLayout(mgr);
    }

    @Override
    public Component add(Component comp) {
        return contentPane.add(comp);
    }

    @Override
    public Component add(String name, Component comp) {
        return contentPane.add(name, comp);
    }

    @Override
    public Component add(Component comp, int index) {
        return contentPane.add(comp, index);
    }

    @Override
    public void add(Component comp, Object constraints) {
        contentPane.add(comp, constraints);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        contentPane.add(comp, constraints, index);
    }

    @Override
    public void remove(int index) {
        contentPane.remove(index);
    }

    @Override
    public void removeAll() {
        contentPane.removeAll();
    }

    /**
     * Get the toolbar buttons; override this method if a toolbar is needed
     */
    List<JButton> getToolbarButtons() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Builds, respectively also refreshes the UI
     */
    abstract void refreshUI();
    
    /**
     * Wipes the display
     */
    abstract void clearDisplay();

    /**
     * Get the annotations which have to be saved
     * 
     * @return See above
     */
    abstract List<AnnotationData> getAnnotationsToSave();

    /**
     * Get the annotations which have to be saved
     * 
     * @return See above
     */
    abstract List<Object> getAnnotationsToRemove();
    
    /** 
     * Informs the UI when the related nodes have been set.
     * */
    abstract void onRelatedNodesSet();
}
