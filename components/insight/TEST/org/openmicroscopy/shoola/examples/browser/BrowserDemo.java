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
package org.openmicroscopy.shoola.examples.browser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.openmicroscopy.shoola.examples.data.Gateway;
import org.openmicroscopy.shoola.examples.data.LoginCredentials;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/** 
 * Demo applications to browse a dataset.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class BrowserDemo
	extends JFrame
{

	/** Use to display image identifier. */
	private static final String ITEM = "Dataset's ID: ";
	
	/** Entry point to access the various services. */
	private Gateway gateway;
	
	/** The component displaying the images. */
	private BrowserCanvas canvas;
	
	/** The collection of loaded datasets. */
	private Map<Long, List<BufferedImage>> loadedDatasets;
	
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
		loadedDatasets = new HashMap<Long, List<BufferedImage>>();
		setTitle("Browser Demo");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	/**
	 * Displays the specified images.
	 * 
	 * @param images The images to display.
	 */
	private void displayImages(List<BufferedImage> images)
	{
		canvas.setImages(images);
	}

	/**
	 * Browses the specified datasets.
	 * 
	 * @param id The identifier of the dataset.
	 */
	private void browseDataset(long id)
	{
		//first check if already loaded.
		if (loadedDatasets.containsKey(id)) {
			displayImages(loadedDatasets.get(id));
			return;
		}
		//Load the image in the dataset
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(id);
		List<DatasetData> datasets = null;
		try {
			datasets = gateway.getDatasets(ids);
		} catch (Exception e) {
			//handle exception
		}
		if (datasets == null || datasets.size() != 1) {
			JOptionPane.showMessageDialog(this,
					"Cannot browse the selected dataset.");
			return;
		}
		DatasetData d = datasets.get(0);
		Collection<ImageData> images = d.getImages();
		if (images == null || images.size() == 0) {
			JOptionPane.showMessageDialog(this,
					"The selected dataset is empty.");
			return;
		}
		List<Long> pixels = new ArrayList<Long>();
		Iterator<ImageData> i = images.iterator();
		ImageData image;
		while (i.hasNext()) {
			image = i.next();
			pixels.add(image.getDefaultPixels().getId());
		}
		try {
			List<BufferedImage> l = gateway.getThumbnailSet(pixels, 96);
			loadedDatasets.put(id, l);
			displayImages(l);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	/** 
	 * Loads and displays the datasets owned by the user currently logged in.
	 * 
	 * @return See above.
	 */
	private JComponent displayDatasets()
	{
		//Retrieve the datasets.
		List<DatasetData> datasets = null;
		try {
			datasets = gateway.getDatasets(null);
		} catch (Exception e) {

		}
		if (datasets == null || datasets.size() == 0)
			return new JLabel("No datasets to display.");
		Iterator<DatasetData> i = datasets.iterator();
		DatasetData dataset;
		Object[] items = new Object[datasets.size()];
		int index = 0;
		while (i.hasNext()) {
			dataset = i.next();
			items[index] = ITEM+dataset.getId();
			index++;
		}
		canvas = new BrowserCanvas();
		JComboBox box = new JComboBox(items);
		box.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JComboBox box = (JComboBox) e.getSource();
				String s = (String) box.getSelectedItem();
				String[] values = s.split(":");
				long id = Long.parseLong(values[1].trim());
				browseDataset(id);
			}
		});
		box.setSelectedIndex(0);
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("Select the dataset to browse"));
		p.add(box);
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(p);
		content.add(new JScrollPane(canvas));
		return content;
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param lc Credentials required to connect to server.
	 */
	BrowserDemo(LoginCredentials lc)
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
		} else comp = displayDatasets();
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
		if (args == null || args.length < 3)
			return;
		String u = args[0];
		String p = args[1];
		String h = args[2];
		String port = null;
		LoginCredentials lc = new LoginCredentials(u, p, h);
		if (args.length > 3) {
			port = args[4];
			lc.setPort(Integer.parseInt(port));
		}
		//or just comment code above and do for example
		// lc = new LoginCredentials("root", "ome", "localhost");
		BrowserDemo viewer = new BrowserDemo(lc);
		viewer.setSize(500, 500);
		viewer.setVisible(true);
	}
	
}
