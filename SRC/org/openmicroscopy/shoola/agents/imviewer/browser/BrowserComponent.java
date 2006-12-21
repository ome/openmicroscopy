/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserComponent
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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
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
        view = new BrowserUI();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(model, view);
        view.initialize(controller, model);
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
    public JComponent getUI()
    {
        return view;
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRenderedImage(BufferedImage)
     */
    public void setRenderedImage(BufferedImage image)
    {
        if (image == null) 
            throw new IllegalArgumentException("Image cannot be null.");
        model.setRenderedImage(image);
        view.paintImage();
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
     * @see Browser#removeComponent(JComponent)
     */
    public void removeComponent(JComponent c)
    {
        if (c == null)
            throw new IllegalArgumentException("Component cannot be null.");
        view.removeComponentFromLayer(c);
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#addComponent(JComponent)
     */
    public void addComponent(JComponent c)
    {
        if (c == null)
            throw new IllegalArgumentException("Component cannot be null.");
        view.addComponentToLayer(c);
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setZoomFactor(double)
     */
    public void setZoomFactor(double factor)
    {
    	if (factor != -1) 
    	{
	        if (factor > ZoomAction.MAX_ZOOM_FACTOR ||
	            factor < ZoomAction.MIN_ZOOM_FACTOR)
	            throw new IllegalArgumentException("The zoom factor is value " +
	                    "between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
	                    ZoomAction.MAX_ZOOM_FACTOR);
    	}
    	else
    	{
    		int width = model.getRenderedImage().getWidth();
    		int height = model.getRenderedImage().getHeight();
    		Dimension viewport = view.getCurrentViewport();
    		double zoomFactorX = viewport.getWidth()/width;
    		double zoomFactorY = viewport.getHeight()/height;
    		factor = Math.min(zoomFactorX, zoomFactorY); 
    	}
        model.setZoomFactor(factor);
        view.zoomImage();  
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getZoomFactor()
     */
    public double getZoomFactor()
    {
        return model.getZoomFactor();	
    }
    
    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getTitle()
     */
    public String getTitle() { return model.getTitle(); }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLensImage(double)
     */
    public BufferedImage getLensImage(double lensFactor)
    {
        return null;
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setComponentsSize(int, int)
     */
    public void setComponentsSize(int w, int h)
    {
        view.setComponentsSize(w, h);
        view.setPreferredSize(new Dimension(w+5, h+5));
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
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUnitBarSize(double)
     */
    public void setUnitBarSize(double size)
    {
    	double oldUnit = model.getUnitInMicrons();
        model.setUnitBarSize(size);
        
        Rectangle viewRect = view.getViewport().getBounds();
        if (viewRect.width >= model.getUnitBarSize()) {
        	view.repaint();
        	return;
        }
        UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
        un.notifyInfo("Scale bar size", "A scale bar of the selected size " +
        		"cannot be displayed on the image. Please select a new size.");
        model.setUnitBarSize(oldUnit);
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isUnitBar()
     */
    public boolean isUnitBar()
    {
        return model.isUnitBar();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarValue()
     */
    public String getUnitBarValue()
    {
        return model.getUnitBarValue();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarSize()
     */
    public double getUnitBarSize()
    {
        return model.getUnitBarSize();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUnitBarColor()
     */
    public Color getUnitBarColor()
    {
        return model.getUnitBarColor();
    }

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUnitBarColor(Color)
     */
	public void setUnitBarColor(Color color)
	{
		if (model.getUnitBarColor().equals(color)) return;
		model.setUnitBarColor(color);
		view.repaint();
	}

    /** 
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#scrollTo(Rectangle)
     */
	public void scrollTo(Rectangle bounds)
	{
		if (bounds == null) return;
		view.scrollTo(bounds);
	}
    
}
