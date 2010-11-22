/*
 * ome.ij.dm.TreeViewer 
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
package ome.ij.dm;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

//Third-party libraries
import ij.IJ;

//Application-internal dependencies
import ome.ij.data.DataService;
import ome.ij.data.ServicesFactory;
import ome.ij.dm.browser.Browser;
import ome.ij.dm.browser.BrowserFactory;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Displays the {@link Browser}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TreeViewer 
	extends JFrame
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to close the manager. */
	static final String	CLOSE_MANAGER_PROPERTY = "closeManager";
	
	/** The title of the window. */
	private static final String TITLE = "Manager";

	/** Action ID indicating to quit the plug-in. */
	private static final int	QUIT = 0;
	
	/** Action ID indicating to view the selected image. */
	private static final int	VIEW = 1;
	
	/** Action ID indicating to refresh the display. */
	private static final int	REFRESH = 2;
	
	/** Reference to the browser. */
	private Browser browser;
	
	/** Button indicating to <code>Quit</code> the plug-in. */
	private JButton quitButton;
	
	/** Button indicating to <code>Refresh</code> the display. */
	private JButton refreshButton;
	
	/** Button indicating to <code>View</code> the selected image. */
	private JButton viewButton;
	
	/** The viewers. */
	private Map<Long, String> viewers;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		viewers = new HashMap<Long, String>();
		browser = BrowserFactory.createBrowser();
		browser.addPropertyChangeListener(this);
		IconManager icons = IconManager.getInstance();
		quitButton = new JButton("Quit");
		quitButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		quitButton.setHorizontalTextPosition(SwingConstants.CENTER);
		quitButton.setIcon(icons.getIcon(IconManager.QUIT_22));
		quitButton.setToolTipText("Logs out and exits.");
		quitButton.addActionListener(this);
		quitButton.setActionCommand(""+QUIT);
		UIUtilities.unifiedButtonLookAndFeel(quitButton);
		refreshButton = new JButton("Refresh");
		refreshButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		refreshButton.setHorizontalTextPosition(SwingConstants.CENTER);
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		refreshButton.setIcon(icons.getIcon(IconManager.REFRESH_22));
		refreshButton.setToolTipText("Reloads the display.");
		refreshButton.addActionListener(this);
		refreshButton.setActionCommand(""+REFRESH);
		viewButton = new JButton("View");
		viewButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		viewButton.setHorizontalTextPosition(SwingConstants.CENTER);
		UIUtilities.unifiedButtonLookAndFeel(viewButton);
		viewButton.setEnabled(false);
		viewButton.setIcon(icons.getIcon(IconManager.IMAGE_22));
		viewButton.setToolTipText("View the selected image.");
		viewButton.addActionListener(this);
		viewButton.setActionCommand(""+VIEW);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { onWindowClosing(); }
		});
	}

	/** Invokes when closing the window. */
	private void onWindowClosing()
	{
		firePropertyChange(CLOSE_MANAGER_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}
	
	/**
	 * Creates the tool bar hosting the controls.
	 * 
	 * @return See above.
	 */
	private JToolBar createMenuBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setRollover(true);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(quitButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(refreshButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(viewButton);
		
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(createMenuBar(), BorderLayout.NORTH);
		c.add(new JScrollPane(browser.getUI()), BorderLayout.CENTER);
	}
	
	/**
	 * Returns the full path to the file locally stored.
	 * 
	 * @param image The image to handle.
	 * @return See above.
	 */
	private String getFile(ImageData image)
	{
		long id = image.getId();
		if (viewers.containsKey(id)) return viewers.get(id);
		DataService ds = ServicesFactory.getInstance().getDataService();
		String dir = System.getProperty("java.io.tmpdir");
		String name = image.getName()+"_ID_"+id+"."+OMETIFFFilter.OME_TIFF;
		String path = dir+File.separator+name;
		File f = new File(path);
		try {
			f = ds.exportImageAsOMETiff(f, image.getId());
			f.deleteOnExit();
		} catch (Exception e) {
			return null;
		}
		path = f.getAbsolutePath();
		viewers.put(id, path);
		return path;
	}
	
	/** Views the selected image. */
	private void viewImage()
	{
		DataObject object = browser.getSelectedObject();
		if (!(object instanceof ImageData)) return;
		ImageData image = (ImageData) object;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		IJ.showStatus("Loading Image from server...");
		String path = getFile(image);
		if (path == null) {
			IJ.showMessage("An error occurred while loading the image.");
		} else {
			IJ.runPlugIn("loci.plugins.LociImporter", path);
		}
		IJ.showStatus("");
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/** Creates a new instance. */
	TreeViewer()
	{
		super();
		setIconImage(IconManager.getOMEImageIcon());
		setTitle(TITLE);
		initComponents();
		buildUI();
	}
	
	/** Activates the window. */
	public void activate()
	{
		browser.activate();
		if (isVisible()) toFront();
		else {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        setSize(3*(screenSize.width/10), 6*(screenSize.height/10));
	        UIUtilities.centerAndShow(this);
		}
	}

	/** Closes the plug-in. */
	public void discard()
	{
		browser.discard();
		setVisible(false);
		dispose();
	}
	
	/** 
	 * Handles button clicks.
	 * @see ActionListener#actionPerformed(ActionEvent e)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case QUIT:
				firePropertyChange(CLOSE_MANAGER_PROPERTY, 
						Boolean.valueOf(false), Boolean.valueOf(true));
				break;
			case REFRESH:
				browser.refresh();
				break;
			case VIEW:
				viewImage();
		}
	}

	/**
	 * Reacts to property fired by the browser.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY.equals(name)) {
			DataObject da = browser.getSelectedObject();
			viewButton.setEnabled(da instanceof ImageData);
		} else if (Browser.VIEW_DISPLAY_PROPERTY.equals(name)) {
			viewImage();
		} else if (Browser.ERROR_EXIT_PROPERTY.equals(name)) {
			onWindowClosing();
		}
 	}
	
}
