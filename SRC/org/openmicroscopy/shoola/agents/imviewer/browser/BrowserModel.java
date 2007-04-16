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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;

//Third-party libraries
import sun.awt.image.IntegerInterleavedRaster;

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
	static final double 		RATIO = 0.50;
	
	/** The red mask. */
	private static final int	RED_MASK = 0x00ff0000;
	
	/** The green mask. */
	private static final int	GREEN_MASK = 0x0000ff00;
	
	/** The blue mask. */
	private static final int	BLUE_MASK = 0x000000ff;
	
	/** The blank mask. */
	private static final int	BLANK_MASK = 0x00000000;
	
	/** The text above the red band image. */
	private static final String	RED = "Red";
	
	/** The text above the green band image. */
	private static final String	GREEN = "Green";
	
	/** The text above the blue band image. */
	private static final String	BLUE = "Blue";
	
	/** The text above the combined image. */
	private static final String	COMBINED = "Combined";
	
	/** Channel prefix. */
	private static final String PREFIX ="w=";
	
	/** Gap between the images. */
	static final int 	GAP = 2;
	
	/** The title of the browser. */
	private static final String TITLE = "View";
	
	/** The title of the annotator. */
	private static final String TITLE_ANNOTATOR = "Annotation";
	
	/** The title of the grid view. */
	private static final String TITLE_GRIDVIEW = "Split";
	
    /** Reference to the component that embeds this model. */ 
    private Browser         	component;
    
    /** The original image. */
    private BufferedImage   	renderedImage;
    
    /**
     * The image painted on screen.
     * This image may have been transformed i.e. zoomed, sharpened etc.
     */
    private BufferedImage   	displayedImage;
    
    /** A smaller version (50%) of the original image. */
    private BufferedImage		annotateImage;
    
    /** The zoom factor. */
    private double          	zoomFactor;
    
    /** Reference to the {@link ImViewer}. */
    private ImViewer        	parent;
    
    /** 
     * Flag to indicate if the unit bar is painted or not on top of the
     * displayed image.
     */
    private boolean         	unitBar;
    
    /** The value of the unit bar in microns. */
    private double          	unitInMicrons;
    
    /** The default color of the unit bar. */
    private Color				unitBarColor;
    
    /** The bacground color of the canvas. */
    private Color				backgroundColor;
    
    /** Unloaded image data. */
    private ImageData			data;

    /** Collection of retrieved images composing the grid. */
    private List<BufferedImage>	gridImages;

    /** Collection of images composing the grid. */
    private List<SplitImage>	splitImages;
    
    /**
     * Creates a buffered image.
     * 
     * @param buf		The buffer hosting the data.
     * @param sizeX		The image's width.
     * @param sizeY		The image's height.
     * @param redMask	The mask applied on the red component.
     * @param greenMask	The mask applied on the green component.
     * @param blueMask	The mask applied on the blue component.
     * @return See above.
     */
    private BufferedImage createBandImage(DataBuffer buf, int sizeX, int sizeY, 
    							int redMask, int greenMask, int blueMask)
    {
    	SinglePixelPackedSampleModel sampleModel =
            new SinglePixelPackedSampleModel(
                      DataBuffer.TYPE_INT, sizeX, sizeY, sizeX,                                                                                                      
                      new int[] {
                    		  redMask,    // Red
                    		  greenMask, //Green
                    		  blueMask //Blue
                      });
        WritableRaster raster = 
            new IntegerInterleavedRaster(sampleModel, buf, new Point(0, 0));
      
        ColorModel colorModel = new DirectColorModel(32, redMask, greenMask, 
        											blueMask);
        return new BufferedImage(colorModel, raster, false, null);
    }
   
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
        gridImages = new ArrayList<BufferedImage>();
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
        	annotateImage = Factory.magnifyImage(RATIO, renderedImage);
        } else annotateImage = null;
        displayedImage = null;
        gridImages.clear();
    }
    
    /** Sets the images composing the grid. */
    void setGridImages()
    {
    	if (getRGBSplit()) return;
    	if (gridImages.size() != 0) return;
    	gridImages.clear();
    	List images = parent.getGridImages();
    	if (images != null) {
    		Iterator i = images.iterator();
        	while (i.hasNext()) {
        		gridImages.add(Factory.magnifyImage(RATIO, 
        						(BufferedImage) i.next()));
    		}
    	}
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
     * Returns the title of the <code>Grid View</code>.
     * 
     * @return See above.
     */
    String getGridViewTitle() { return TITLE_GRIDVIEW; }
    
    /**
     * Returns the icon of the <code>Annotator</code>.
     * 
     * @return See above.
     */
    Icon getGridViewIcon()
    { 
    	IconManager icons = IconManager.getInstance();
    	return icons.getIcon(IconManager.GRIDVIEW); 
    }
    
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
     * Returns the default timepoint.
     * 
     * @return See above.
     */
	int getDefaultT() { return parent.getDefaultT(); }
	
    /**
     * Returns the default z-section.
     * 
     * @return See above.
     */
	int getDefaultZ() { return parent.getDefaultZ(); }
	
	/**
	 * Returns the number of pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	int getMaxX() { return parent.getMaxX(); }
	
	/**
	 * Returns the number of pixels along the Y-axis.
	 * 
	 * @return See above.
	 */
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
   
    /**
     * Returns the size of the grid.
     * 
     * @return See above.
     */
    Dimension getGridSize()
    {
    	int w = (int) (getMaxX()*RATIO);
    	int h = (int) (getMaxY()*RATIO);
    	int n = parent.getMaxC()+1; //add one for combined image.
    	if (n <=3) n = 4;
    	int index = 0;
    	if (n%2 != 0) index = 1;
    	int col = (int) Math.floor(Math.sqrt(n))+index; 
    	int row = n/col+index;
    	return new Dimension(col*w+(col-1)*GAP, row*h+(row-1)*GAP);
    }
    
    /**
     * Returns a collection of images composing the grid.
     * 
     * @return See above.
     */
    List getGridImages()
    { 
    	if (splitImages == null) splitImages = new ArrayList<SplitImage>();
    	else splitImages.clear();
    	boolean c = parent.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
    	if (getRGBSplit()) {	
        	BufferedImage r = null, g = null, b = null;
        	if (!c) {
        		int w = annotateImage.getWidth();
            	int h = annotateImage.getHeight();
            	DataBuffer buf = annotateImage.getRaster().getDataBuffer();
        		boolean[] rgb = parent.hasRGB();
            	if (rgb[0])
            		r = createBandImage(buf, w, h, RED_MASK, BLANK_MASK, 
            							BLANK_MASK);
            	if (rgb[1])
            		g = createBandImage(buf, w, h, BLANK_MASK, GREEN_MASK, 
            							BLANK_MASK);
            	if (rgb[2])
            		b = createBandImage(buf, w, h, BLANK_MASK, BLANK_MASK, 
            							BLUE_MASK);
        	}
        	
        	splitImages.add(new SplitImage(r, RED));
        	splitImages.add(new SplitImage(g, GREEN));
        	splitImages.add(new SplitImage(b, BLUE));
    	} else { 
	    	int index = 0;
	    	Iterator i = gridImages.iterator();
	    	BufferedImage img;
	    	String n;
	    	while (i.hasNext()) {
	    		n = PREFIX+
	    			parent.getChannelMetadata(index).getEmissionWavelength();
	    		img = (BufferedImage) i.next();
	    		splitImages.add(new SplitImage(img, n));
	    		index++;
			}
    	}
    	splitImages.add(new SplitImage(annotateImage, COMBINED));
    	return splitImages;
    }
    
    /**
     * Returns the index of the selected pane. 
     * 
     * @return See above.
     */
    int getSelectedIndex() { return parent.getSelectedIndex(); }

    /**
     * Returns <code>true</code> if the displayed image is
	 * split into its red, green and blue components. Returns <code>false</code>
	 * if the selected channels are displayed independently.
	 * 
     * @return See above.
     */
	boolean getRGBSplit() { return parent.getRGBSplit(); }
    
}
