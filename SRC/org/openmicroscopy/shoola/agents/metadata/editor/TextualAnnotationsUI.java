/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TextualAnnotationsUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;


/** 
 * UI component displaying the textual annotations.
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
class TextualAnnotationsUI 
	extends AnnotationUI
	implements ActionListener, DocumentListener
{
    
	/** The title associated to this component. */
	private static final String TITLE = "Comments ";
	
	/** Indicates to display the annotations by date. */
	private static final int	DATE_VIEW = 0;
	
	/** Indicates to display the annotation by user. */
	private static final int	USER_VIEW = 1;
	
	/** 
	 * Indicates to clear the annotations entered by the currently 
	 * logged in user. 
	 */
	private static final int	CLEAR = 2;
	
	/** Button indicating to order the annotations by date. */
	private JToggleButton		dateView;
	
	/** Button indicating to display the annotations by user. */
	private JToggleButton		userView;
	
	/** One of the constants defined by this class. */
	private int					layoutIndex;
	
	/** The UI displaying the annotations ordered by date. */
	private JScrollPane			datePane;
	
	/** The UI displaying the annotations ordered by user. */
	private JScrollPane			userPane;
	
	/**
	 * Area displaying the latest textual annotation made by 
	 * the currently logged in user if any. 
	 */
	private MultilineLabel 		area;
	
	/** 
	 * Button to clear the annotations entered by the currently logged in 
	 * user.
	 */
	private JButton				clearButton;
	
	/** The UI component hosting the previous annotations. */
	private JPanel				previousPane;
	
	/** The UI component acting like a tree. */
	private TreeComponent		previousTree;
	
	/** Collection of annotations to remove. */
	private Set<AnnotationData>	toRemove;
	
	/** The text set in the {@link #area}. */
	private String				originalText;
	
	/** The menu bar. */
	private JToolBar 			displayBar;
	
	/** The border displaying the title. */
	private TitledLineBorder	border;
	
	/**
	 * Returns <code>true</code> if the data object has been 
	 * previously annotated, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean hasPreviousTextualAnnotations()
	{
		Map<Long, List> m = model.getTextualAnnotationByOwner();
		long userID = MetadataViewerAgent.getUserDetails().getId();
		if (m.size() > 1 && !m.containsKey(userID)) return true;
		List l = m.get(userID);
		if (l == null) return false;
		int n = l.size();
		if (n > 1 && n != toRemove.size()) return true;
		return false;
	}
	
	/** Initializes the components. */
	private void initializePreviousComponent()
	{
		previousPane = new JPanel();
		previousPane.setLayout(new BoxLayout(previousPane, BoxLayout.Y_AXIS));
		/*
		double[][] tl = {{200, TableLayout.PREFERRED}, //columns
				{TableLayout.PREFERRED, 300, TableLayout.PREFERRED}}; //rows
		previousPane.setLayout(new TableLayout(tl));
		*/
		/*
		previousPane.add(UIUtilities.buildComponentPanelRight(displayBar), 
							"1, 0");
							*/
		previousPane.add(UIUtilities.buildComponentPanelRight(displayBar), 
						Component.RIGHT_ALIGNMENT);
		JPanel collapse = new JPanel();
		TitledLineBorder border = new TitledLineBorder("Previous "+TITLE, 
													getBackground());
		collapse.setBorder(border);
		previousPane.setBorder(border);
		previousTree = new TreeComponent();
		previousTree.insertNode(previousPane, collapse, false);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		area = new MultilineLabel();
		UIUtilities.setTextAreaDefault(area);
		area.setEditable(true);
		//area.getDocument().addDocumentListener(this);
		IconManager icons = IconManager.getInstance();
		dateView = new JToggleButton(icons.getIcon(IconManager.ORDER_BY_DATE));
		String tip = "Sort "+TITLE.toLowerCase();
		dateView.setToolTipText(tip+" by date");
		dateView.setActionCommand(""+DATE_VIEW);
		dateView.addActionListener(this);
		userView = new JToggleButton(icons.getIcon(IconManager.ORDER_BY_USER));
		userView.setToolTipText(tip+" by user");
		userView.setActionCommand(""+USER_VIEW);
		userView.addActionListener(this);
		dateView.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(dateView);
		group.add(userView);
		displayBar = new JToolBar();
		displayBar.setBorder(null);
		displayBar.setFloatable(false);
		displayBar.add(UIUtilities.setTextFont("Display: "));
		displayBar.add(Box.createHorizontalStrut(5));
		displayBar.add(dateView);
		displayBar.add(userView);
		initializePreviousComponent();
		
		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Remove all your "+TITLE+".");
		clearButton.addActionListener(this);
		clearButton.setActionCommand(""+CLEAR);
		toRemove = new HashSet<AnnotationData>();
		userPane = new JScrollPane();
		datePane = new JScrollPane();
	}
	
	/**
	 * Builds and lays out the area hosting the annotation.
	 * 
	 * @return See above.
	 */
	private JPanel buildAreaPane()
	{
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 100} }; //rows
		TableLayout layout = new TableLayout(tl);
		p.setLayout(layout);
		p.add(UIUtilities.setTextFont("Comment"), "0, 0, l, c");
		p.add(new JScrollPane(area), "2, 0, 2, 1");
		return p;
	}
	
	/**
	 * Builds and lays out the UI component hosting the collection
	 * of annotations ordered by date.
	 * 
	 * @return See above.
	 */
	private JPanel layoutDatePane()
	{
		//if (datePane != null) return datePane;
		JPanel datePane = new JPanel();
		datePane.setBackground(UIUtilities.BACKGROUND);
		List l = model.getTextualAnnotationsByDate();
		if (l == null) return datePane;
		double[] columns = {TableLayout.FILL};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		datePane.setLayout(layout);
		
		//populate now
		Iterator i = l.iterator();
		TextualAnnotationData data;
		TextualAnnotationComponent comp;
		int index = 0;
		while (i.hasNext()) {
			data = (TextualAnnotationData) i.next();
			if (!toRemove.contains(data)) {
				comp = new TextualAnnotationComponent(this, model, data);
				layout.insertRow(index, TableLayout.PREFERRED);
				datePane.add(comp, "0, "+index+", f, c");
				if (index%2 == 0) 
					comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_EVEN);
	            else comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_ODD);
				index++;
			}
		}
		return datePane;
	}
	
	/**
	 * Builds and lays out the UI component hosting the collection
	 * of annotations ordered by user.
	 * 
	 * @return See above.
	 */
	private JPanel layoutUserPane()
	{
		JPanel userPane = new JPanel();
		userPane.setBackground(UIUtilities.BACKGROUND);
		Map<Long, List> annotations = model.getTextualAnnotationByOwner();
		if (annotations == null) return userPane;
		Iterator i = annotations.keySet().iterator();
		List l;
		Iterator j;
		TextualAnnotationData data;
		TextualAnnotationComponent comp;
		int index;
		while (i.hasNext()) {
			l = annotations.get(i.next());
			if (l != null) {
				j = l.iterator();
				index = 0;
				while (j.hasNext()) {
					data = (TextualAnnotationData) j.next();
					comp = new TextualAnnotationComponent(this, model, data);
					if (index%2 == 0) 
						comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_EVEN);
		            else 
		            	comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_ODD);
					userPane.add(comp);
					index++;
				}
			}
		}
		return userPane;
	}
	
	/** Removes all annotations entered by the currently logged in user. */
	private void deleteAllAnnotations()
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		Map<Long, List> annotations = model.getTextualAnnotationByOwner();
		List l = annotations.get(userID);
		if (l == null || l.size() == 0) return;
		toRemove.clear();
		Iterator i = l.iterator();
		while (i.hasNext()) 
			toRemove.add((AnnotationData) i.next());
		setAreaText("");
		layoutPreviousNodes();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/** Lays out the node. */
	private void layoutPreviousNodes()
	{
		setNodesTitle();
		switch (layoutIndex) {
			case DATE_VIEW:
				datePane.getViewport().add(layoutDatePane());
				break;
			case USER_VIEW:
				userPane.getViewport().add(layoutUserPane());
		}
		previousPane.repaint();
	}
	
	/** Sets the title of the components. */
	private void setNodesTitle()
	{
		int n = 0;
		if (model.getRefObject() instanceof TagAnnotationData) n = 0;
		else n = model.getTextualAnnotationCount()-toRemove.size();
		title = TITLE+LEFT+n+RIGHT;
		border.setTitle(title);
		((TitledBorder) getBorder()).setTitle(title);
	}
	
	/**
	 * Sets the text of the {@link #area}.
	 * 
	 * @param text The value to set.
	 */
	private void setAreaText(String text)
	{
		area.getDocument().removeDocumentListener(this);
		area.setText(text);
		area.getDocument().addDocumentListener(this);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	TextualAnnotationsUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
		border = new TitledLineBorder(title, getBackground());
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
		add(buildAreaPane());
	}
	
	/**
	 * Deletes the specified annotation.
	 * 
	 * @param data	The annotation to remove.
	 */
	void deleteAnnotation(AnnotationData data)
	{
		if (data == null) return;
		if (model.isCurrentUserOwner(data)) {
			toRemove.add(data);
			if (data.getContentAsString().equals(originalText))
				setAreaText("");
			layoutPreviousNodes();
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		}
	}
	
	/**
	 * Overridden to lay out the annotations.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		setAreaText("");
		setNodesTitle();
		add(buildAreaPane());
		//Fill area
		//Retrieve the latest annotation for the currently logged in user.
		Map<Long, List> annotations = model.getTextualAnnotationByOwner();
		long userID = MetadataViewerAgent.getUserDetails().getId();
		List l = annotations.get(userID);
		if (l != null && l.size() > 0 && originalText == null) {
			TextualAnnotationData data = (TextualAnnotationData) l.get(0);
			setAreaText(data.getText());
			originalText = area.getText();
		}
		if (!hasPreviousTextualAnnotations()) return;
		add(previousTree);
		layoutPreviousNodes();
		switch (layoutIndex) {
			case DATE_VIEW:
				previousPane.add(datePane, Component.LEFT_ALIGNMENT);
				break;
			case USER_VIEW:
				previousPane.add(userPane, Component.LEFT_ALIGNMENT);
		}
		previousPane.add(UIUtilities.buildComponentPanelRight(clearButton), 
				Component.RIGHT_ALIGNMENT);
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }

	/**
	 * Returns the collection of annotations to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{
		List<AnnotationData> l = getAnnotationToSave();
		Iterator<AnnotationData> i = toRemove.iterator();
		while (i.hasNext())
			l.add(i.next());
		return l;
	}

	/**
	 * Returns the collection of annotations to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		String text = area.getText();
		if (text == null) return l;
		text = text.trim();
		if (text.length() == 0) return l;
		if (text.equals(originalText)) return l;
		l.add(new TextualAnnotationData(text));
		return l;
	}
	
	/**
	 * Returns <code>true</code> if we have textual annotation to save
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (model.getRefObject() instanceof TagAnnotationData) return false;
		List<AnnotationData> l = getAnnotationToRemove();
		if (l.size() > 0) return true;
		String text = area.getText();
		if (text == null) return false;
		text = text.trim();
		if (text.length() == 0) return false;
		if (text.equals(originalText)) return false;
		return true;
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{ 
		removeAll();
		toRemove.clear();
		setAreaText("");
		originalText = null;
		initializePreviousComponent();
		setNodesTitle();
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		toRemove.clear();
		setAreaText("");
		originalText = null;
	}
	
	/**
	 * Orders the previous annotation either by date or by user.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLEAR:
				deleteAllAnnotations();
				break;
			case USER_VIEW:
			case DATE_VIEW:
				layoutIndex = index;
				layoutPreviousNodes();
		}
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
