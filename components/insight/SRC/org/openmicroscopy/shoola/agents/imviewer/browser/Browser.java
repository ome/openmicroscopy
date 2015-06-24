/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.Browser
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the browser component.
 * The Viewer provides a UI component to display the rendered image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface Browser
    extends ObservableComponent
{
	
    /**
     * Returns the widget that displays the image.
     *  
     * @return The viewer widget.
     */
    public JComponent getUI();
   
    /**
     * Sets the original rendered image.
     * 
     * @param image The buffered image.
     */
    public void setRenderedImage(Object image);
    
    /**
     * Returns the image displayed on screen.
     * 
     * @return See above.
     */
    public BufferedImage getDisplayedImage();
    
    /**
     * Returns the image displayed on screen.
     * 
     * @return See above.
     */
    public BufferedImage getDisplayedProjectedImage();
    
    /**
     * Returns the original image returned by the rendering engine.
     * 
     * @return See above.
     */
    public BufferedImage getRenderedImage();
    
    /**
     * Removes the specified component from the layered pane hosting 
     * the image.
     * 
     * @param c 		The component to remove.
     * @param viewIndex Identifies the index of the view i.e. 
     * 					{@link ImViewer#VIEW_INDEX} or 
     * 					{@link ImViewer#GRID_INDEX}.
     */
    public void removeComponent(JComponent c, int viewIndex);
    
    /**
     * Adds the specified component to the layered pane hosting
     * the image.
     * 
     * @param c 		The component to add.
     * @param viewIndex Identifies the index of the view i.e. 
     * 					{@link ImViewer#VIEW_INDEX} or 
     * 					{@link ImViewer#GRID_INDEX}.
     * @param reset		Flag indicating to re-organize the components in the 
     * 					layer.
     */
    public void addComponent(JComponent c, int viewIndex, boolean reset);
    
    /**
     * Sets the zoom factor.
     * 
     * @param factor The zoom factor to set.
     * @param reset	 Pass <code>true</code> to reset the magnification factor.
     * 				 <code>false</code> to set it.
     */
    public void setZoomFactor(double factor, boolean reset);
    
    /**
     * Gets the zoom factor.
     * 
     * @return The zoom factor to get.
     */
    public double getZoomFactor();
    
    /**
     * Returns the name of the Browser.
     * 
     * @return See above.
     */
    public String getTitle();
    
    /**
     *  Returns the icon associated to the browser.
     *  
     * @return See above.
     */
    public Icon getIcon();
    
    /**
     * Sets the size of the components composing the display.
     * 
     * @param w The width to set.
     * @param h The height to set.
     */
    public void setComponentsSize(int w, int h);

    /**
     * Displays if the passed value is <code>true</code>, doesn't display it
     * if if the passed value is <code>false</code>.
     * 
     * @param b Pass <code>true</code> to display the unit bar, 
     *          <code>false</code> otherwise.
     */
    public void setUnitBar(boolean b);

    /**
     * Sets the size of the unit bar in microns.
     * 
     * @param size The size of the unit bar in microns.
     */
    public void setUnitBarSize(double size);

    /**
     * Returns <code>true</code> if the unit bar is displayed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUnitBar();

    /**
     * Returns the value (with two decimals) of the unit bar or 
     * <code>null</code> if the actual value is <i>negative</i>.
     * 
     * @return See above.
     */
    public String getUnitBarValue();

    /**
     * Returns the size of the unit bar.
     * 
     * @return See above.
     */
    public double getUnitBarSize();
    
    /**
     * Returns the unit used to determine the size of the unit bar.
     * The unit depends on the size stored. The unit of reference in the
     * OME model is in microns, but this is a transformed unit.
     * 
     * @return See above.
     */
    public double getUnitInRefUnits();
    
    /**
     * Returns the color of the unit bar.
     * 
     * @return See above.
     */
    public Color getUnitBarColor();

    /**
     * Sets the color of the unit bar.
     * 
     * @param color The value to set.
     */
	public void setUnitBarColor(Color color);

	/**
	 * Scrolls to the location.
	 * 
	 * @param bounds 			The bounds of the node.
	 * @param blockIncrement	Pass <code>true</code> to consider block
	 * 							increment, <code>false</code> otherwise. 
	 */
	public void scrollTo(Rectangle bounds, boolean blockIncrement);
    
    /**
     * Sets the background color of the canvas.
     * 
     * @param color The value to set.
     */
	public void setBackgroundColor(Color color);
	
	/**
	 * Returns the grid view.
	 * 
	 * @return See above.
	 */
	public JComponent getGridView();

	/**
	 * Returns the projection view.
	 * 
	 * @return See above.
	 */
	public JComponent getProjectionView();
	
	/**
	 * Sets the selected pane.
	 * 
	 * @param index The index of the selected pane.
	 */
	public void setSelectedPane(int index);
	
	/**
	 * Returns the name of the projection view.
	 * 
	 * @return See above.
	 */
	public String getProjectionViewTitle();
	
	/**
	 * Returns the icon associated to the projection view.
	 * 
	 * @return See above.
	 */
	public Icon getProjectionViewIcon();
	
	/**
     * Returns the name of the grid view.
     * 
     * @return See above.
     */
	public String getGridViewTitle();
	
	/**
	 * Returns the icon associated to the grid view.
	 * 
	 * @return See above.
	 */
	public Icon getGridViewIcon();

	/** 
	 * Displays a grid of images.
	 * If the rgb flag is turned on, the grid is composed of the 
	 * red, green and blue component of the main image and the main image
	 * itself. If the flag is turned off, the grid is composed of the images 
	 * representing the active channels, one per channel and the main image
	 * itself.
	 */
	public void viewSplitImages();

	/**
	 * Returns the grid image.
	 * 
	 * @return See above.
	 */
	public BufferedImage getGridImage();

	/**
	 * Returns the coordinate of the point w.r.t the grid image 
	 * coordinate system if the passed rectangle is contained in an image 
	 * composing the grid, <code>null</code> otherwise.
	 * 
	 * @param rect The rectangle to handle.
	 * @return See above.
	 */
	public Point isOnImageInGrid(Rectangle rect);
	
	/**
	 * Returns the magnification factor used to render the annotate image.
	 * 
	 * @return See above.
	 */
	public double getRatio();

	/**
	 * Sets the ratio of the an image composing the grid.
	 * 
	 * @param r The value to set.
	 */
	public void setGridRatio(double r);

	/**
	 * Returns the magnification factor used to render a grid image.
	 * 
	 * @return See above.
	 */
	public double getGridRatio();

	/**
	 * Returns the background color of the canvas.
	 * 
	 * @return See above.
	 */
	public Color getBackgroundColor();
	
	/**
	 * Initializes the magnification factor.
	 * 
	 * @param f The value to set.
	 */ 
	public void initializeMagnificationFactor(double f);

    /**
     * Sets the projected image for preview.
     * 
     * @param image The buffered image.
     */
	public void setRenderProjected(Object image);
	
	/**
	 * Returns <code>true</code> if a projected image has already been
	 * built, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasProjectedPreview();

	/**
	 * Returns the projected image if any, otherwise <code>null</code>.
	 * 
	 * @return See above.
	 */
	public BufferedImage getProjectedImage();

	/** Invokes when the color model changes. */
	public void onColorModelChange();

	/**
	 * Sets the image displayed in the bird eye view.
	 * 
	 * @param result The value to set.
	 */
	void setBirdEyeView(BufferedImage result);
	
	/**
	 * Returns the visible rectangle.
	 * 
	 * @return See above.
	 */
	Rectangle getVisibleRectangle();

	/** Invokes when the viewer is resized.*/
	void onComponentResized();
	
	/**
	 * Sets the selected region.
	 * 
	 * @param region The selected region.
	 */
	void setSelectedRegion(Rectangle region);
	
	/** 
	 * Reacts to {@link ImViewer} change events.
	 * 
	 * @param b Pass <code>true</code> to enable the UI components, 
	 *          <code>false</code> otherwise.
	 */
	void onStateChange(boolean b);

	/**
	 * Sets the location of the selection region when the user zooms in or out.
	 * 
	 * @param rx The ratio along the X-axis.
	 * @param ry The ratio along the Y-axis.
	 */
	void setViewLocation(double rx, double ry);

	/** Discards the browser.*/
	void discard();
	
	/**
         * Returns if interpolation is enabled or not
         * @return
         */
        public boolean isInterpolation();
    
       /**
        * En-/Disables interpolation
        * 
        * @param interpolation
        */
        public void setInterpolation(boolean interpolation);

}