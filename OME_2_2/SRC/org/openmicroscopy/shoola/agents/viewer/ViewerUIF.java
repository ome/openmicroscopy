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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import org.openmicroscopy.shoola.env.ui.TopWindow;
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
	extends TopWindow
{
	
	/** Constants usd to draw the XY-axis. */
	public static final int			START = 25, ORIGIN = 5, LENGTH = 20, 
									ARROW = 3;

	/** Canvas to display the currently selected 2D image. */
	private ImageCanvas             canvas;
	
    private JLayeredPane            layer;
    
	/** z-slider and t-slider. */
	private JSlider					tSlider, zSlider;
	
	/** Tool bar of the Agent. */
	private ToolBar					toolBar;
	
	private JMenuItem				viewer3DItem;
	
    private JMenuItem               movieItem;
    
	private ViewerCtrl 				control;
	
	private IconManager				im;
	
	private JScrollPane 			scrollPane;

	private boolean					active;
	
	ViewerUIF(ViewerCtrl control, Registry registry, PixelsDimensions pxsDims, 
				int defaultT, int defaultZ)
	{
		super("", registry.getTaskBar());
		active = false;
		this.control = control;
		im = IconManager.getInstance(registry);
		int maxT = pxsDims.sizeT-1;
		int maxZ = pxsDims.sizeZ-1;
		setJMenuBar(createMenuBar(maxZ, maxT));
		toolBar = new ToolBar(control, registry, maxT, defaultT, maxZ, 
								defaultZ);
		initSliders(maxT, defaultT, maxZ, defaultZ);
		buildGUI();
	}

    public ImageCanvas getCanvas() { return canvas; }
    
	public JScrollPane getScrollPane() { return scrollPane; }
	
	public JSlider getTSlider() { return tSlider; }
	
	public JSlider getZSlider() { return zSlider; }
	
	public ToolBar getToolBar() { return toolBar; } 
	
    /** 
     * Return the bufferedImage displayed. 
     * Note that we save the zoomed image, not the original bufferedImage. 
     */
    BufferedImage getDisplayImage() { return canvas.getDisplayImage(); }
    
	void setActive(boolean b) { active = b; }
	
    /** Reset the zoomLevel to 1. */
    void resetMagFactor() { canvas.resetMagFactor(); }
    
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
		
		boolean bT = false, bZ = false;
		if (maxT != 0) bT = true;
		if (maxZ != 0) bZ = true;
		
		toolBar.getTField().setEditable(bT);
		toolBar.getZField().setEditable(bZ);
		toolBar.getViewer3D().setEnabled(bZ);
		viewer3DItem.setEnabled(bZ);
        if (bZ && bT) movieItem.setEnabled(true);
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
		tSlider = new JSlider(JSlider.VERTICAL, 0, maxT, t);
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
     * @param img   Buffered image to display.
     */
     void setImage(BufferedImage img)
     {
        if (!active) {
            int w = img.getWidth()+2*START; 
            int h  = img.getHeight()+2*START;
            setSizePaintedComponents(new Dimension(w, h));
            setWindowSize(img.getWidth()+4*START, img.getHeight()+4*START);
        } 
        canvas.paintImage(img);
        active = true; 
    }
    
    /** Set the size of the components used to display the image. */
    void setSizePaintedComponents(Dimension d)
    {
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        layer.setPreferredSize(d);
        layer.setSize(d);
    }
	
	/** Create a menu. */
	private JMenuBar createMenuBar(int maxZ, int maxT)
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createControlsMenu(maxZ, maxT));
		return menuBar;
	}

	/** Create the control Menu. */
	private JMenu createControlsMenu(int maxZ, int maxT)
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
		viewer3DItem = new JMenuItem("3D view", 
						im.getIcon(IconManager.VIEWER3D));
		control.attachItemListener(viewer3DItem, ViewerCtrl.VIEWER3D);
		viewer3DItem.setEnabled(maxZ != 0);
		menu.add(viewer3DItem);
        movieItem = new JMenuItem("Movie", im.getIcon(IconManager.MOVIE));
        control.attachItemListener(menuItem, ViewerCtrl.MOVIE);
		menu.add(movieItem);
		menuItem = new JMenuItem("SAVE AS...", im.getIcon(IconManager.SAVEAS));
		control.attachItemListener(menuItem, ViewerCtrl.SAVE_AS);
		menu.add(menuItem);
        
        if (maxT == 0 && maxZ == 0) movieItem.setEnabled(false);
		return menu;
	}
		
	/**
	 * Specifies icons, text, and tooltips for the display buttons in the
	 * TaskBar.
	 * Those buttons are managed by the superclass, we only have to specify
	 * what they should look like.
	 */
	private void configureDisplayButtons()
	{
		configureQuickLaunchBtn(im.getIcon(IconManager.VIEWER), 
												"Bring up the Viewer.");
		configureWinMenuEntry("Viewer ", 
							im.getIcon(IconManager.VIEWER));
	}
		
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
        Container container = getContentPane();
        layer = new JLayeredPane();
        canvas = new ImageCanvas(this);        
        layer.add(canvas, new Integer(0));
        scrollPane = new JScrollPane(layer);
        container.add(toolBar, BorderLayout.NORTH);
        container.add(buildMain(), BorderLayout.WEST);
        container.add(scrollPane, BorderLayout.CENTER);
        //Configure the display buttons in the TaskBar.
        configureDisplayButtons();
	}
	
	/** Build and lay out the panel with slider and scrollpane. */
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
		
        //t-slider
        label = new JLabel("T ");
        JPanel pt = new JPanel();
        pt.setLayout(new BoxLayout(pt, BoxLayout.Y_AXIS));
        pt.add(label);
        pt.add(tSlider);
        p.add(pz);
        p.add(pt);
		return p;
	}
	
	/** Set the size of the window w.r.t. the size of the screen. */
	private void setWindowSize(int w, int h)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 7*(screenSize.width/10);
		int height = 7*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;
		setSize(w, h);		
	}

}
