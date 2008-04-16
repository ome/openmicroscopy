
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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.openmicroscopy.shoola.util.image.geom.Factory;

import tree.DataField;
import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.Controller;
import ui.components.CustomPopupMenu;
import ui.components.FileChooserReturnFile;
import ui.components.PopupMenuButton;
import util.FilePathMethods;
import util.ImageFactory;
import util.PreferencesManager;

public class FormFieldImage extends FormField {
	
	JLabel imageLabel;
	String imagePath;
	String zoomPercent;
	
	CustomPopupMenu zoomPopupMenu;
	
	JButton getImageButton;
	JButton zoomButton;
	
	
	public FormFieldImage(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		imageLabel = new JLabel();
		
		Border toolBarButtonBorder = new EmptyBorder(2,2,2,2);
		
		Icon openImageIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON);
		
		Action[] getImageActions = new Action[] {
				new GetAbsoluteImagePathAction(),
				new GetRelativeImagePathAction()};
		getImageButton = new PopupMenuButton("Choose an image to display", 
				openImageIcon, getImageActions);
		
		getImageButton.setBorder(toolBarButtonBorder);
		

		// zoom chooser
		String[] zoomOptions = {"25%", "50%", "75%", "100%", "125%", "150%", "175%", "200%"};
		zoomPercent = dataField.getAttribute(DataFieldConstants.IMAGE_ZOOM);
		if (zoomPercent == null) {
			zoomPercent = "100";
		}
		zoomPopupMenu = new CustomPopupMenu(zoomOptions);
		zoomPopupMenu.setSelectedItem(zoomPercent + "%");
		zoomPopupMenu.addPropertyChangeListener(new ZoomChangedListener());
		Icon zoomIcon = ImageFactory.getInstance().getIcon(ImageFactory.ZOOM_ICON);
		zoomButton = new JButton(zoomIcon);
		zoomButton.setBorder(toolBarButtonBorder);
		zoomButton.setToolTipText("Zoom Image");
		zoomButton.addMouseListener(new PopupListener());
		

		
		JToolBar imageToolBar = new JToolBar("Close to dock");
		imageToolBar.add(getImageButton);
		imageToolBar.add(zoomButton);
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.setBackground(null);
		toolBarContainer.add(imageToolBar, BorderLayout.SOUTH);
		
		JPanel imageLabelContainer = new JPanel(new BorderLayout());
		imageLabelContainer.setBackground(null);
		imageLabelContainer.add(imageLabel, BorderLayout.WEST);
		imageLabelContainer.add(toolBarContainer, BorderLayout.CENTER);
		
		horizontalBox.add(imageLabelContainer);
		
		// load the image
		dataFieldUpdated();
		
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		
		if (getImageButton != null)	// just in case!
			getImageButton.setEnabled(enabled);
		
