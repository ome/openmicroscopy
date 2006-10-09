/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserModel
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
import java.awt.Point;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.Magnifier;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * The Model component in the <code>Browser</code> MVC triad.
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
class BrowserModel
{

    /** Default length of the scale bar. */
    private static final int    LENGTH = 40;
    
    /** Reference to the component that embeds this model. */ 
    private Browser         component;
    
    /** Reference to the magnifier displayed on top of the image. */
    private Magnifier       magnifier;
    
    /** The original image. */
    private BufferedImage   renderedImage;
    
    /**
     * The image painted on screen.
     * This image may have been transformed i.e. zoomed, sharpened etc.
     */
    private BufferedImage   displayedImage;
    
    /** The lens image created if requested. */
    private BufferedImage   lensImage;
    
    /** The zoom factor. */
    private double          zoomFactor;
    
    /** The title of the {@link Browser}. */
    private String          title;
    
    /** Reference to the {@link ImViewer}. */
    private ImViewer        parent;
    
    /** 
     * Flag to indicate if the unit bar is painted or not on top of the
     * displayed image.
     */
    private boolean         unitBar;
    
    /** The value by which to increase or decrease the size of the unit bar. */
    private double          unit;
    
    /** 
     * Creates a new instance.
     * 
     * @param title The title of the  {@link Browser}.
     * @param parent    The parent of this component.
     *                  Mustn't be <code>null</code>.
     */
    BrowserModel(String title, ImViewer parent)
    {
        if (parent == null) throw new IllegalArgumentException("No parent.");
        this.parent = parent;
        this.title = title;
        unitBar = true;
        unit = -1;
    }
    
    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }

    /**
     * Sets the rendered image.
     * 
     * @param image The image to set.
     */
    void setRenderedImage(BufferedImage image)
    {
        renderedImage = image;
        displayedImage = null;
    }
    
    /**
     * Returns the image to paint on screen. This image is a transformed 
     * version of the rendered image. We apply several transformations to the
     * {@link #renderedImage} e.g. zooming.
     * 
     * @return See above.
     */
    BufferedImage getDisplayedImage() { return displayedImage; }
    
    /**
     * Returns the rendered image.
     * 
     * @return See above.
     */
    BufferedImage getRenderedImage() { return renderedImage; }
    
    /**
     * Sets the zoom factor.
     * 
     * @param factor See above.
     */
    void setZoomFactor(double factor) { zoomFactor = factor; }

    /**
     * Returns the zoom factor.
     * 
     * @return See above.
     */
    double getZoomFactor() { return zoomFactor; }
    
    /**
     * Returns the title of the {@link Browser}.
     * 
     * @return See above.
     */
    String getTitle() { return title; }
    
    /**
     * Creates the {@link #displayedImage}. The method should be invoked
     * after the {@link #setRenderedImage(BufferedImage)} method.
     */
    void createDisplayedImage()
    {
        if (renderedImage == null) return;
        if (zoomFactor != ZoomAction.DEFAULT_ZOOM_FACTOR) 
            displayedImage = Factory.magnifyImage(renderedImage, zoomFactor, 0);
        else displayedImage = renderedImage;
    }
    
    /**
     * Creates the lens image when requested. Then when the mouse is dragged,
     * a subimage of the lens image is extracted and returned.
     * 
     * @param f The lens factor.
     */
    void createLensImage(double f)
    {
        if (lensImage == null)
            lensImage = Factory.magnifyImage(displayedImage, f, 0);
    }
    
    /**
     * The size in microns of a pixel along the X-axis.
     * 
     * @return See above.
     */
    float getPixelsSizeX() { return parent.getPixelsSizeX(); }
    
    /**
     * Returns <code>true</code> if the unit bar is painted on top of 
     * the displayed image, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isUnitBar() { return unitBar; }
    
    /**
     * Sets the value of the flag controlling if the unit bar is painted or not.
     * 
     * @param unitBar   Pass <code>true</code> to paint the unit bar, 
     *                  <code>false</code> otherwise.
     */
    void setUnitBar(boolean unitBar) { this.unitBar = unitBar; }
    
    /**
     * Sets the size of the unit bar.
     * 
     * @param size The size of the unit bar.
     */
    void setUnitBarSize(double size)
    {
        unit = size/getPixelsSizeX();
    }
    
    /**
     * Returns the size of the unit bar.
     * 
     * @return See above.
     */
    double getUnitBarSize()
    { 
        if (unit == -1) setUnitBarSize(5);
        return unit; 
    }
    
    Magnifier getMagnifier() { return magnifier; }
    
    BufferedImage getSubLensImage(Point p)
    {
        return lensImage;
    }

    void setMagnifier(Magnifier magnifier)
    {
        this.magnifier = magnifier;
    }
    
}
