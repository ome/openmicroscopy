/*
 * org.openmicroscopy.shoola.agents.viewer.util.Preview
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

package org.openmicroscopy.shoola.agents.viewer.util;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.canvas.PreviewCanvas;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Preview of the image to save.
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
class Preview
    extends JDialog
{
    
    private static final Dimension  HBOX = new Dimension(10, 0);
    
    PreviewCanvas                   canvas;
     
    JLayeredPane                    layer;
    
    JButton                         save, cancel;
    
    Preview(ImageSaverMng mng)
    {
        super(mng.getReferenceFrame(), "Save Preview", true);
        PreviewMng manager = new PreviewMng(this, mng);
        initComponents(IconManager.getInstance(mng.getRegistry()));
        manager.attachListeners();
        buildGUI();
    }
    
    /** Initializes the components. */
    private void initComponents(IconManager im)
    {
        canvas = new PreviewCanvas();
        layer = new JLayeredPane();
        layer.add(canvas, new Integer(0));
        save = new JButton(im.getIcon(IconManager.SAVE));
        save.setToolTipText(
            UIUtilities.formatToolTipText("Save the preview image."));
        cancel = new JButton(im.getIcon(IconManager.CLOSE));
        cancel.setToolTipText(
            UIUtilities.formatToolTipText("Don't save the preview image."));
    }
    
    /** Set the {@link BufferedImage} to save in the canvas. */
    void setImage(BufferedImage img)
    { 
        int w = img.getWidth()+3*ViewerUIF.START;
        int h = img.getHeight()+2*ViewerUIF.START;
        setContainerSize(new Dimension(w, h));
        w += 2*ViewerUIF.START;
        h += 2*ViewerUIF.START;
        setWindowSize(w, h);
        canvas.paintImage(img); 
    }
    
    /** Set the {@link BufferedImage}s to save in the canvas. */
    void setImages(BufferedImage img, BufferedImage lensImage)
    { 
        int w = img.getWidth()+lensImage.getWidth()+3*ViewerUIF.START;
        int h = img.getHeight()+lensImage.getHeight()+2*ViewerUIF.START;
        setContainerSize(new Dimension(w, h));
        w += 2*ViewerUIF.START;
        h += 2*ViewerUIF.START;
        setWindowSize(w, h);
        canvas.paintImages(img, lensImage);
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        JScrollPane scrollPane = new JScrollPane(layer);
        canvas.setContainer(scrollPane);
        getContentPane().add(buildBar(), BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
    }
    
    /** Set the buttons in a {@link JToolBar}. */
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(save);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(cancel);
        return bar;
    }
    
    /** Set the dimension of the component containing the images. */
    private void setContainerSize(Dimension d)
    {
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        layer.setPreferredSize(d);
        layer.setSize(d);
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
