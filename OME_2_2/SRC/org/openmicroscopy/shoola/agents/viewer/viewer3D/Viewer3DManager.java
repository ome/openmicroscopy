/*
 * org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D;

//Java imports
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.DrawingCanvasMng;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Viewer3DManager
{

    /** Current image displayed. */
    private BufferedImage   XYimage, XZimage, ZYimage;
    
    /** Reference to the {@link Viewer3D view}. */
    private Viewer3D        view;
    
    private ViewerCtrl      control;
    
    /** Color model before 3D view. */
    private int             previousModel;
    
    public Viewer3DManager(Viewer3D view, ViewerCtrl control, int previousModel)
    {
        this.view = view;
        this.control = control;
        this.previousModel = previousModel;
        System.out.println("model: "+previousModel);
        attachListener();
    }
    
    /** Attach a window listener to the dialog. */
    private void attachListener()
    {
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { onClosing(); }
        });
    }
        
    public BufferedImage getXYimage() { return XYimage; }
    
    public BufferedImage getXZimage() { return XZimage; }

    public BufferedImage getZYimage() { return ZYimage; }

    /** Forward event to the {@link Viewer3D view}. */
    public void onPlaneSelected(int x, int y)
    {
        view.onPlaneSelected(x, y);
    }
    
    /** Forward event to the {@link Viewer3D view}. */
    public void onXZPlaneSelected(int x, int z)
    {
        view.onPlaneSelected(z, x, Viewer3D.XZ);
    }
    
    /** Forward event to the {@link Viewer3D view}. */
    public void onZYPlaneSelected(int z, int y)
    {
        view.onPlaneSelected(z, y, Viewer3D.ZY);
    }
    
    /** 
     * Set the drawing area. 
     * 
     * @param x x-coordinate of top-left corner.
     * @param y y-coordinate of top-left corner.
     */
    public void setDrawingBounds(int x, int y, int w, int h)
    {
        view.drawing.setPreferredSize(new Dimension(w, h));
        view.drawing.setBounds(x, y, w, h); 
    }
    
    
    /**
     * Display the 3 images in the canvas for the first time.
     * 
     * @param XYimage   XYImage to display.
     * @param XZimage   XZImage to display.
     * @param ZYimage   ZYImage to display.
     */
    void setImages(BufferedImage XYimage, BufferedImage XZimage, 
                    BufferedImage ZYimage)
    {
        this.XYimage = XYimage;
        this.XZimage = XZimage;
        this.ZYimage = ZYimage;
        int xyW = XYimage.getWidth(), xyH = XYimage.getHeight(),
            xzH = XZimage.getHeight(), zyW = ZYimage.getWidth();
        
        int xMax = zyW+xyW+3*Viewer3D.SPACE;
        int yMax = xzH+xyH+3*Viewer3D.SPACE;
        setSizePaintedComponents(new Dimension(xMax, yMax));
        view.canvas.paintImages(xyW, zyW, xyH);
        DrawingCanvasMng dm = view.drawing.getManager(); 
        dm.setDrawingAreas(xyW, xyH, zyW);
        view.drawing.setSizes(xyW, xyH, zyW);
        setWindowSize(new Dimension(xMax+2*Viewer3D.SPACE, 
                yMax+2*Viewer3D.SPACE));
       
    } 
    
    /** The XZImage and ZYImage to display. */
    void setImages(BufferedImage XZimage, BufferedImage ZYimage)
    {
        this.XZimage = XZimage;
        this.ZYimage = ZYimage;
        view.canvas.repaint();  
    }
    
    /** The XZImage, ZYImage and XYImage to display. */ 
    void resetImages(BufferedImage XYimage, BufferedImage XZimage, 
                    BufferedImage ZYimage)  
    {
        this.XYimage = XYimage;
        this.XZimage = XZimage;
        this.ZYimage = ZYimage; 
        view.canvas.repaint();
    }
    
    /** Reset the color model and planeDef. */
    void onClosing()
    {
        control.synchPlaneSelected(view.getCurZ(), previousModel);
        view.dispose();
    }
    
    /** Set the size of the canvas and layer. */
    private void setSizePaintedComponents(Dimension d)
    {
        view.canvas.setPreferredSize(d);
        view.canvas.setSize(d);
        view.layer.setPreferredSize(d);
        view.layer.setSize(d);
    }
    
    /** Set the size of the window. */
    private void setWindowSize(Dimension d)
    {
        int w = d.width;
        int h = d.height;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 7*(screenSize.width/10);
        int height = 7*(screenSize.height/10);
        if (w > width) w = width;
        if (h > height) h = height; 
        view.setSize(w, h);
    }
    
}
