/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserUI
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts the UI components displaying the rendered image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class BrowserUI
    extends JScrollPane
{

    /** Location of the top left corner of the image. */
    static final int            TOP_LEFT_IMAGE = 35;
    
    /**
     * The Layered pane hosting the {@link BrowserCanvas} and any other 
     * UI components added on top of it.
     */
    private JLayeredPane        layeredPane;

    /** The canvas hosting the image. */
    private BrowserCanvas       browserCanvas;
    
    /** Reference to the Model. */
    private BrowserModel        model;
    
    /** Reference to the Control. */
    private BrowserControl      controller;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        layeredPane = new JLayeredPane();
        browserCanvas = new BrowserCanvas(model, this);
        //The image canvas is always at the bottom of the pile.
        layeredPane.add(browserCanvas, new Integer(0));
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        getViewport().add(layeredPane);
    }
    
    /**
     * Sets the size of the components b/c a layeredPane doesn't have a layout
     * manager.
     * 
     * @param w The width to set.
     * @param h The height to set.
     */
    private void setComponentsSize(int w, int h)
    {
        Dimension d = new Dimension(w, h);
        layeredPane.setPreferredSize(d);
        layeredPane.setSize(d);
        browserCanvas.setPreferredSize(d);
        browserCanvas.setSize(d);
    }
    
    /** Creates a new instance. */
    BrowserUI() {}
    
    /**
     * Links this View to its Controller and Model
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     */
    void initialize(BrowserControl controller, BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) throw new NullPointerException("No control.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }

    /**
     * Adds the component to the {@link #layeredPane}. The component will
     * be added to the top of the pile
     * 
     * @param c The component to add.
     */
    void addComponentToLayer(JComponent c)
    {
        layeredPane.add(c, new Integer(1));
    }
    
    /**
     * Removes the component from the {@link #layeredPane}.
     * 
     * @param c The component to remove.
     */
    void removeComponentFromLayer(JComponent c)
    {
        layeredPane.remove(c);
    }

    /**
     * Creates the displayed image and paints it.
     * This method should be called straight after setting the 
     * rendered image.
     */
    void paintImage()
    {
        if (model.getRenderedImage() == null) return;
        BufferedImage img = model.getDisplayedImage();
        model.createDisplayedImage();
        //first time so we set the size of the canvas and layeredPane.
        if (img == null) {
            img = model.getDisplayedImage();
            setComponentsSize(img.getWidth()+2*TOP_LEFT_IMAGE,
                                img.getHeight()+2*TOP_LEFT_IMAGE);
        }
        browserCanvas.repaint();
    }
    
    /** Displays the zoomed image. */
    void zoomImage()
    {
        if (model.getRenderedImage() == null) return;
        model.createDisplayedImage();
        BufferedImage img = model.getDisplayedImage();
        setComponentsSize(img.getWidth()+2*TOP_LEFT_IMAGE,
                            img.getHeight()+2*TOP_LEFT_IMAGE);
        browserCanvas.repaint();
    }
    
}
