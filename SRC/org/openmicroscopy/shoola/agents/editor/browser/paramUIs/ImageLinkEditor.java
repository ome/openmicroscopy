 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ImageLinkEditor 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;


//Java imports

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomPopupMenu;
import org.openmicroscopy.shoola.agents.editor.uiComponents.RelativeFileChooser;
import org.openmicroscopy.shoola.agents.editor.util.PreferencesManager;
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * This is the UI component for choosing and displaying a link to an image. 
 * Currently this UI class is not used!!
 * 
 * If the link is valid, the image will be retrieved and displayed.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageLinkEditor 
	extends AbstractParamEditor 
	implements PropertyChangeListener,
	ActionListener
{
	
	/**
	 * A Label used for displaying the linked image.
	 */
	private JLabel 				imageLabel;
	
	/**
	 * An absolute file path to the image. 
	 */
	private String 				imagePath;
	
	/**
	 * The value of the zoom (percent), as a string.
	 * This is edited by a pop-up menu and used to refresh the image
	 * at the appropriate zoom percentage.
	 */
	private String 				zoomPercent;
	
	/**
	 * A list of options for the zoom pop-up menu. 
	 */
	private String[] 			zoomOptions = {"25%", "50%", "75%", "100%", 
											"125%", "150%", "175%", "200%"};
	
	/**
	 * A pop-up menu to pick the zoom amount. 
	 */
	private CustomPopupMenu 	zoomPopupMenu;
	
	/**
	 * A toolbar button 
	 */
	private JButton 			getImageButton;
	
	/**
	 * A button to pick the zoom 
	 */
	private JButton 			zoomButton;
	
	/**
	 * An icon to display if there is no image
	 */
	private ImageIcon 			noImageIcon;
	
	/**
	 * Initialises the UI components.
	 */
	private void initialise() {
		IconManager iM = IconManager.getInstance();
		
		imageLabel = new CustomLabel();
		getImageButton = new CustomButton(iM.getIcon(
				IconManager.OPEN_IMAGE_ICON));
		getImageButton.addActionListener(this);
		getImageButton.setToolTipText("Link to an image on your computer, " +
				"to display the image in this field.");
		
		// zoom chooser
		zoomPopupMenu = new CustomPopupMenu(zoomOptions);
		Icon zoomIcon = iM.getIcon(IconManager.ZOOM_ICON);
		zoomButton = new CustomButton(zoomIcon);
		zoomButton.setToolTipText("Zoom Image");
		zoomButton.addMouseListener(new PopupListener());
		
		noImageIcon = iM.getImageIcon(IconManager.NO_IMAGE_ICON_32);
		
		// set the value of the pop-up menu, according to IMAGE_ZOOM attribute
		/*
		 zoomPercent = getParameter().getAttribute(ImageParam.IMAGE_ZOOM);
		if (zoomPercent == null) {
			zoomPercent = "100";
		}
		
		zoomPopupMenu.setSelectedItem(zoomPercent + "%");
		*/
		zoomPopupMenu.addPropertyChangeListener(this);
	}

	/**
	 * Builds the UI, adding Image (as a label) and toolBar. 
	 */
	private void buildUI() {
		JToolBar imageToolBar = new JToolBar("Close to dock");
		imageToolBar.setBackground(null);
		imageToolBar.add(getImageButton);
		imageToolBar.add(zoomButton);
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.setBackground(null);
		toolBarContainer.add(imageToolBar, BorderLayout.SOUTH);
		
		add(imageLabel, BorderLayout.WEST);
		add(toolBarContainer, BorderLayout.CENTER);
	}

	/**
	 * Gets the value of {@link ImageParam#ABSOLUTE_IMAGE_PATH} or 
	 * {@link ImageParam#RELATIVE_IMAGE_PATH}
	 */
	private void getAndDisplayImageLink() {
		
		imagePath = getParameter().getAttribute
			(DataReference.REFERENCE);
		
		refreshImage();
	}

	/**
	 * Displays the image, based on the value of <code>imagePath</code>
	 * and the zoom.
	 */
	private void refreshImage() {
		if (imagePath == null) {
			imageLabel.setText("");
			imageLabel.setToolTipText("");
			imageLabel.setIcon(null);
			return;
		}
		
		Float ratio = Float.parseFloat(zoomPercent) / 100;
		
		URL imageURL = null;
		String osSensitiveImagePath = imagePath;
	    if (System.getProperty("os.name").startsWith("Mac OS")) {
	    	osSensitiveImagePath = "file://" + imagePath;
	    } else {
	    	osSensitiveImagePath = "file:///" + imagePath;
	    }
	    
		try {
			imageURL = new URL(osSensitiveImagePath);
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(this, "Malformed URL file path");
			return;
		}
		
		
		ImageIcon imageIcon = new ImageIcon(imageURL);
		
		try {
			BufferedImage image = Factory.createImage(imageIcon.getImage());
			image = Factory.magnifyImage(ratio, image);
			
			imageIcon.setImage(image);
		} catch (Exception ex) {
			
			// Image has not been found. 
			imageIcon = noImageIcon;
			}
		
		imageLabel.setIcon(imageIcon);
		imageLabel.setToolTipText(imagePath);
		
	}

	/**
	 * Opens a fileChooser, for the user to pick a file.
	 * Then this file path is saved to dataField. 
	 * If the user checks "Relative link" then filePath is saved as relative. 
	 */
	private void getAndSaveLink() {
		
		String currentFilePath = PreferencesManager.getPreference(
				PreferencesManager.CURRENT_IMAGES_FOLDER);
		
		String startDir = null;	//TODO		get the directory of the editor file
		
		/*
		 * This creates a modal FileChooser dialog. 
		 */
		RelativeFileChooser relFileChooser = new RelativeFileChooser(null,
				startDir, currentFilePath);
		
		
		String linkedFilePath = relFileChooser.getPath();
		
		if (linkedFilePath == null) 
			return;		// user canceled
	
		
		attributeEdited(DataReference.REFERENCE, linkedFilePath);
		
	}

	/**
	 * A MouseAdapter that shows the zoom popup menu when the zoom button
	 * is clicked. 
	 * 
	 * @author will
	 *
	 */
	private class PopupListener extends MouseAdapter 
	{
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    private void maybeShowPopup(MouseEvent e) {
	    	zoomPopupMenu.show(e.getComponent(),
	                       e.getX(), e.getY());   
	    }
	}

	/**
	 * Creates an instance.
	 * 
	 * @param param
	 */
	public ImageLinkEditor(IParam param) 
	{	
		super(param);
		
		setLayout(new BorderLayout());
	
		initialise();
		
		buildUI();
		
		getAndDisplayImageLink();
	}
	
	/**
	 * Property change listener for the pop-up menu.
	 * If the {@link CustomPopupMenu#ITEM_NAME} property has changed, 
	 * the {@link ImageParam#IMAGE_ZOOM} attribute is set and the whole
	 * field is refreshed.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName().equals(CustomPopupMenu.ITEM_NAME)) {
			String zoom = evt.getNewValue().toString().replace("%", "");
			
			zoomPercent = zoom;
			// zoom attribute is saved directly.
			// not added to undo/redo via controller. 
			// getParameter().setAttribute(ImageParam.IMAGE_ZOOM, zoom);
			/*refresh the whole panel, and stay in editing mode*/
			ImageLinkEditor.this.firePropertyChange
				(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);;
		}
	}
	
	
	/**
	 * Calls the super.attributeEdited(), then 
	 * fires propertyChange to refresh link, and size of panel
	 * 
	 * @see AbstractParamEditor#attributeEdited(String, Object)
	 */
	public void attributeEdited(String attributeName, Object newValue) 
	{	 
		super.attributeEdited(attributeName, newValue);
		
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
	}

	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Image Link"; }

	/**
	 * ActionPerformed for the Choose Image Link button.
	 * Calls {@link #getAndSaveLink()}
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		getAndSaveLink();
	}

}
