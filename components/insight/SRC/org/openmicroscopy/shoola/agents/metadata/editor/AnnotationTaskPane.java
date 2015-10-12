package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Container;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.editor.AnnotationTaskPaneUI.Filter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link JXTaskPane} for displaying a certain type of annotation
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AnnotationTaskPane extends JXTaskPane {

    /** Reference to the {@link EditorUI} */
    private EditorUI view;

    /** Reference to the {@link EditorModel} */
    private EditorModel model;

    /** Reference to the {@link EditorControl} */
    private EditorControl controller;

    /** The component hosting the UI elements */
    private AnnotationTaskPaneUI ui;

    /**
     * The different kind of annotations
     * 
     * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
     */
    public enum AnnotationType {
        /** Tags */
        TAGS("Tags"),

        /** ROIs */
        ROIS("ROIs"),

        /** Map annotations */
        MAP("Key-Value Pairs"),

        /** File attachments */
        ATTACHMENTS("Attachments"),

        /** Other annotations */
        OTHER("Other"),

        /** Rating */
        RATING("Rating"),

        /** User comments */
        COMMENTS("Comments");

        /** Human readable name for this annotation type */
        String name = "";

        /**
         * Creates a new enumeration instance
         * 
         * @param name
         *            Human readable name for this annotation type
         */
        AnnotationType(String name) {
            this.name = name;
        }
    }

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
    public AnnotationTaskPane(AnnotationType type, EditorUI view,
            EditorModel model, EditorControl controller) {
        setTitle(type.name);
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
    public void setAnnotationCount(int n) {
        setTitle(type.name + " (" + n + ")");
    }

    /**
     * Refreshes the display
     */
    public void refreshUI() {
        ui.refreshUI();
    }

    /**
     * Apply a {@link Filter}
     * 
     * @param filter
     */
    public void filter(Filter filter) {
        ui.filter(filter);
    }

    /**
     * Build the UI component
     */
    private void buildUI() {
        switch (type) {
        case TAGS:
            ui = new TagsTaskPaneUI(model, view, controller);
            break;
        case MAP:
            ui = new MapTaskPaneUI(model, view, controller);
            break;
        case ATTACHMENTS:
            ui = new AttachmentsTaskPaneUI(model, view, controller);
            break;
        case ROIS:
        case OTHER:
        case RATING:
        case COMMENTS:
        default:
            ui = new DummyTaskPaneUI(model, view, controller);
            MetadataViewerAgent
                    .getRegistry()
                    .getLogger()
                    .warn(this,
                            "UI for displaying " + type.toString()
                                    + " annotations not implemented yet!");
        }

        add(ui);
    }

}
