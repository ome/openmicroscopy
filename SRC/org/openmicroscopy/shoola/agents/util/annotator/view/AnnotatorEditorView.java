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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
	
	 /** The default annotation text. */
    private static final String		DEFAULT_TEXT = "No annotation for ";
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension	SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /** Button to finish the operation. */
    private JButton             	saveButton;
    
    /** Area where to annotate the <code>DataObject</code>. */
    private JTextArea           	annotationArea; 
    
    /** Component delete the annotation. */
    private JButton           		deleteButton;

    /** 
     * Flag indicating that the default text is displayed for the 
     * current user.
     */
    private boolean             	defaultText;
    
    /** The UI component hosting the display. */
    private JPanel					pane;
    
    /** A {@link DocumentListener} for the {@link #annotationArea}. */
    private DocumentListener    	annotationAreaListener;
	
	/** The layout, one of the constants defined by {@link AnnotatorFactory}. */
	private int						layout;
	
	/** 
	 * Tree hosting the user who annotates the object and the 
	 * date of annotation. 
	 */
	private JTree					treeDisplay;
	
	/** Convenience reference. */
	private DateFormat 				df; 
	
	/** Reference to the model. */
	private AnnotatorEditorModel	model;
	
	/** Reference to the control. */
	private AnnotatorEditorControl controller;
	
    /** Handles the selection of a node in the tree. */
    private void handleNodeSelection()
    {
    	Object o = treeDisplay.getLastSelectedPathComponent();
		if (o instanceof OwnerNode) {
			showSingleAnnotation(((OwnerNode) o).getOwnerID());
		} else if (o instanceof TimeNode) {
			TimeNode tm = (TimeNode) o;
			showDateAnnotation(tm.getOwnerID(), tm.getIndex());
		}
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
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
    	
        saveButton = new JButton(
        			controller.getAction(AnnotatorEditorControl.SAVE));
        annotationArea = new MultilineLabel();
        annotationArea.setBackground(Color.WHITE);
        annotationArea.setBorder(new TitledBorder("Annotation"));
        deleteButton = new JButton(
    			controller.getAction(AnnotatorEditorControl.DELETE));
        
        annotationAreaListener = new DocumentListener() {
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	defaultText = false;
            	model.setAnnotated(true);
            }
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
            	defaultText = false;
                model.setAnnotated(true);
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        annotationArea.getDocument().addDocumentListener(
                            annotationAreaListener);
        annotationArea.addMouseListener(new MouseAdapter() {
            //Removes default message.
            public void mouseClicked(MouseEvent e)
            {
                if (isAnnotatable() && defaultText) {
                	annotationArea.getDocument().removeDocumentListener(
                            annotationAreaListener);
                    annotationArea.setText("");
                    setComponentsEnabled(true);
                    deleteButton.setEnabled(false);
                    annotationArea.getDocument().addDocumentListener(
                            annotationAreaListener);
                }
            }
        
        });
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
				double[][] tl = {{TableLayout.FILL, 5, TableLayout.FILL}, //columns
	    				{0, TableLayout.FILL} }; //rows
	    		p.setLayout(new TableLayout(tl));
	    		p.add(new JScrollPane(treeDisplay), "0, 0, 0, 1");
	    		p.add(empty, "1, 0, f, t");
	    		p.add(new JScrollPane(annotationArea), "2, 0, 2, 1");   
				break;

			case AnnotatorEditor.VERTICAL_LAYOUT:
				double[][] tl2 = {{TableLayout.FILL}, //columns
	    				{200, 5, 200} }; //rows
	    		p.setLayout(new TableLayout(tl2));
	    		p.add(new JScrollPane(treeDisplay), "0, 0");
	    		p.add(empty, "0, 1");
	    		p.add(new JScrollPane(annotationArea), "0, 2"); 
		}
        return p;
    }
    
    /**
     * Sets the specified text to the {@link #annotationArea}.
     * 
     * @param text  The text to set.
     */
    private void addAnnotationText(String text)
    {
        annotationArea.getDocument().removeDocumentListener(
                                        annotationAreaListener);
        annotationArea.setText(text);
        annotationArea.getDocument().addDocumentListener(
                                        annotationAreaListener);
    }
    

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(deleteButton);
        p.add(saveButton);
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
    	AnnotationData data = model.getLastAnnotationFor(ownerID);
    	if (data == null) {
    		ExperimenterData details = model.getUserDetails();
    		String n = "Name not available"; //TODO: REMOVE ASAP
            try {
            	n = details.getFirstName()+" "+details.getLastName();
            } catch (Exception e) {}
    		addAnnotationText(DEFAULT_TEXT+n);
    	} else {
    		addAnnotationText(data.getText());
        	setComponentsEnabled(ownerID == model.getUserDetails().getId());
    	}
    }
    
    /**
     * Shows the specified annotation for the passed experimenter.
     * 
     * @param ownerID	The experimenter's id.
     * @param index		The annotation index in the list of annotation.
     */
    private void showDateAnnotation(long ownerID, int index)
    {
    	Map annotations = model.getAnnotations();
    	List list = (List) annotations.get(ownerID);
    	int size = list.size();
        if (size > 0) {
            AnnotationData data = (AnnotationData) list.get(index);
            addAnnotationText(data.getText());  
        }
        ExperimenterData details = model.getUserDetails();
        setComponentsEnabled(index == size-1 && (ownerID == details.getId()));
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
        Timestamp date;
        AnnotationData data;
        Iterator i = annotations.iterator();
        List<Timestamp> 
        	timestamps = new ArrayList<Timestamp>(annotations.size());
        while (i.hasNext()) {
        	data = (AnnotationData) i.next();
        	date = data.getLastModified();
        	if (date == null) date = new Timestamp(new Date().getTime());
        	timestamps.add(date);
		}
        i = model.sortByDate(timestamps).iterator();
        int n = timestamps.size()-1;
        int index = 0;
        TimeNode node;
        long ownerID = parent.getOwnerID();
        while (i.hasNext()) {
        	node = new TimeNode(ownerID, n-index, (Timestamp) i.next());
        	dtm.insertNodeInto(node, parent, parent.getChildCount());; 
        	index++;
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param layout The selected layout.
     */
	AnnotatorEditorView(int layout)
	{
		this.layout = layout;
		df = DateFormat.getDateInstance();
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
        deleteButton.setEnabled(b);
        annotationArea.setEditable(b);
        if (b) {
            annotationArea.requestFocus();
            annotationArea.selectAll();
        }
    }
	
    /** Shows the annotations. */
    void showAnnotations()
    {
    	DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
    	
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
        while (i.hasNext()) {
            id = (Long) i.next();
            list = (List) annotations.get(id);
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
        	addAnnotationText(DEFAULT_TEXT+currentUser.toString());
        	defaultText = true;
        }
        //treeDisplay.expandPath(new TreePath(root.getPath()));
        treeDisplay.setSelectionPath(new TreePath(currentUser.getPath()));
        setComponentsEnabled(true);
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
        addAnnotationText("");
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
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
		return (isAnnotatable() && !defaultText && b);
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
			return df.format((Timestamp) getUserObject()) ;
		}
	}
	
}
