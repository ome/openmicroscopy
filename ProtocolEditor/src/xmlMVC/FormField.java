package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

// FormField dictates how dataField is displayed as a row in the complete form
// This FormField superclass merely arranges Name and Description (as a toolTip)
// Subclasses have eg TextFields etc

public class FormField extends JPanel {
	
	// swing components
	Box horizontalBox;
	Component leftIndent = Box.createHorizontalStrut(0);
	JButton collapseButton;	
	JLabel nameLabel;
	JLabel descriptionLabel;
	Icon infoIcon;
	Icon wwwIcon;
	JButton descriptionButton;
	JButton urlButton;	// used (if url) to open browser 
	
	Icon collapsedIcon;
	Icon notCollapsedIcon;
	
	ArrayList<JComponent> visibleAttributes = new ArrayList<JComponent>();
	
	public static final Dimension MINSIZE = new Dimension(30, 25);
	
	boolean showDescription = false;	// not saved, just used to toggle
	
	DataField dataField;
	
	public FormField(DataField dataField) {
		
		this.dataField = dataField;
		
		// build the formField panel
		Border eb = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		this.setBorder(eb);
		this.setLayout(new BorderLayout());
		this.addMouseListener(new FormPanelMouseListener());
		
		horizontalBox = Box.createHorizontalBox();
		
		boolean subStepsCollapsed = dataField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED);
		
