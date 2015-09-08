/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.events.iviewer;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;

import omero.gateway.model.ChannelData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.PixelsData;

/** 
 * Event to bring the measurement tool.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MeasurementTool 
	extends RequestEvent
{

    /** The pixels set the measurement tool is for. */
    private PixelsData      	pixels;
    
    /** The ID of the image. */
    private long        		imageID;
    
    /** The name of the image. */
    private String      		name;
    
    /** The currently selected z-section. */
    private int					defaultZ;
    
    /** The currently selected timepoint. */
    private int					defaultT;
    
    /** Collection of pairs (channel's index, channel's color). */
    private Map					activeChannels;
    
    /** The image's magnification factor. */
    private double				magnification;
    
    /** The bounds of the component posting the event. */
    private Rectangle   		requesterBounds;

    /** Thumbnail of the rendered image. */
    private BufferedImage		thumbnail;
    
    /** The rendered image. */
    private BufferedImage		renderedImage;
    
    /** The channel metadata. */
    private List<ChannelData> 	channelData;
    
    /** The measurements if any. */
    private List<FileAnnotationData> measurements;
    
    /** Flag indicating that the tool is for HCS data. */
    private boolean HCSData;
    
    /** Flag indicating if it is a big image or not.*/
    private boolean bigImage;
    
    /** The size along the X-axis.*/
    private int			sizeX;
    
    /** The size along the Y-axis.*/
    private int			sizeY;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param imageID The image ID.
     * @param pixels The pixels set the measurement tool is for.
     * @param name The name of the image.
     * @param defaultZ The currently selected z-section.
     * @param defaultT The currently selected timepoint.
     * @param activeChannels Collection of pairs 
     * (channel's index, channel's color).
     * @param magnification The magnification factor.
     * @param bounds The bounds of the component posting the event.
     * @param channelData The channel metadata.
     */
    public MeasurementTool(SecurityContext ctx, long imageID, PixelsData pixels,
    		String name, int defaultZ, int defaultT, Map activeChannels,
    		double magnification, Rectangle bounds,
    		List<ChannelData> channelData)
    {
        if (pixels == null) 
            throw new IllegalArgumentException("Pixels set not valid.");
        if (imageID < 0) 
            throw new IllegalArgumentException("Image ID not valid.");
        if (channelData == null || channelData.size() == 0) 
            throw new IllegalArgumentException("Channel data not valid.");
        this.ctx = ctx;
        this.channelData = channelData;
        this.pixels = pixels;
        this.imageID = imageID;
        this.name = name;
        this.defaultT = defaultT;
        this.defaultZ = defaultZ;
        this.activeChannels = activeChannels;
        this.magnification = magnification;
        requesterBounds = bounds;
        HCSData = false;
        bigImage = false;
        sizeX = 0;
        sizeY = 0;
    }
    
    /**
     * Sets the size along the X-axis and Y-axis.
     * 
     * @param sizeX The size along the X-axis.
     * @param sizeY The size along the Y-axis.
     */
    public void setSize(int sizeX, int sizeY)
    {
    	this.sizeX = sizeX;
    	this.sizeY = sizeY;
    }
    
    /**
     * Returns the size along the X-axis.
     * 
     * @return See above.
     */
    public int getSizeX() { return sizeX; }
    
    /**
     * Returns the size along the Y-axis.
     * 
     * @return See above.
     */
    public int getSizeY() { return sizeY; }
    
    /**
     * Sets the flag indicating if the tool is for big image data.
     * 
     * @param value The value to set.
     */
    public void setBigImage(boolean value) { bigImage = value; }
    
    /**
     * Returns <code>true</code> if big image data, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isBigImage() { return bigImage; }
    
    /**
     * Sets the flag indicating if the tool is for HCS data.
     * 
     * @param value The value to set.
     */
    public void setHCSData(boolean value) { HCSData = value; }
    
    /**
     * Returns <code>true</code> if HCS data, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isHCSData() { return HCSData; }
    
    /**
     * Sets the measurements.
     * 
     * @param measurements The value to set.
     */
    public void setMeasurements(List<FileAnnotationData> measurements)
    {
    	this.measurements = measurements;
    }
    
    /**
     * Returns the measurements if any.
     * 
     * @return See above.
     */
    public List<FileAnnotationData> getMeasurements()
    {
    	return measurements;
    }
    
    /**
     * Sets the icon of the window.
     * 
     * @param thumbnail The icon to set.
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
    	this.thumbnail = thumbnail;
    }
    
    /**
     * Sets the rendered image.
     * 
     * @param renderedImage The image to set.
     */
    public void setRenderedImage(BufferedImage renderedImage)
    {
    	this.renderedImage = renderedImage;
    }
    
    /**
     * Returns the rendered image.
     * 
     * @return See above. 
     */
    public BufferedImage getRenderedImage() { return renderedImage; }
    
    /**
     * Returns the thumbnail.
     * 
     * @return See above. 
     */
    public BufferedImage getThumbnail() { return thumbnail; }
    
    /**
     * Returns the image ID.
     * 
     * @return See above. 
     */
    public long getImageID() { return imageID; }

    /**
     * Returns the name of the image.
     * 
     * @return See above. 
     */
    public String getName() { return name; }

    /**
     * Returns the pixels set.
     * 
     * @return See above. 
     */
    public PixelsData getPixels() { return pixels; }
    
    /**
     * Returns the bounds of the component posting the event. 
     * Returns <code>null</code> if not available.
     * 
     * @return See above.
     */
    public Rectangle getRequesterBounds() { return requesterBounds; }
    
    /**
     * Returns the currently selected z-section.
     * 
     * @return See above.
     */
    public int getDefaultZ() { return defaultZ; }
    
    /**
     * Returns the currently selected timepoint.
     * 
     * @return See above.
     */
    public int getDefaultT() { return defaultT; }
    
    /**
     * Returns the image's magnification factor.
     * 
     * @return See above.
     */
    public double getMagnification() { return magnification; }
    
    /**
     * Returns the collection of pairs (channel's index, channel's color).
     * 
     * @return See above.
     */
    public Map getActiveChannels() { return activeChannels; }
   
    /**
     * Returns the channel metadata. 
     * 
     * @return See above.
     */
    public List<ChannelData> getChannelData() { return channelData; }
    
    /**
     * Returns the security context.
     * 
     * @return See above.
     */
    public SecurityContext getSecurityContext() { return ctx; }

}
