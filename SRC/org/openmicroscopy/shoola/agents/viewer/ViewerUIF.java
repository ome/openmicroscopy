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
	
	/** Constants usd to draw the XY-axis. */
	public static final int			START = 25, ORIGIN = 5, LENGTH = 20, 
									ARROW = 3;
	
	private static final Dimension  HBOX = new Dimension(20, 0);
	
	/** Canvas to display the currently selected 2D image. */
	private ImageCanvas             canvas;
	
	/** z-slider and t-slider. */
	private JSlider					tSlider, zSlider;
	
	/** Tool bar of the Agent. */
	private ToolBar					toolBar;
	
	/** Movie menu. */
	private JMenu					movieMenu;
	
	private ViewerCtrl 				control;
	
	private Registry				registry;
	
	private IconManager				im;
	
	private JScrollPane 			scrollPane;

	private boolean					active;
	
	ViewerUIF(ViewerCtrl control, Registry registry, PixelsDimensions pxsDims, 
				int defaultT, int defaultZ)
	{
		setFrame();
		active = false;
		this.control = control;
		this.registry = registry;
		im = IconManager.getInstance(registry);
		int maxT = pxsDims.sizeT-1;
		int maxZ = pxsDims.sizeZ-1;
		int t = defaultT;
		int z = defaultZ;
		setJMenuBar(createMenuBar(maxT));
		toolBar = new ToolBar(control, registry, maxT, t, maxZ, z);
		initSliders(maxT, t, maxZ, z);
		buildGUI();
	}
	
	public JScrollPane getScrollPane() { return scrollPane; }
	
	public JSlider getTSlider() { return tSlider; }
	
	public JSlider getZSlider() { return zSlider; }
	
	public ToolBar getToolBar() { return toolBar; } 
	
	void setActive(boolean b) { active = b; }
	
	/** Display the name of the image in the title. */
	void setImageName(String imageName) { setTitle(imageName); }
	
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
		toolBar.getPause().setEnabled(b);
		toolBar.getForward().setEnabled(b);
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
		if (!active) {
			int imageWidth = img.getWidth()+2*START, 
			imageHeight  = img.getHeight()+2*START;
			Dimension d = new Dimension(imageWidth, imageHeight);
			canvas.setPreferredSize(d);
			canvas.setSize(d);
			setWindowSize(imageWidth+4*START, imageHeight+6*START);
		} 
		canvas.paintImage(img);
		active = true; 
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
		movieMenu = new JMenu("Movie");
		JMenuItem menuItem = new JMenuItem("Play", 
									im.getIcon(IconManager.MOVIE));
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_PLAY);
		menuItem.setEnabled(maxT != 0);
		movieMenu.add(menuItem);
		menuItem = new JMenuItem("Pause", 
									im.getIcon(IconManager.PLAYER_PAUSE));
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_PAUSE);
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
		menuItem = new JMenuItem("Forward", 
								im.getIcon(IconManager.FORWARD));
		menuItem.setEnabled(maxT != 0);
		control.attachItemListener(menuItem, ViewerCtrl.MOVIE_FORWARD);
		movieMenu.add(menuItem);
	}
	
	/** Create the control Menu. */
	private JMenu createControlMenu()
	{
		JMenu menu = new JMenu("Controls");
		JMenuItem menuItem = new JMenuItem("Rendering", 
										im.getIcon(IconManager.RENDER));
		control.attachItemListener(menuItem, ViewerCtrl.RENDERING);
		menu.add(menuItem);
		menuItem = new JMenuItem("Inspector", 
									im.getIcon(IconManager.INSPECTOR));
		control.attachItemListener(menuItem, ViewerCtrl.INSPECTOR);
		menu.add(menuItem);
		menuItem = new JMenuItem("SAVE AS...", im.getIcon(IconManager.SAVEAS));
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
		canvas = new ImageCanvas(this);
		scrollPane = new JScrollPane(canvas);
		container.add(buildMain());
		container.add(buildTPanel());
		setFrameIcon(im.getIcon(IconManager.VIEWER));
	}
	
	/** Build and layout panel with slider and scrollpane. */
	private JPanel buildMain()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		//z-slider
		JLabel label = new JLabel("Z ");
		JPanel pz = new JPanel();
		pz.setLayout(new BoxLayout(pz, BoxLayout.Y_AXIS));
		pz.add(label);
		pz.add(zSlider);
		p.add(pz);
		p.add(scrollPane);
		return p;
		
	}
	
	/** Build panel with the tSlider. */
	private JPanel buildTPanel()
	{
		JPanel p = new JPanel();
		JLabel label = new JLabel("T ");
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createRigidArea(HBOX));
		p.add(label);
		p.add(tSlider);
		return p;
	}
	
	/** Set the size of the window w.r.t the size of the screen. */
	private void setWindowSize(int w, int h)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 7*(screenSize.width/10);
		int height = 7*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;
		setTBSize(w);
		setSize(w, h);		
	}
	
	/** Add a rigid area to the toolBar. */
	private void setTBSize(int w)
	{
		Dimension d = toolBar.getSize();
		if (w-d.width > 0)
			toolBar.add(Box.createRigidArea(new Dimension(w-d.width, 1)));
	}
	
}
