
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
		super.enableEditing(enabled);	
		
		if (getImageButton != null)	// just in case!
			getImageButton.setEnabled(enabled);
		
		if (zoomButton != null)	// just in case!
			zoomButton.setEnabled(enabled);
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
			setImagePath(imagePath);
			
			// but SAVE the absolute Path
			// (first overwrite the relative path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, null, true);
			dataField.setAttribute(DataFieldConstants.IMAGE_PATH, imagePath, true);
		}
	}
	
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
			 * If the file exists, but contains "untitled", it may have been saved automatically
			 * in the default location (eg by findMoreLikeThis search). 
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
			 * Calculate what the relative path is. 
			 */
			String relativePath = getRelativePathFromAbsolutePath(imagePath);
			
			
			// Need to use the full image path to display the image....
			// This will happen with  dataFieldUpdated()
			//setImagePath(imagePath);
			
			// but SAVE the relative Path
			// (first overwrite the absolute path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.IMAGE_PATH, null, true);
			dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, relativePath, true);
		}
	}
	
	/**
	 * This method returns a file path that is relative to the currently saved location
	 * of the Omero.Editor file (as specified by the Tree.getFile() method. 
	 * 
	 * @param absolutePath
	 * @return
	 */
	public String getRelativePathFromAbsolutePath(String imagePath) {
		
		File editorFile = ((DataField)dataField).getNode().getTree().getFile();
		String editorFilePath = editorFile.getParent();
		
		
		String fileSeparator = File.separator;
		
			
		/*
		 * Need to split both file paths into arrays of directories in order to see
		 * how many directories are similar (starting at the root).
		 * Can't use string.split(File.separator) because the backslash of Windows 
		 * File.separator causes regex errors. Need to use StringTokenizer...
		 */
		StringTokenizer st = new StringTokenizer(editorFilePath, fileSeparator);
		int tokens = st.countTokens();
		
		String[] editorFileDirectories = new String[tokens];
		for (int i=0; i<editorFileDirectories.length; i++) {
			editorFileDirectories[i] = st.nextToken();
		}
		
		st = new StringTokenizer(imagePath, fileSeparator);
		tokens = st.countTokens();
		
		String[] imageFileDirectories = new String[tokens];
		for (int i=0; i<imageFileDirectories.length; i++) {
			imageFileDirectories[i] = st.nextToken();
		}
	
		
		/*
		 * Count the root directories that are common to both file paths. 
		 */
		int commonDirsCount = 0;
		while ((commonDirsCount < editorFileDirectories.length) && 
				(commonDirsCount < imageFileDirectories.length) && 
				(editorFileDirectories[commonDirsCount].equals(imageFileDirectories[commonDirsCount]))) {
			commonDirsCount++;
		}
		
		/*
		 * The relative image file path needs to be built from the remaining directories of 
		 * the image file path.
		 * This is the path from the last common directory to the image. 
		 */
		String relativeFilePath = "";
		for (int i=commonDirsCount; i<imageFileDirectories.length; i++) {
			// don't add fileSeparator at start. 
			if (i > commonDirsCount)
				relativeFilePath = relativeFilePath + File.separator;
			
			relativeFilePath = relativeFilePath + imageFileDirectories[i];
		}
		
		/*
		 * If the editor File has additional directories after the last common directory...
		 * See how many...
		 */
		int editorFileDirsRemaining = editorFileDirectories.length - commonDirsCount;
		
		/*
		 * Add  ../ for every directory level.
		 */
		for (int i=0; i<editorFileDirsRemaining; i++) {
			relativeFilePath = ".." + File.separator + relativeFilePath;
		}
		
		// windows troubleshooting!!
		//JOptionPane.showMessageDialog(null, "FormFieldImage getRelativePath  relativeFilePath = " + relativeFilePath);
		
		return relativeFilePath;
	}
	
	
	/**
	 * This method returns an absolute file path that is constructed from the 
	 * relative file path, and the location
	 * of the Omero.Editor file (as specified by the Tree.getFile() method. 
	 * 
	 * @param absolutePath
	 * @return
	 */
	public String getAbsolutePathFromRelativePath(String relativeImagePath) {
		
		File editorFile = ((DataField)dataField).getNode().getTree().getFile();
		String editorDirectory = editorFile.getParent();
		

		/*
		 * Need to turn the absolute file path of the Editor file into an array of directories...
		 */
		StringTokenizer st = new StringTokenizer(editorDirectory, File.separator);
		int tokens = st.countTokens();
		
		String[] editorFileDirectories = new String[tokens];
		for (int i=0; i<editorFileDirectories.length; i++) {
			editorFileDirectories[i] = st.nextToken();
		}
		
		/*
		 * Count the number of directories that the Editor file path has after the last 
		 * common directory (that is shared with the image file path).
		 * This is denoted in the relative file path by the number of "../" at the start
		 */
		int filePathExtraDirs = 0;
		// look for ".." because you don't know what the file separator is...
		while (relativeImagePath.startsWith("..")) {
			
			filePathExtraDirs++;

			/* 
			 * remove the first 3 characters "../" from the relative image path.
			 * Assume that File.separator is only a single character!!
			 */ 
			relativeImagePath = relativeImagePath.substring(3, relativeImagePath.length());
		}
		
		String absoluteImagePath = "";
		
		/*
		 * Need to build up the root file path that is common to both the Editor file
		 * and the Image. 
		 */
		int commonDirs = editorFileDirectories.length - filePathExtraDirs;
		for(int i=0; i<commonDirs; i++) {
			
			absoluteImagePath = absoluteImagePath.concat(File.separator + editorFileDirectories[i]);
		}
		
		// windows troubleshooting!!
		//JOptionPane.showMessageDialog(null, "absoluteImagePath = " + absoluteImagePath +
		//		"\n relativeImagePath = " + relativeImagePath);
		
		/*
		 * Now make the absolute image path by adding the relative file path
		 * to the common directory path. 
		 */
		absoluteImagePath = absoluteImagePath + File.separator + relativeImagePath;
		
		return absoluteImagePath;
	}
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
		refreshImage();
	}
	
	public void refreshImage() {
		if (imagePath == null) {
			imageLabel.setText("");
			imageLabel.setToolTipText("");
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
		
		String imagePath = dataField.getAttribute(DataFieldConstants.IMAGE_PATH);
		
		if (imagePath == null) {
			String relativeImagePath = dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH);
			if (relativeImagePath != null)
				imagePath = getAbsolutePathFromRelativePath(relativeImagePath);
		}
		
		setImagePath(imagePath);
	}
	
	//open a file
	public static String getImagePath() {
		
		String[] fileExtensions = {"jpg", "png", "gif"};
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
