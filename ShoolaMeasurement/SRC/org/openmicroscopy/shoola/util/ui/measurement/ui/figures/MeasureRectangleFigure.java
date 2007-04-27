/*
 * measurement.ui.figures.MeasureRectangleFigure 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.figures;


//Java imports

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.RectangleFigure;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.INMICRONS;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.MEASUREMENTTEXT_COLOUR;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.SHOWMEASUREMENT;

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
 * @since OME3.0
 */
public class MeasureRectangleFigure
	extends RectangleFigure
{
	private	Rectangle2D bounds;
	
	public MeasureRectangleFigure()
	{
		super();
	}

	
	public void draw(Graphics2D g)
	{
		super.draw(g);
		
		if(SHOWMEASUREMENT.get(this))
		{
			NumberFormat formatter = new DecimalFormat("###.#");
			String rectangleArea = formatter.format(getArea());
			rectangleArea = addUnits(rectangleArea);
			double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
			g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
			bounds = g.getFontMetrics().getStringBounds(rectangleArea, g);
			bounds = new Rectangle2D.Double(this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
			g.drawString(rectangleArea, (int)bounds.getX(), (int)bounds.getY()); 
					
		}
	}


				
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds = super.getDrawingArea();
		if(bounds!=null)
		{
			if(newBounds.getX()>bounds.getX())
			{
				double diff = newBounds.x-bounds.getX();
				newBounds.x = bounds.getX();
				newBounds.width = newBounds.width+diff;
			}
			if(newBounds.getY()>bounds.getY())
			{
				double diff = newBounds.y-bounds.getY();
				newBounds.y = bounds.getY();
				newBounds.height = newBounds.height+diff;
			}
			if(bounds.getX()+bounds.getWidth()>newBounds.getX()+newBounds.getWidth())
			{
				double diff = bounds.getX()+bounds.getWidth()-newBounds.getX()+newBounds.getWidth();
				newBounds.width = newBounds.width+diff;
			}
			if(bounds.getY()+bounds.getHeight()>newBounds.getY()+newBounds.getHeight())
			{
				double diff = bounds.getY()+bounds.getHeight()-newBounds.getY()+newBounds.getHeight();
				newBounds.height = newBounds.height+diff;
			}
		}
		return newBounds;
	}

	public String addUnits(String str)
	{
		if(INMICRONS.get(this))
			return str+"\u00B5m\u00B2";
		else
			return str+"px\u00B2";
	}


	public double getArea()
	{
		return rectangle.getWidth()*rectangle.getHeight();
	}
	
}