		collapseButton = new JButton();
		collapseButton.setFocusable(false);
		collapseButton.setVisible(false);	// only made visible if hasChildren
		collapseButton.setBackground(null);
		unifiedButtonLookAndFeel(collapseButton);
		collapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.COLLAPSED_ICON);
		notCollapsedIcon = ImageFactory.getInstance().getIcon(ImageFactory.NOT_COLLAPSED_ICON);
		if (subStepsCollapsed) collapseButton.setIcon(collapsedIcon);
		else collapseButton.setIcon(notCollapsedIcon);
		collapseButton.setToolTipText("Collapse or Expand sub-steps");
		collapseButton.setBorder(new EmptyBorder(2,2,2,2));
		collapseButton.addActionListener(new CollapseListener());
		collapseButton.addMouseListener(new FormPanelMouseListener());
		horizontalBox.add(collapseButton);
		
		nameLabel = new JLabel(dataField.getName());
		nameLabel.addMouseListener(new FormPanelMouseListener());
		visibleAttributes.add(nameLabel);
		
		wwwIcon = ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON);
		urlButton = new JButton(wwwIcon);
		urlButton.setFocusable(false); // so it is not selected by tabbing
		unifiedButtonLookAndFeel(urlButton);
		urlButton.addActionListener(new URLclickListener());
		urlButton.setBackground(null);
		Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		urlButton.setCursor(handCursor);
		urlButton.setBorder(eb);
		urlButton.setVisible(false);	// only made visible if url exists.
		
		
		
		descriptionLabel = new JLabel();
		visibleAttributes.add(descriptionLabel);
		infoIcon = ImageFactory.getInstance().getIcon(ImageFactory.INFO_ICON);
		descriptionButton = new JButton(infoIcon);
		unifiedButtonLookAndFeel(descriptionButton);
		descriptionButton.setFocusable(false); // so it is not selected by tabbing
		descriptionButton.addActionListener(new ToggleDescriptionListener());
		descriptionButton.setBackground(null);
		descriptionButton.setBorder(eb);
		descriptionButton.setVisible(false);	// only made visible if description exists.
		setDescriptionText(dataField.getAttribute(DataField.DESCRIPTION)); 	// will update description label
		
		horizontalBox.add(leftIndent);
		horizontalBox.add(nameLabel, BorderLayout.WEST);
		horizontalBox.add(descriptionButton);
		horizontalBox.add(urlButton);
		horizontalBox.add(Box.createHorizontalStrut(10));

		setDescriptionText(dataField.getDescription());
		setURL(dataField.getURL());
		
		this.setBackground(null);
		this.add(horizontalBox, BorderLayout.NORTH);
		this.add(descriptionLabel, BorderLayout.CENTER);
	}
	
	// called by dataField to notfiy observers that something has changed.
	public void dataFieldUpdated() {
		setNameText(dataField.getName());
		setDescriptionText(dataField.getAttribute(DataField.DESCRIPTION));
		setURL(dataField.getAttribute(DataField.URL));
		
		dataFieldUpdatedOtherAttributes();
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdatedOtherAttributes() {};
	
	// overridden by subclasses if they have a value and text field
	public void setValue(String newValue) {}
	
	// these methods called when user updates the fieldEditor panel
	public void setNameText(String name) {
		nameLabel.setText(name);
	}
	public void setDescriptionText(String description) {
		if (description == null) return;
		if (description.length() > 0) {
		//	nameLabel.setIcon(infoIcon);
			nameLabel.setToolTipText(description);
			descriptionButton.setVisible(true);
			descriptionLabel.setVisible(showDescription);
			descriptionLabel.setFont(XMLView.FONT_TINY);
			descriptionLabel.setText("<html><div style='width:200px; padding-left:30px;'>" + description + "</div></html>");
		}
		else
		{
			nameLabel.setIcon(null);
			nameLabel.setToolTipText(null);
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
	
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		if (highlight) { 
			this.setBackground(XMLView.BLUE_HIGHLIGHT);
		}
		else {
			this.setBackground(null);
		}
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
	
	public class FocusLostUpdatDataFieldListener implements FocusListener {
		public void focusLost(FocusEvent event) {
			updateDataField();
		}
		public void focusGained(FocusEvent event) {
			panelClicked(true);
		}
	}
	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
	}
	
	public class URLclickListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			BareBonesBrowserLaunch.openURL(dataField.getURL());
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
		dataField.formFieldClicked(clearOthers);
	}
	
	//public void checkForChildren() {
	//	refreshHasChildren(dataField.hasChildren());
	//}

	public void refreshHasChildren(boolean hasChildren) {
		if (!hasChildren) {
			dataField.setAttribute(DataField.SUBSTEPS_COLLAPSED, DataField.FALSE, false);
			collapseButton.setIcon(notCollapsedIcon);
		}
		collapseButton.setVisible(hasChildren);
	}
	
	public class CollapseListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			boolean coll = dataField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED); 
			
			// toggle collapsed state
			if (coll) {
				dataField.setAttribute(DataField.SUBSTEPS_COLLAPSED, DataField.FALSE, false);
			} else {
				dataField.setAttribute(DataField.SUBSTEPS_COLLAPSED, DataField.TRUE, false);
			}
			
			// if root node, collapse children
			if (dataField.getNode().getParentNode() == null) {
				dataField.getNode().collapseAllChildren(!coll);
				collapseButton.setIcon(coll ? notCollapsedIcon :collapsedIcon);
			} else
				// otherwise just do this
				refreshTitleCollapsed();
		}
	}
	
	// called when formField panel is loaded into UI, so that it is displayed correctly
	// also called when user collapses or expands sub-steps
	public void refreshTitleCollapsed() {
		
		boolean collapsed = dataField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED);
			collapseButton.setIcon(collapsed ? collapsedIcon : notCollapsedIcon);
		// tells node whether to hide the childBox containing child panels
		dataField.hideChildren(collapsed);
		
		// this is only needed when building UI (superfluous when simply collapsing)
		refreshHasChildren(dataField.hasChildren());
	}
	
	// overridden by subclasses that have input components, 
	// .. to disable them when in template-edit mode
	public void setExperimentalEditing(boolean enabled) {}
	
	
	public static void unifiedButtonLookAndFeel(AbstractButton b)
    {
        b.setMargin(new Insets(0, 2, 0, 3));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }

	public ArrayList<JComponent> getVisibleAttributes() {
		return visibleAttributes;
	}

}
