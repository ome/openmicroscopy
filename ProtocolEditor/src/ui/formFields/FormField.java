/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui.formFields;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.DataFieldObserver;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;
import tree.IDataFieldSelectable;
import tree.edit.EditCopyDefaultValues;
import ui.FormDisplay;
import ui.IModel;
import ui.XMLView;
import ui.components.ImageBorder;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;

// FormField dictates how dataField is displayed as a row in the complete form
// This FormField superclass merely arranges Name and Description (as a toolTip)
// Subclasses have eg TextFields etc

public abstract class FormField extends JPanel implements DataFieldObserver{
	
	IDataFieldObservable dataFieldObs;
	IAttributeSaver dataField;
	
	/**
	 * A reference to the model, for opening files, saving files, etc etc. 
	 * This must be set by the UI container that this FormField panel is displayed in.
	 * Ie. this reference is passed via the UI classes, not via dataField/Tree etc. 
	 */
	IModel model;
	
	// property change listener, property name
	public static final String HAS_FOCUS = "hasFocus";
	
	boolean textChanged = false;
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();
	FocusListener componentFocusListener = new FormFieldComponentFocusListener();
	
	/*
	 * swing components
	 */ 
	JPanel contentsPanel;
	
	Box horizontalBox;
	JButton collapseButton;	
	JLabel nameLabel;
	Icon lockedTemplateIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_ICON);
	Icon lockedAllIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_RED_ICON);
	JButton descriptionButton;
	JLabel descriptionLabel;
	Icon infoIcon;
	Icon wwwIcon;
	JButton defaultButton;		// visible if a default value set for this field
	JButton urlButton;	// used (if url) to open browser 
	Cursor handCursor;
	
	JButton requiredFieldButton;	// doesn't do anything. Just indicates that field is required. 
	Icon requiredIcon;
	Icon requiredWarningIcon;
	
	JButton collapseAllChildrenButton;
	
	Container childContainer;
	
	boolean childrenCollapsed = false; 	// used for root field only - to toggle all collapsed
	
	boolean highlighted = false;
	Color paintedColour = null;
	public static final Color DEFAULT_BACKGROUND = new Color(237, 239, 246);
	
	Border imageBorder;
	Border imageBorderHighlight;
	
	Icon collapsedIcon;
	Icon notCollapsedIcon;
	Icon spacerIcon;		// A blank icon to replace collapsedIcon if there are no children.
	
	// used in Diff (comparing two trees), to get a ref to all components, to colour red if different!
	ArrayList<JComponent> visibleAttributes = new ArrayList<JComponent>();
		
	boolean showDescription = false;	// not saved, just used to toggle
	
	public FormField(IDataFieldObservable dataFieldObs) {
		
		this.dataFieldObs = dataFieldObs;
		this.dataFieldObs.addDataFieldObserver(this);
		
		// save a reference to the datafield as an IAttributeSaver (get and set-Attribute methods)
		if (dataFieldObs instanceof IAttributeSaver) {
			this.dataField = (IAttributeSaver)dataFieldObs;
		} else {
			throw new RuntimeException("FormField(dataField) needs dataField to implement IAttributeSaver");
		}
		
		//System.out.println("FormField Constructor: name is " + dataField.getAttribute(DataFieldConstants.ELEMENT_NAME));
		
		// build the formField panel
		Border eb = BorderFactory.createEmptyBorder(2, 1, 2, 1);
		
		imageBorder = ImageBorder.getImageBorder();
		imageBorderHighlight = ImageBorder.getImageBorderHighLight();
		
		this.setBorder(null);
		this.setBackground(null);
		this.setLayout(new BorderLayout());
		//this.setFocusable(true);
		this.addMouseListener(new FormPanelMouseListener());
		
		horizontalBox = Box.createHorizontalBox();
		
		boolean subStepsCollapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED);
		
		/*
		 * A button to expand or collapse the visibility of this field's children.
		 * This button is only visible if this field has some children. 
		 */
		collapseButton = new JButton();
		//collapseButton.setBorder(BorderFactory.createLineBorder(Color.red));
		collapseButton.setFocusable(false);
		//collapseButton.setVisible(false);	// only made visible if hasChildren
		collapseButton.setBackground(null);
		collapseButton.setOpaque(true);
		//unifiedButtonLookAndFeel(collapseButton);
		collapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.COLLAPSED_ICON);
		notCollapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.NOT_COLLAPSED_ICON);
		spacerIcon = ImageFactory.getInstance().getIcon(ImageFactory.SPACER_ICON);
		if (subStepsCollapsed) collapseButton.setIcon(collapsedIcon);
		else collapseButton.setIcon(notCollapsedIcon);
		collapseButton.setToolTipText("Collapse or Expand sub-steps");
		collapseButton.setBorder(new EmptyBorder(2,2,2,2));
		//collapseButton.setOpaque(true);
		collapseButton.addActionListener(new CollapseListener());
		collapseButton.addMouseListener(new FormPanelMouseListener());
		/*
		JPanel collapseButtonContainer = new JPanel(new BorderLayout());
		collapseButtonContainer.setBackground(null);
		collapseButtonContainer.add(collapseButton, BorderLayout.WEST);
		*/
		
		/*
		 * A label to display the name of the field.
		 * This is the only component that is always visible (but could be "")
		 */
		nameLabel = new JLabel();
		nameLabel.setBackground(null);
		nameLabel.setOpaque(false);
		nameLabel.addMouseListener(new FormPanelMouseListener());
		
		/*
		 * A description label displays description below the field. Visibility false unless 
		 * descriptionButton is clicked.
		 */
		descriptionLabel = new JLabel();
		descriptionLabel.setBackground(null);
		visibleAttributes.add(descriptionLabel);
		infoIcon = ImageFactory.getInstance().getIcon(ImageFactory.INFO_ICON);
		descriptionButton = new JButton(infoIcon);
		//unifiedButtonLookAndFeel(descriptionButton);
		descriptionButton.setFocusable(false); // so it is not selected by tabbing
		descriptionButton.addActionListener(new ToggleDescriptionListener());
		descriptionButton.setBackground(null);
		descriptionButton.setBorder(eb);
		descriptionButton.setVisible(false);	// only made visible if description exists.
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION)); 	// will update description label
		
		
		/*
		 * A url-link button, that is only visible if a URL has been set.
		 */
		wwwIcon = ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON);
		urlButton = new JButton(wwwIcon);
		urlButton.setFocusable(false); // so it is not selected by tabbing
		unifiedButtonLookAndFeel(urlButton);
		urlButton.addActionListener(new URLclickListener());
		urlButton.setBackground(null);
		handCursor = new Cursor(Cursor.HAND_CURSOR);
		urlButton.setCursor(handCursor);
		urlButton.setBorder(eb);
		urlButton.setVisible(false);	// only made visible if url exists.
		
		
		/*
		 * Default icon/button. Shows if a default is set. 
		 * Tool-tip shows what the default is. 
		 * Clicking the button loads defaults for this field,
		 * Disabled if field is fully locked.
		 */
		Icon defaultIcon = ImageFactory.getInstance().getIcon
			(ImageFactory.DEFAULT_ICON);
		defaultButton = new JButton(defaultIcon);
		defaultButton.setFocusable(false);
		defaultButton.setBackground(null);
		defaultButton.setBorder(eb);
		defaultButton.setVisible(false);	// only visible if default set. 
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadDefaultValue();
			}
		});
		
		/*
		 * Required field button. Doesn't do anything, just indicates that the field is required.
		 */
		requiredIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_ASTERISK_ICON);
		requiredWarningIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_ASTERISK_WARNING_ICON);
		requiredFieldButton = new JButton(requiredIcon);
		requiredFieldButton.setFocusable(false);
		requiredFieldButton.setBackground(null);
		requiredFieldButton.setBorder(eb);
		requiredFieldButton.setVisible(false);	// only visible if requiredField = true;
		
		
		/*
		 * button to allow collapsing or expanding all children
		 * This is only visible for the root node. 
		 */
		collapseAllChildrenButton = new JButton("Collapse/Expand All", notCollapsedIcon);
		collapseAllChildrenButton.setToolTipText("Collapse or Expand every field in this document");
		collapseAllChildrenButton.setBackground(null);
		collapseAllChildrenButton.addActionListener(new CollapseChildrenListener());
		collapseAllChildrenButton.setVisible(false);
		// visibility set by refreshRootField(boolean);
		
		/*
		 * complex layout required to limit the size of nameLabel (expands with html content)
		 * horizontalFrameBox holds collapseButton and then the BorderLayout JPanel
		 * nameLabel is in the WEST of this, then the horizontalBox for all other items is in the CENTER
		 */
		contentsPanel = new JPanel(new BorderLayout());
		
		horizontalBox.add(descriptionButton);
		horizontalBox.add(urlButton);
		horizontalBox.add(defaultButton);
		horizontalBox.add(requiredFieldButton);
		horizontalBox.add(collapseAllChildrenButton);
		horizontalBox.add(Box.createHorizontalStrut(10));
		
		contentsPanel.add(nameLabel, BorderLayout.WEST);
		contentsPanel.add(horizontalBox, BorderLayout.CENTER);
		contentsPanel.add(descriptionLabel, BorderLayout.SOUTH);
		
		contentsPanel.setBackground(Color.red);
		contentsPanel.setBorder(imageBorder);
		
		this.add(collapseButton, BorderLayout.WEST);
		this.add(contentsPanel, BorderLayout.CENTER);

		// refresh the current state
		// can't call dataFieldUpdated() because subclasses override it, and try to 
		// update components that have not been instantiated until after their constructors. 
		setNameText(addHtmlTagsForNameLabel(dataField.getAttribute(DataFieldConstants.ELEMENT_NAME)));
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		setURL(dataField.getAttribute(DataFieldConstants.URL));
		
		refreshChildDisplayOrientation();
		
		refreshBackgroundColour();
		
		refreshDefaultValue();
		
		//this.add(horizontalFrameBox, BorderLayout.NORTH);
		//this.add(descriptionLabel, BorderLayout.WEST);
		
	}
	
	// called by dataField to notify observers that something has changed.
	public void dataFieldUpdated() {
		setNameText(addHtmlTagsForNameLabel(dataField.getAttribute(DataFieldConstants.ELEMENT_NAME)));
		setDescriptionText(dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		setURL(dataField.getAttribute(DataFieldConstants.URL));
		
		refreshChildDisplayOrientation();
		
		refreshBackgroundColour();
		
		refreshLockedStatus();	// also does required-Field status. 
		
		refreshDefaultValue();
	}
	
	/**
	 * This method checks to see if the current field is locked: 
	 * Then it passes the locked status to enableEditing().
	 * Also does requiredField status. 
	 */
	public void refreshLockedStatus() {
		
		String lockedLevel = ((DataField)dataField).getLockedLevel();
		/*
		 * Allow editing unless locked level is fully locked
		 */
		if (lockedLevel == null) {
			enableEditing(true);
			defaultButton.setEnabled(true);
		} else {
			boolean fullyLocked = 
				lockedLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES);
			
			defaultButton.setEnabled(!fullyLocked);
			enableEditing(!fullyLocked);
		}
		
		/*
		 * Show the required field icon if requiredField attribute = true. 
		 */
		requiredFieldButton.setVisible(
				dataField.isAttributeTrue(DataFieldConstants.REQUIRED_FIELD));
		requiredFieldButton.setIcon(isFieldFilled() ? requiredIcon : 
				requiredWarningIcon);
		requiredFieldButton.setToolTipText(isFieldFilled() ? 
				"Required Field" : "Required Field Not Filled");
	}
	
	/**
	 * Checks to see whether a default value exists for this field.
	 * If so, the default button becomes visible, with tool-tip
	 * displaying the default value;
	 */
	public void refreshDefaultValue() {
		
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		
		defaultButton.setVisible(defaultValue != null);
		
		if (defaultValue != null)
			defaultButton.setToolTipText("Default: " + defaultValue);
	
		else 
			defaultButton.setToolTipText(null);
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * This FormField superclass has no editable components, but subclasses
	 * should override this method for their additional components. 
	 * 
	 * @param enabled
	 */
	public abstract void enableEditing(boolean enabled);
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * For fields that have a single 'value', this method will return true if 
	 * that value is filled (not null). 
	 * For fields with several attributes, it depends on what is considered 'filled'.
	 * This method can be used to check that 'Obligatory Fields' have been completed 
	 * when a file is saved. 
	 * Subclasses should override this method.
	 * 
	 * @return	True if the field has been filled out by user. Required values are not null. 
	 */
	public abstract boolean isFieldFilled();
	
	
	public void refreshChildDisplayOrientation() {
		boolean childrenHorizontal = dataField.isAttributeTrue(DataFieldConstants.DISPLAY_CHILDREN_HORIZONTALLY);
		
		if (childContainer instanceof JToolBar) {
			((JToolBar)childContainer).setOrientation(childrenHorizontal ? JToolBar.HORIZONTAL : JToolBar.VERTICAL);
		}
	}
	
	// these methods called when user updates the fieldEditor panel
	public void setNameText(String name) {
		nameLabel.setText(name);
		
		String lockedLevel = dataField.getAttribute(DataFieldConstants.LOCK_LEVEL);
		if (lockedLevel == null) {
			nameLabel.setIcon(null);
			nameLabel.setToolTipText(null);
		} else {
			String toolTipText = "";
			Icon newIcon = lockedTemplateIcon;
			
			if (lockedLevel.equals(DataFieldConstants.LOCKED_TEMPLATE)) {
				toolTipText += "Template Locked";
			} else if (lockedLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES)) {
				toolTipText += "Field Locked";
				newIcon = lockedAllIcon;
			}
			
			String user = dataField.getAttribute(DataFieldConstants.LOCKED_FIELD_USER_NAME);
			if (user != null)
			toolTipText = toolTipText + " by " + user;
			
			String lockedTimeUTC = dataField.getAttribute(DataFieldConstants.LOCKED_FIELD_UTC);
			if (lockedTimeUTC != null) {
				Calendar lockedTime = new GregorianCalendar();
				lockedTime.setTimeInMillis(new Long(lockedTimeUTC));
				SimpleDateFormat time = new SimpleDateFormat("HH:mm 'on' EEE, MMM d, yyyy");
				toolTipText = toolTipText + " at " + time.format(lockedTime.getTime());
			}
			
			nameLabel.setToolTipText(toolTipText);
			nameLabel.setIcon(newIcon);
		}
	}
	
	public void setDescriptionText(String description) {
		if ((description != null) && (description.trim().length() > 0)) {
			String htmlDescription = "<html><div style='width:200px; padding-left:30px;'>" + description + "</div></html>";
			descriptionButton.setToolTipText(htmlDescription);
			descriptionButton.setVisible(true);
			descriptionLabel.setVisible(showDescription);
			descriptionLabel.setFont(XMLView.FONT_TINY);
			descriptionLabel.setText(htmlDescription);
		}
		else
		{
			descriptionButton.setToolTipText(null);
			descriptionButton.setVisible(false);
			descriptionLabel.setText(null);
			descriptionLabel.setVisible(false);
		}
	}
	public void setURL(String url) {
		if (url == null) {
			urlButton.setVisible(false);
			return;
		}
		if (url.length() > 0) {
			urlButton.setVisible(true);
			urlButton.setToolTipText(url);
		} else {
			urlButton.setVisible(false);
		}
	}
	
	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their values under a different attributes (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public abstract String[] getValueAttributes();
	
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		highlighted = highlight;
		refreshHighlighted();
	}
	private void refreshHighlighted() {
		if (highlighted) { 
			contentsPanel.setBackground(XMLView.BLUE_HIGHLIGHT);
			contentsPanel.setBorder(imageBorderHighlight);
		}
		else {
			contentsPanel.setBackground((paintedColour == null) ? DEFAULT_BACKGROUND : paintedColour);
			contentsPanel.setBorder(imageBorder);
		}
	}
	
	/**
	 * This method takes the value in the DataFieldConstants.DEFAULT attribute,
	 * and copies it into the attribute specified by 
	 * EditCopyDefaultValues.getValueAttributeForLoadingDefault(dataField)
	 */
	public void loadDefaultValue() {
		String valueAttribute = EditCopyDefaultValues.
				getValueAttributeForLoadingDefault(dataField);
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		
		dataField.setAttribute(valueAttribute, defaultValue, true);
	}
	
	public class FormPanelMouseListener implements MouseListener {
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mousePressed(MouseEvent event) {}
		public void mouseReleased(MouseEvent event) {}
		
		public void mouseClicked(MouseEvent event) {
			
			int clickType = event.getModifiers();
			if (clickType == XMLView.SHIFT_CLICK) {
				panelClicked(false);
			} else
				panelClicked(true);
		}
	}	
	
	// add this to every focusable component (button, field etc within the panel)
	public class FormFieldComponentFocusListener implements FocusListener {
		// when the component gets focus, the field is selected 
		// - this re-applies focus to the component - recursion is prevented by 
		// checking that this field is not already highlighted. 
		public void focusGained(FocusEvent e) {
			if (!highlighted)
				panelClicked(true);
		}
		public void focusLost(FocusEvent e) {}
	}
	// add this to every non-focusable component (Panels etc)
	public class FocusGainedPropertyChangedListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals(FormField.HAS_FOCUS)) {
				panelClicked(true);
			}
		}
	}
	
	public class URLclickListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			BareBonesBrowserLaunch.openURL(dataField.getAttribute(DataFieldConstants.URL));
		}
	}
	
	public class ToggleDescriptionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (showDescription) showDescription = false;
			else showDescription = true;
			
			descriptionLabel.setVisible(showDescription);
		}
	}
	
	public void panelClicked(boolean clearOthers) {
		if (dataFieldObs instanceof IDataFieldSelectable){
			//System.out.println("FormField panelClicked() name:" + 
			//		dataField.getAttribute(DataFieldConstants.ELEMENT_NAME) + " clearOthers = " + clearOthers);
			((IDataFieldSelectable)dataFieldObs).dataFieldSelected(clearOthers);
		}
	}
	
	
	
	public class CollapseListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			boolean collapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED); 
			
			// toggle collapsed state
			setSubStepsCollapsed(!collapsed);
		}
	}
	
	public void setSubStepsCollapsed(boolean collapsed) {
		if (hasChildren()) {
			dataField.setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, Boolean.toString(collapsed), false);
			refreshTitleCollapsed();
		}
	}
	
	// called when formField panel is loaded into UI, so that it is displayed correctly.
	// UI is rebuilt when hierarchy structure changes. 
	// also called when user collapses or expands sub-steps.
	public void refreshTitleCollapsed() {
		
		boolean collapsed = subStepsCollapsed();
		
		//System.out.println("FormField   refreshTitleCollapsed()  collapsed=" + collapsed);
		
		if (hasChildren()) {
			collapseButton.setIcon(collapsed ? collapsedIcon : notCollapsedIcon);
		} else {
			collapseButton.setIcon(spacerIcon);
			// this is only needed when building UI (superfluous when simply collapsing)
			// if this node has just lost it's children, need to un-collapse it (if collapsed)
			if (dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED)) {
				dataField.setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, DataFieldConstants.FALSE, false);
			}
		}
		
		showChildren(!collapsed);
		
		refreshChildDisplayOrientation();
	}
	
	public boolean subStepsCollapsed() {
		// default for Custom XML fields is false
		if ((dataField.getAttribute(DataFieldConstants.INPUT_TYPE).equals(DataFieldConstants.CUSTOM))
				&& (dataField.getAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED) == null)) {
			dataField.setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, DataFieldConstants.TRUE, false);
			return true;
		}
		
		return dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED);
	}

	public boolean hasChildren() {
		return ((DataField)dataField).hasChildren();
	}
	
	public void refreshRootField(boolean rootField) {
		// only show this button for the root FormField (ie if parent == null)
		collapseAllChildrenButton.setVisible(rootField);
	}
	
	/**
	 * Sets a reference to the UI container that displays all the children of this field. 
	 * This is subsequently used to hide or show all the child fields, when this
	 * field is collapsed or expanded. 
	 * 
	 * @param container
	 */
	public void setChildContainer(Container container) {
		childContainer = container;
	}
	public Container getChildContainer() {
		return childContainer;
	}
	
	/**
	 * This sets a reference to the model of this application. 
	 * It will be used by the UI component that contains this FormField panel (FormDisplay).
	 * 
	 * @param model
	 */
	public void setModel(IModel model) {
		this.model = model;
	}
	
	// Lazy loading of child panels:
	// only load children when they need to be displayed.
	public void showChildren(boolean visible) {
		if (!hasChildren()) 
			return;
		// check visible and that children have not already been loaded
		if (childContainer == null) 
			return;
		if (visible && childContainer.getComponentCount() == 0) {
			ArrayList<DataFieldNode> children = ((DataField)dataField).getNode().getChildren();
			FormDisplay.showChildren(children, childContainer, model);
		}
		childContainer.setVisible(visible);
	}
	
	public class CollapseChildrenListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			// toggle collapsed state of node and it's children
			childrenCollapsed = !childrenCollapsed;
			
			if (getParent() instanceof FormFieldContainer) {
				((FormFieldContainer)getParent()).collapseAllFormFieldChildren(childrenCollapsed);
			}
			
			// expand this node
			setSubStepsCollapsed(false);
			
			collapseAllChildrenButton.setIcon(childrenCollapsed ? collapsedIcon : notCollapsedIcon);
		}	
	}
	
	public static void unifiedButtonLookAndFeel(AbstractButton b)
    {
        b.setMargin(new Insets(0, 2, 0, 3));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }
	
	private void refreshBackgroundColour() {
		paintedColour = getColorFromString(dataField.getAttribute(DataFieldConstants.BACKGROUND_COLOUR));
		
		refreshHighlighted();
	}
	
	// used to convert a stored string Colour attribute to a Color
	public static Color getColorFromString(String colourAttribute) {
		if (colourAttribute == null) 
			return null;
		
		try{
			String[] rgb = colourAttribute.split(":");
			int red = Integer.parseInt(rgb[0]);
			int green = Integer.parseInt(rgb[1]);
			int blue = Integer.parseInt(rgb[2]);
			return new Color(red,green,blue);
		} catch (Exception ex) {
			return null;
		}
	}
	
	
	/*
	 * returns the pixel-distance this panel-bottom from the top of FormDisplay.
	 * used to control scrolling to display this field within the ScrollPanel that contains FormDisplay
	 */ 
	public int getHeightOfPanelBottom() {
		if (isThisRootField()) {
			//System.out.println("FormField getHeightOfPanelBottom ROOT = " + getHeight());
			return this.getHeight();
		// if panel has a parent, this panel will be within a box, below the parent
		} else {
			int y = this.getHeight() + this.getY();		// get position within the box...
			// then add parent's position - will call recursively 'till root.
			//y = y + ((FormField)df.getNode().getParentNode().getDataField().getFormField()).getHeightOfPanelBottom();
			y = y + ((FormFieldContainer)this.getParent()).getYPositionWithinRootContainer();
			//System.out.println("   FormField getHeightOfPanelBottom = " + y);
			return y;
		}
	}
	
	private boolean isThisRootField() {
		return ((FormFieldContainer)this.getParent()).isRootContainer();
		
	}

	public ArrayList<JComponent> getVisibleAttributes() {
		return visibleAttributes;
	}
	
	private String addHtmlTagsForNameLabel(String text) {
		text = "<html>" + text + "</html>";
		return text;
	}
	
	
	public class TextChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			textChanged = true;		// some character was typed, so set this flag
		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				JTextComponent source = (JTextComponent)event.getSource();
				
				setDataFieldAttribute(source.getName(), source.getText(), true);
				
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	

	// called to update dataField with attribute
	protected void setDataFieldAttribute(String attributeName, String value, boolean notifyUndoRedo) {
		dataField.setAttribute(attributeName, value, notifyUndoRedo);
	}
	
	public Dimension getMaximumSize() {
		int h = getPreferredSize().height;
		int w = 10000;
		return new Dimension(w, h);
	}
	

}
