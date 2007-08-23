/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.PartialLineBorder;
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
    
    /** The maximum number of UI indexes for the tabbed panes. */
    static final int				MAX_INDEX = 3;
    
    /** The maximum length of the name. */
    private static final int		MAX_NAME = 255;
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** The text indicating where the new <code>Dataset</code> will be added. */
    private static final String     PROJECT_PARENT_MSG = " to project: ";
    
    /** The text indicating where the new <code>Image</code> will be added. */
    private static final String     DATASET_PARENT_MSG = " to dataset: ";
    
    /** The text indicating where the new <code>Category</code> will be added. */
    private static final String     CATEGORY_GROUP_PARENT_MSG = 
                                            " to category group: ";
    
    /** The text indicating where the new <code>Image</code> will be added. */
    private static final String     CATEGORY_PARENT_MSG = " to category : ";
    
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
    
    /**
     * The message identifying in which container the new object will be 
     * added.
     */ 
    private String          messageParent;
    
    /** Indicates that a warning message is displayed if <code>true</code>. */
    private boolean         warning;
    
    /** Flag indicating if the name or description is modified. */
    private boolean			edit;
    
    /** 
     * The tabbed pane hosting the various components if we are in the 
     * <code>Editor</code> mode.
     */
    private JTabbedPane     tabs;
    
    /**
     * The component hosting the name and the description of the 
     * <code>DataObject</code>.
     */
    private DOBasic         doBasic;
    
    /** 
     * The component hosting the annotation, <code>null</code> if the data 
     * object is not annotable.
     */
    private AnnotatorEditor	annotator;
    
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
        if (model.isAnnotatable()) {	
            //annotator = new DOAnnotation(view, model);
        	annotator = model.createAnnotator();
        	annotator.addPropertyChangeListener(controller);
        }
    }
    
    /** 
     * Sets the {@link #message} corresponding to 
     * the <code>DataObject</code>. 
     */
    private void getMessage()
    {
        Class nodeType = model.getHierarchyObject().getClass();
        if (nodeType.equals(ProjectData.class)) {
            messageParent = null;
            message = PROJECT_MSG;
        } else if (nodeType.equals(DatasetData.class)) {
            messageParent = PROJECT_PARENT_MSG;
            message = DATASET_MSG;
        } else if (nodeType.equals(CategoryData.class)) {
            messageParent = CATEGORY_GROUP_PARENT_MSG;
            message = CATEGORY_MSG;
        } else if (nodeType.equals(CategoryGroupData.class)) {
            messageParent = null;
            message = CATEGORY_GROUP_MSG;
        } else if (nodeType.equals(ImageData.class)) {
            Class parentType = model.getParentClass();
            messageParent = null;
            if (parentType != null) {
                if (parentType.equals(DatasetData.class))
                    messageParent = DATASET_PARENT_MSG;
                else if (parentType.equals(CategoryData.class))
                    messageParent = CATEGORY_PARENT_MSG;
            }
            message = IMAGE_MSG;
        } 
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
                String subTitle = "Add a new "+ message.toLowerCase();
                if (messageParent != null) 
                    subTitle += messageParent+model.getParentName()+"."; 
                titlePanel = new TitlePanel(message, subTitle, 
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
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
    /**
     * Builds the tool bar hosting the {@link #finishButton} and
     * the passed button if not <code>null</code>.
     * 
     * @param button The button to add.
     * @return See above.
     */
    JPanel buildBasicToolBar(JButton button)
    {
    	JPanel bar = new JPanel();
        bar.setBorder(null);
        if (button != null) {
        	bar.add(button);
            bar.add(Box.createRigidArea(H_SPACER_SIZE));
        }
        bar.add(finishButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
    /** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setOpaque(true);
        emptyMessagePanel.setBorder(new PartialLineBorder(Color.BLACK));
        Rectangle r = titlePanel.getBounds();
        
        emptyMessagePanel.setLayout(new BoxLayout(emptyMessagePanel,
                                                BoxLayout.X_AXIS));
        IconManager im = IconManager.getInstance();
        JLabel label = new JLabel(im.getIcon(IconManager.ERROR));
        emptyMessagePanel.add(label);
        int w = label.getWidth();
        label = new JLabel(EMPTY_MSG);
        int h = label.getFontMetrics(label.getFont()).getHeight();
        w += getFontMetrics(getFont()).stringWidth(EMPTY_MSG);
        emptyMessagePanel.add(label);
        Insets i = emptyMessagePanel.getInsets();
        h += i.top+i.bottom+2;
        emptyMessagePanel.setBounds(0, r.height-h, 3*w/2, h);
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
                tabs = new JTabbedPane(JTabbedPane.TOP, 
                                                   JTabbedPane.WRAP_TAB_LAYOUT);
                tabs.setAlignmentX(LEFT_ALIGNMENT);
                tabs.addTab(PROPERTIES_TITLE, 
                        im.getIcon(IconManager.PROPERTIES), doBasic);
                if (annotator != null) {
                	tabs.addTab(DOBasic.ANNOTATION, 
                            im.getIcon(IconManager.ANNOTATION), 
                            annotator.getUI());
                }
                /*
                ExperimenterData exp = model.getDataObjectOwner();
            	
                Map details = EditorUtil.transformExperimenterData(exp);
                DOInfo info = new DOInfo(this, model, details, true, 
                                    DOInfo.OWNER_TYPE);
                
                tabs.addTab(OWNER_TITLE,  im.getIcon(IconManager.OWNER), 
                			info);
                */			
                DataObject hierarchyObject = model.getHierarchyObject();
                if (hierarchyObject instanceof ImageData) {
                	ExperimenterData exp = model.getDataObjectOwner();
                	
                    Map details = EditorUtil.transformExperimenterData(exp);
                    details = EditorUtil.transformImageData(
                    						(ImageData) hierarchyObject);
                    DOInfo info = new DOInfo(this, model, details, false, 
                            DOInfo.INFO_TYPE);
                    tabs.addTab(INFO_TITLE, im.getIcon(IconManager.IMAGE), 
                    			info);  
                }
                //Add a tab listeners to the info
                tabs.addChangeListener(controller);
                int index = model.getSelectedTabbedIndex();
                switch (index) {
					case Editor.PROPERTIES_INDEX:
						tabs.setSelectedIndex(index);
						break;
					case Editor.ANNOTATIONS_INDEX:
						if (annotator != null) 
							tabs.setSelectedIndex(index);
						else {
							setEditorSelectedPane(0);
							tabs.setSelectedIndex(0);
						}
						break;
					case Editor.INFO_INDEX:
						if (hierarchyObject instanceof ImageData) 
							tabs.setSelectedIndex(index);
						else {
							setEditorSelectedPane(0);
							tabs.setSelectedIndex(0);
						}
						break;
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
        c.setOpaque(true);
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane pane = new JScrollPane(c);
        c.setPreferredSize(pane.getViewport().getExtentSize());
        add(pane, BorderLayout.CENTER);
        if (model.getEditorType() == Editor.CREATE_EDITOR) 
            add(buildToolBar(), BorderLayout.SOUTH);
    }

    /** 
     * Handles the <code>finish</code> action for the 
     * {@link Editor#PROPERTIES_EDITOR} editortype.
     */
    private void finishEdit()
    {
        if (edit) controller.updateObject(fillDataObject());
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
    
    /** Create a default thumbnail. */
    private void setTitlePanelIcon()
    {
    	Class nodeType = model.getHierarchyObject().getClass();
        if (nodeType.equals(ImageData.class) && 
        		model.getEditorType() == Editor.PROPERTIES_EDITOR) {
           //Create a default Thumbnail.
        	IconManager icons = IconManager.getInstance();
        	JLabel label = new JLabel(icons.getImageIcon(IconManager.IMAGE_48));
        			//new ImageIcon(Factory.createDefaultThumbnail()));
            
            titlePanel.setIconComponent(label);
        }   
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
        setTitlePanelIcon();
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
    	if (doBasic == null) return;
    	if (doBasic.hasDataToSave()) {
    		finishButton.setEnabled(true);
            edit = true;
    	} else {
    		finishButton.setEnabled(false);
            edit = false;
    	}
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
    } 
    
    /**
     * Sets the value of the {@link #edit} flag.
     * 
     * @param b The value to set.
     */
    void setEdit(boolean b)
    { 
    	edit = b; 
    	finishButton.setEnabled(b);
    }
    
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
        if (model.getHierarchyObject() instanceof ImageData && tabs != null) {
            if (b) tabs.addChangeListener(controller);
            else tabs.removeChangeListener(controller);
        }
        //model.getParentModel().onComponentStateChange(b);
        //finishButton.setEnabled(b);
    }
    
    /**
     * Returns <code>true</code> if the editor has some data to save 
     * before closing, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasDataToSave()
    {
        if (model.getEditorType() == Editor.CREATE_EDITOR) {
            String s = doBasic.getNameText();
            if (s == null || s.length() ==0) return false;
            return true;
        }
        if (edit && doBasic.hasDataToSave()) return true;
        return false;
    }
    
    /**
     * Handles the <code>finish</code> action depending on the type of editor.
     */
    void finish()
    {
        String s = doBasic.getNameText();
        if (s == null || s.length() == 0) {
            doBasic.resetNameArea();
            handleNameAreaRemove(0);
            return;
        }
        if (s.length() > MAX_NAME) {
        	UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo("Editor", "The name is too long. Cannot be more " +
        			"than "+MAX_NAME+" characters long.");
        	doBasic.resetName();
        	return;
        }
        finishButton.setEnabled(false);
        switch (model.getEditorType()) {
            case Editor.CREATE_EDITOR:
                controller.createObject(fillDataObject());
                break;
            case Editor.PROPERTIES_EDITOR:
                finishEdit();
        }
    }
    
    /** Sets the emission wavelengths values. */
    void setChannelsData()
    {
    	if (tabs == null) return;
    	Component c;
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            c = tabs.getComponentAt(i);
            Object ho = model.getHierarchyObject();
            if (c instanceof DOInfo && ho instanceof ImageData) {
                ImageData img = (ImageData) ho;
                Map<String, String> 
                	details = EditorUtil.transformImageData(img);
                List waves = model.getChannelsData();
                if (waves == null) return;
                String s = "";
                Iterator k = waves.iterator();
                int j = 0;
                while (k.hasNext()) {
                    s += 
                       ((ChannelMetadata) k.next()).getEmissionWavelength();
                    if (j != waves.size()-1) s +=", ";
                    j++;
                }
                details.put(EditorUtil.WAVELENGTHS, s);
                ((DOInfo) c).setChannelsData(details);
            } 
        }
    }
    
    /**
     * Sets the index of the selected tabbed pane.
     * 
     * @param selectedIndex The index to set.
     */
    void setEditorSelectedPane(int selectedIndex)
    {
        if (model.getEditorType() == Editor.PROPERTIES_EDITOR)
            model.setEditorSelectedPane(selectedIndex);
    }
    
    /** Sets the focus on the name area. */
	void setFocusOnName()
	{
		doBasic.nameArea.requestFocus();
	}
	
	/**
	 * Sets the default button for the specified root pane.
	 * 
	 * @param rootPane The root pane to consider.
	 */
	void setDefaultButton(JRootPane rootPane)
	{
		rootPane.setDefaultButton(finishButton);
	}

	/**
	 * Returns the window containing the editor.
	 * 
	 * @return See above.
	 */
	JFrame getParentUI() { return model.getParentModel().getUI(); }
	
	/**
	 * Forwards all to the model.
	 * 
	 * @param b    	Pass <code>true</code> to allow cancellation, 
     *              <code>false</code> otherwise.
     * @param text	The text to display.
     * @param hide  Pass <code>true</code> to hide the progress bar.
     *              <code>false</code> otherwise.
	 */
	void setStatus(boolean b, String text, boolean hide) 
	{
		model.getParentModel().setStatus(b, text, hide);
	}

	 /**
     * Adds the passed nodes for annotation.
     * 
     * @param nodes	The nodes to pass.
     */
	void addSelectedNodes(List nodes)
	{
		//if (doBasic != null) doBasic.addSiblings(nodes);
		if (annotator != null) annotator.addSelectedNodes(nodes);
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

    /**
     * Overridden to set the size of the title panel.
     * @see JPanel#setSize(Dimension)
     */
    public void setSize(Dimension d) { setSize(d.width, d.height); }

}
