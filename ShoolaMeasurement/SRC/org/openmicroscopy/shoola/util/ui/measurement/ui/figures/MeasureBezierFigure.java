/*
 * measurement.ui.figures.MeasureBezierFigure 
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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.geom.BezierPath.Node;

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
public class MeasureBezierFigure 
	extends BezierFigure
{
	private	Rectangle2D bounds;
	
	public MeasureBezierFigure()
	{
		super();
	}
	
	public MeasureBezierFigure(boolean closed)
	{
		super(closed);
	}
	
//	public void rationalise()
//	{
//		for( int j =0 ; j < path.size() ; j++)
//		{
//			Node n0= path.get(j);
//			for( int i = 1 ; i < 3 ; i++)
//			{
//				double length = 0;
//				length += Math.sqrt(Math.pow(n0.x[0]-n0.x[i],2) + Math.pow(n0.y[i]-n0.y[0],2));
//				if(length > 1)
//				{
//					n0.x[i] = n0.x[0]+(n0.x[i]-n0.x[0])/Math.sqrt(Math.pow(n0.x[i]-n0.x[0],2));
//					n0.y[i] = n0.y[0]+(n0.y[i]-n0.y[0])/Math.sqrt(Math.pow(n0.y[i]-n0.y[0],2));
//				}
//			}	
//		}
//	}

	public void draw(Graphics2D g)
	{
		//rationalise();
		super.draw(g);
		if(SHOWMEASUREMENT.get(this))
		{
			if(CLOSED.get(this))
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonArea = formatter.format(getArea());
				polygonArea = addAreaUnits(polygonArea);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				bounds = g.getFontMetrics().getStringBounds(polygonArea, g);
				bounds = new Rectangle2D.Double(this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
				g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(polygonArea, (int)bounds.getX(), (int)bounds.getY()); 
			}
			else
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonLength = formatter.format(getLength());
				polygonLength = addLineUnits(polygonLength);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				bounds = g.getFontMetrics().getStringBounds(polygonLength, g);
				
				if(getPointCount() > 1)
				{
					int midPoint = this.getPointCount()/2-1;
					if(midPoint<0)
						midPoint = 0;
					Point2D p0 = getPoint(midPoint);
					Point2D p1 = getPoint(midPoint+1);
					double x, y;
					x = Math.min(p0.getX(),p1.getX())+Math.abs(p0.getX()-p1.getX());
					y = Math.min(p0.getY(),p1.getY())+Math.abs(p0.getY()-p1.getY());
					bounds = new Rectangle2D.Double(x-bounds.getWidth()/2,
							y+bounds.getHeight()/2,
							bounds.getWidth(), bounds.getHeight());
					g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
					g.drawString(polygonLength, (int)bounds.getX(), (int)bounds.getY());
				}
			}
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
	
	
	public String addDegrees(String str)
	{
		return str + "\u00B0";
	}
	
	public String addLineUnits(String str)
	{
		if(INMICRONS.get(this))
			return str+"\u00B5m";
		else
			return str+"px";
	}
	
	public String addAreaUnits(String str)
	{
		if(INMICRONS.get(this))
			return str+"\u00B5m\u00B2";
		else
			return str+"px\u00B2";
	}
	
	public double getLength()
	{
		double length = 0;
		for(int i = 0 ; i < path.size()-1 ; i++)
		{
			Point2D p0 = getPoint(i);
			Point2D p1 = getPoint(i+1);
			
			length += Math.sqrt(Math.pow(p1.getX()-p0.getX(),2)+Math.pow(p1.getX()-p0.getX(),2)); 
		}
		return length;
	}
	
	public double getArea()
	{
		double area = 0;
		Point2D centre = path.getCenter();
		for(int i = 0 ; i < path.size() ; i++)
		{
			Point2D p0 = getPoint(i);
			Point2D p1;
			if(i==path.size()-1)
				p1 = getPoint(0);
			else
				p1 = getPoint(i+1);
			
			p0.setLocation(p0.getX()-centre.getX(), p0.getY()-centre.getY());
			p1.setLocation(p1.getX()-centre.getX(), p1.getY()-centre.getY());
			area += (p0.getX()*p1.getY()-p1.getX()*p0.getY());
		}
		return Math.abs(area/2);
	}
}


