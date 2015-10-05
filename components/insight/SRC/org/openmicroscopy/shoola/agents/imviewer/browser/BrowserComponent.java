/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements the {@link Browser} interface to provide the functionality
 * required of the browser component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 * 
 * @see org.openmicroscopy.shoola.agents.imviewer.browser.BrowserControl
 * @see org.openmicroscopy.shoola.agents.imviewer.browser.BrowserModel
 * @see org.openmicroscopy.shoola.agents.imviewer.browser.BrowserUI
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
class BrowserComponent
    extends AbstractComponent
    implements Browser
{
    
    /** The Model sub-component. */
    private BrowserModel    model;
    
    /** The Control sub-component. */
    private BrowserControl  controller;
    
    /** The View sub-component. */
    private BrowserUI       view;
    
    /** The sub-component displaying the grid. */
    private GridUI			gridView;
    
    /** The sub-component displaying the projection. */
    private ProjectionUI	projectionView;
	
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    BrowserComponent(BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new BrowserControl();
        model.getParentModel().addPropertyChangeListener(controller);
        view = new BrowserUI();
        gridView = new GridUI();
        projectionView = new ProjectionUI();
        view.setSibling(ImViewer.GRID_INDEX, gridView);
        view.setSibling(ImViewer.PROJECTION_INDEX, projectionView);
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(controller, model);
        gridView.initialize(model, view);
        projectionView.initialize(model, view);
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
    public JComponent getUI() { return view; }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRenderedImage(Object)
     */
    public void setRenderedImage(Object image)
    {
        if (image == null) 
            throw new IllegalArgumentException("Image cannot be null.");
        boolean hasImage = model.hasImage();
        if (image instanceof BufferedImage)
        	model.setRenderedImage((BufferedImage) image);
        else  
        	throw new IllegalArgumentException("Image type not supported.");
        //Paint only if selected.
        if (!hasImage) {
        	view.locateScrollBars();
        }
        paintImage();
    }
    
    /**
     * (Re)paints the image
     */
    private void paintImage() {
        view.paintMainImage();
        viewSplitImages();
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getDisplayedImage()
     */
    public BufferedImage getDisplayedImage()
    {
        return model.getDisplayedImage();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getRenderedImage()
     */
    public BufferedImage getRenderedImage()
    {
        return model.getRenderedImage();
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#removeComponent(JComponent, int)
     */
    public void removeComponent(JComponent c, int index)
    {
        if (c == null)
            throw new IllegalArgumentException("Component cannot be null.");
        switch (index) {
			case ImViewer.VIEW_INDEX:
				view.removeComponentFromLayer(c);
				break;
			case ImViewer.GRID_INDEX:
				gridView.removeComponentFromLayer(c);
				break;
			case ImViewer.PROJECTION_INDEX:
				projectionView.removeComponentFromLayer(c);
				break;
		}
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#addComponent(JComponent, int, boolean reset)
     */
    public void addComponent(JComponent c, int index, boolean reset)
    {
        if (c == null)
            throw new IllegalArgumentException("Component cannot be null.");
        switch (index) {
			case ImViewer.VIEW_INDEX:
				view.addComponentToLayer(c, reset);
				break;
			case ImViewer.GRID_INDEX:
				gridView.addComponentToLayer(c);
				break;
			case ImViewer.PROJECTION_INDEX:
				projectionView.addComponentToLayer(c);
				break;
		}
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setZoomFactor(double, boolean)
     */
    public void setZoomFactor(double factor, boolean reset)
    {
    	int index = model.getSelectedIndex();
    	if (factor != ZoomAction.ZOOM_FIT_FACTOR) {
	        if (factor > ZoomAction.MAX_ZOOM_FACTOR ||
	            factor < ZoomAction.MIN_ZOOM_FACTOR)
	            throw new IllegalArgumentException(
	            		"The zoom factor is a value " +
	                    "between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
	                    ZoomAction.MAX_ZOOM_FACTOR);
	        model.setZoomFactor(factor);
    		if (!reset) {
    			if (index == ImViewer.VIEW_INDEX) view.zoomImage();  
    			else if (index == ImViewer.PROJECTION_INDEX)
    				projectionView.zoomImage(true);
    		}
    	} else {
    	    BufferedImage img = null;
            Dimension viewport = null;
            if (index == ImViewer.VIEW_INDEX) {
                img = model.getRenderedImage();
                viewport = view.getViewportSize();
            } else if (index == ImViewer.PROJECTION_INDEX) {
                img = model.getProjectedImage();
                viewport = projectionView.getViewportSize();
            }
            if (img != null) {
                int width = img.getWidth();
                int height = img.getHeight();
                double zoomFactorX = 0;
                if (width > 0) zoomFactorX = viewport.getWidth()/width;
                double zoomFactorY = 0;
                if (height > 0) zoomFactorY = viewport.getHeight()/height;
                factor = Math.min(zoomFactorX, zoomFactorY); 
            }
            model.setZoomFactor(factor);
            if (!reset) {
                if (index == ImViewer.VIEW_INDEX || 
                        index == ImViewer.PROJECTION_INDEX) {
                    view.zoomImage();
                    projectionView.zoomImage(true);
                }
            }
    	}
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getZoomFactor()
     */
    public double getZoomFactor() { return model.getZoomFactor(); }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getTitle()
     */
    public String getTitle() { return model.getTitle(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getIcon()
     */
    public Icon getIcon() { return model.getIcon(); }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setComponentsSize(int, int)
     */
    public void setComponentsSize(int w, int h)
    {
        view.setComponentsSize(w, h);
        view.setPreferredSize(new Dimension(w+5, h+5));
        gridView.setGridSize();
        projectionView.setComponentsSize(w, h);
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUnitBar(boolean)
     */
    public void setUnitBar(boolean b)
    {
        if (b == model.isUnitBar()) return;
        model.setUnitBar(b);
        view.repaint();
        int index = model.getSelectedIndex();
        if (index == ImViewer.GRID_INDEX) 
        	gridView.repaint();
        else if (index == ImViewer.PROJECTION_INDEX)
        	projectionView.repaint();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUnitBarSize(double)
     */
    public void setUnitBarSize(double size)
    {
    	double oldUnit = model.getUnitInRefUnits();
        model.setUnitBarSize(size);
        
        Rectangle viewRect = view.getViewport().getBounds();
        if (viewRect.width >= model.getUnitBarSize()) {
        	view.repaint();
        	int index = model.getSelectedIndex();
            if (index == ImViewer.GRID_INDEX) 
            	gridView.repaint();
            else if (index == ImViewer.PROJECTION_INDEX)
            	projectionView.repaint();
        	return;
        }
        if (viewRect.width > 0) {
        	  UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
              un.notifyInfo("Scale bar size", "A scale bar of " +
              		"the selected size: "+size +
              		"\ncannot be displayed on the image. " +
              		"Please select a new size.");
              model.setUnitBarSize(oldUnit);
        }
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isUnitBar()
     */
    public boolean isUnitBar() { return model.isUnitBar(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarValue()
     */
    public String getUnitBarValue() { return model.getUnitBarValue(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarSize()
     */
    public double getUnitBarSize() { return model.getUnitBarSize(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarColor()
     */
    public Color getUnitBarColor() { return model.getUnitBarColor(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUnitBarColor(Color)
     */
	public void setUnitBarColor(Color color)
	{
		if (model.getUnitBarColor().equals(color)) return;
		model.setUnitBarColor(color);
		view.repaint();
		int index = model.getSelectedIndex();
        if (index == ImViewer.GRID_INDEX) 
        	gridView.repaint();
        else if (index == ImViewer.PROJECTION_INDEX)
        	projectionView.repaint();
	}

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#scrollTo(Rectangle, boolean)
     */
	public void scrollTo(Rectangle bounds, boolean blockIncrement)
	{
		if (bounds == null) return;
		view.scrollTo(bounds, blockIncrement);
	}

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#onComponentResized()
     */
	public void onComponentResized() { view.onComponentResized(); }
	
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setBackgroundColor(Color)
     */
	public void setBackgroundColor(Color color)
	{
		if (model.getBackgroundColor().equals(color)) return;
		model.setBackgroundColor(color);
		view.getViewport().setBackground(color);
		int index = model.getSelectedIndex();
        if (index == ImViewer.GRID_INDEX) 
        	gridView.getViewport().setBackground(color);
        else if (index == ImViewer.PROJECTION_INDEX)
        	projectionView.getViewport().setBackground(color);
	}

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getBackgroundColor()
     */
	public Color getBackgroundColor()
	{
		return model.getBackgroundColor();
	}

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getGridView()
     */
	public JComponent getGridView() { return gridView; }
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getProjectionView()
     */
	public JComponent getProjectionView() { return projectionView; }
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedPane(int)
     */
	public void setSelectedPane(int index)
	{
		switch (index) {
			case ImViewer.GRID_INDEX:
			    if (model.hasNoGridImages())
                    model.setGridImages();
				gridView.paintImage();
				break;
			case ImViewer.PROJECTION_INDEX:	
				break;
			case ImViewer.VIEW_INDEX:	
				view.zoomImage();
		}
	}
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getGridViewIcon()
     */
	public Icon getGridViewIcon() { return model.getGridViewIcon(); }

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getGridViewTitle()
     */
	public String getGridViewTitle() { return model.getGridViewTitle(); }

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getProjectionViewIcon()
     */
	public Icon getProjectionViewIcon()
	{ 
		return model.getProjectionViewIcon(); 
	}

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getProjectionViewTitle()
     */
	public String getProjectionViewTitle()
	{ 
		return model.getProjectionViewTitle(); 
	}
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#viewSplitImages()
     */
	public void viewSplitImages()
	{
		if (model.getSelectedIndex() != ImViewer.GRID_INDEX) return;
		model.setGridImages();
		if (gridView != null) gridView.repaint();
	}

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getGridImage()
     */
	public BufferedImage getGridImage()
	{
	    if (model.getCombinedImage() != null)
            return gridView.getGridImage();
        model.setGridImages();
		return gridView.getGridImage();
	}
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isOnImageInGrid(Rectangle rect)
     */
	public Point isOnImageInGrid(Rectangle rect)
	{
		return gridView.isOnImageInGrid(rect);
	}

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getRatio()
     */
	public double getRatio() { return model.getRatio(); }

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setGridRatio(double)
     */
	public void setGridRatio(double r)
	{
		model.setGridRatio(r);
		gridView.setGridRatio();
	}
	
	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getGridRatio()
     */
	public double getGridRatio() { return model.getGridRatio(); }

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#initializeMagnificationFactor(double)
     */
	public void initializeMagnificationFactor(double f)
	{
		model.setZoomFactor(f);
	}

	/** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRenderProjected(BufferedImage)
     */
	public void setRenderProjected(Object image)
	{
		if (image == null) 
            throw new IllegalArgumentException("Image cannot be null.");
		if (image instanceof BufferedImage) {
			model.setProjectedImage((BufferedImage) image);
			model.createDisplayedProjectedImage();
			BufferedImage img = model.getDisplayedProjectedImage();
			if (img == null) return;
		}
		projectionView.zoomImage(false);
        projectionView.repaint();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#hasProjectedPreview()
	 */
	public boolean hasProjectedPreview()
	{
		return model.getProjectedImage() != null;
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getDisplayedProjectedImage()
	 */
	public BufferedImage getDisplayedProjectedImage()
	{
		return model.getDisplayedProjectedImage();
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getProjectedImage()
	 */
	public BufferedImage getProjectedImage()
	{
		return model.getProjectedImage();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onColorModelChange()
	 */
	public void onColorModelChange()
	{
		view.clearGridImages();
		viewSplitImages();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getUnitInRefUnits()
	 */
	public double getUnitInRefUnits() { return model.getUnitInRefUnits(); }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setBirdEyeView(Object)
	 */
	public void setBirdEyeView(BufferedImage image)
	{
		if (view == null) return;
		view.setBirdEyeView(image);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getVisibleRectangle()
	 */
	public 	Rectangle getVisibleRectangle()
	{
		if (view == null) return null;
		return view.getVisibleRectangle();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedRegion(Rectangle)
	 */
	public void setSelectedRegion(Rectangle region)
	{
		if (view == null || region == null) return;
		view.setSelectedRegion(region);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onStateChange(boolean)
	 */
	public void onStateChange(boolean b)
	{ 
		if (view == null) return;
		view.onStateChange(b);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setViewLocation(double, double)
	 */
	public void setViewLocation(double rx, double ry)
	{
		if (view == null) return;
		view.setViewLocation(rx, ry);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#discard()
	 */
	public void discard()
	{
		model.discard();
		if (model.isBigImage()) view.setBirdEyeView(null);
	}

        /**
         * Returns if interpolation is enabled or not
         * 
         * @return
         */
        public boolean isInterpolation() {
            return model.isInterpolation();
        }
    
        /**
         * En-/Disables interpolation
         * @param interpolation
         */
        public void setInterpolation(boolean interpolation) {
            model.setInterpolation(interpolation);
            paintImage();
        }
	    
}
