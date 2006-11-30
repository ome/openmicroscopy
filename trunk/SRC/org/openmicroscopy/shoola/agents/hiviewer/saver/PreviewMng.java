/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.PreviewMng
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;




//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutUtils;

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
class PreviewMng
    implements ActionListener
{
    
    /** Action command ID: save the preview image.*/
    private static final int    SAVE = 0;
    
    /** Action command ID: redo the preview image.*/
    private static final int    PREVIEW = 1;
    
    /** Action command ID: close the preview widget.*/
    private static final int    CANCEL = 2;
    
    /** Default color of the background. */
    private static final Color  DEFAULT_BG = Color.WHITE;
    
    /** Default space between each thumbnails. */
    private static final int    DEFAULT_SPACE = 4;
    
    /** Reference to the view. */
    private Preview         view;
    
    /** Reference to the parent model. */
    private ContainerSaver  model;
    
    /** The maximum size of the tumbnails. */
    private Dimension       maxDim;
    
    /** The previewed image. */
    private BufferedImage   image;
    
    /** Creates and paints the previewed image. */
    private void preview()
    {
        int index = view.colors.getSelectedIndex();
        Color c = view.getSelectedColor(index);
        int space = Integer.parseInt((String) view.spacing.getSelectedItem());
        createImage(c, space);
        view.previewCanvas.paintImage();
    }
    
    /** 
     * Builds the previewed image. 
     * 
     * @param color The background color of the image.
     * @param space The space between each thumbnail.
     * */
    private void createImage(Color color, int space)
    {
        Set thumbnails = model.getThumbnails();
        if (maxDim == null)  maxDim(thumbnails);
        int n = thumbnails.size();
        n = (int) Math.ceil(Math.sqrt(n));
        BufferedImage child;
        Iterator children = thumbnails.iterator();
        int w = maxDim.width*n+(n+1)*space;
        int h = maxDim.height*n+(n+1)*space;
        BufferedImage newImage = new BufferedImage(w, h, 
                            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);
        int finalI = 0;
        try {
            for (int i = 0; i < n; ++i) {
                finalI = i;
                for (int j = 0; j < n; ++j) {
                    if (!children.hasNext()) //Done, less than n^2 children.
                        return;  //Go to finally.
                    child = (BufferedImage) children.next();
                    //if (i == 0) finalWidth += gap+maxWidth;
                    g2.drawImage(child, null, (j+1)*space+j*maxDim.width, 
                                (i+1)*space+i*maxDim.height);
                }
            }  
            finalI++;
        } finally {
            double ratio = (double) thumbnails.size()/n;
            double d = Math.floor(ratio);
            double diff = Math.abs(ratio-d);
            if (diff != 0)  finalI++; 
            int hSub = maxDim.height*finalI+(finalI+1)*space;
            image = newImage.getSubimage(0, 0, w, hSub);
        }
    }
    
    /**
     * Determines the maximum dimension of a thumbnail.
     * 
     * @param thumbnails The collection of thumbnails.
     */
    private void maxDim(Set thumbnails)
    {
        maxDim = new Dimension(0, 0);
        BufferedImage child;
        Iterator children = thumbnails.iterator();
        Dimension d;
        while (children.hasNext()) {
            child = (BufferedImage) children.next();
            d = new Dimension(child.getWidth(), child.getHeight());
            maxDim = LayoutUtils.max(maxDim, d);
        }
    }
    
    /** Initializes the listeners. */
    private void initListeners()
    {
        attachButtonListener(view.save, SAVE);
        attachButtonListener(view.cancel, CANCEL);
        attachButtonListener(view.preview, PREVIEW);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.closeWindow(); }
        });
    }
    
    /** Attaches listeners to a JButton. 
     * 
     * @param button 	The component to attach the listener to
     * @param id		The action command id.
     */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view The view this class controls. Mustn't be <code>null</code>.
     * @param model Reference to the parent model. Mustn't be <code>null</code>.
     */
    PreviewMng(Preview view, ContainerSaver  model)
    {
        if (view == null) throw new IllegalArgumentException("No view.");
        if (model == null) throw new IllegalArgumentException("No model.");
        this.view = view;
        this.model = model;
        maxDim = null;
        initListeners();
    }

    /**
     * Returns the previewed image.
     * 
     * @return See below.
     */
    BufferedImage getImage()
    {
        if (image == null) createImage(DEFAULT_BG, DEFAULT_SPACE);
        return image;
    }
    
    /** 
     * Handles action fired by JButton. 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case SAVE:
                    view.closeWindow();
                    model.saveImage(getImage()); break;
                case CANCEL:
                    view.closeWindow(); 
                    model.closeWindow();
                    break;
                case PREVIEW:
                    preview();   
            } 
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }

}
