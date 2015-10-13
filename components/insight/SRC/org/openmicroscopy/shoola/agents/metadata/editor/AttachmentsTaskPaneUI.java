package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import omero.gateway.model.FileAnnotationData;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.WrapLayout;

/**
 * A {@link AnnotationTaskPaneUI} for displaying {@link FileAnnotationData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AttachmentsTaskPaneUI extends AnnotationTaskPaneUI {

    /** Hold the {@link DocComponent}s representing the file annotations */
    private List<DocComponent> filesDocList;

    /**
     * The selection menu to attach either local documents or already upload
     * files.
     */
    private JPopupMenu docSelectionMenu;

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
    public AttachmentsTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        filesDocList = new ArrayList<DocComponent>();

        setLayout(new WrapLayout(WrapLayout.LEFT));
        setBackground(UIUtilities.BACKGROUND_COLOR);
    }

    @Override
    void refreshUI() {
        removeAll();
        filesDocList.clear();

        Collection<FileAnnotationData> files = model.getAllAttachments();
        Iterator<FileAnnotationData> it;

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
            filesDocList.add(doc);
            add(doc);
        } else {
            it = files.iterator();
            while (it.hasNext()) {
                FileAnnotationData data = it.next();
                DocComponent doc = new DocComponent(data, model);
                doc.addPropertyChangeListener(controller);
                filesDocList.add(doc);
                add(doc);
            }
        }

        revalidate();
    }

    @Override
    List<JButton> getToolbarButtons() {
        List<JButton> buttons = new ArrayList<JButton>();

        IconManager icons = IconManager.getInstance();

        final JButton addDocsButton = new JButton(
                icons.getIcon(IconManager.PLUS_12));
        addDocsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        addDocsButton.setToolTipText("Attach a document.");
        addDocsButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (addDocsButton.isEnabled()) {
                    Point p = e.getPoint();
                    createDocSelectionMenu().show(addDocsButton, p.x, p.y);
                }
            }

        });
        UIUtilities.unifiedButtonLookAndFeel(addDocsButton);
        buttons.add(addDocsButton);

        final JButton removeDocsButton = new JButton(
                icons.getIcon(IconManager.MINUS_12));
        UIUtilities.unifiedButtonLookAndFeel(removeDocsButton);
        removeDocsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        removeDocsButton.setToolTipText("Remove Attachments.");
        removeDocsButton.addMouseListener(controller);
        removeDocsButton.setActionCommand("" + EditorControl.REMOVE_DOCS);
        buttons.add(removeDocsButton);

        return buttons;
    }

    /**
     * Creates the selection menu.
     * 
     * @return See above.
     */
    private JPopupMenu createDocSelectionMenu() {
        if (docSelectionMenu != null)
            return docSelectionMenu;
        docSelectionMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Local document...");
        item.setToolTipText("Import a local document to the server "
                + "and attach it.");
        item.addActionListener(controller);
        item.setActionCommand("" + EditorControl.ADD_LOCAL_DOCS);
        docSelectionMenu.add(item);
        item = new JMenuItem("Uploaded document...");
        item.setToolTipText("Attach a document already uploaded "
                + "to the server.");
        item.addActionListener(controller);
        item.setActionCommand("" + EditorControl.ADD_UPLOADED_DOCS);
        docSelectionMenu.add(item);
        return docSelectionMenu;
    }

}
