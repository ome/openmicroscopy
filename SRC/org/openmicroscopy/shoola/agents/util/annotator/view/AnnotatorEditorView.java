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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


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
    
    /** Button to display the annotation history. */
    private JToggleButton			historyButton;
    
    /** Button to display the annotation per user. */
    //private JButton					backwardButton;
    
    /** Area where to annotate the <code>DataObject</code>. */
    private JTextArea           	annotationArea;
    
    /** Hosts a list of users who annotated the selected object. */
    private JList               	annotatedByList;
    
    /** Hosts a list of annotation date. */
    private JList					historyList;
    
    /** Component delete the annotation. */
    private JButton           		deleteButton;
    
    /** The model keeping track of the users who annotated a data object. */
    private DefaultListModel    	listModel;
    
    /** The model keeping track of the dates the annotation was made. */
    private DefaultListModel    	historyListModel;
    
    /** Maps of users who annotated the data object. */
    private Map<Integer, Long>		ownersMap;
    
    /** The index of the current user.*/
    private int                 	userIndex;
    
    /** Indicates if the history is visible or not. */
    private boolean					history;
    
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
	
	/** Reference to the model. */
	private AnnotatorEditorModel	model;
	
	/** Reference to the control. */
	private AnnotatorEditorControl controller;
	
	/** Displays the previous annotation. */
    private void displayHistory()
    {
    	history = true;
    	int ownerIndex = annotatedByList.getSelectedIndex();
    	if (ownerIndex == -1) return;
    	List l = getOwnerAnnotations(ownerIndex);
    	if (l == null) return;
    	formatDateList(l);
    	int n = 0;//historyListModel.getSize()-1;
    	historyList.setSelectedIndex(n);
    	if (pane != null) remove(pane);
    	pane = buildHistoryPane();
    	add(pane, BorderLayout.CENTER);
    	validate();
    	repaint();
    }
    
    /** Displays the main panel. */
    private void backWard()
    {
    	history = false;
    	if (pane != null) remove(pane);
    	pane = buildAnnotationPanel();
    	annotatedByList.setSelectedIndex(annotatedByList.getSelectedIndex());
    	//reset the value of the text area
    	showSingleAnnotation();
    	add(pane, BorderLayout.CENTER);
    	validate();
    	repaint();
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
    	historyButton = new JToggleButton(
    			controller.getAction(AnnotatorEditorControl.HISTORY));
    	historyButton.setToolTipText("Shows History List.");
    	//UIUtilities.unifiedButtonLookAndFeel(historyButton);
        saveButton = new JButton(
        			controller.getAction(AnnotatorEditorControl.SAVE));
        historyListModel = new DefaultListModel();
        historyList = new JList(historyListModel);
        historyList.setBackground(getBackground());
        historyList.setBorder(new TitledBorder("Annotated"));
        historyList.setSelectionMode(
                ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        historyList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1)
                    showDateAnnotation();
            }
        });
        
        annotationArea = new MultilineLabel();
        annotationArea.setBackground(Color.WHITE);
        annotationArea.setBorder(new TitledBorder("Annotation"));
        deleteButton = new JButton(
    			controller.getAction(AnnotatorEditorControl.DELETE));
        listModel = new DefaultListModel();
        annotatedByList = new JList(listModel);
        annotatedByList.setBackground(getBackground());
        annotatedByList.setBorder(new TitledBorder("Annotated by"));
        annotatedByList.setSelectionMode(
                ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        annotatedByList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1)
                    showSingleAnnotation();
            }
        });
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
	    		p.add(new JScrollPane(annotatedByList), "0, 0, 0, 1");
	    		p.add(empty, "1, 0, f, t");
	    		p.add(new JScrollPane(annotationArea), "2, 0, 2, 1");   
				break;

			case AnnotatorEditor.VERTICAL_LAYOUT:
				double[][] tl2 = {{TableLayout.FILL}, //columns
	    				{200, 5, 200} }; //rows
	    		p.setLayout(new TableLayout(tl2));
	    		p.add(new JScrollPane(annotatedByList), "0, 0");
	    		p.add(empty, "0, 1");
	    		p.add(new JScrollPane(annotationArea), "0, 2"); 
		}
        return p;
    }
    
    /** 
     * Builds the UI component displaying the history.
     * 
     * @return See above.
     */
    private JPanel buildHistoryPane()
    {
    	//Set panel layout and border
        JPanel p = new JPanel();
        JPanel empty = new JPanel();
	    empty.setOpaque(true);
	    switch (layout) {
			case AnnotatorEditor.HORIZONTAL_LAYOUT:
			default:
				double[][] tl = {{TableLayout.FILL, 5, TableLayout.FILL}, //columns
	    				{0, TableLayout.FILL} }; //rows
			    p.setLayout(new TableLayout(tl));
			    p.add(new JScrollPane(historyList), "0, 0, 0, 1");
			    
			    p.add(empty, "1, 0, f, t");
			    p.add(new JScrollPane(annotationArea), "2, 0, 2, 1");    
				break;

			case AnnotatorEditor.VERTICAL_LAYOUT:
				double[][] tl2 = {{TableLayout.FILL}, //columns
	    				{200,  5, 200} }; //rows
	    		p.setLayout(new TableLayout(tl2));
	    		p.add(new JScrollPane(historyList), "0, 0");
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
        p.add(new JSeparator(JSeparator.HORIZONTAL));
        p.add(historyButton);
        add(UIUtilities.buildComponentPanel(p));
        add(new JSeparator());
        add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
        pane = buildAnnotationPanel();
        add(pane);
    }
    
    /**
     * Displays the users' name in a list box.
     * 
     * @param owners Array of users who annotated the selected item.
     */
    private void formatUsersList(String[] owners)
    {
        // remove all users from list before adding new
        listModel.removeAllElements();
        
        // add each user to list
        Timestamp date;
        DateFormat df = DateFormat.getDateInstance();
        AnnotationData data;
        for (int i = 0; i < owners.length; i++) {
            data = getOwnerLastAnnotation(i);
            date = data.getLastModified();
            if (date == null) date = new Timestamp(new Date().getTime());;
            listModel.addElement(owners[i]+" ("+df.format(date)+")");  
        }
    }
    
    /**
     * Formats the list by date.
     * 
     * @param annotations Collections of annotations.
     */
    private void formatDateList(List annotations)
    {
    	//remove all users from list before adding new
        historyListModel.removeAllElements();
        
        // add each user to list
        Timestamp date;
        DateFormat df = DateFormat.getDateInstance();
        AnnotationData data;
        Iterator i = annotations.iterator();
        List<Timestamp> 
        	timestamps = new ArrayList<Timestamp>(annotations.size());
        while (i.hasNext()) {
        	data = (AnnotationData) i.next();
        	date = data.getLastModified();
        	if (date == null) date = new Timestamp(new Date().getTime());
        	//historyListModel.addElement(df.format(date)); 
        	timestamps.add(date);
		}
        i = model.sortByDate(timestamps).iterator();
        while (i.hasNext()) 
        	historyListModel.addElement(df.format((Timestamp) i.next())); 
		
    }
    
    /**
     * Returns the last annotation made by the selected user.
     * 
     * @param index The index of the selected user.
     * @return See below.
     */
    private AnnotationData getOwnerLastAnnotation(int index)
    { 
        //Map annotations = model.getAnnotations();
        Long ownerID = ownersMap.get(new Integer(index));
        if (ownerID == null) return null;    //empty list
        return model.getLastAnnotationFor(ownerID.longValue());
    }
    
    /**
     * Returns the list of annotations made by the selected user.
     * 
     * @param index The index of the selected user.
     * @return See below.
     */
    private List getOwnerAnnotations(int index)
    { 
        Map annotations = model.getAnnotations();
        Long ownerID = ownersMap.get(new Integer(index));
        if (ownerID == null) return new ArrayList();    //empty list
        return (List) annotations.get(ownerID);
    }
    
    //private AnnotationD
    /** Shows a single annotation. */
    private void showSingleAnnotation()
    {
        int index = annotatedByList.getSelectedIndex();
        if (index == -1) {
            ExperimenterData details = model.getUserDetails();
            addAnnotationText(DEFAULT_TEXT+details.getFirstName()+" "+
                                details.getLastName());
            defaultText = true;
            setComponentsEnabled(true);
            deleteButton.setEnabled(false);
            return;
        }
        AnnotationData data = getOwnerLastAnnotation(index);
        if (data != null) {
            System.err.println(data.getText());
            addAnnotationText(data.getText());  
        }
        setComponentsEnabled(index == userIndex);
    }
    
    /** Shows a single annotation. */
    private void showDateAnnotation()
    {
    	int count = 0;//historyListModel.getSize()-1;
        int index = historyList.getSelectedIndex();
        if (index == -1) {
        	ExperimenterData details = model.getUserDetails();
            addAnnotationText(DEFAULT_TEXT+details.getFirstName()+" "+
                                details.getLastName());
            defaultText = true;
            setComponentsEnabled(true);
            deleteButton.setEnabled(false);
            return;
        }
        int ownerIndex = annotatedByList.getSelectedIndex();
        List list = getOwnerAnnotations(ownerIndex);
        if (list.size() > 0) {
        	int n = list.size()-1-index; //index;
            AnnotationData data = (AnnotationData) list.get(n);
            addAnnotationText(data.getText());  
        }
        setComponentsEnabled(index == count && (ownerIndex == userIndex));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param layout The selected layout.
     */
	AnnotatorEditorView(int layout)
	{
		this.layout = layout;
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
		userIndex = -1;
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
        deleteButton.setSelected(false);
        ExperimenterData userDetails = model.getUserDetails();
        if (userDetails == null) return;
        Map annotations = model.getAnnotations();
        String[] owners = new String[annotations.size()];
        Iterator i = annotations.keySet().iterator();
        Long id;
        int index = 0;
        ownersMap = new HashMap<Integer, Long>();
        List list;
        ExperimenterData data;
        while (i.hasNext()) {
            id = (Long) i.next();
            list = (List) annotations.get(id);
            data = ((AnnotationData) list.get(0)).getOwner();
            if (userDetails.getId() == id.intValue()) userIndex = index;
            String n = "Name not available"; //TODO: REMOVE ASAP
            try {
            	n = data.getFirstName()+" "+data.getLastName();
            } catch (Exception e) {}
            owners[index] = n;
            ownersMap.put(new Integer(index), id);
            index++;
        }
        //No annotation for the current user, so allow creation.
        
        setComponentsEnabled(true);
        formatUsersList(owners);
        //annotatedByList.clearSelection();
        if (userIndex != -1) annotatedByList.setSelectedIndex(userIndex);
        showSingleAnnotation();
        if (history) displayHistory();
    }
   
    /**
     * Reacts to a new selection in the browser.
     * 
     * @param b     Passed <code>true</code> to enable the controls,
     *              <code>true</code> otherwise.
     */
    void onSelectedDisplay(boolean b)
    {
        setComponentsEnabled(b);
        userIndex = -1;
        addAnnotationText("");
        listModel.clear();
        historyListModel.clear();
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
        if (userIndex == -1) return true;//no annotation for current user
        return (annotatedByList.getSelectedIndex() == userIndex); 
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

    /** Shows the history list or the main panel. */
	void history()
	{
		if (history) backWard();
		else displayHistory(); 
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
		int index = annotatedByList.getSelectedIndex();
		return (userIndex == index && index != -1);
	}

	boolean hasHistory() 
	{
		return (annotatedByList.getSelectedIndex() != -1);
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
    
}
