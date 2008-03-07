package ui.formFields;

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

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.CustomPopupMenu;
import ui.components.FileChooserReturnFile;
import util.ImageFactory;
import util.PreferencesManager;

public class FormFieldImage extends FormField {
	
	JLabel imageLabel;
	String imagePath;
	String zoomPercent;
	
	CustomPopupMenu zoomPopupMenu;
	
	SpinnerNumberModel percentModel;
	JSpinner percentSizeSpinner;
	
	public FormFieldImage(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		imageLabel = new JLabel();
		
		Border toolBarButtonBorder = new EmptyBorder(2,2,2,2);
		
		Icon openImageIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON);
		JButton getImageButton = new JButton(openImageIcon);
		getImageButton.setToolTipText("Choose image to display");
		getImageButton.setBorder(toolBarButtonBorder);
		getImageButton.addActionListener(new GetImageListener());

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
		JButton zoomButton = new JButton(zoomIcon);
		zoomButton.setBorder(toolBarButtonBorder);
		zoomButton.setToolTipText("Zoom Image");
		zoomButton.addMouseListener(new PopupListener());
		

		
		JToolBar imageToolBar = new JToolBar("Close to dock");
		imageToolBar.add(getImageButton);
		imageToolBar.add(zoomButton);
		JPanel toolBarContainer = new JPanel(new BorderLayout());
		toolBarContainer.add(imageToolBar, BorderLayout.SOUTH);
		
		JPanel imageLabelContainer = new JPanel(new BorderLayout());
		imageLabelContainer.add(imageLabel, BorderLayout.WEST);
		imageLabelContainer.add(toolBarContainer, BorderLayout.CENTER);
		
		horizontalBox.add(imageLabelContainer);
		
		String imageFilePath = dataField.getAttribute(DataFieldConstants.IMAGE_PATH);
		setImagePath(imageFilePath);
	}
	
	
	public class GetImageListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String imagePath = getImagePath();
			if (imagePath == null) // user canceled
				return;
			setImagePath(imagePath);
			dataField.setAttribute(DataFieldConstants.IMAGE_PATH, imagePath, true);
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
			
		BufferedImage image = createBufferedImage(imageIcon.getImage());
		image = Factory.magnifyImage(ratio, image);
		
		imageIcon.setImage(image);
		imageLabel.setIcon(imageIcon);
		
		imageLabel.setToolTipText(imagePath);
		
		//System.out.println("Imagelabel width " + imageLabel.getWidth() + ", height" + imageLabel.getHeight());
		
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
		setImagePath(dataField.getAttribute(DataFieldConstants.IMAGE_PATH));
	}
	
	//open a file
	public static String getImagePath() {
		
		String[] fileExtensions = {"jpg", "png", "gif"};
		String currentFilePath = PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER);
		
		// Create a file chooser
		FileChooserReturnFile fc = new FileChooserReturnFile(fileExtensions, currentFilePath);
		File file = fc.getFileFromUser();
		
		// remember where folder was
		if (file != null) {
			PreferencesManager.setPreference(PreferencesManager.CURRENT_FILES_FOLDER, file.getParent());
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
