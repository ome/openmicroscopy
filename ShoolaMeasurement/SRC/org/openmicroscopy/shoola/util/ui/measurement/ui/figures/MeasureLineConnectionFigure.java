/*
 * measurement.ui.figures.MeasureLineConnectionFigure 
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
import java.util.ArrayList;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.LineConnectionFigure;

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
public class MeasureLineConnectionFigure
	extends LineConnectionFigure
{
	private ArrayList<Rectangle2D> boundsArray = new ArrayList<Rectangle2D>();
	private ArrayList<Double> lengthArray = new ArrayList<Double>();
	private ArrayList<Double> angleArray = new ArrayList<Double>();

	public MeasureLineConnectionFigure()
	{
		super();
	}


	public void draw(Graphics2D g)
	{
		super.draw(g);
		boundsArray.clear();
		lengthArray.clear();
		angleArray.clear();
		if(SHOWMEASUREMENT.get(this))
		{
			if(getPointCount()==2)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double angle = getAngle(0, 1);
				if(angle>90)
					angle = Math.abs(angle-180);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle, g);
				Point2D.Double lengthPoint = getLengthPosition(0, 1);
				Rectangle2D bounds = new Rectangle2D.Double(lengthPoint.x, lengthPoint.y+rect.getHeight()*2, rect.getWidth(), rect.getHeight());
				g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for( int x = 1 ; x < this.getPointCount()-1; x++)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double angle = getAngle(x-1, x, x+1);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle, g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x, getPoint(x).y, rect.getWidth(), rect.getHeight());
				g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for( int x = 1 ; x < this.getPointCount(); x++)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double length = getLength(x-1, x);
				lengthArray.add(length);
				String lineLength = formatter.format(length);
				lineLength = addUnits(lineLength);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineLength, g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x-15, getPoint(x).y-15,rect.getWidth()+30, rect.getHeight()+30);
				Point2D.Double lengthPoint = getLengthPosition(x-1, x);
				g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineLength, (int)lengthPoint.x, (int)lengthPoint.y);
				boundsArray.add(bounds);
			}
		}
	}
	
	public ArrayList<Double> getLengthArray()
	{
		return lengthArray;
	}
	
	public ArrayList<Double> getAngleArray()
	{
		return angleArray;
	}
	
	
	public String addDegrees(String str)
	{
		return str + "\u00B0";
	}
	
	public String addUnits(String str)
	{
		if(INMICRONS.get(this))
			return str+"\u00B5m";
		else
			return str+"px";
	}
				
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds = super.getDrawingArea();
		if(boundsArray!=null)
			for(int i = 0 ; i < boundsArray.size(); i++)
			{
						Rectangle2D bounds = boundsArray.get(i);
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

	public Point2D.Double getLengthPosition(int i, int j)
	{
		Point2D.Double p0 = getPoint(i);
		Point2D.Double p1 = getPoint(j);
		
		double lx = (p0.x-p1.x)/2;
		double ly = (p0.y-p1.y)/2;
		double x = p0.x-lx;
		double y = p0.y-ly;
		
		return new Point2D.Double(x, y);
	}
	
	public double getLength(int i , int j)
	{
		return getPoint(i).distance(getPoint(j));
	}
	
	public double getAngle(int i, int j, int k)
	{
		Point2D p0 = getPoint(i);
		Point2D p1 = getPoint(j);
		Point2D p2 = getPoint(k);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), p0.getY()-p1.getY());
		Point2D v1 = new Point2D.Double(p2.getX()-p1.getX(), p2.getY()-p1.getY());
		return Math.toDegrees(Math.acos(dotProd(v0, v1)));
	}
	
	public double getAngle(int i, int j)
	{
		Point2D p0 = getPoint(i);
		Point2D p1 = getPoint(j);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), p0.getY()-p1.getY());
		Point2D v1 = new Point2D.Double(1,0);
		return Math.toDegrees(Math.acos(dotProd(v0, v1)));
	}
	
	public double dotProd(Point2D p0, Point2D p1)
	{
		double adotb = p0.getX()*p1.getX()+p0.getY()*p1.getY();
		double normab = Math.sqrt(p0.getX()*p0.getX()+p0.getY()*p0.getY())*
						Math.sqrt(p1.getX()*p1.getX()+p1.getY()*p1.getY());
		
		return adotb/normab;
	}
}


