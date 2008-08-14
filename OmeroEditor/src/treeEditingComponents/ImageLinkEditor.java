 /*
 * treeEditingComponents.ImageLinkEditor 
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
package treeEditingComponents;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.openmicroscopy.shoola.util.image.geom.Factory;

import tree.DataField;
import tree.DataFieldConstants;
import treeModel.fields.FieldPanel;
import treeModel.fields.IParam;
import treeModel.fields.ImageParam;
import treeModel.fields.LinkParam;
import ui.components.CustomPopupMenu;
import ui.formFields.FormFieldImage.GetImagePathAction;
import ui.formFields.FormFieldImage.ZoomChangedListener;
import uiComponents.CustomButton;
import uiComponents.CustomLabel;
import uiComponents.RelativeFileChooser;
import util.FilePathMethods;
import util.ImageFactory;
import util.PreferencesManager;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
	extends AbstractParamEditor {
	
	private JLabel imageLabel;
	String imagePath;
	String zoomPercent;
	
	CustomPopupMenu zoomPopupMenu;
	
	JButton getImageButton;
	JButton zoomButton;
	
	JDialog customGetImageDialog;
	
	public ImageLinkEditor(IParam param) {
		
		super(param);
		
		setLayout(new BorderLayout());
		
		imageLabel = new CustomLabel();
		
		Icon openImageIcon = ImageFactory.getInstance().getIcon(
				ImageFactory.OPEN_IMAGE_ICON);
		
		getImageButton = new CustomButton(new GetImagePathAction());
		
		// zoom chooser
		String[] zoomOptions = {"25%", "50%", "75%", "100%", "125%", "150%", "175%", "200%"};
		zoomPercent = param.getAttribute(DataFieldConstants.IMAGE_ZOOM);
		if (zoomPercent == null) {
			zoomPercent = "100";
		}
		zoomPopupMenu = new CustomPopupMenu(zoomOptions);
		zoomPopupMenu.setSelectedItem(zoomPercent + "%");
		zoomPopupMenu.addPropertyChangeListener(new ZoomChangedListener());
		
		Icon zoomIcon = ImageFactory.getInstance().getIcon(ImageFactory.ZOOM_ICON);
		zoomButton = new CustomButton(zoomIcon);
		zoomButton.setToolTipText("Zoom Image");
		zoomButton.addMouseListener(new PopupListener());
		
		JToolBar imageToolBar = new JToolBar("Close to dock");
		imageToolBar.setBackground(null);
		imageToolBar.add(getImageButton);
		imageToolBar.add(zoomButton);
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.setBackground(null);
		toolBarContainer.add(imageToolBar, BorderLayout.SOUTH);
		
		add(imageLabel, BorderLayout.WEST);
		add(toolBarContainer, BorderLayout.CENTER);
		
		getAndDisplayImageLink();
	}
	
	private void getAndDisplayImageLink() {
		
		imagePath = getParameter().getAttribute
			(ImageParam.ABSOLUTE_IMAGE_PATH);
		
		if (imagePath == null) {
			String relativeImagePath = getParameter().
				getAttribute(ImageParam.RELATIVE_IMAGE_PATH);
			if (relativeImagePath != null) {
				//File editorFile = ((DataField)dataField).getNode().getTree().getFile();
				File editorFile = null; 	// TODO get reference to file!
				imagePath = FilePathMethods.getAbsolutePathFromRelativePath
					(editorFile, relativeImagePath);
			}
		}
		
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
				getParameter().setAttribute(ImageParam.IMAGE_ZOOM, zoom);
				/*refresh the whole panel*/
				ImageLinkEditor.this.firePropertyChange
					(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);;
			}
		}
	}
	
	public class GetImagePathAction extends AbstractAction {
		
		public GetImagePathAction() {
			putValue(Action.NAME, "");
			putValue(Action.SHORT_DESCRIPTION, "Link to an image on your computer, to display the image in this field.");
			putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			getAndSaveLink();
		}
	}
	
	/**
	 * Opens a fileChooser, for the user to pick a file.
	 * Then this file path is saved to dataField. 
	 * If the user checks "Relative link" then filePath is saved as relative. 
	 */
	public void getAndSaveLink() {
		
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
	
		
		/*
		 * If the user checked the "Relative Link" checkBox, save as a relative link.
		 * Otherwise, save as absolute link.
		 * Attributes are mutually exclusive.
		 */
		if (relFileChooser.isRelativeLink()) {
			saveLinkToParam(ImageParam.RELATIVE_IMAGE_PATH, linkedFilePath);
		} else {
			// Save the absolute Path
			saveLinkToParam(ImageParam.ABSOLUTE_IMAGE_PATH, linkedFilePath);
		}
		
	}
	
	
	/**
	 * To ensure that only one type of link is saved to dataField.
	 * Update several attributes at once, making sure that all apart from 
	 * one are null.
	 * 
	 * @param name
	 * @param value
	 */
	public void saveLinkToParam(String name, String value) {
		
		if (name.equals(DataFieldConstants.ABSOLUTE_IMAGE_PATH) || 
				name.equals(DataFieldConstants.RELATIVE_IMAGE_PATH)) {
			
			/*
			 * Make a map with all values null, then update one.
			 */
			HashMap<String, String> newValues = new HashMap<String, String>();
			newValues.put(DataFieldConstants.ABSOLUTE_IMAGE_PATH, null);
			newValues.put(DataFieldConstants.RELATIVE_IMAGE_PATH, null);
			
			newValues.put(name, value);
			
			// Updates new values, and adds to undo queue as one action. 
			attributeEdited("Image", newValues);
		}
			
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
	
	/**
	 * Calls the super.attributeEdited(), then 
	 * fires propertyChange to refresh link, and size of panel
	 */
	public void attributeEdited(String attributeName, Object newValue) {
		 
		super.attributeEdited(attributeName, newValue);
		
		this.firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
	}

	public String getEditDisplayName() {
		return "Edit Image Link";
	}

}
