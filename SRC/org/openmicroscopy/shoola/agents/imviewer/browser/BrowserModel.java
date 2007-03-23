/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

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

	/** 
	 * Factor use to determine the size of the annotate image
	 * w.r.t the rendered image.
	 */
	static final double RATIO = 0.40;
	
	/** The title of the browser. */
	private static final String TITLE = "View";
	
	/** The title of the annotator. */
	private static final String TITLE_ANNOTATOR = "Annotation";
	
    /** Reference to the component that embeds this model. */ 
    private Browser         component;
    
    /** The original image. */
    private BufferedImage   renderedImage;
    
    /**
     * The image painted on screen.
     * This image may have been transformed i.e. zoomed, sharpened etc.
     */
    private BufferedImage   displayedImage;
    
    /** A smaller version (40%) of the original image. */
    private BufferedImage	annotateImage;
    
    /** The lens image created if requested. */
    private BufferedImage   lensImage;
    
    /** The zoom factor. */
    private double          zoomFactor;
    
    /** Reference to the {@link ImViewer}. */
    private ImViewer        parent;
    
    /** 
     * Flag to indicate if the unit bar is painted or not on top of the
     * displayed image.
     */
    private boolean         unitBar;
    
    /** The value of the unit bar in microns. */
    private double          unitInMicrons;
    
    /** The default color of the unit bar. */
    private Color			unitBarColor;
    
    /** The bacground color of the canvas. */
    private Color			backgroundColor;
    
    /** Unloaded image data. */
    private ImageData		data;
    
    /** 
     * Creates a new instance.
     * 
     * @param parent    The parent of this component.
     *                  Mustn't be <code>null</code>.
     * @param imageID	The id of the image.
     */
    BrowserModel(ImViewer parent, long imageID)
    {
        if (parent == null) throw new IllegalArgumentException("No parent.");
        //unloaded image data
        data = new ImageData();
        data.setId(imageID);
        this.parent = parent;
        unitBar = true;
        unitInMicrons = UnitBarSizeAction.getDefaultValue(); // size microns.
        unitBarColor = ImagePaintingFactory.UNIT_BAR_COLOR;
        backgroundColor = ImagePaintingFactory.DEFAULT_BACKGROUND;
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
        //Create the annotate image.
        if (renderedImage != null) {
        	annotateImage = Factory.magnifyImage(renderedImage, RATIO, 0);
        } else annotateImage = null;
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
    String getTitle() { return TITLE; }
    
    /**
     * Returns the icon of the {@link Browser}.
     * 
     * @return See above.
     */
    Icon getIcon()
    { 
    	IconManager icons = IconManager.getInstance();
    	return icons.getIcon(IconManager.VIEWER); 
    }
    
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
     * Returns the number of z-sections. 
     * 
     * @return See above.
     */
    int getMaxZ() { return parent.getMaxZ(); }
    
    /** 
     * Returns the number of timepoints. 
     * 
     * @return See above.
     */
    int getMaxT() { return parent.getMaxT(); }
    
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
    void setUnitBarSize(double size) { unitInMicrons = size; }
    
    /**
     * Returns the unit (in microns) used to determine the size of the unit bar.
     * 
     * @return See above.
     */
    double getUnitInMicrons() { return unitInMicrons; }
    
    /**
     * Returns the size of the unit bar.
     * 
     * @return See above.
     */
    double getUnitBarSize()
    { 
        double v = unitInMicrons;
        if (getPixelsSizeX() > 0) v = unitInMicrons/getPixelsSizeX();
        v *= zoomFactor;
        return v;
    }
    
    /**
     * Returns the unit bar value.
     * 
     * @return See above.
     */
    String getUnitBarValue()
    {
    	return UIUtilities.twoDecimalPlaces(unitInMicrons);
    }
    
    /**
     * Returns the color of the unit bar.
     * 
     * @return See above.
     */
    Color getUnitBarColor() { return unitBarColor; }

    /**
     * Sets the color of the unit bar.
     * 
     * @param c  The color to set.
     */
    void setUnitBarColor(Color c) { unitBarColor = c; }

    /**
     * Returns the background color of the canvas.
     * 
     * @return See above.
     */
	Color getBackgroundColor() { return backgroundColor; }
	
    /**
     * Set the background color of the canvas.
     * 
     * @param c  The color to set.
     */
	void setBackgroundColor(Color c) { backgroundColor = c; }
	
	/**
	 * Returns the unloaded image data.
	 * 
	 * @return See above.
	 */
	ImageData getImageData() { return data; }
	
	/**
     * Returns the title of the <code>Annotator</code>.
     * 
     * @return See above.
     */
    String getAnnotatorTitle() { return TITLE_ANNOTATOR; }
    
    /**
     * Returns the icon of the <code>Annotator</code>.
     * 
     * @return See above.
     */
    Icon getAnnotatorIcon()
    { 
    	IconManager icons = IconManager.getInstance();
    	return icons.getIcon(IconManager.ANNOTATION); 
    }
    
    /**
     * Returns a smaller version of the rendered image.
     * 
     * @return See above.
     */
    BufferedImage getAnnotateImage() { return annotateImage; }
    
    /**
     * Sets the selected XY-plane. A new plane is then rendered.
     * 
     * @param z The selected z-section.
     * @param t The selected timepoint.
     */
    void setSelectedXYPlane(int z, int t)
    {
    	if (z < 0) z = parent.getDefaultZ();
    	if (t < 0) t = parent.getDefaultT();
    	parent.setSelectedXYPlane(z, t);
    }
    
    /**
     * Returns the default z-section.
     * 
     * @return See above.
     */
	int getDefaultZ() { return parent.getDefaultZ(); }
	
	int getMaxX() { return parent.getMaxX(); }
	
	int getMaxY() { return parent.getMaxY(); }

    /**
     * Returns the size in microns of a pixel along the Y-axis.
     * 
     * @return See above.
     */
    float getPixelsSizeY() { return parent.getPixelsSizeY(); }

    /**
     * Returns the size in microns of a pixel along the Y-axis.
     * 
     * @return See above.
     */
    float getPixelsSizeZ() { return parent.getPixelsSizeZ(); }
	
}
