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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
import org.openmicroscopy.shoola.agents.viewer.canvas.LensCanvas;
import org.openmicroscopy.shoola.agents.viewer.controls.BottomBar;
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBar;
import org.openmicroscopy.shoola.agents.viewer.controls.ToolBarManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * View of the {@link Viewer} Agent.
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
    
    public static final String      NANOMETER = " \u00B5m";
    
    public static final BasicStroke SCALE_STROKE = new BasicStroke(2.0f);
    
    public static final Color       SCALE_COLOR = new Color(204, 255, 204);
    
    public static final int         SCALE_BORDER = 5;
    
    /** Position of the canvas in the layer. */
    public static final int         IMAGE_LEVEL = 0, LENS_LEVEL = 1, 
                                    ROI_LEVEL = 2;
    
    public static final int         ANNOTATE_LEVEL = 3;
    
    /** Background color. */
    public static final Color       BACKGROUND_COLOR = new Color(204, 204, 255);
    
    public static final Color       STEELBLUE = new Color(0x4682B4);
    
    public static final Dimension   TOOLBAR_DIMENSION = new Dimension(20, 300);

    /** Dimension of the separator between the toolBars. */
    public static final Dimension   SEPARATOR_END = new Dimension(100, 0);
    
    public static final Dimension   SEPARATOR = new Dimension(15, 0);
    
    /** Lens constant. */
    /** Constant to fix the width of the Lens. */
    public static final int         MAX_WITH = 100, DEFAULT_WIDTH = 60,
                                    MIN_WIDTH = 10;
    
    /** Constant to fix the magFactor for the Lens. */
    public static final double      MIN_MAG = 1.0, DEFAULT_MAG = 1.5, 
                                    MAX_MAG = 3.0;
    
    /** Constants used to draw the XY-axis. */
    public static final int         START = 35, ORIGIN = 5, LENGTH = 20, 
                                    ARROW = 3;
    
    private static final int        DEFAULT_WINDOW_SIZE = 100;
    
    /** Canvas to display the currently selected 2D image. */
    private ImageCanvas             canvas;
    
    /** Drawing canvas displayed on top of the {@link ImageCanvas canvas}. */
    private DrawingCanvas           drawingCanvas;
    
    /** Lens canvas displayed on top of the {@link ImageCanvas canvas}. */
    private LensCanvas              lensCanvas;
    
    /** z-slider and t-slider. */
    private JSlider                 tSlider, zSlider;
    
    /** Tool bar of the Agent. */
    private ToolBar                 toolBar;
    
    /** Bottom bar displaying message. */
    private BottomBar               bottomBar;
    
    private JMenuItem               viewer3DItem;
    
    private JMenuItem               movieItem;
    
    private ViewerCtrl              control;
    
    private IconManager             im;
    
    private JScrollPane             scrollPane;
    
    private boolean                 imageDisplay;

    private JLayeredPane            layer;
    
    private int                     windowWidth, windowHeight;
    
    ViewerUIF(ViewerCtrl control, Registry registry, PixelsDimensions pxsDims, 
                int defaultT, int defaultZ)
    {
        super("");
        this.control = control;
        im = IconManager.getInstance(registry);
        windowWidth = DEFAULT_WINDOW_SIZE;
        windowHeight = DEFAULT_WINDOW_SIZE;
        imageDisplay = false;
        int maxT = pxsDims.sizeT-1, maxZ = pxsDims.sizeZ-1;
        setJMenuBar(createMenuBar(maxZ, maxT));
        initBars(registry, maxT, defaultT, maxZ, defaultZ);
        initSliders(maxT, defaultT, maxZ, defaultZ);
        initContainers();
        buildGUI();
    }
    
    public LensCanvas getLensCanvas() { return lensCanvas; }
    
    public DrawingCanvas getDrawingCanvas() { return drawingCanvas; }
    
    public ImageCanvas getCanvas() { return canvas; }
    
    public JScrollPane getScrollPane() { return scrollPane; }
    
    public JSlider getTSlider() { return tSlider; }
    
    public JSlider getZSlider() { return zSlider; }
    
    public ToolBar getToolBar() { return toolBar; } 
    
    public BottomBar getBottomBar() { return bottomBar; } 
   
    void setImageDisplay(boolean b) { imageDisplay = b; }

    boolean isImageDisplay() { return imageDisplay; }
    
    /** Remove the lens if any pin. */
    void resetLens() { canvas.resetLens(); }
    
    /** Reset the zoomLevel to {@link ImageInspector#ZOOM_DEFAULT}. */
    void resetMagFactor() { canvas.resetDefault(); }
    
    /** Display the name of the image in the header. */
    void setImageName(String imageName) { setTitle(imageName); }
    
    /** Reset the default values for timepoint and z-section in the stack; */
    void setDefaultZT(int t, int z, int sizeT, int sizeZ)
    {
        ToolBarManager tbm = toolBar.getManager();
        tbm.onTChange(t);
        tbm.onZChange(z);
        int maxZ = sizeZ-1, maxT = sizeT-1;
        tbm.setMaxT(maxT);
        tbm.setMaxZ(maxZ);
        toolBar.getZLabel().setText("/"+maxZ);
        toolBar.getTLabel().setText("/"+maxT);
        resetSliders(maxT, t, maxZ, z);
        boolean bT = false, bZ = false, bZT = false;
        if (maxT != 0) bT = true;
        if (maxZ != 0) bZ = true;
        toolBar.getTField().setEditable(bT);
        toolBar.getZField().setEditable(bZ);
        toolBar.getViewer3D().setEnabled(bZ);
        if (bT || bZ) bZT = true;
        viewer3DItem.setEnabled(bZ);
        toolBar.getMovie().setEnabled(bZT);
        movieItem.setEnabled(bZT);
    }
    
    /**
     * Display the image in the viewer, and set the size of 
     * different components.
     * 
     * @param img   Buffered image to display.
     */
     void setImage(BufferedImage img)
     {
        if (!imageDisplay) { 
            int w = img.getWidth()+2*START, h = img.getHeight()+2*START;
            setSizePaintedComponents(new Dimension(w, h));
            w += 2*START;
            h += 2*START;
            setWindowSize(w, h);
        }
        canvas.paintImage(img);
        setImageDisplay(true);
    }

    void setUnitBarSize(double x) { canvas.setUnitBarSize(x); }
    
    /** Set the size of the components used to display the image. */
    void setSizePaintedComponents(Dimension d)
    {
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        layer.setPreferredSize(d);
        layer.setSize(d);
    }
    
    /** Add the {@link drawingCanvas} to the layer. */
    void addCanvasToLayer(JComponent component, int level)
    {
        layer.add(component, new Integer(level));
    }
    
    /** Remove the {@link drawingCanvas} from the layer. */
    void removeCanvasFromLayer(JComponent component)
    {
        layer.remove(component);
        layer.repaint();
    }

    private void initContainers()
    {
        layer = new JLayeredPane();
        drawingCanvas = new DrawingCanvas();
        lensCanvas = new LensCanvas();
        canvas = new ImageCanvas(this, control, lensCanvas);
        addCanvasToLayer(canvas, IMAGE_LEVEL);
        scrollPane = new JScrollPane(layer);
    }
    
    /** Initialize the toolBar and the bottom bar. */
    private void initBars(Registry reg, int maxT, int t, int maxZ, int z) 
    {
        toolBar = new ToolBar(control, reg, maxT, t, maxZ, z);
        bottomBar = new BottomBar();
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
        menuItem = new JMenuItem("ROI", im.getIcon(IconManager.ROI));
        control.attachItemListener(menuItem, ViewerCtrl.ROI);
        menu.add(menuItem);
        menuItem = new JMenuItem("Inspector", 
                                    im.getIcon(IconManager.INSPECTOR));
        control.attachItemListener(menuItem, ViewerCtrl.INSPECTOR);
        menu.add(menuItem);
        viewer3DItem = new JMenuItem("3D view", 
                        im.getIcon(IconManager.VIEWER3D));
        control.attachItemListener(viewer3DItem, ViewerCtrl.VIEWER3D);
        viewer3DItem.setEnabled(maxZ != 0);
        //menu.add(viewer3DItem);
        movieItem = new JMenuItem("Movie", im.getIcon(IconManager.MOVIE));
        control.attachItemListener(movieItem, ViewerCtrl.MOVIE);
        menu.add(movieItem);
        menuItem = new JMenuItem("SAVE AS...", im.getIcon(IconManager.SAVEAS));
        control.attachItemListener(menuItem, ViewerCtrl.SAVE_AS);
        menu.add(menuItem);
        
        if (maxT == 0 && maxZ == 0) movieItem.setEnabled(false);
        return menu;
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        Container container = getContentPane();
        container.add(toolBar, BorderLayout.NORTH);
        container.add(buildMain(), BorderLayout.WEST);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(bottomBar, BorderLayout.SOUTH);
    }
    
    /** Build and lay out a panel with slider and scrollpane. */
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
        int width = 8*(screenSize.width/10);
        int height = 8*(screenSize.height/10);
        if (w > width) w = width;
        if (h > height) h = height;
        windowWidth = w;
        windowHeight = h;
    }
    
    /** Overrides the {@link #setOnScreen()} method. */
    public void setOnScreen()
    {
        setSize(windowWidth, windowHeight); 
        UIUtilities.centerAndShow(this);
    }

}
