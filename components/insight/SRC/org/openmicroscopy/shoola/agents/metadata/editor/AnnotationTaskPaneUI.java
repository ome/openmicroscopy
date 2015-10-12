package org.openmicroscopy.shoola.agents.metadata.editor;

import javax.swing.JPanel;

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
        ADDED_BY_OTHERS(
                "Show added by others");

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
     * Refresh the UI
     */
    abstract void refreshUI();

}
