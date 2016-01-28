/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.viewer;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import omero.api.RenderingEnginePrx;
import org.openmicroscopy.shoola.examples.data.Gateway;
import org.openmicroscopy.shoola.examples.data.LoginCredentials;
import omero.gateway.model.ImageData;

/** 
 * Demo viewer. This will work fine for a user with few images.
 * The purpose is to show of to use the rendering engine.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ViewerDemo 
	extends JFrame
{

	/** Use to display image identifier. */
	private static final String ITEM = "Image's ID: ";
	
	/** Entry point to access the various services. */
	private Gateway gateway;
	
	/** Map to display. */
	private Map<Long, ImageData> mapImages;
	
	/** The component displaying the image. */
	private ViewerPane viewer;
	
	/**
	 * Views the specified image.
	 * 
	 * @param id The identifier of the image.
	 */
	private void viewImage(long id)
	{
		id = 1001;
		ImageData image = mapImages.get(id);
		if (image == null) return;
		try {
			RenderingEnginePrx proxy = gateway.loadRenderingControl(
					image.getDefaultPixels().getId());
			//interact with proxy to change z-section etc.
			viewer.setRenderingControl(image, proxy);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		pack();
	}
	
	/** 
	 * Loads and displays the images owned by the user currently logged in.
	 * @param viewType The type of viewer, java, processing, or other.
	 * @return See above.
	 */
	private JComponent displayImages(ViewType viewType)
	{
		//Retrieve the image.
		List<ImageData> images = null;
		try {
			images = gateway.getImages();
		} catch (Exception e) {
			//handle exception
		}
		if (images == null || images.size() == 0)
			return new JLabel("No images to display.");
		mapImages = new HashMap<Long, ImageData>();
		Iterator<ImageData> i = images.iterator();
		ImageData image;
		Object[] items = new Object[images.size()];
		int index = 0;
		while (i.hasNext()) {
			image = i.next();
			items[index] = ITEM+image.getId();
			mapImages.put(image.getId(), image);
			index++;
		}
		viewer = new ViewerPane(viewType);
		JComboBox box = new JComboBox(items);
		box.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JComboBox box = (JComboBox) e.getSource();
				String s = (String) box.getSelectedItem();
				String[] values = s.split(":");
				long id = Long.parseLong(values[1].trim());
				viewImage(id);
			}
		});
		box.setSelectedIndex(0);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel("Select the image to view"));
		p.add(box);
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(p);
		content.add(viewer);
		return content;
	}
	
	/** 
	 * Builds the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel controls = new JPanel();
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				//necessary to close session.
				if (gateway != null) gateway.shutdDown();
				setVisible(false);
				dispose();
			}
		});
		controls.add(closeButton);
		return controls;
	}
	
	/** Initializes the window. */
	private void initialize()
	{
		setTitle("Viewer Demo");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param lc Credentials required to connect to server.
	 * @param viewType The type of viewer to use to render the image.
	 */
	public ViewerDemo(LoginCredentials lc, ViewType viewType)
	{
		initialize();
		gateway = new Gateway();
		JComponent comp;
		boolean connected = false;
		try {
			connected = gateway.login(lc);
		} catch (Exception e) {
		
		}
		
		if (!connected) {
			JLabel label = new JLabel();
			StringBuffer buffer = new StringBuffer();
			buffer.append("<html><body>");
			buffer.append("Cannot connect to server: "+lc.getHostName());
			buffer.append("<br>UserName: "+lc.getUserName());
			buffer.append("</body></html>");
			label.setText(buffer.toString());
			comp = label;
		} else comp = displayImages(viewType);
		getContentPane().add(comp, BorderLayout.CENTER);
		getContentPane().add(buildControls(), BorderLayout.SOUTH);
	}
	
	/**
	 * Starts the demo.
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//read from arguments
	
		/*if (args == null || args.length < 3)
			return;
		String u = args[0];
		String p = args[1];
		String h = args[2];
		String port = null;
		LoginCredentials lc = new LoginCredentials(u, p, h);
		if (args.length > 3) {
			port = args[4];
			lc.setPort(Integer.parseInt(port));
		}*/
		//or just comment code above and do for example
		LoginCredentials lc = new LoginCredentials("root", "ome", "localhost");
		ViewerDemo viewer = new ViewerDemo(lc, ViewType.PROCESSING);
		viewer.setSize(500, 500);
		viewer.setVisible(true);
	}
	
}
