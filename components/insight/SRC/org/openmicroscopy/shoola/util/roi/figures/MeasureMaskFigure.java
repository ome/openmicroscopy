/*
* org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.figures;




//Java imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.FigureListener;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MeasureMaskFigure 
	extends MeasureRectangleFigure
	implements ROIFigure
{
	
	/** The BufferedImage of the Mask. */
	protected BufferedImage 			mask;
	
    /** Creates a new instance. */
    public MeasureMaskFigure() 
    {
        this(DEFAULT_TEXT, 0, 0, 0, 0, null, false, true);
    }

    /** 
     * Creates a new instance.
     * @param text text of the ellipse. 
     * */
    public MeasureMaskFigure(String text) 
    {
        this(text, 0, 0, 0, 0, null, false, true);
    }
    
    /** 
     * Creates a new instance.
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * */
    public MeasureMaskFigure(double x, double y, double width, 
			double height, BufferedImage mask) 
    {
    	this(DEFAULT_TEXT, x, y, width, height, mask, false, true);
    }

    /** 
     * Creates a new instance.
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * @param readOnly Is the figure read only.
     * @param clientObject the figure is a client object
     * */
    public MeasureMaskFigure(double x, double y, double width, 
			double height, BufferedImage mask, boolean readOnly, 
			boolean clientObject) 
    {
    	this(DEFAULT_TEXT, x, y, width, height, mask, readOnly, clientObject);
    }
    
    /** 
     * Creates a new instance.
     * @param text text of the ellipse. 
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * @param readOnly the figure is readOnly
     * @param clientObject the figure is a client object
     * */
    public MeasureMaskFigure(String text, double x, double y, double width, 
    	double height, BufferedImage mask, boolean readOnly, 
    	boolean clientObject) 
    {
		super(text, x, y, width, height, readOnly, clientObject);
		this.mask = mask;
    }
    
    /**
     * Set the mask of the maskFigure to the mask parameter.
     * @param mask See above.
     */
    public void setMask(BufferedImage mask)
    {
    	this.mask = mask;
    }    
    
    /**
     * get the mask of the maskFigure.
     * return See above.
     */
    public BufferedImage getMask()
    {
    	return this.mask;
    }
    

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.MASK_TYPE; }
    
	public void draw(Graphics2D g)
	{
		g.drawImage(mask, (int)getX(), (int)getY(), (int)getWidth(), 
				(int)getHeight(), null);
	}
    
	/**
	 * Has the mask got a pixel.
	 * @param rgb
	 * @return
	 */
	private boolean hasColour(int rgb)
	{
		Color toColor = new Color(rgb);
		return (toColor.getAlpha()!=0);
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		Rectangle r = rectangle.getBounds();
		List<Point> vector = new ArrayList<Point>(r.height*r.width);
		int xEnd = r.x+r.width, yEnd = r.y+r.height;
		int x, y;
		for (y = r.y; y < yEnd; ++y) 
			for (x = r.x; x < xEnd; ++x) 
				if (hasColour(mask.getRGB(x-r.x,y-r.y))) 
					vector.add(new Point(x, y));
						
		return vector;
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * MeasureMaskFigure#clone()
	 */
	public MeasureMaskFigure clone()
	{
		MeasureMaskFigure that = (MeasureMaskFigure) super.clone();
		that.setReadOnly(this.isReadOnly());
		that.setClientObject(this.isClientObject());
		that.setObjectDirty(true);
		that.setMask(this.getMask());
		return that;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#getFigureListeners()
	 */
	public List<FigureListener> getFigureListeners()
	{
		List<FigureListener> figListeners = new ArrayList<FigureListener>();
		Object[] listeners = listenerList.getListenerList();
		for(Object listener : listeners)
			if(listener instanceof FigureListener)
				figListeners.add((FigureListener)listener);
		return figListeners;
	}
	
}

