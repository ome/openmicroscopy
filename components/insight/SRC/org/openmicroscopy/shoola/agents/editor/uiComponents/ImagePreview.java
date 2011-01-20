 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.ImagePreview 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * UI for previewing an image. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImagePreview 
	extends JPanel {
	
	private String					imagePath;
	
	/**
	 * builds the UI. 
	 */
	private void buildUI()
	{
		setLayout(new BorderLayout());
		setBackground(null);
		
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
			ex.printStackTrace();
			return;
		}
		
		
		ImageIcon imageIcon = new ImageIcon(imageURL);
		
		try {
			// lookup the max dimension of a thumbnail from editor.xml config
			String max = (String)EditorAgent.getRegistry().lookup("/ui/thumbnail");
			
			BufferedImage image = Factory.createImage(imageIcon.getImage());
			int maxDimension =  Math.max(image.getWidth(), image.getHeight());
			Float ratio = Float.valueOf(max) / maxDimension;
			image = Factory.magnifyImage(ratio, image);
			
			imageIcon.setImage(image);
		} catch (Exception ex) {
			
		}
		
		JLabel imageLabel = new CustomLabel();
		imageLabel.setIcon(imageIcon);
		add(imageLabel, BorderLayout.WEST);
	}
	
	/**
	 * Creates an instance, and builds the UI with the image.
	 * 
	 * @param imagePath
	 */
	public ImagePreview(String imagePath)
	{
		this.imagePath = imagePath;
		buildUI();
	}
	
}
