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
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
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
 * The {@link Editor}'s view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EditorUI
    extends JPanel
{
    
    /** The default height of the <code>TitlePanel</code>. */
    public static final int    	    TITLE_HEIGHT = 80;
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    static final Dimension          SMALL_V_SPACER_SIZE = 
                                                new Dimension(1, 6);
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    
    /** The text corresponding to the creation of a <code>Project</code>. */
    private static final String     PROJECT_MSG = "Project";
    
    /** The text corresponding to the creation of a <code>Dataset</code>. */
    private static final String     DATASET_MSG = "Dataset";
    
    /** 
     * The text corresponding to the creation of a
     * <code>Category Group</code>.
     */
    private static final String     CATEGORY_GROUP_MSG = "Category group";
    
    /** The text corresponding to the creation of a <code>Category</code>. */
    private static final String     CATEGORY_MSG = "Category";
    
    /** The text corresponding to the creation of a <code>Image</code>. */
    private static final String     IMAGE_MSG = "Image";
    
    /**
     * The message displayed when the name of the <code>DataObject</code> is 
     * null or of length 0.
     */
    private static final String     EMPTY_MSG = "The name is empty.";
    
    /** 
     * The title of the main tabbed pane when the <code>DataObject</code>
     * is edited.
     */
    private static final String     PROPERTIES_TITLE = "Properties";
    
    /** 
     * The title of the tabbed pane hosting the details of the owner of the
     * edited <code>DataObject</code>.
     */
    private static final String     OWNER_TITLE = "Permissions";
    
    /** The title of the tabbed pane hosting the details on the image. */
    private static final String     INFO_TITLE = "Info";

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
    private EditorModel     model;
    
    /** Reference to the Control. */
    private EditorControl   controller;
    
    /** Initializes the components. */
    private void initComponents()
    {
        //TitleBar
        titleLayer = new JLayeredPane();
        
        cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                controller.close(true);
            }
        });
        finishButton = new JButton("Save");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {  finish(); }
        });
        doBasic = new DOBasic(this, model, controller); 
    }
    
    /** 
     * Sets the {@link #message} corresponding to 
     * the <code>Dataobject</code>. 
     */
    private void getMessage()
    {
        Class nodeType = model.getHierarchyObject().getClass();
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
     */
    private void buildTitlePanel()
    {
        IconManager im = IconManager.getInstance();
        switch (model.getEditorType()) {
            case Editor.CREATE_EDITOR:
                titlePanel = new TitlePanel(message, 
                        "Create a new "+ message.toLowerCase()+".", 
                        im.getIcon(IconManager.CREATE_BIG));
                break;
            case Editor.PROPERTIES_EDITOR:
                titlePanel = new TitlePanel(message, 
                        "Edit the "+ message.toLowerCase()+": "+
                         model.getDataObjectName(), 
                        im.getIcon(IconManager.PROPERTIES_BIG));
        }
        titleLayer.add(titlePanel, new Integer(0));
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        //bar.setRollover(true);
        //bar.setFloatable(false);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        return bar;
    }
    
    /** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setOpaque(true);
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
        switch (model.getEditorType()) {
            case Editor.CREATE_EDITOR:
                return doBasic;
            case Editor.PROPERTIES_EDITOR:
                IconManager im = IconManager.getInstance();
                JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
                                                   JTabbedPane.WRAP_TAB_LAYOUT);
                tabs.setAlignmentX(LEFT_ALIGNMENT);
                tabs.addTab(PROPERTIES_TITLE, 
                            im.getIcon(IconManager.PROPERTIES), doBasic);
                ExperimenterData exp = model.getExperimenterData();
                Map details = EditorUtil.transformExperimenterData(exp);
                tabs.addTab(OWNER_TITLE,  im.getIcon(IconManager.OWNER),
                            new DOInfo(this, model, details, true));
                DataObject hierarchyObject = model.getHierarchyObject();
                if (hierarchyObject instanceof ImageData) {
                    details = EditorUtil.transformPixelsData(
                            ((ImageData) hierarchyObject).getDefaultPixels());
                    tabs.addTab(INFO_TITLE, im.getIcon(IconManager.IMAGE),
                               new DOInfo(this, model, details, false));
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
        setOpaque(true);
        add(titleLayer, BorderLayout.NORTH);
        JComponent c = buildCenterComponent();
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(c, BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        p.setOpaque(true);
        add(p, BorderLayout.SOUTH);
    }
    
    /**
     * Handles the <code>finish</code> action depending on the type of editor.
     */
    private void finish()
    {
        String s = doBasic.getNameText();
        if (s == null || s.length() == 0) {
            doBasic.resetNameArea();
            handleNameAreaRemove(0);
            return;
        }
        switch (model.getEditorType()) {
            case Editor.CREATE_EDITOR:
                controller.createObject(fillDataObject());
                break;
            case Editor.PROPERTIES_EDITOR:
                finishEdit();
        }
    }
    
    /** Removes the annotation. */
    private void removeAnnotate()
    {
        AnnotationData data = model.getAnnotationData();
        if (doBasic.isAnnotationDeleted()) {
            if (data != null) 
                controller.deleteAnnotation(fillDataObject(), data);
        }
    }
    
    /** Edits and annotates the object. */
    private void editAndAnnotate()
    {
        AnnotationData data = model.getAnnotationData();
        if (doBasic.isAnnotationDeleted()) {
            if (data != null) 
                controller.deleteAnnotation(fillDataObject(), data);
        } else {  
            if (data == null) { 
                DataObject ho = model.getHierarchyObject();
                if (ho instanceof ImageData)
                    data = new AnnotationData(AnnotationData.IMAGE_ANNOTATION);
                else 
                    data = new AnnotationData(
                            AnnotationData.DATASET_ANNOTATION); 
                data.setText(doBasic.getAnnotationText());
                controller.createAnnotation(fillDataObject(), data);
            } else {
                data.setText(doBasic.getAnnotationText());
                controller.updateAnnotation(fillDataObject(), data);
            }
        }  
    }

    /** 
     * Handles the <code>finish</code> action for the 
     * {@link Editor#PROPERTIES_EDITOR} editortype.
     */
    private void finishEdit()
    {
        if (edit) {
            if (doBasic.isAnnotable()) {
                if (model.isAnnotated()) editAndAnnotate();
                else {
                    AnnotationData data = model.getAnnotationData();
                    if (doBasic.isAnnotationDeleted()) {
                        if (data != null) 
                            controller.deleteAnnotation(fillDataObject(), data);
                    } else {
                        controller.updateObject(fillDataObject());
                    }
                }
            } else controller.updateObject(fillDataObject());
        } else {
            if (doBasic.isAnnotable()) {
                if (model.isAnnotated()) editAndAnnotate();
                else removeAnnotate();
            }
        }
    } 
    
    /**
     * Fills the <code>name</code> and <code>description</code> of the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    private DataObject fillDataObject()
    {
        DataObject hierarchyObject = model.getHierarchyObject();
        if (hierarchyObject instanceof ProjectData) {
            ProjectData p = (ProjectData) hierarchyObject;
            p.setName(doBasic.getNameText());
            p.setDescription(doBasic.descriptionArea.getText());
            return p;
        } else if (hierarchyObject instanceof DatasetData) {
            DatasetData d = (DatasetData) hierarchyObject;
            d.setName(doBasic.getNameText());
            d.setDescription(doBasic.descriptionArea.getText());
            return d;
        } else if (hierarchyObject instanceof CategoryData) {
            CategoryData c = (CategoryData) hierarchyObject;
            c.setName(doBasic.getNameText());
            c.setDescription(doBasic.descriptionArea.getText());
            return c;
        } else if (hierarchyObject instanceof CategoryGroupData) {
            CategoryGroupData cg = (CategoryGroupData) hierarchyObject;
            cg.setName(doBasic.getNameText());
            cg.setDescription(doBasic.descriptionArea.getText());
            return cg;
        } else if (hierarchyObject instanceof ImageData) {
            ImageData i = (ImageData) hierarchyObject;
            i.setName(doBasic.getNameText());
            i.setDescription(doBasic.descriptionArea.getText());
            return i;
        } 
        return null;
    }
    
    /** Creates a new instance. */
    EditorUI()
    {
        warning = false;
        edit = false;
    }
    
    /**
     * Links MVC.
     * 
     * @param controller    Reference to the control.
     *                      Mustn't be <code>null</code>.   
     * @param model         Reference to the control. 
     *                      Mustn't be <code>null</code>.   
     */
    void initialize(EditorControl controller, EditorModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.controller = controller;
        this.model = model;
        initComponents();
        getMessage();
        buildGUI(); 
    }
    
    /**
     * Displays an error message when the length of the inserted name is
     * <code>0</code>.
     * 
     * @param length The length of the inserted text.
     */
    void handleNameAreaRemove(int length)
    {
        if (length == 0) {
            warning = true;
            finishButton.setEnabled(false);
            buildEmptyPanel();
            titleLayer.add(emptyMessagePanel, new Integer(1)); 
        } else finishButton.setEnabled(true);
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
     * Sets the specified thumbnail 
     * 
     * @param thumbnail The thumbnail to set.
     */
    void setThumbnail(BufferedImage thumbnail)
    {
        JLabel label = new JLabel(new ImageIcon(thumbnail));
        label.addMouseListener(new MouseAdapter() {
            
            /**
             * Views the image if the user double-clicks on the thumbnail.
             */
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) 
                    model.browse(model.getHierarchyObject());
            }
        });
        titlePanel.setIconComponent(label);
        doBasic.addListeners();
    }

    /** Shows the images' annotations. */
    void showLeavesAnnotations()
    {
        if (doBasic != null) doBasic.showLeavesAnnotations();
    }
    
    /** Shows the retrieved annotations.  */
    void showAnnotations()
    {
        if (doBasic != null) doBasic.showAnnotations();
    }
    
    /**
     * Sets the value of the {@link #edit} flag.
     * 
     * @param b The value to set.
     */
    void setEdit(boolean b) { edit = b; }
    
    /** Displays the classifications. */ 
    void showClassifications()
    { 
        if (doBasic != null) doBasic.showClassifications();
    }
    
    /** 
     * Reacts to state change.
     * 
     * @param b Pass <code>true</code> to enable the trees, <code>false</code>
     *          otherwise.
     */
    void onStateChanged(boolean b)
    {
        model.getParentModel().onComponentStateChange(b);
        finishButton.setEnabled(b);
    }
    
    /**
     * Overridden to set the size of the title panel.
     * @see JPanel#setSize(int, int)
     */
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        Dimension d  = new Dimension(width, TITLE_HEIGHT);
        titlePanel.setSize(d);
        titlePanel.setPreferredSize(d);
        titleLayer.setSize(d);
        titleLayer.setPreferredSize(d);
    }

}