		if (zoomButton != null)	// just in case!
			zoomButton.setEnabled(enabled);
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
	public String[] getValueAttributes() {
		return new String[] {DataFieldConstants.RELATIVE_IMAGE_PATH, DataFieldConstants.ABSOLUTE_IMAGE_PATH};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * For ImageField, relative OR absolute image path must be filled out (not null).  
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		return ((dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH) != null) || 
				(dataField.getAttribute(DataFieldConstants.ABSOLUTE_IMAGE_PATH) != null));
	}
	
	public class GetAbsoluteImagePathAction extends AbstractAction {
		
		public GetAbsoluteImagePathAction() {
			putValue(Action.NAME, "Set Absolute Image Path");
			putValue(Action.SHORT_DESCRIPTION, "Link to an image that will stay in the same absolute file location.");
			putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			String imagePath = getImagePath();
			if (imagePath == null) // user canceled
				return;
			
			// Save the absolute Path
			// (first overwrite the relative path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, null, true);
			// this will cause this field to refresh, displaying the image.
			dataField.setAttribute(DataFieldConstants.ABSOLUTE_IMAGE_PATH, imagePath, true);
		}
	}
	
	/**
	 * The action that allows users to choose a Relative image path. 
	 * @author will
	 *
	 */
	public class GetRelativeImagePathAction extends AbstractAction {
		
		public GetRelativeImagePathAction() {
			putValue(Action.NAME, "Set Relative Image Path");
			putValue(Action.SHORT_DESCRIPTION, "Link to an image that will stay in the same file location, relative to this file");
			putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			
			/*
			 * First, check that the user has saved the current editor file somewhere. 
			 */
			File editorFile = ((DataField)dataField).getNode().getTree().getFile();
			String editorFilePath = editorFile.getAbsolutePath();
				
			/*
			 * If the file does not exist, forget about it!!
			 */
			if (!editorFile.exists()) {
				JOptionPane.showMessageDialog(null, "This editor file has not yet been saved. \n" +
						"You must first save the editor file, before a relative " +
						"image file path can be defined.", 
						"Please save file first", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			/*
			 * If the file exists, but contains "untitled", it may have been saved in a 
			 * temporary location (not in a correct folder). 
			 * Need to check that the user has saved it where they want. 
			 */
			if (editorFile.getName().contains("untitled")) {
				
				Object[] options = {"Cancel", "Continue anyway"};
				
				int yesNo = JOptionPane.showOptionDialog(null, "The current file is called 'untitled' and \n" +
						"may therefore be in a temporary file location. \n" +
						"If so, please cancel and save the file in its 'proper' place.", 
						"Temporary file?", JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				
				if (yesNo == 0) {
					return;
				}
				
			}
			
			String imagePath = getImagePath();
			if (imagePath == null) // user canceled
				return;
			

			/*
			 * Calculate what the relative path is, based on the location of the editorFile. 
			 */
			String relativePath = FilePathMethods.getRelativePathFromAbsolutePath(editorFile, imagePath);
			
			
			// Save the relative Path
			// (first overwrite the absolute path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.ABSOLUTE_IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.ABSOLUTE_IMAGE_PATH, null, true);
			// This will cause this field to Refresh, displaying the image.
			dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, relativePath, true);
		}
	}
	
	
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
		refreshImage();
	}
	
	public void refreshImage() {
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
			BufferedImage image = createBufferedImage(imageIcon.getImage());
			image = Factory.magnifyImage(ratio, image);
			
			imageIcon.setImage(image);
		} catch (Exception ex) {
			
			// Image has not been found. 
			imageIcon = ImageFactory.getInstance().getImageIcon(ImageFactory.NO_IMAGE_ICON);
		}
		
		imageLabel.setIcon(imageIcon);
		imageLabel.setToolTipText(imagePath);
		
	}
	
	public BufferedImage createBufferedImage(Image img) {
        BufferedImage buff = new BufferedImage(img.getWidth(null), 
            img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = buff.createGraphics();
        gfx.drawImage(img, 0, 0, null);
        gfx.dispose();
        return buff;
    }
	

	public class ZoomChangedListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(CustomPopupMenu.ITEM_NAME)) {
				String zoom = evt.getNewValue().toString().replace("%", "");
				
				zoomPercent = zoom;
				dataField.setAttribute(DataFieldConstants.IMAGE_ZOOM, zoom, false);
				refreshImage();
			}
		}
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		
		String imagePath = dataField.getAttribute(DataFieldConstants.ABSOLUTE_IMAGE_PATH);
		
		if (imagePath == null) {
			String relativeImagePath = dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH);
			if (relativeImagePath != null) {
				File editorFile = ((DataField)dataField).getNode().getTree().getFile();
				imagePath = FilePathMethods.getAbsolutePathFromRelativePath(editorFile, relativeImagePath);
			}
		}
		
		setImagePath(imagePath);
	}
	
	//open a file
	public static String getImagePath() {
		
		String[] fileExtensions = {".jpg", ".png", ".gif"};
		String currentFilePath = PreferencesManager.getPreference(PreferencesManager.CURRENT_IMAGES_FOLDER);
		
		// Create a file chooser
		FileChooserReturnFile fc = new FileChooserReturnFile(fileExtensions, currentFilePath);
		File file = fc.getFileFromUser();
		
		// remember where folder was
		if (file != null) {
			PreferencesManager.setPreference(PreferencesManager.CURRENT_IMAGES_FOLDER, file.getParent());
			return file.getAbsolutePath();
		}
		
		return null;
		
	}
	
	class PopupListener extends MouseAdapter {
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

}
