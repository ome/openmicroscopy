/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TextualAnnotationsUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.SeparatorOneLineBorder;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import pojos.AnnotationData;
import pojos.TextualAnnotationData;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;


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
	
	/** The default description. */
	private static final String	DEFAULT_TEXT_COMMENT = "Add comment";
        
	/** The title associated to this component. */
	private static final String TITLE = "Comments ";
	
	/** Action id to save the comment */
	private static final int ADD_COMMENT = 4;
	
	/** Reference to the control. */
	private EditorControl 		controller;
	
	/**
	 * Area displaying the latest textual annotation made by 
	 * the currently logged in user if any. 
	 */
	private OMEWikiComponent	commentArea;

	/** The text set in the {@link #commentArea}. */
	private String				originalText;
	
	/** The constraints used to lay out the components. */
	private GridBagConstraints constraints;
	
	/** Flag indicating that the {@link #expand} value has been set.*/
	private boolean set;

	/** The collection of annotation to display.*/
	private List annotationToDisplay;
	
	/** The collection of annotations to remove.*/
	private List annotationToRemove;
	
	/** Scrollpane hosting the comment text field */
	private JScrollPane pane;
	
	/** The add comment button */
	private JButton addButton;

	/** Initializes the components. */
	private void initComponents()
	{
		set = false;

		commentArea = new OMEWikiComponent(false);
		commentArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		commentArea.addPropertyChangeListener(controller);
		originalText = DEFAULT_TEXT_COMMENT;
		commentArea.setDefaultText(originalText);
		commentArea.setText(originalText);
		//commentArea.setBackground(UIUtilities.BACKGROUND_COLOR);
		commentArea.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		commentArea.setComponentBorder(EDIT_BORDER);
		commentArea.addFocusListener(new FocusListener() {
                    
                    public void focusLost(FocusEvent arg0) {
                        if(StringUtils.isEmpty(commentArea.getText()) || commentArea.getText().equals(DEFAULT_TEXT)) {
                            pane.getViewport().setPreferredSize(null);
                            pane.revalidate();
                            addButton.setVisible(false);
                        }
                    }
                    
                    public void focusGained(FocusEvent arg0) {
                        Dimension d = pane.getPreferredSize();
                        pane.getViewport().setPreferredSize(new Dimension(d.width, 60));
                        pane.revalidate();
                        addButton.setVisible(true);
                    }
                });
		
		setBorder(new SeparatorOneLineBorder());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		
		addButton = new JButton(IconManager.getInstance().getIcon(IconManager.SAVE));
		formatButton(addButton, DEFAULT_TEXT_COMMENT, ADD_COMMENT);
		addButton.setVisible(false);
        addButton.setEnabled(false);
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
		
    	if (!model.isAnnotationLoaded()) 
    		return;
    	
		pane = new JScrollPane(commentArea);
		pane.getViewport().setPreferredSize(null);
    	pane.setBorder(null);
    	
		setLayout(new GridBagLayout());
		
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 0, 2, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1; 
		constraints.weighty = 1; 
		add(pane, constraints);
		
		constraints.gridx = 1;
		constraints.weightx = 0;
		constraints.weighty = 0; 
		constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.SOUTH;
        add(addButton, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0; 
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
	}
	
	/**
	 * Displays the annotations.
	 * 
	 * @param list The annotations to display.
	 */
	private void displayAnnotations(List list)
	{
		annotationToDisplay = list;
		if (CollectionUtils.isEmpty(list)) {
			originalText = DEFAULT_TEXT_COMMENT;
			setAreaText(DEFAULT_TEXT_COMMENT, true);
		}
		
		boolean enabled = model.canAnnotate();
		if (enabled && model.isMultiSelection()) {
			enabled = !model.isAcrossGroups();
		}
		commentArea.setEnabled(enabled);
		
		if (!CollectionUtils.isEmpty(list)) {
			Color c = UIUtilities.BACKGROUND_COLOUR_ODD;
			for(Object obj : annotationToDisplay) {
				TextualAnnotationData data = (TextualAnnotationData) obj;
				TextualAnnotationComponent 
					comp = new TextualAnnotationComponent(model, data);
				comp.addPropertyChangeListener(controller);
				comp.setAreaColor(c);
				add(comp, constraints);
				constraints.gridy++;
				
				if (c == UIUtilities.BACKGROUND_COLOUR_ODD)
					c = UIUtilities.BACKGROUND_COLOUR_EVEN;
				else
					c = UIUtilities.BACKGROUND_COLOUR_ODD;
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
		buildGUI();
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
		originalText = DEFAULT_TEXT_COMMENT;
		setAreaText(DEFAULT_TEXT_COMMENT, true);
		addButton.setEnabled(false);
		addButton.setVisible(false);
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
			case ADD_COMMENT:
			    saveComment();
			    break;
		}
	}
	
	/** Saves the comment */
	private void saveComment() {
	    List<AnnotationData> comments = getAnnotationToSave();
	    model.fireAnnotationSaving(new DataToSave(comments, Collections.emptyList()), null, false);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
	        addButton.setEnabled(hasDataToSave());
	        firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
	        addButton.setEnabled(hasDataToSave());
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
	public void focusGained(FocusEvent e) {
	}
	
        /**
         * Formats the specified button.
         * 
         * @param button
         *            The button to handle.
         * @param text
         *            The tool tip text.
         * @param actionID
         *            The action command id.
         */
        private void formatButton(JButton button, String text, int actionID) {
            button.setOpaque(false);
            UIUtilities.unifiedButtonLookAndFeel(button);
            button.setBackground(UIUtilities.BACKGROUND_COLOR);
            button.setToolTipText(text);
            button.addActionListener(this);
            button.setActionCommand("" + actionID);
        }
	
}
