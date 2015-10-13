package org.openmicroscopy.shoola.agents.metadata.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.FileAnnotationData;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link AnnotationTaskPaneUI} for displaying {@link FileAnnotationData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class OtherTaskPaneUI extends AnnotationTaskPaneUI {

    /** Hold the {@link DocComponent}s representing the annotations */
    private List<DocComponent> otherList;

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
    public OtherTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        otherList = new ArrayList<DocComponent>();

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setBackground(UIUtilities.BACKGROUND_COLOR);
    }

    @Override
    void refreshUI() {
        removeAll();
        otherList.clear();

        Collection<AnnotationData> files = model.getAllOtherAnnotations();
        Iterator<AnnotationData> it;

        switch (filter) {
        case ADDED_BY_ME:
            it = files.iterator();
            while (it.hasNext()) {
                if (!model.isLinkOwner(it.next()))
                    it.remove();
            }
            break;
        case ADDED_BY_OTHERS:
            it = files.iterator();
            while (it.hasNext()) {
                if (!model.isAnnotatedByOther(it.next()))
                    it.remove();
            }
            break;
        case SHOW_ALL:
            break;
        }

        if (files.isEmpty()) {
            DocComponent doc = new DocComponent(null, model);
            otherList.add(doc);
            add(doc);
        } else {
            it = files.iterator();
            while (it.hasNext()) {
                AnnotationData data = it.next();
                DocComponent doc = new DocComponent(data, model);
                doc.addPropertyChangeListener(controller);
                otherList.add(doc);
                add(doc);
            }
        }

        revalidate();
    }
}
