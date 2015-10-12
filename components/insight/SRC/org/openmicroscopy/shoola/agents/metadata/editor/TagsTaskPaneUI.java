package org.openmicroscopy.shoola.agents.metadata.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;

import omero.gateway.model.TagAnnotationData;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link AnnotationTaskPaneUI} for displaying {@link TagAnnotationData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TagsTaskPaneUI extends AnnotationTaskPaneUI {

    /** Hold the {@link DocComponent}s representing the tag data */
    private List<DocComponent> tagsDocList;

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
    public TagsTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        setBackground(UIUtilities.BACKGROUND_COLOR);

        tagsDocList = new ArrayList<DocComponent>();
    }

    @Override
    void refreshUI() {
        removeAll();
        tagsDocList.clear();

        Collection<TagAnnotationData> tags = model.getTags();
        Iterator<TagAnnotationData> it;

        switch (filter) {
        case ADDED_BY_ME:
            it = tags.iterator();
            while (it.hasNext()) {
                if (!model.isLinkOwner(it.next()))
                    it.remove();
            }
            break;
        case ADDED_BY_OTHERS:
            it = tags.iterator();
            while (it.hasNext()) {
                if (!model.isAnnotatedByOther(it.next()))
                    it.remove();
            }
            break;
        case SHOW_ALL:
            break;
        }

        if (tags.isEmpty()) {
            DocComponent doc = new DocComponent(null, model);
            tagsDocList.add(doc);
            add(doc);
        } else {
            it = tags.iterator();
            while (it.hasNext()) {
                TagAnnotationData data = it.next();
                DocComponent doc = new DocComponent(data, model);
                doc.addPropertyChangeListener(controller);
                tagsDocList.add(doc);
                add(doc);
            }
        }

        revalidate();
    }

}
