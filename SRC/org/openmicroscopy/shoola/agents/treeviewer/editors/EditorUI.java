/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorUI
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * The <code>DataObject</code> editor.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class EditorUI
    extends JPanel
{

    /** Indicates that this class is hosting the <code>Edit</code> UI. */
    public static final int     EDIT = TreeViewer.PROPERTIES_EDITOR;
    
    /** Indicates that this class is hosting the <code>Create</code> UI. */
    public static final int     CREATE = TreeViewer.CREATE_EDITOR;
    
    /** Bounds property to indicate that the edition is cancelled. */
    public static final String  CANCEL_EDITION_PROPERTY = "cancelEdition";
    
    /** The default height of the <code>TitlePanel</code>. */
    public static final int    	TITLE_HEIGHT = 80;
    
    /** The text corresponding to the creation of a <code>Project</code>. */
    private static final String PROJECT_MSG = "Project";
    
    /** The text corresponding to the creation of a <code>Dataset</code>. */
    private static final String DATASET_MSG = "Dataset";
    
    /** 
     * The text corresponding to the creation of a
     * <code>Category Group</code>.
     */
    private static final String CATEGORY_GROUP_MSG = "Category group";
    
    /** The text corresponding to the creation of a <code>Category</code>. */
    private static final String CATEGORY_MSG = "Category";
    
    /** The text corresponding to the creation of a <code>Image</code>. */
    private static final String IMAGE_MSG = "Image";
    
    /**
     * The message displayed when the name of the <code>DataObject</code> is 
     * null or of length 0.
     */
    private static final String EMPTY_MSG = "The name is empty.";
    
    /** 
     * The title of the main tabbed pane when the <code>DataObject</code>
     * is edited.
     */
    private static final String PROPERTIES_TITLE = "Properties";
    
    /** 
     * The title of the tabbed pane hosting the details of the owner of the
     * edited <code>DataObject</code>.
     */
    private static final String OWNER_TITLE = "Owner";
    
    /** The title of the tabbed pane hosting the details on the image. */
    private static final String INFO_TITLE = "Info";
    
    /** Button to finish the operation e.g. create, edit, etc. */
    private JButton         finishButton;
    
    /** Button to cancel the object creation. */
    private JButton         cancelButton;
    
    /** The panel displaying the message when no name is entered. */
    private JPanel          emptyMessagePanel;
    
    /** The component hosting the title and the warning messages if required. */
    private JLayeredPane    titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
    /** The message identifying the <code>Dataobject</code> to create. */
    private String          message;
    
    /** Indicates that a warning message is displayed if <code>true</code>. */
    private boolean         warning;
    
    /** One of the types defined by this class. */
    private int             editorType;
    
    /**
     * <code>true</code> if the name or description is modified.
     * <code>false</code> otherwise;
     */
    private boolean			edit;
    
    /**
     * The component hosting the name and the description of the 
     * <code>DataObject</code>.
     */
    private DOBasic         doBasic;
    
    /** Reference to the Model. */
    private TreeViewer      model;
    
    /** The currently edited {@link DataObject}. */
    private DataObject      hierarchyObject;
    
    /** The annotations related to the currently edited {@link DataObject}. */ 
    private Map             annotations;
    
    /** 
     * Returns the last annotation.
     * 
     * @param annotations Collection of {@link AnnotationData} 
     *                      related to the {@link DataObject} if any
     * @return See above.
     */
    private AnnotationData getLastAnnotation(Set annotations)
    {
        if (annotations == null || annotations.size() == 0) return null;
        List list = new ArrayList(annotations);
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((AnnotationData) o1).getLastModified(),
                          t2 = ((AnnotationData) o2).getLastModified();
                long n1 = t1.getTime();
                long n2 = t2.getTime();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return v;
            }
        };
        Collections.sort(list, c);
        return (AnnotationData) list.get(list.size()-1);
    }
    
    /** 
     * Checks if the specified type is supported by this class.
     * 
     * @param type The type to control.
     */
    private void checkEditorType(int type)
    {
        switch (type) {
            case EDIT:
            case CREATE:     
                return;
            default:
                throw new IllegalArgumentException("Type not supported.");
        }
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        //TitleBar
        titleLayer = new JLayeredPane();
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                firePropertyChange(CANCEL_EDITION_PROPERTY, Boolean.FALSE,
                        Boolean.TRUE);
            }
        });
        finishButton = new JButton("Finish");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  finish(); }
        });
        doBasic = new DOBasic(this); 
    }
    
    /** 
     * Sets the {@link #message} corresponding to 
     * the <code>Dataobject</code>. 
     * 
     * @param nodeType  The specified class identifying the
     *                  <code>Dataobject</code>.
     */
    private void getMessage(Class nodeType)
    {
        if (nodeType.equals(ProjectData.class))
            message = PROJECT_MSG;
        else if (nodeType.equals(DatasetData.class))
            message = DATASET_MSG;
        else if (nodeType.equals(CategoryData.class)) 
            message = CATEGORY_MSG;
        else if (nodeType.equals(CategoryGroupData.class)) 
            message = CATEGORY_GROUP_MSG;
        else if (nodeType.equals(ImageData.class))
            message = IMAGE_MSG;
    }
    
    /**
     * Builds the panel hosting the title according to the 
     * <code>DataObject</code> and the editorType.
     *
     */
    private void buildTitlePanel()
    {
        IconManager im = IconManager.getInstance();
        switch (editorType) {
            case CREATE:
                titlePanel = new TitlePanel(message, 
                        "Create a new "+ message.toLowerCase()+".", 
                        im.getIcon(IconManager.CREATE_BIG));
                break;
            case EDIT:
                titlePanel = new TitlePanel(message, 
                        "Edit the "+ message.toLowerCase()+": "+
                         getDataObjectName(), 
                        im.getIcon(IconManager.PROPERTIES_BIG));
        }
        titleLayer.add(titlePanel, 0);
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.add(finishButton);
        bar.add(cancelButton);
        return bar;
    }
    
    /** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setBorder(
                            BorderFactory.createLineBorder(Color.BLACK));
        Rectangle r = titlePanel.getBounds();
        emptyMessagePanel.setLayout(new BoxLayout(emptyMessagePanel,
                                                BoxLayout.X_AXIS));
        IconManager im = IconManager.getInstance();
        JLabel label = new JLabel(im.getIcon(IconManager.ERROR));
        emptyMessagePanel.add(label);
        int w = label.getWidth();
        label = new JLabel(EMPTY_MSG);
        int h = label.getFontMetrics(label.getFont()).getHeight();
        w += EMPTY_MSG.length()*getFontMetrics(getFont()).charWidth('m');
        emptyMessagePanel.add(label);
        Insets i = emptyMessagePanel.getInsets();
        h += i.top+i.bottom+2;
        emptyMessagePanel.setBounds(0, r.height-h, w, h);
    }
    
    /**
     * Builds the main component hosted by this class.
     * 
     * @return See above.
     */
    private JComponent buildCenterComponent()
    {
        switch (editorType) {
            case CREATE:
                return doBasic;
            case EDIT:
                IconManager im = IconManager.getInstance();
                JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
                                                   JTabbedPane.WRAP_TAB_LAYOUT);
                tabs.setAlignmentX(LEFT_ALIGNMENT);
                tabs.addTab(PROPERTIES_TITLE, 
                            im.getIcon(IconManager.PROPERTIES), doBasic);
                ExperimenterData exp = getExperimenterData();
                Map details = EditorUtil.transformExperimenterData(exp);
                tabs.addTab(OWNER_TITLE,  im.getIcon(IconManager.OWNER),
                            new DOInfo(details));
                if (hierarchyObject instanceof ImageData) {
                    details = EditorUtil.transformPixelsData(
                            ((ImageData) hierarchyObject).getDefaultPixels());
                    tabs.addTab(INFO_TITLE, im.getIcon(IconManager.IMAGE),
                               new DOInfo(details));
                }
                return tabs;
        }
        return null;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        buildTitlePanel();
        setLayout(new BorderLayout(0, 0));
        add(titleLayer, BorderLayout.NORTH);
        add(buildCenterComponent(), BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        add(p, BorderLayout.SOUTH);
    }
    
    /**
     * Handles the <code>finish</code> action according to the
     * {@link #editorType}.
     */
    private void finish()
    {
        switch (editorType) {
            case CREATE:
                model.saveObject(fillDataObject(), TreeViewer.CREATE_OBJECT);
                break;
            case EDIT:
                finishEdit();
        }
    }
    
    /** Creates, updates or deletes the annotation. */
    private void annotateOnly()
    {
        AnnotationData data = getAnnotationData();
        int algorithm = TreeViewer.UPDATE_ANNOTATION; 
        if (doBasic.isAnnotationDeleted())
            algorithm = TreeViewer.DELETE_ANNOTATION;
        else { 
            if (data == null) {
                data = new AnnotationData();
                algorithm = TreeViewer.CREATE_ANNOTATION;
            }
            data.setText(doBasic.getAnnotationText());
        }
        model.saveObject(null, data, algorithm);
    }
    
    /** Edits and annotates the object. */
    private void editAndAnnotate()
    {
        AnnotationData data = getAnnotationData();
        int algorithm = TreeViewer.UPDATE_ANNOTATION; 
        if (doBasic.isAnnotationDeleted())
            algorithm = TreeViewer.DELETE_ANNOTATION;
        else { 
            if (data == null) {
                data = new AnnotationData();
                algorithm = TreeViewer.CREATE_ANNOTATION;
            }
            data.setText(doBasic.getAnnotationText());
        }
        model.saveObject(fillDataObject(), data, algorithm);
    }
    
    /**
     * Fills the <code>name</code> and <code>description</code> of the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    private DataObject fillDataObject()
    {
        if (hierarchyObject instanceof ProjectData) {
            ProjectData p = (ProjectData) hierarchyObject;
            p.setName(doBasic.nameArea.getText());
            p.setDescription(doBasic.descriptionArea.getText());
            return p;
        } else if (hierarchyObject instanceof DatasetData) {
            DatasetData d = (DatasetData) hierarchyObject;
            d.setName(doBasic.nameArea.getText());
            d.setDescription(doBasic.descriptionArea.getText());
            return d;
        } else if (hierarchyObject instanceof CategoryData) {
            CategoryData c = (CategoryData) hierarchyObject;
            c.setName(doBasic.nameArea.getText());
            c.setDescription(doBasic.descriptionArea.getText());
            return c;
        } else if (hierarchyObject instanceof CategoryGroupData) {
            CategoryGroupData cg = (CategoryGroupData) hierarchyObject;
            cg.setName(doBasic.nameArea.getText());
            cg.setDescription(doBasic.descriptionArea.getText());
            return cg;
        } else if (hierarchyObject instanceof ImageData) {
            ImageData i = (ImageData) hierarchyObject;
            i.setName(doBasic.nameArea.getText());
            i.setDescription(doBasic.descriptionArea.getText());
            return i;
        } 
        return null;
    }
    
    /** 
     * Handles the <code>finish</code> action for the {@link #EDIT} editor
     * type.
     */
    private void finishEdit()
    {
        if (edit) {
            if (doBasic.isAnnotable()) editAndAnnotate();
            else model.saveObject(fillDataObject(), TreeViewer.UPDATE_OBJECT);
        } else {
            if (doBasic.isAnnotable()) annotateOnly();
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to {@link TreeViewer}.
     *              Mustn't be <code>null</code>.
     * @param hierarchyObject The {@link DataObject} to edit.
     * @param editorType    The type of editor. One of the following constants:
     *                      {@link #CREATE}, {@link #EDIT}.
     */
    public EditorUI(TreeViewer model, DataObject hierarchyObject,
                        int editorType)
    {
        if (hierarchyObject == null)
            throw new IllegalArgumentException("No User object not supported.");
        if (model == null)
            throw new IllegalArgumentException("No model.");
        checkEditorType(editorType);
        this.model = model;
        this.hierarchyObject = hierarchyObject;
        this.editorType = editorType;
        initComponents();
        getMessage(hierarchyObject.getClass());
        buildGUI(); 
    }
    
    /**
     * Resets the UI components when the name's of the <code>DataObject</code>
     * is null or of lenght 0.
     */
    void handleEmptyNameArea()
    {
        warning = true;
        finishButton.setEnabled(false);
        buildEmptyPanel();
        titleLayer.add(emptyMessagePanel, 1);
    }
    
    /**
     * Enables the {@link #finishButton} and removes the warning message
     * when the name of the <code>DataObject</code> is valid.
     * Sets the {@link #edit} flag to <code>true</code>.
     */
    void handleNameAreaInsert()
    {
        finishButton.setEnabled(true);
        edit = true;
        if (warning) {
            titleLayer.remove(emptyMessagePanel);
            titleLayer.repaint();
        }
        warning = false;
    }
    
    /**
     * Enables the {@link #finishButton} and sets the {@link #edit} flag
     * to <code>true</code>.
     */
    void handleDescriptionAreaInsert()
    {
        finishButton.setEnabled(true);
        edit = true;
    }
    
    /**
     * Enables the {@link #finishButton} and sets the {@link #edit} flag
     * to <code>true</code>.
     */
    void handleAnnotationAreaInsert()
    {
        finishButton.setEnabled(true);
    }
    
    /**
     * Returns the editor type.
     * 
     * @return See above.
     */
    int getEditorType() { return editorType; }
    
    /**
     * Returns <code>true</code> if it's possible to annotate 
     * the currenlty edited <code>DataObject</code>, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotable()
    { 
        if (hierarchyObject == null) return false;
        else if ((hierarchyObject instanceof DatasetData) ||
                (hierarchyObject instanceof ImageData)) return true; 
        return false;
    }
    
    /**
     * Returns <code>true</code> if the current user can modify the 
     * currently edited object, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isEditable()
    {
        ExperimenterData owner = getExperimenterData();
        if (owner == null) return false;
        return (owner.getId() == model.getUserDetails().getId());
    }
    
    /** 
     * Returns the name of the currenlty edited <code>DataObject</code>.
     * 
     * @return See above.
     */
    String getDataObjectName()
    { 
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getName();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getName();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getName();
        return null;
    }
    
    /** 
     * Returns the description of the currenlty edited <code>DataObject</code>.
     * 
     * @return See above.
     */
    String getDataObjectDescription()
    { 
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getDescription();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getDescription();
        return null;
    }
    
    /**
     * Returns the information on the owner of the {@link DataObject}. 
     * 
     * @return See above.
     */
    ExperimenterData getExperimenterData()
    {
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof ProjectData)
            return ((ProjectData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof CategoryData)
            return ((CategoryData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof CategoryGroupData)
            return ((CategoryGroupData) hierarchyObject).getOwner();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getOwner();
        return null;
    }
    
    /**
     * Returns the annotation of the currently edited <code>DataObject</code>.
     *  
     * @return See above.
     */
    AnnotationData getAnnotationData()
    {
        if (hierarchyObject == null) return null;
        else if (hierarchyObject instanceof ImageData)
            return getLastAnnotation(
                    ((ImageData) hierarchyObject).getAnnotations());
        else if (hierarchyObject instanceof DatasetData)
            return getLastAnnotation(
                    ((DatasetData) hierarchyObject).getAnnotations());
        return null;
    }
     
    /**
     * Returns the annotations for the currently edited object, 
     * <code>null</code> it there is no annotation for that object.
     * 
     * @return See above.
     */
    Map getAnnotations() { return annotations; }
    
    /**
     * Returns <code>true</code> if the <code>DataObject</code> has been 
     * classified, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isClassified()
    {
        if (hierarchyObject == null) return false;
        else if (hierarchyObject instanceof ImageData)
            return true;
        return false;
    }
    
    /**
     * Returns the user's details.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails() { return model.getUserDetails(); }
    
    /** 
     * Browses the specified node.
     * 
     * @param node The node to browse.
     */
    void browse(TreeImageDisplay node)
    {
        Object object = node.getUserObject();
        if (object instanceof DataObject) model.browse((DataObject) object);
    }
    
    /** Retrieves the classification for the currently edited object. */
    void retrieveClassification()
    {
        if (hierarchyObject == null || !(hierarchyObject instanceof ImageData))
            throw new IllegalArgumentException("The method can only be" +
                    "invoked for Image.");
        model.retrieveClassification(((ImageData) hierarchyObject).getId());
    }
    
    /**
     * Returns <code>true</code> if the DataObject is an Image,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasThumbnail()
    {
        if (hierarchyObject == null) return false;
        else if (hierarchyObject instanceof ImageData)
            return true;
        return false;
    }
    
    /**
     * Sets the specified thumbnail 
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
        if (thumbnail ==  null) return;
        if (hasThumbnail()) {
            JLabel label = new JLabel(new ImageIcon(thumbnail));
            label.addMouseListener(new MouseAdapter() {
                
                /**
                 * Views the image if the user double-clicks on the thumbnail.
                 */
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2) model.browse(hierarchyObject);
                }
            });
            titlePanel.setIconComponent(label);
            doBasic.addListeners();
        }
    }
    
    /**
     * Sets the retrieved annotations.
     * 
     * @param map The map with the annotations.
     */
    public void setAnnotations(Map map)
    {
        if (map == null) throw new NullPointerException("No annotations");
        if (!isAnnotable())
            throw new IllegalArgumentException("This mehod should only be " +
                    "invoked for annotable Data object.");
        ViewerSorter sorter = new ViewerSorter();
        sorter.setAscending(false);
        HashMap sortedAnnotations = new HashMap();
        Set set;
        Integer index;
        Iterator i = map.keySet().iterator();
        Iterator j;
        AnnotationData annotation;
        Integer ownerID;
        List userAnnos;
        while (i.hasNext()) {
            index = (Integer) i.next();
            set = (Set) map.get(index);
            j = set.iterator();
            while (j.hasNext()) {
                annotation = (AnnotationData) j.next();;
                ownerID = new Integer(annotation.getOwner().getId());
                userAnnos = (List) sortedAnnotations.get(ownerID);
                if (userAnnos == null) {
                    userAnnos = new ArrayList();
                    sortedAnnotations.put(ownerID, userAnnos);
                }
                userAnnos.add(annotation);
            }
        }
        i = sortedAnnotations.keySet().iterator();
        List timestamps, annotations, results, list;
        HashMap m;
        Iterator k, l;
        AnnotationData data;
        while (i.hasNext()) {
            ownerID = (Integer) i.next();
            annotations = (List) sortedAnnotations.get(ownerID);
            k = annotations.iterator();
            m = new HashMap(annotations.size());
            timestamps = new ArrayList(annotations.size());
            while (k.hasNext()) {
                data = (AnnotationData) k.next();
                m.put(data.getLastModified(), data);
                timestamps.add(data.getLastModified());
            }
            results = sorter.sort(timestamps);
            l = results.iterator();
            list = new ArrayList(results.size());
            while (l.hasNext())
                list.add(m.get(l.next()));
            sortedAnnotations.put(ownerID, list);
        }
        this.annotations = sortedAnnotations;
        doBasic.showAnnotations();
    }
    
    /**
     * Displays the specified set of nodes.
     * 
     * @param nodes The nodes to set.
     */ 
    public void setClassifiedNodes(Set nodes)
    {
        if (nodes == null) throw new IllegalArgumentException("No nodes");
        doBasic.setClassifiedNodes(nodes);      
    }
    
    /**
     * Sets the size of the {@link #titlePanel} and the {@link #titleLayer}.
     * 
     * @param width The width of the components.
     */
    public void setComponentsSize(int width)
    {
        Dimension d  = new Dimension(width, TITLE_HEIGHT);
        titlePanel.setSize(d);
        titlePanel.setPreferredSize(d);
        titleLayer.setSize(d);
        titleLayer.setPreferredSize(d);
    }
    
}
