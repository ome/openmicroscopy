/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorView
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator.view;




//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.ExperimenterData;

/** 
 * Displays the list of annotations and a text area to enter the annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorEditorView 
	extends JPanel
{
	
	/** Background color of the hightlighted node. */
	private static final Color		HIGHLIGHT = new Color(204, 255, 204);
	
	/** Background color of the even rows. */
	private static final Color		BACKGROUND = Color.WHITE;
	
	/** Background color of the add rows. */
	private static final Color		BACKGROUND_ONE = new Color(236, 243, 254);

    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension	SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * horizontally.
     */
    private static final Dimension	SMALL_H_SPACER_SIZE = new Dimension(6, 1);
    
    
    /** The preferred size of the annotation area. */
    private static final Dimension	AREA_SIZE = new Dimension(200, 150);
    
    /** Default text. */
    private static final String		COMMENT = " annotation";
    
    /** Button to finish the operation. */
    private JButton             	saveButton;
    
    /** Area where to annotate the <code>DataObject</code>. */
    private MultilineLabel           annotationArea; 
    
    /** Component delete the annotation. */
    private JButton           		deleteButton;
    
    /** The UI component hosting the display. */
    private JPanel					pane;

	/** The layout, one of the constants defined by {@link AnnotatorFactory}. */
	private int						layout;
	
	/** The id of the selected experimenter. */
	private long					selectedOwnerID;
	
	/** 
	 * Tree hosting the user who annotates the object and the 
	 * date of annotation. 
	 */
	private JTree					treeDisplay;
	
	/** Reference to the model. */
	private AnnotatorEditorModel	model;
	
	/** Reference to the control. */
	private AnnotatorEditorControl	controller;
	
	/** 
	 * (Key, Value) pairs where key is the user id and the value 
	 * the collection of UI component hosting the annotations.
	 */
	private Map<Long, List> 		areas;
	
	/** Panel hosting the UI components displaying the annotations. */
	private JPanel					listAnnotations;
	
	/** ScrollPane hosting the UI component displaying the annotations. */
	private JScrollPane				scrollAnnotations;
	
	/** Flag indicating if the node is brought up on screen programatically.*/
	private boolean					autoScroll;
	
	/** The component displaying the number of annotations. */
	private JLabel					commentLabel;
	
	/** The listener attached to the text area. */
	private DocumentListener		listener;
	
    /** Handles the selection of a node in the tree. */
    private void handleNodeSelection()
    {
    	Object o = treeDisplay.getLastSelectedPathComponent();
		if (o instanceof OwnerNode) {
			long ownerID = ((OwnerNode) o).getOwnerID();
			showSingleAnnotation(ownerID);
			selectedOwnerID = ownerID;
		} else if (o instanceof TimeNode) {
			TimeNode tm = (TimeNode) o;
			long ownerID = tm.getOwnerID();
			if (ownerID != selectedOwnerID)
				showSingleAnnotation(ownerID);
			selectedOwnerID = ownerID;
			showDateAnnotation(ownerID, tm.getIndex());
		}
		validate();
		repaint();
    }

    /** 
     * Scrolls to the passed node.
     * 
     * @param c The component to handle.
     */
    private void scrollToNode(JComponent c)
    {
    	if (c == null) return;
    	autoScroll = true;
    	Rectangle bounds = c.getBounds();
    	Rectangle viewRect = scrollAnnotations.getViewport().getViewRect();
		if (!viewRect.contains(bounds)) {
			int x = 0;
			int y = 0;
			int w = viewRect.width-bounds.width;
			if (w < 0) w = -w;
			x = bounds.x-w/2;
			int h = viewRect.height-bounds.height;
			if (h < 0) h = -h;
			y = bounds.y-h/2;
			JScrollBar hBar = scrollAnnotations.getHorizontalScrollBar();
			JScrollBar vBar = scrollAnnotations.getVerticalScrollBar();
			vBar.setValue(y);
			hBar.setValue(x);
        } 
		if (c == annotationArea) {
			c.setVisible(true);
			c.requestFocus();
		}
    }
    
    /**
     * Sets the default values of the {@link #annotationArea}.
     * 
     * @param title The title to set.
     */
    private void setAnnotationAreaDefault(String title)
    {
    	CompoundBorder border = BorderFactory.createCompoundBorder(
    			new TitledLineBorder(title), 
    			BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    	annotationArea.setBorder(border);
    	annotationArea.setOriginalBackground(BACKGROUND);
    	annotationArea.setOpaque(true);
    	annotationArea.setEditable(true);
    	annotationArea.setPreferredSize(AREA_SIZE);
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
    	commentLabel = new JLabel("0 "+COMMENT);
    	listAnnotations = new JPanel();
    	listAnnotations.setLayout(new BoxLayout(listAnnotations, 
    							BoxLayout.Y_AXIS));
    	scrollAnnotations = new JScrollPane(listAnnotations);
    	JScrollBar vBar = scrollAnnotations.getVerticalScrollBar();
    	//necessary to set the location of the scrollbar when 
    	// the component is embedded in another UI component. */
    	vBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!e.getValueIsAdjusting() && !autoScroll)
				scrollAnnotations.getVerticalScrollBar().setValue(0);
			}
		});
    	treeDisplay = new JTree();
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        treeDisplay.setModel(new DefaultTreeModel(root));
    	treeDisplay.setRootVisible(false);
        treeDisplay.setVisible(true);
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeDisplay.setCellRenderer(new EditorTreeCellRenderer());
        
        treeDisplay.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				handleNodeSelection();
			}
		});
        treeDisplay.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
            	treeDisplay.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
            public void treeExpanded(TreeExpansionEvent e) {
            	treeDisplay.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
        });
            
        saveButton = new JButton(
        			controller.getAction(AnnotatorEditorControl.SAVE));
        annotationArea = new MultilineLabel();
        deleteButton = new JButton(
    			controller.getAction(AnnotatorEditorControl.DELETE));

        listener = new DocumentListener() {
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	model.setAnnotated(true);
            	saveButton.setEnabled(true);
            }
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                model.setAnnotated(true);
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        annotationArea.getDocument().addDocumentListener(listener);
        setComponentsEnabled(false);
    }
    
    /**
     * Builds a panel hosting the {@link #annotationArea} and the list of users
     * who annotated the data object.
     * 
     * @return See above.
     */
    private JPanel buildAnnotationPanel()
    {
        JPanel p = new JPanel();
        JPanel empty = new JPanel();
        empty.setOpaque(true);
        switch (layout) {
			case AnnotatorEditor.HORIZONTAL_LAYOUT:
			default:
				double[][] tl = {{200, 5, TableLayout.FILL}, //columns
	    				{0, TableLayout.FILL} }; //rows
	    		p.setLayout(new TableLayout(tl));
	    		p.add(new JScrollPane(treeDisplay), "0, 0, 0, 1");
	    		p.add(empty, "1, 0, f, t");
	    		p.add(scrollAnnotations , "2, 0, 2, 1");  
				break;
			case AnnotatorEditor.VERTICAL_LAYOUT:
				double[][] tl2 = {{450}, //columns
	    				{200, 5, 300} }; //rows
	    		p.setLayout(new TableLayout(tl2));
	    		p.add(new JScrollPane(treeDisplay), "0, 0");
	    		p.add(empty, "0, 1");
	    		p.add(scrollAnnotations, "0, 2"); 
		}
        return p;
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(deleteButton);
        p.add(saveButton);
        p.add(Box.createRigidArea(SMALL_H_SPACER_SIZE));
        p.add(commentLabel);
        add(UIUtilities.buildComponentPanel(p));
        add(new JSeparator());
        add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
        pane = buildAnnotationPanel();
        add(pane);
    }

    /**
     * Shows the last annotation for the passed experimenter.
     * 
     * @param ownerID The experimenter's id.
     */
    private void showSingleAnnotation(long ownerID)
    {
		listAnnotations.removeAll();
		List l = areas.get(new Long(ownerID));
		Iterator i = l.iterator();
		MultilineLabel c;
		while (i.hasNext()) {
			c = (MultilineLabel) i.next();
			if (c != annotationArea)
				c.setBackground(c.getOriginalBackground());
			listAnnotations.add(c);
		}
    	setComponentsEnabled(ownerID == model.getUserDetails().getId());
    }
    
    /**
     * Shows the specified annotation for the passed experimenter.
     * 
     * @param ownerID	The experimenter's id.
     * @param index		The annotation index in the list of annotation.
     */
    private void showDateAnnotation(long ownerID, int index)
    {
    	List l = areas.get(new Long(ownerID));
    	MultilineLabel c;
    	int j = index; 
    	long userID = model.getUserDetails().getId();
    	if (ownerID == userID) j++;
    	for (int i = 0; i < l.size(); i++) {
			c = (MultilineLabel) l.get(i);
			c.setBackground(c.getOriginalBackground());
			if (j == i && c != annotationArea) 
				c.setBackground(HIGHLIGHT);
		}
    	c = (MultilineLabel) l.get(j);
    	scrollToNode(c);
    	deleteButton.setEnabled(index != -1 && (ownerID == userID));
    }
    
    /**
     * Creates child nodes and adds to the passed parent.
     * 
     * @param parent		The parent's node.
     * @param annotations	The annotations to convert.
     * @param dtm			The default tree model.
     */
    private void buildTreeNode(OwnerNode parent, List annotations, 
    							DefaultTreeModel dtm)
    {
    	long ownerID = parent.getOwnerID();
    	Long id = new Long(ownerID);
    	boolean user = model.getUserDetails().getId() == ownerID;
    	TimeNode node = new TimeNode(ownerID, -1, null);
    	if (user) {
    		dtm.insertNodeInto(node, parent, parent.getChildCount());
    		setAnnotationAreaDefault(node.toString());
    	}
    	if ((annotations == null || annotations.size() == 0) && user) {
    		List<JTextArea> l = new ArrayList<JTextArea>(1);
    		l.add(annotationArea);
    		areas.put(id, l);
    		return;
    	}

        List<JComponent> l;
        int row = 0;
        if (user) {
        	l = new ArrayList<JComponent>(annotations.size()+1);
        	l.add(annotationArea);
        	row = 1;
        } else {
        	l = new ArrayList<JComponent>(annotations.size());
        }
        Iterator i = annotations.iterator();
        int index = 0;
        MultilineLabel area;
        Timestamp date;
        AnnotationData data;
        while (i.hasNext()) {
        	data = (AnnotationData) i.next();
        	date = data.getLastModified();
        	if (date == null) date = new Timestamp(new Date().getTime());
        	node = new TimeNode(ownerID, index, date);
        	data = (AnnotationData) annotations.get(index);
        	dtm.insertNodeInto(node, parent, parent.getChildCount());
        	area = new MultilineLabel();
            area.setEditable(false);
            area.setOpaque(true);
        	area.setBorder(new TitledLineBorder(node.toString()));
        	area.setText(data.getText());
        	if (index%2 == row) area.setOriginalBackground(BACKGROUND);
            else area.setOriginalBackground(BACKGROUND_ONE);
        	l.add(area);
        	index++;
		}
        areas.put(id, l);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param layout The selected layout.
     */
	AnnotatorEditorView(int layout)
	{
		this.layout = layout;
		areas = new HashMap<Long, List>();
		selectedOwnerID = -1;
	}
	
	/**
	 * Links this View to its Controller.
	 * 
	 * @param model 		The Model. Mustn't be <code>null</code>.
	 * @param controller 	The Controller. Mustn't be <code>null</code>.
	 */
	void initialize(AnnotatorEditorModel model, 
					AnnotatorEditorControl controller)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	/** 
     * Sets the UI components enabled.
     * 
     * @param b The enabled flag. 
     */
    void setComponentsEnabled(boolean b)
    {
        saveButton.setEnabled(b);
        deleteButton.setEnabled(!b);
        //annotationArea.setEditable(b);
        //if (b) {
        //    annotationArea.requestFocus();
        //    annotationArea.selectAll();
        //}
    }
	
    /** Shows the annotations. */
    void showAnnotations()
    {
    	DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
    	dtm.reload(root);
    	listAnnotations.removeAll();
    	areas.clear();
    	deleteButton.setSelected(false);
    	ExperimenterData userDetails = model.getUserDetails();
        if (userDetails == null) return;
        Map annotations = model.getAnnotations();
        Iterator i = annotations.keySet().iterator();
        Long id;
        int index = 0;
        List list;
        ExperimenterData data;
        OwnerNode owner;
        OwnerNode currentUser = null;
        int number = 0;
        while (i.hasNext()) {
            id = (Long) i.next();
            list = (List) annotations.get(id);
            number += list.size();
            data = ((AnnotationData) list.get(0)).getOwner();
            owner = new OwnerNode(data);
            dtm.insertNodeInto(owner, root, root.getChildCount());
            buildTreeNode(owner, list, dtm);
            if (userDetails.getId() == id.intValue()) currentUser = owner;
            index++;
        }
        //No annotation for the current user so we add a node
        if (currentUser == null) { 
        	currentUser = new OwnerNode(userDetails);
        	dtm.insertNodeInto(currentUser, root, root.getChildCount());
        	buildTreeNode(currentUser, null, dtm);
        }
        treeDisplay.setSelectionPath(new TreePath(currentUser.getPath()));
        setComponentsEnabled(true);
        String text = number+COMMENT;
        if (number > 1) text += "s";
        commentLabel.setText(text);
    }
   
    /**
     * Reacts to a new selection in the browser.
     * 
     * @param b Passed <code>true</code> to enable the controls,
     *          <code>true</code> otherwise.
     */
    void onSelectedDisplay(boolean b)
    {
        setComponentsEnabled(b);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
    	dtm.reload(root);
    	listAnnotations.removeAll();
    	areas.clear();
    	annotationArea.getDocument().removeDocumentListener(listener);
    	annotationArea.setText("");
    	annotationArea.getDocument().addDocumentListener(listener);
    	model.setAnnotated(false);
    	
        repaint();
    }
    
    /**
     * Returns <code>true</code> if the data object is annotated,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotatable()
    { 
    	Object o = treeDisplay.getLastSelectedPathComponent();
    	if (o instanceof OwnerNode) {
    		ExperimenterData details = model.getUserDetails();
    		return (details.getId() == ((OwnerNode) o).getOwnerID());
    	} else if (o instanceof TimeNode) {
    		TimeNode tm = (TimeNode) o;
    		int index = tm.getIndex();
    		Map annotations = model.getAnnotations();
        	List list = (List) annotations.get(tm.getOwnerID());
        	if (list == null || list.size() == 0) return true;
        	return (index == list.size()-1);
    	}
    	return false;
    }
    
    /** 
     * Returns the text of the annotation. 
     * 
     * @return See above. 
     */
    String getAnnotationText()
    { 
    	String s = annotationArea.getText();
    	if (s == null) return "";
    	return s.trim(); 
    }

	/**
	 * Returns <code>true</code> if the current user has annotation,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAnnotation() 
	{
		AnnotationData data = model.getAnnotationData();
		if (data == null) return false;
		return true;
	}

	/**
	 * Returns <code>true</code> if the user has annotation to save.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() 
	{
		boolean b = true;
		if (getAnnotationText().length() == 0) b = false;
		return (isAnnotatable() && b);
	}
   
	/** Inner class hosting the experimenter details. */
	class OwnerNode 
		extends DefaultMutableTreeNode
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param ho The original object. Never pass <code>null</code>. 
		 */
		OwnerNode(ExperimenterData ho)
		{
			super();
			if (ho == null)
				throw new NullPointerException("No experimenter.");
			setUserObject(ho);
		}
		
		/**
		 * Returns the id of the experimenter.
		 * 
		 * @return See above.
		 */
		long getOwnerID()
		{
			return ((ExperimenterData) getUserObject()).getId();
		}
		
		/**
		 * Overridden to return the first and last name of the experimenter.
		 * @see Object#toString()
		 */
		public String toString()
		{ 
			ExperimenterData data = (ExperimenterData) getUserObject();
			String n = "Name not available"; //TODO: REMOVE ASAP
            try {
            	n = data.getFirstName()+" "+data.getLastName();
            } catch (Exception e) {}
			return n; 
		}
	}
	
	/** Inner class hosting the annotation time. */
	class TimeNode
		extends DefaultMutableTreeNode
	{
		
		/** The index in the annotation list. */
		private int index;
		
		/** The id of the experimenter who entered the annotation. */
		private long ownerID;
		
		/**
		 * Creates a new instance.
		 *
		 * @param ownerID	The id of the experimenter who entered the 
		 * 					annotation.
		 * @param index		The index in the annotation list.
		 * @param date		The timestamp.
		 */
		TimeNode(long ownerID, int index, Timestamp date)
		{
			super();
			setUserObject(date);
			this.index = index;
			this.ownerID = ownerID;
		}
		
		/**
		 * Returns the id of the experimenter.
		 * 
		 * @return See above.
		 */
		long getOwnerID() { return ownerID; }
		
		/**
		 * Returns the index.
		 * 
		 * @return See above.
		 */
		int getIndex() { return index; }
		
		/**
		 * Overridden to return a formatted date
		 * @see Object#toString()
		 */
		public String toString()
		{ 
			if (getUserObject() == null) return "New Annotation";
			String s = getUserObject().toString();
			return s.substring(0, s.indexOf("."));//df.format((Timestamp) getUserObject()) ;
		}
	}
	
}
