/*
 * org.openmicroscopy.shoola.agents.viewer.ViewerUIF
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.viewer;


//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBar;
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBarManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ViewerUIF
	extends JInternalFrame
{
	
	private static final int		EXTRA = 20;
	
	private static final Dimension  HBOX = new Dimension(EXTRA, 0);
	
	/** Canvas to display the currently selected 2D image. */
	private ImageCanvas             canvas;
	
	private JPanel					contents;
	
	/** z-slider and t-slider. */
	private JSlider					tSlider, zSlider;
	
	/** Tool bar of the Agent. */
	private ToolBar					toolBar;
	
	/** Movie menu. */
	private JMenu					movieMenu;
	
	private ViewerCtrl 				control;
	
	private Registry				registry;
	
	private boolean					active;
	
	ViewerUIF(ViewerCtrl control, Registry registry)
	{
		setFrame();
		this.control = control;
		this.registry = registry;
		PixelsDimensions pxsDims = control.getPixelsDims();
		int maxT = pxsDims.sizeT-1;
		int maxZ = pxsDims.sizeZ-1;
		int t = control.getDefaultT();
		int z = control.getDefaultZ();
		setJMenuBar(createMenuBar(maxT));
		toolBar = new ToolBar(control, registry, maxT, t, maxZ, z);
		initSliders(maxT, t, maxZ, z);
		buildGUI();
	}
	
	public JSlider getTSlider() { return tSlider; }
	
	public JSlider getZSlider() { return zSlider; }
	
	public ToolBar getToolBar() { return toolBar; } 
	
	/** Set the title of the Image. */
	void setImageName(String imageName)
	{
		setTitle(imageName);
	}
	
	/** Reset the default values for timepoint and z-section in the stack; */
	void setDefaultZT(int t, int z, int sizeT, int sizeZ)
	{
		ToolBarManager tbm = toolBar.getManager();
		tbm.onTChange(t);
		tbm.onZChange(z);
		int maxZ = sizeZ-1;
		int maxT = sizeT-1;
		tbm.setMaxT(maxT);
		tbm.setMaxZ(maxZ);
		toolBar.getZLabel().setText("/"+maxZ);
		toolBar.getTLabel().setText("/"+maxT);
		resetSliders(maxT, t, maxZ, z);
		boolean b;
		if (maxT == 0) b = false;
		else b = true;
		//toolbar Movie controls.
		toolBar.getRewind().setEnabled(b);
		toolBar.getPlay().setEnabled(b);
		toolBar.getStop().setEnabled(b);
		toolBar.getFPS().setEnabled(b);
		toolBar.getEditor().setEnabled(b);
		toolBar.repaint();
		//MovieMenu items
		Component[] components = movieMenu.getMenuComponents();
		Component c;
		for (int i = 0; i < components.length; i++) {
			c = components[i];
			if (c instanceof JMenuItem) c.setEnabled(b);
		}
	}
	
	/** Set the internalFrame status. */
	private void setFrame()
	{
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
	}
	
	/** Reset the sliders' values when a new image is selected. */
	private void resetSliders(int maxT, int t, int maxZ, int z)
	{
		tSlider.removeChangeListener(control);
		tSlider.setMaximum(maxT);
		tSlider.setValue(t);
		tSlider.addChangeListener(control);
		zSlider.removeChangeListener(control);
		zSlider.setMaximum(maxZ);
		zSlider.setValue(z);
		zSlider.addChangeListener(control);
		zSlider.setEnabled(maxZ != 0);
		tSlider.setEnabled(maxT != 0);
	}

	/** Initiliazes the z-slider and t-slider. */
	private void initSliders(int maxT, int t, int maxZ, int z)
	{
		tSlider = new JSlider(JSlider.HORIZONTAL, 0, maxT, t);
		String s = "Move the slider to navigate across time.";
		tSlider.setToolTipText(UIUtilities.formatToolTipText(s));
		tSlider.setEnabled(maxT != 0);
		zSlider = new JSlider(JSlider.VERTICAL, 0, maxZ, z);
		s = "Move the slider to navigate across Z stack.";
		zSlider.setToolTipText(UIUtilities.formatToolTipText(s));
		zSlider.setEnabled(maxZ != 0);
	}
	
	/**
	 * Display the image in the viewer.
	 * 
	 * @param img	Buffered image to display.
	 */
	 void setImage(BufferedImage img)
	 {
		int w = img.getWidth();
		int h = img.getHeight();
		Dimension d = new Dimension(w, h);
		canvas.setPreferredSize(d);
		canvas.setSize(d);
		canvas.paintImage(img);
		if (!active)
			setWindowSize(w+2*EXTRA, h+2*EXTRA+2*toolBar.getHeight());
	 	active = true;
	 	revalidate();
	}

	/** Create a menu. */
	private JMenuBar createMenuBar(int maxT)
	{
		JMenuBar menuBar = new JMenuBar(); 
		createMovieMenu(maxT);
		menuBar.add(createControlMenu());
		menuBar.add(movieMenu);
		return menuBar;
	}

	/** Create a Movie menu. */
	private void createMovieMenu(int maxT)
	{
		IconManager im = IconManager.getInstance(registry);
		movieMenu = new JMenu("Movie");
		JMenuItem menuItem = new JMenuItem("Play", 
									im.getIcon(IconManager.MOVIE));
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_PLAY);
		menuItem.setEnabled(maxT != 0);
		movieMenu.add(menuItem);
		menuItem = new JMenuItem("Stop", 
									im.getIcon(IconManager.STOP));
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_STOP);
		menuItem.setEnabled(maxT != 0);
		movieMenu.add(menuItem);
		menuItem = new JMenuItem("Rewind", 
									im.getIcon(IconManager.REWIND));
		menuItem.setEnabled(maxT != 0);
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_REWIND);
		movieMenu.add(menuItem);
	}
	
	/** Create the control Menu. */
	private JMenu createControlMenu()
	{
		JMenu menu = new JMenu("Controls");
		JMenuItem menuItem = new JMenuItem("Rendering");
		control.attachItemListener(menuItem, ViewerCtrl.RENDERING);
		menu.add(menuItem);
		menuItem = new JMenuItem("Inspector");
		control.attachItemListener(menuItem, ViewerCtrl.INSPECTOR);
		menu.add(menuItem);
		menuItem = new JMenuItem("SAVE AS...");
		control.attachItemListener(menuItem, ViewerCtrl.SAVE_AS);
		menu.add(menuItem);
		return menu;
	}	
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(toolBar);
		canvas = new ImageCanvas();
		contents = new JPanel();
		buildContents();
		JScrollPane scrollPane = new JScrollPane(contents);
		container.add(scrollPane);
		setFrameIcon(IconManager.getOMEIcon());
	}
		
	private void buildContents()
	{
		JPanel p = new JPanel(), pt = new JPanel(), pz = new JPanel();

		//t-slider
		JLabel label = new JLabel("T ");
		pt.setLayout(new BoxLayout(pt, BoxLayout.X_AXIS));
		pt.add(Box.createRigidArea(HBOX));
		pt.add(label);
		pt.add(tSlider);
		//z-slider
		label = new JLabel("Z ");
		pz.setLayout(new BoxLayout(pz, BoxLayout.Y_AXIS));
		pz.add(label);
		pz.add(zSlider);

		//image+z-slider panel
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(pz);
		p.add(canvas);
		contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
		contents.add(p);
		contents.add(pt);
	}
	
	/** Set the size of the window w.r.t the size of the screen. */
	private void setWindowSize(int w, int h)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 8*(screenSize.width/10);
		int height = 9*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;
		setSize(w, h);		
	}
	
}
