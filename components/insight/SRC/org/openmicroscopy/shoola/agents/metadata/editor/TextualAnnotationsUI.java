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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
//import info.clearthought.layout.TableLayout; 

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.SeparatorOneLineBorder;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import pojos.AnnotationData;
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
	implements ActionListener, DocumentListener, FocusListener
{
    
	/** The length of the text before hiding the comment. */
	private static final int	MAX_LENGTH_TEXT = 200;
	
	/** The default description. */
    private static final String	DEFAULT_TEXT_COMMENT = "Comments";
    
	/** The title associated to this component. */
	private static final String TITLE = "Comments ";
	
	/** Action id to hide the previous comments. */
	private static final int	HIDE = 2;
	
	/** Action id to show the previous comments. */
	private static final int	MORE = 3;
	
	/** Reference to the control. */
	private EditorControl 		controller;
	
	/**
	 * Area displaying the latest textual annotation made by 
	 * the currently logged in user if any. 
	 */
	private OMEWikiComponent	commentArea;
	
	/** Display the other comments. */
	private JPanel				moreComponent;
	
	/** Hide the other comments. */
	private JPanel				hideComponent;
	
	/** Display comments between the first and last comments. */
	private JButton				inBetweenComponent;

	/** The text set in the {@link #commentArea}. */
	private String				originalText;
	
	/** Component hosting the previous comments. */
	private JScrollPane			previousComments;
	
	/** Flag indicating to build the UI once. */
	private boolean 			init;
	
	/** Flag indicating that the comments added by other users are visible. */
	private boolean				expanded;
	
	/** Flag indicating that the latest comment was displayed or not. */
	private boolean				partial;
	
	/** The constraints used to lay out the components. */
	private GridBagConstraints constraints;
	
	/** Flag indicating that the {@link #expand} value has been set.*/
	private boolean set;

	/** The collection of annotation to display.*/
	private List annotationToDisplay;
	
	/** The collection of annotations to remove.*/
	private List annotationToRemove;
	
	/**
	 * Builds and lays out the component hosting all previous annotations.
	 * 
	 * @return See above.
	 */
	private JPanel displayAllPreviousComments()
	{
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		List list = annotationToDisplay;
		if (list != null) {
			Iterator i = list.iterator();
			TextualAnnotationData data;
			TextualAnnotationComponent comp;
			int index = 0;
			while (i.hasNext()) {
				data = (TextualAnnotationData) i.next();
				comp = new TextualAnnotationComponent(model, data);
				comp.addPropertyChangeListener(controller);
				if (index%2 == 0) 
					comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_EVEN);
				else comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_ODD);
				p.add(comp);
				index++;
			}
		}
		return p;
	}
	
	/** 
	 * Builds and lays out the component hosting the first and last previous 
	 * annotations.
	 * 
	 * @return See above.
	 */
	private JPanel displayPartialPreviousComments()
	{
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		List list = annotationToDisplay;
		if (list == null || list.size() == 0) return p;
		
		//at least one.
		TextualAnnotationData data = (TextualAnnotationData) list.get(0);
		TextualAnnotationComponent 
			comp = new TextualAnnotationComponent(model, data);
		comp.addPropertyChangeListener(controller);
		comp.setAreaColor(UIUtilities.BACKGROUND_COLOUR_EVEN);
		p.add(comp);
		
		int n = list.size();
		if (n == 1) return p;
		Color c = UIUtilities.BACKGROUND_COLOUR_ODD;
		if (n > 2) {
			JPanel lp = UIUtilities.buildComponentPanel(inBetweenComponent, 0, 
					0);
			inBetweenComponent.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			lp.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			p.add(lp);
			c = UIUtilities.BACKGROUND_COLOUR_EVEN;
		}
		data = (TextualAnnotationData) list.get(n-1);
		comp = new TextualAnnotationComponent(model, data);
		comp.addPropertyChangeListener(controller);
		comp.setAreaColor(c);
		p.add(comp);
		return p;
	}

	/** Initializes the components. */
	private void initComponents()
	{
		set = false;
		JButton moreButton = new JButton("more");
		moreButton.setBorder(null);
		UIUtilities.unifiedButtonLookAndFeel(moreButton);
		moreButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		moreButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		moreButton.setToolTipText("Display previous comments.");
		moreButton.addActionListener(this);
		moreButton.setActionCommand(""+MORE);
		moreComponent = UIUtilities.buildComponentPanel(moreButton, 0, 0);
		moreComponent.setBackground(UIUtilities.BACKGROUND_COLOR);

		JButton hideButton = new JButton("less");
		UIUtilities.unifiedButtonLookAndFeel(hideButton);
		hideButton.setBorder(null);
		hideButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		hideButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		hideButton.setToolTipText("Hide previous comments.");
		hideButton.addActionListener(this);
		hideButton.setActionCommand(""+HIDE);
		hideComponent = UIUtilities.buildComponentPanel(hideButton, 0, 0);
		hideComponent.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		inBetweenComponent = new JButton("...");
		UIUtilities.unifiedButtonLookAndFeel(inBetweenComponent);
		inBetweenComponent.setBorder(null);
		inBetweenComponent.setBackground(UIUtilities.BACKGROUND_COLOR);
		inBetweenComponent.setForeground(UIUtilities.HYPERLINK_COLOR);
		inBetweenComponent.setToolTipText("Display all comments.");
		inBetweenComponent.addActionListener(this);
		inBetweenComponent.setActionCommand(""+MORE);
		
		commentArea = new OMEWikiComponent(false);
		commentArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		commentArea.addPropertyChangeListener(controller);
		originalText = DEFAULT_TEXT_COMMENT;
		commentArea.setDefaultText(originalText);
		commentArea.setText(originalText);
		//commentArea.setBackground(UIUtilities.BACKGROUND_COLOR);
		commentArea.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		commentArea.setComponentBorder(EDIT_BORDER);
		
		previousComments = new JScrollPane();
		previousComments.setBorder(null);
		setBorder(new SeparatorOneLineBorder());
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/**
	 * Sets the text of the {@link #commentArea}.
	 * 
	 * @param text 			The value to set.
	 * @param addDefault 	Pass <code>true</code> to set the default text,
	 * 						<code>false</code> otherwise.
	 */
	private void setAreaText(String text, boolean addDefault)
	{
		commentArea.removeDocumentListener(this);
		commentArea.setText(text);
		if (addDefault) commentArea.setDefaultText(text);
		commentArea.addDocumentListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
    	if (!model.isAnnotationLoaded()) return;
		JScrollPane pane = new JScrollPane(commentArea);
		Dimension d = pane.getPreferredSize();
		pane.getViewport().setPreferredSize(new Dimension(d.width, 60));
    	pane.setBorder(null);
    	JLabel l = new JLabel();
    	
		//Font f = l.getFont();
		//l = UIUtilities.setTextFont("", Font.BOLD, f.getSize()-1);
		l.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
		setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		constraints.weightx = 1.0;  
		add(l, constraints);
		constraints.gridy++;
		add(pane, constraints);
	}

	/** Hides the previous comments. */
	private void hidePreviousComments()
	{
		List l = annotationToDisplay;
		if (partial) {
			if (l != null && l.size() > 2) {
				remove(hideComponent);
				constraints.gridy = 2;
				add(moreComponent, constraints);
			}
			constraints.gridy = 3;
			previousComments.getViewport().add(
					displayPartialPreviousComments());
			add(previousComments, constraints);
		} else {
			remove(hideComponent);
			constraints.gridy = 2;
			add(moreComponent, constraints);
			previousComments.getViewport().removeAll();
		}
		
		revalidate();
		repaint();
	}
	
	/** Lays out the node. */
	private void layoutPreviousComments()
	{
		List l = annotationToDisplay;
		int n = 3;
		if (!partial) n = 1;
		if (l != null && l.size() >= n) {
			remove(moreComponent);
			constraints.gridy = 2;
			add(hideComponent, constraints);
		}
		previousComments.getViewport().removeAll();
		previousComments.getViewport().add(displayAllPreviousComments());
		revalidate();
		repaint();
	}
	
	/**
	 * Displays the annotations.
	 * 
	 * @param list The annotations to display.
	 */
	private void displayAnnotations(List list)
	{
		annotationToDisplay = list;
		boolean hasPrevious = true;
		if (list == null || list.size() == 0) hasPrevious = false;
		if (!hasPrevious) {
			originalText = DEFAULT_TEXT_COMMENT;
			setAreaText(DEFAULT_TEXT_COMMENT, true);
		}
		
		boolean enabled = model.canAnnotate();
		if (enabled && model.isMultiSelection()) {
			enabled = !model.isAcrossGroups();
		}
		commentArea.setEnabled(enabled);
		if (hasPrevious) {
			TextualAnnotationData data = (TextualAnnotationData) list.get(0);
			String text = data.getText();
			if (!set) expanded = text.length() < MAX_LENGTH_TEXT;
			//layout.setRow(3, TableLayout.PREFERRED);
			constraints.gridy = 3;
			add(previousComments, constraints);
			if (expanded) {
				partial = true;
				previousComments.getViewport().add(
						displayPartialPreviousComments());
				if (list.size() > 2) {
					constraints.gridy = 2;
					add(moreComponent, constraints);
				}
			} else {
				partial = false;
				constraints.gridy = 2;
				add(moreComponent, constraints);
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 		Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller 	Reference to the controller. 
	 * 						Mustn't be <code>null</code>.
	 */
	TextualAnnotationsUI(EditorModel model, EditorControl controller)
	{
		super(model);
		this.controller = controller;
		title = TITLE;
		initComponents();
		init = false; 
	}
	
	/**
	 * Removes the textual annotation from the view.
	 * 
	 * @param annotation The annotation to remove.
	 */
	void removeTextualAnnotation(TextualAnnotationData annotation)
	{
		if (annotationToRemove == null) annotationToRemove = new ArrayList();
		annotationToRemove.clear();
		annotationToRemove.add(annotation);
		previousComments.getViewport().removeAll();
		List l = model.getTextualAnnotationsByDate();
		List toKeep = new ArrayList();
		if (l != null) {
			Iterator i = l.iterator();
			Object o;
			TextualAnnotationData data;
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof TextualAnnotationData) {
					data = (TextualAnnotationData) o;
					if (data.getId() != annotation.getId())
						toKeep.add(data);
				}
			}
		}
		displayAnnotations(toKeep);
		revalidate();
		repaint();
		firePropertyChange(EditorControl.SAVE_PROPERTY, 
			Boolean.valueOf(false), Boolean.valueOf(true));
	}
	
	/**
	 * Overridden to lay out the annotations.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		//if (!init) {
			buildGUI();
			init = true;
		//}
		if (model.isMultiSelection()) {
			displayAnnotations(null);
		} else {
			displayAnnotations(model.getTextualAnnotationsByDate());
		}
		revalidate();
		repaint();
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
	protected List<Object> getAnnotationToRemove()
	{
		List<Object> l = new ArrayList<Object>();
		if (annotationToRemove != null)
			l.addAll(annotationToRemove);
		return l;
	}

	/**
	 * Returns the collection of annotations to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		String text = commentArea.getText();
		if (text == null) return l;
		text = text.trim();
		if (text.length() == 0) return l;
		if (text.equals(originalText) || text.equals(DEFAULT_TEXT_COMMENT)) 
			return l;
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
		String text = commentArea.getText();
		if (text == null) return false;
		text = text.trim();
		if (text.length() == 0 && originalText != null) return true;
		if (originalText.equals(text) || text.equals(DEFAULT_TEXT_COMMENT)) 
			return false;
		return true;
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() {}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData(Object)
	 */
	protected void clearData(Object oldObject)
	{
		if (annotationToRemove != null) annotationToRemove.clear();
		annotationToDisplay = null;
		if (previousComments != null)
			previousComments.getViewport().removeAll();
		originalText = DEFAULT_TEXT_COMMENT;
		setAreaText(DEFAULT_TEXT_COMMENT, true);
	}
	
	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle()
	{
		title = TITLE;
	}
	
	/**
	 * Orders the previous annotation either by date or by user.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case MORE:
				set = true;
				expanded = true;
				layoutPreviousComments();
				break;
			case HIDE:
				set = true;
				expanded = false;
				hidePreviousComments();
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
	 * Resets the default text of the text fields if <code>null</code> or
	 * length <code>0</code>.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		Object src = e.getSource();
		String text;
		if (src == commentArea) {
			text = commentArea.getText();
			boolean b = false;
			if (text == null || text.length() == 0) {
				text = DEFAULT_TEXT_COMMENT;
				b = true;
			}
			text = text.trim();
			originalText = text;
			setAreaText(DEFAULT_TEXT_COMMENT, b);
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

	/**
	 * Required by the {@link FocusListener} I/F but no-op implementation 
	 * in our case.
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}
	
}
