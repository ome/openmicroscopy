package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
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

    /** Flag to indicate if the FileAnnotations should be selectable */
    private boolean selectable;
    
    /** The collection of annotations to replace. */
    private List<FileAnnotationData>        toReplace;
    
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
    AttachmentsTaskPaneUI(EditorModel model, EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        filesDocList = new ArrayList<DocComponent>();
        toReplace = new ArrayList<FileAnnotationData>();
        
        setLayout(new WrapLayout(WrapLayout.LEFT));
        setBackground(UIUtilities.BACKGROUND_COLOR);
    }

    /**
     * Attaches the passed file. Returns <code>true</code> if the file
     * does not already exist, <code>false</code> otherwise.
     * 
     * @param files The files to attach.
     * @return See above
     */
    boolean attachFiles(File[] files)
    {
        List<FileAnnotationData> list = getCurrentAttachmentsSelection();
        DocComponent doc;
        List<File> toAdd = new ArrayList<File>();
        Object data = null;
        if (filesDocList.size() > 0) {
            Iterator<DocComponent> i = filesDocList.iterator();
            FileAnnotationData fa;
            while (i.hasNext()) {
                doc = i.next();
                data = doc.getData();
                if (data instanceof FileAnnotationData) {
                    fa = (FileAnnotationData) data;
                    for (int j = 0; j < files.length; j++) {
                        if (fa.getId() >= 0 &&
                                fa.getFileName().equals(files[j].getName())) {
                            toReplace.add(fa);
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < files.length; i++) {
            toAdd.add(files[i]);
        }
        
        if (toAdd.size() > 0) {
            data = null;
            try {
                Iterator<File> j = toAdd.iterator();
                while (j.hasNext()) {
                    list.add(new FileAnnotationData(j.next()));
                }
                
            }
            catch (Exception e) {} 
            
            firePropertyChange(EditorControl.SAVE_PROPERTY, 
                    Boolean.valueOf(false), Boolean.valueOf(true));
        }
        layoutAttachments(list);
        return toAdd.size() > 0;
    }
    
    /**
     * Removes the passed file from the display.
     * 
     * @param file The file to remove.
     */
    void removeAttachedFile(Object file)
    { 
        if (file == null) return;
        FileAnnotationData fData = (FileAnnotationData) file;
        List<FileAnnotationData> attachments = getCurrentAttachmentsSelection();
        Iterator<FileAnnotationData> i = attachments.iterator();
        FileAnnotationData data;
        List<FileAnnotationData> toKeep = new ArrayList<FileAnnotationData>();
        while (i.hasNext()) {
            data = i.next();
            if (data.getId() != fData.getId())
                toKeep.add(data);
        }
        if (filesDocList.size() > 0) {
            Iterator<DocComponent> j = filesDocList.iterator();
            DocComponent doc;
            Object fa;
            while (j.hasNext()) {
                doc = j.next();
                fa = doc.getData();
                if (fa instanceof FileAnnotationData) {
                    data = (FileAnnotationData) fa;
                    if (data.getId() <= 0 && !data.equals(file)) {
                        toKeep.add(data);
                    }
                }
            }
        }
        handleObjectsSelection(FileAnnotationData.class, toKeep, true);
    }
    
    /**
     * Returns the collection of attachments.
     * 
     * @return See above.
     */
    List<FileAnnotationData> removeAttachedFiles()
    {
        List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
        if (filesDocList.size() == 0) {
            return list;
        }
        List<FileAnnotationData> toKeep = new ArrayList<FileAnnotationData>();
        FileAnnotationData data;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = filesDocList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof FileAnnotationData) {
                    data = (FileAnnotationData) object;
                    if (data.getId() > 0)
                        list.add(data);
                }
            } else {
                toKeep.add((FileAnnotationData) object);
            }
        }
        handleObjectsSelection(FileAnnotationData.class, toKeep, false);
        return list;
    }
    
    /**
     * Handles the selection of objects via the selection wizard.
     * 
     * @param type    The type of objects to handle.
     * @param objects The objects to handle.
     * @param fire    Pass <code>true</code> to notify, <code>false</code>
     *                otherwise.
     */
    void handleObjectsSelection(Class<?> type, Collection objects, boolean fire)
    {
        layoutAttachments(objects);
        List<Long> ids = new ArrayList<Long>();
        Iterator i = objects.iterator();
        FileAnnotationData data;
        Collection attachments = model.getAllAttachments();
        if (attachments == null || attachments.size() != objects.size()) {
        } else {
            while (i.hasNext()) {
                data = (FileAnnotationData) i.next();
                if  (data != null) ids.add(data.getId());
            }
            i = attachments.iterator();
            while (i.hasNext()) {
                data = (FileAnnotationData) i.next();
                if (data != null && !ids.contains(data.getId())) {
                    break;
                }
            }
        }
    }
    /**
     * Returns the list of attachments currently selected by the user.
     * 
     * @return See above.
     */
    private List<FileAnnotationData> getCurrentAttachmentsSelection() 
    {
        List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
        if (filesDocList.size() == 0)  return list;
        DocComponent doc;
        Object object;
        FileAnnotationData data;
        Iterator<DocComponent> i = filesDocList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (object instanceof FileAnnotationData) {
                data = (FileAnnotationData) object;
                if (data.getId() > 0)
                    list.add(data);
            }
        }
        return list;
    }
    

    @Override
    void refreshUI() {
        clearDisplay();
        
        Collection list;
        if (model.isMultiSelection()) 
            list = model.getAllAttachments();
        else 
            list = model.getAttachments();
        
        layoutAttachments(list);
    }
    
    void layoutAttachments(Collection list) {
        removeAll();
        filesDocList.clear();
        DocComponent doc;
        int h = 0;
        int v;
        
        if (list != null && list.size() > 0) {
            Iterator i = list.iterator();
            DataObject data;
            switch (filter) {
            case SHOW_ALL:
                while (i.hasNext()) {
                    data = (DataObject) i.next();
                    if (!toReplace.contains(data)) {
                        doc = new DocComponent(data, model, true, selectable);
                        doc.addPropertyChangeListener(controller);
                        filesDocList.add(doc);
                        add(doc);
                        v = doc.getPreferredSize().height;
                        if (h < v)
                            h = v;
                    }
                }
                break;
            case ADDED_BY_OTHERS:
                while (i.hasNext()) {
                    data = (DataObject) i.next();
                    if (!toReplace.contains(data)) {
                        doc = new DocComponent(data, model, true, selectable);
                        doc.addPropertyChangeListener(controller);
                        filesDocList.add(doc);
                        if (model.isAnnotatedByOther(data)) {
                            add(doc);
                            v = doc.getPreferredSize().height;
                            if (h < v)
                                h = v;
                        }
                    }
                }
                break;
            case ADDED_BY_ME:
                while (i.hasNext()) {
                    data = (DataObject) i.next();
                    if (!toReplace.contains(data)) {
                        doc = new DocComponent(data, model, true, selectable);
                        doc.addPropertyChangeListener(controller);
                        filesDocList.add(doc);
                        if (model.isLinkOwner(data)) {
                            add(doc);
                            v = doc.getPreferredSize().height;
                            if (h < v)
                                h = v;
                        }
                    }
                }
            }
        }
        
        if (filesDocList.size() == 0 ) {
            doc = new DocComponent(null, model, true, false);
            filesDocList.add(doc);
            add(doc);
        }
    }

    @Override 
    void clearDisplay() {
        removeAll();
        filesDocList.clear();
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

        final JButton selectButton = new JButton(icons.getIcon(IconManager.ANALYSIS));
        selectButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        selectButton.setToolTipText("Select Files for Scripts");
        selectButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (selectButton.isEnabled()) {
                    selectable = !selectable;
                    refreshUI();
                }
            }

        });
        UIUtilities.unifiedButtonLookAndFeel(selectButton);
        buttons.add(selectButton);
        
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
    
    @Override
    List<AnnotationData> getAnnotationsToSave() {
        List<AnnotationData> l = new ArrayList<AnnotationData>();
        
        Collection<FileAnnotationData> original = model.getAllAttachments();
        Iterator<FileAnnotationData> j = original.iterator();
        List<Long> ids = new ArrayList<Long>();
        while (j.hasNext()) {
            ids.add(((AnnotationData) j.next()).getId());
        }
        Iterator<DocComponent> i = filesDocList.iterator();
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        Map<Long, AnnotationData> 
            annotations = new HashMap<Long, AnnotationData>();
        Integer count;
        while (i.hasNext()) {
            DocComponent doc = i.next();
            Object object = doc.getData();
            if (object instanceof FileAnnotationData) {
                AnnotationData annotation = (AnnotationData) object;
                long id = annotation.getId();
                if (!ids.contains(id)) {
                    l.add(annotation);
                } else {
                    count = map.get(id);
                    if (count != null) {
                        count++;
                        map.put(id, count);
                    } else {
                        count = 1;
                        annotations.put(id, annotation);
                        map.put(id, count);
                    }
                }
            }
        }
        
        //check the count
        Entry<Long, Integer> entry;
        Iterator<Entry<Long, Integer>> k = map.entrySet().iterator();
        int n = filesDocList.size();
        Map<DataObject, Boolean> m;
        while (k.hasNext()) {
            entry = k.next();
            count = entry.getValue();
            if (count != null && count == n) {
                //Check if the annotation needs to be added
                AnnotationData annotation = annotations.get(entry.getKey());
                m = model.getObjectsWith(annotation);
                if (m.size() < count) {
                    l.add(annotation);
                }
            }
        }
        
        return l;
    }

    @Override
    List<Object> getAnnotationsToRemove() {
        List<Object> l = new ArrayList<Object>();
        
        Set<Long> idsToKeep = new HashSet<Long>();
        
        Iterator<DocComponent> i = filesDocList.iterator();
        while (i.hasNext()) {
            DocComponent doc = i.next();
            Object object = doc.getData();
            if (object instanceof FileAnnotationData) {
                AnnotationData annotation = (AnnotationData) object;
                long id = annotation.getId();
                if (id > 0) 
                    idsToKeep.add(id);
            }
        }
        Collection<FileAnnotationData> original = model.getAllAttachments();
        Iterator<FileAnnotationData> j = original.iterator();
        while (j.hasNext()) {
            AnnotationData annotation = (AnnotationData) j.next();
            long id = annotation.getId();
            if (!idsToKeep.contains(id))
                l.add(annotation);
        }
        
        return l;
    }

    Collection<FileAnnotationData> getSelectedFileAnnotations() {
        Collection<FileAnnotationData> result = new ArrayList<FileAnnotationData>();
        
        for(DocComponent c : filesDocList) 
            if(c.isSelected())
                result.add((FileAnnotationData)c.getData());
        
        return result;
    }
    
    /**
     * Returns <code>true</code> some tags can be unlinked,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasAttachmentsToUnlink()
    {
        if (filesDocList.size() == 0) return false;
        DocComponent doc;
        Object object;
        Iterator<DocComponent> i = filesDocList.iterator();
        while (i.hasNext()) {
            doc = i.next();
            object = doc.getData();
            if (doc.canUnlink()) {
                if (object instanceof FileAnnotationData) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    void onRelatedNodesSet() {
        
        
    }

}
