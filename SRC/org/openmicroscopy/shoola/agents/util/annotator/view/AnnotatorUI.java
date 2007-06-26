/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorUI 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
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
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.actions.FinishAction;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;


/** 
 * Displays the list of annotations if some of the <code>DataObject</code>s
 * have already been annotated and a text area to enter the annotation.
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
class AnnotatorUI
	extends JPanel
{

    /** Area where to annotate the <code>DataObject</code>. */
    private MultilineLabel           annotationArea; 
	
	/** The id of the selected experimenter. */
	private long					selectedOwnerID;
	
	/** The id of the selected object. */
	private AnnotateNode			selectedNode;
	
	/** 
	 * Tree hosting the users who annotates the object and the 
	 * date of annotation. 
	 */
	private JTree					ownerTree;
	
	/** Reference to the model. */
	private AnnotatorModel			model;
	
	/** Reference to the control. */
	private AnnotatorControl		controller;
	
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
	private boolean					init;
	
	/** The component displaying the number of annotations. */
	private JLabel					commentLabel;
	
	/** The listener attached to the text area. */
	private DocumentListener		listener;
	
	/** Tree hosting the selected images. */
	private JTree					objectsTree;
	
    /** Handles the selection of a node in the {@link #ownerTree}. */
    private void handleNodeSelection()
    {
    	Object o = ownerTree.getLastSelectedPathComponent();
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

    /** Handles the selection of a node in the {@link #objectsTree}. */
    private void handleAnnotateNodeSelection()
    {
    	AnnotateNode node = 
    				(AnnotateNode) objectsTree.getLastSelectedPathComponent();
		selectedNode = node;
		showObjectAnnotation(selectedNode);
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
    	init = true;
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
     * Enables the <code>Finish</code> action depending on the
     * length of the text entered.
     */
    private void handleAnnotationAreaInsert()
    {
    	FinishAction action = (FinishAction)
    		controller.getAction(AnnotatorControl.FINISH);
    	action.setEnabled(hasDataToSave());
    }
    
    
    /** Initializes the UI components. */
    private void initComponents()
    {
    	commentLabel = new JLabel("0 "+AnnotatorUtil.COMMENT);
    	listAnnotations = new JPanel();
    	listAnnotations.setLayout(new BoxLayout(listAnnotations, 
    							BoxLayout.Y_AXIS));
    	scrollAnnotations = new JScrollPane(listAnnotations);
    	JScrollBar vBar = scrollAnnotations.getVerticalScrollBar();
    	//necessary to set the location of the scrollbar when 
    	// the component is embedded in another UI component. */
    	vBar.addAdjustmentListener(new AdjustmentListener() {
		
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!e.getValueIsAdjusting() && !init) {
					JScrollBar vBar = scrollAnnotations.getVerticalScrollBar();
			    	vBar.setValue(0);
		    	} else if (e.getValueIsAdjusting()) init = true;
			}
		
		});
    	ownerTree = AnnotatorUtil.initTree();
        ownerTree.setCellRenderer(new EditorTreeCellRenderer());
        
        ownerTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				handleNodeSelection();
			}
		});
        ownerTree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
            	ownerTree.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
            public void treeExpanded(TreeExpansionEvent e) {
            	ownerTree.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
        });
         
        objectsTree = AnnotatorUtil.initTree();
        objectsTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				handleAnnotateNodeSelection();
			}
		});
        objectsTree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
            	objectsTree.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
            public void treeExpanded(TreeExpansionEvent e) {
            	objectsTree.setSelectionPath(
            			new TreePath(e.getPath().getLastPathComponent()));
            }
        });
        annotationArea = new MultilineLabel();
        annotationArea.setBorder(BorderFactory.createEtchedBorder());
        annotationArea.setRows(AnnotatorUtil.ROWS);
        listener = new DocumentListener() {
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	handleAnnotationAreaInsert();
            }
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
            	handleAnnotationAreaInsert();
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        annotationArea.getDocument().addDocumentListener(listener);
        handleAnnotationAreaInsert();
    }
    
    /**
     * Builds a panel hosting the {@link #annotationArea} and the list of users
     * who annotated the data object.
     * 
     * @return See above.
     */
    private JComponent buildAnnotationPanel()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        if (model.getAnnotationMode() == Annotator.BULK_ANNOTATE_MODE) {
        	double[][] tl = {{TableLayout.FILL}, //columns
    						{TableLayout.FILL} }; //rows
    		p.setLayout(new TableLayout(tl));
        	AnnotatorUtil.setAnnotationAreaDefault(annotationArea, 
        			AnnotatorUtil.NEW_ANNOTATION);
        	p.add(annotationArea, "0, 0");
        	commentLabel.setText(AnnotatorUtil.BULK_TEXT);
        } else {
        	JPanel empty = new JPanel();
            empty.setOpaque(true);
            double[][] tl = {{250, 5, 250, 5, TableLayout.FILL}, //columns
    				{0, TableLayout.FILL} }; //rows
    		p.setLayout(new TableLayout(tl));
    		p.add(new JScrollPane(objectsTree), "0, 0, 0, 1");
    		p.add(empty, "1, 0, f, t");
    		p.add(new JScrollPane(ownerTree), "2, 0, 0, 1");
    		p.add(empty, "3, 0, f, t");
    		p.add(scrollAnnotations , "4, 0, 4, 1");  
        }
        
		return p;
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(commentLabel);
        add(UIUtilities.buildComponentPanel(p));
        add(new JSeparator());
        add(Box.createRigidArea(AnnotatorUtil.SMALL_V_SPACER_SIZE));
        add(buildAnnotationPanel());
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
		JScrollPane pane;
		while (i.hasNext()) {
			c = (MultilineLabel) i.next();
			if (c != annotationArea) {
				c.setBackground(c.getOriginalBackground());
				listAnnotations.add(c);
			} else {
				pane = new JScrollPane(c);
				pane.getVerticalScrollBar().setVisible(true);
				listAnnotations.add(pane);
			}
		}
    	//setComponentsEnabled(ownerID == model.getUserDetails().getId());
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
				c.setBackground(UIUtilities.HIGHLIGHT);
		}
    	c = (MultilineLabel) l.get(j);
    	scrollToNode(c);
    	//deleteButton.setEnabled(index != -1 && (ownerID == userID));
    }
    
    /**
     * Displays the list of annotations linked to the data object specified
     * by the passed id.
     * 
     * @param node The selected node.
     */
    private void showObjectAnnotation(AnnotateNode node)
    {
    	long objectID = node.getUserObjectID();
    	DefaultTreeModel dtm = (DefaultTreeModel) ownerTree.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
    	dtm.reload(root);
    	listAnnotations.removeAll();
    	areas.clear();
    	//deleteButton.setSelected(false);
    	ExperimenterData userDetails = model.getUserDetails();
        if (userDetails == null) return;
    	Map annotations = null;
        annotations = model.getAnnotationsFor(objectID);
        OwnerNode currentUser = null;
        int number = 0;
        if (annotations != null) { // no annotation for this image.
        	Iterator i = annotations.keySet().iterator();
            Long id;
            int index = 0;
            List list;
            ExperimenterData data;
            OwnerNode owner;
            while (i.hasNext()) {
                id = (Long) i.next();
                list = (List) annotations.get(id);
                number += list.size();
                data = ((AnnotationData) list.get(0)).getOwner();
                owner = new OwnerNode(data);
                owner.setNumberOfAnnotations(list.size());
                dtm.insertNodeInto(owner, root, root.getChildCount());
                buildTreeNode(owner, list, dtm);
                if (userDetails.getId() == id.intValue()) currentUser = owner;
                index++;
            }
        } 
        
        //No annotation for the current user so we add a node
        if (currentUser == null) { 
        	currentUser = new OwnerNode(userDetails);
        	dtm.insertNodeInto(currentUser, root, root.getChildCount());
        	buildTreeNode(currentUser, null, dtm);
        }
        TreePath path = new TreePath(currentUser.getPath());
        ownerTree.setSelectionPath(path);
        ownerTree.expandPath(path);
        //setComponentsEnabled(true);
        String text = number+AnnotatorUtil.COMMENT;
        if (number > 1) text += "s";
        text += " for "+node.getObjectName();
        commentLabel.setText(text);
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
    		AnnotatorUtil.setAnnotationAreaDefault(annotationArea, 
    												node.toString());
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
        	if (index%2 == row) area.setOriginalBackground(
        			UIUtilities.BACKGROUND);
            else area.setOriginalBackground(UIUtilities.BACKGROUND_ONE);
        	l.add(area);
        	index++;
		}
        areas.put(id, l);
    }
    
    /**
     * Builds the nodes composing the {@link #objectsTree}.
     * 
     * @param parent
     * @param nodes
     * @param dtm
     */
    private void buildImageTree(DefaultMutableTreeNode parent, Set nodes,
    							DefaultTreeModel dtm)
    {
    	Iterator i = nodes.iterator();
    	AnnotateNode node;
    	while (i.hasNext()) {
    		node = new AnnotateNode(i.next());
    		if (selectedNode == null) 
    			selectedNode = node;
    		dtm.insertNodeInto(node, parent, parent.getChildCount());
		}
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 Reference to the model. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
	AnnotatorUI(AnnotatorModel model, AnnotatorControl controller)
	{
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		areas = new HashMap<Long, List>();
		selectedOwnerID = -1;
		selectedNode = null;
		initComponents();
		buildGUI();
	}
	
	/** Shows the annotations. */
    void showAnnotations()
    {
    	//Build the image tree
    	DefaultTreeModel dtm = (DefaultTreeModel) objectsTree.getModel();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
    	root.removeAllChildren();
    	buildImageTree(root, model.getSelectedObjects(), dtm);
    	dtm.reload(root);
    	//if (selectedNode != null)
    	//	objectsTree.setSe
    	if (selectedNode != null) {
    		showObjectAnnotation(selectedNode);
    		objectsTree.setSelectionPath(new TreePath(selectedNode.getPath()));
    	}
    		
    }
	
	/**
	 * Returns the textual annotation.
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
	 * Returns <code>true</code> if the user has annotation to save.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() 
	{
		return !(getAnnotationText().length() == 0) ;
	}
	
	/**
	 * Returns the name of the selected <code>Dataset</code> or
	 * the partial name if the selected node is an <code>Image</code>.
	 * 
	 * @return See above.
	 */
	String getSelectedObjectName()
	{
		if (selectedNode == null) return "";
		return selectedNode.toString();
	}
	
	/**
	 * Returns the currently selected data object if any or <code>null</code>.
	 * 
	 * @return See above.
	 */
	DataObject getSelectedDataObject()
	{
		if (selectedNode == null) return null;
		Object uo = selectedNode.getUserObject();
		if (uo instanceof DataObject) return (DataObject) uo;
		return null;
	}
	
}
