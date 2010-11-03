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
import java.awt.Font;
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
import info.clearthought.layout.TableLayout; 

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
		List list = model.getTextualAnnotationsByDate();
		if (list != null) {
			Iterator i = list.iterator();
			TextualAnnotationData data;
			TextualAnnotationComponent comp;
			int index = 0;
			while (i.hasNext()) {
				data = (TextualAnnotationData) i.next();
				comp = new TextualAnnotationComponent(model, data);
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
		List list = model.getTextualAnnotationsByDate();
		if (list == null || list.size() == 0) return p;
		
		//at least one.
		TextualAnnotationData data = (TextualAnnotationData) list.get(0);
		TextualAnnotationComponent 
			comp = new TextualAnnotationComponent(model, data);
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
		comp.setAreaColor(c);
		p.add(comp);
		return p;
	}
	
	/**
	 * Returns <code>true</code> if the data object has been 
	 * previously annotated, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean hasPreviousTextualAnnotations()
	{
		List l = model.getTextualAnnotationsByDate();
		if (l == null || l.size() == 0) return false;
		return true;
	}

	/** Initializes the components. */
	private void initComponents()
	{
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

		JButton hideButton = new JButton("hide");
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
		commentArea.setBackground(UIUtilities.BACKGROUND_COLOR);
		commentArea.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		commentArea.setComponentBorder(EDIT_BORDER);
		
		previousComments = new JScrollPane();
		previousComments.setBorder(null);
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
    	setBorder(new SeparatorOneLineBorder());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED, 
			60, 0, 0}};
    	setLayout(new TableLayout(size));
    	JScrollPane pane = new JScrollPane(commentArea);
    	pane.setBorder(null);
    	JLabel l = new JLabel();
    	
		Font f = l.getFont();
		l = UIUtilities.setTextFont("comment", Font.BOLD, f.getSize()-1);
		l.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		add(l, "0, 0");
    	add(pane, "0, 1");
	}

	/** Hides the previous comments. */
	private void hidePreviousComments()
	{
		TableLayout layout = (TableLayout) getLayout();
		layout.setRow(3, TableLayout.PREFERRED);
		//remove(hideComponent);
		List l = model.getTextualAnnotationsByDate();
		if (partial) {
			if (l != null && l.size() > 2) {
				remove(hideComponent);
				layout.setRow(2, TableLayout.PREFERRED);
				add(moreComponent, "0, 2");
			}
			previousComments.getViewport().add(displayPartialPreviousComments());
			add(previousComments, "0, 3");
		} else {
			remove(hideComponent);
			layout.setRow(2, TableLayout.PREFERRED);
			add(moreComponent, "0, 2");
			previousComments.getViewport().removeAll();
		}
		
		revalidate();
		repaint();
	}
	
	/** Lays out the node. */
	private void layoutPreviousComments()
	{
		TableLayout layout = (TableLayout) getLayout();
		List l = model.getTextualAnnotationsByDate();
		int n = 3;
		if (!partial) n = 1;
		if (l != null && l.size() >= n) {
			remove(moreComponent);
			layout.setRow(2, TableLayout.PREFERRED);
			add(hideComponent, "0, 2");
		}
		previousComments.getViewport().removeAll();
		previousComments.getViewport().add(displayAllPreviousComments());
		revalidate();
		repaint();
		
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
	 * Overridden to lay out the annotations.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		
		if (!hasPreviousTextualAnnotations()) {
			originalText = DEFAULT_TEXT_COMMENT;
			setAreaText(DEFAULT_TEXT_COMMENT, true);
		}
		
		commentArea.setEnabled(model.isWritable());
		TableLayout layout = (TableLayout) getLayout();
		layout.setRow(2, 0);
		layout.setRow(3, 0);
		if (hasPreviousTextualAnnotations()) {
			List list = model.getTextualAnnotationsByDate();
			TextualAnnotationData data = (TextualAnnotationData) list.get(0);
			String text = data.getText();
			expanded = text.length() < MAX_LENGTH_TEXT;
			/*
			List l = model.getTextualAnnotationsByDate();
			if (l.size() > 2) {
				layout.setRow(2, TableLayout.PREFERRED);
				add(moreComponent, "0, 2");
			}
			
			if (expanded) {
				previousComments.getViewport().add(
						displayAllPreviousComments());
			} else {
				previousComments.getViewport().add(
						displayPartialPreviousComments());
			}
			*/
			layout.setRow(3, TableLayout.PREFERRED);
			add(previousComments, "0, 3");
			if (expanded) {
				partial = true;
				previousComments.getViewport().add(
						displayPartialPreviousComments());
				if (list.size() > 2) {
					layout.setRow(2, TableLayout.PREFERRED);
					add(moreComponent, "0, 2");
				}
			} else {
				partial = false;
				layout.setRow(2, TableLayout.PREFERRED);
				add(moreComponent, "0, 2");
				//hidePreviousComments();
			}
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
	protected List<AnnotationData> getAnnotationToRemove()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		if (originalText != null && originalText.length() > 0) {
			String text = commentArea.getText();
			if (text != null) {
				text = text.trim();
				if (text.length() == 0) {
					TextualAnnotationData data = model.getLastUserAnnotation();
					if (data != null) l.add(data);
				}
			}
		}
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
	protected void clearDisplay() 
	{ 
		if (previousComments != null)
			previousComments.getViewport().removeAll();
		originalText = DEFAULT_TEXT_COMMENT;
		setAreaText(DEFAULT_TEXT_COMMENT, true);
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		clearDisplay();
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
				expanded = true;
				layoutPreviousComments();
				break;
			case HIDE:
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
