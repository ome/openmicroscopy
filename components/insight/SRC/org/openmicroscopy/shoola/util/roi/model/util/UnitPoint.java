package org.openmicroscopy.shoola.util.roi.model.util;


import java.awt.geom.Point2D;

import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/**
 * A 'Unit' aware wrapper around {@link Point2D}, which uses
 * {@link Length} objects as coordinates
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class UnitPoint {

	/** The x value */
	public Length x;
	
	/** The y value */
	public Length y;
	
	/**
	 * Creates an 'empty' instance
	 */
	public UnitPoint() {
		
	}
			
	/**
	 * Creates an instance
	 * @param x The x value
	 * @param y The y value
	 */
	public UnitPoint(Length x, Length y) {
		if(!x.getUnit().equals(y.getUnit())) {
			// TODO: Do some transformation
			throw new IllegalArgumentException();
		}
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Get the unit
	 * @return See above
	 */
	public UnitsLength getUnit() {
		return x.getUnit();
	}
	
	/**
	 * Get the value as {@link Point2D}
	 * @return See above
	 */
	public Point2D getValue() {
		return new Point2D.Double(x.getValue(), y.getValue());
	}
	
	/**
	 * Get the distance to another point; see {@link Point2D#distance(Point2D)}
	 * @param other The other point
	 * @return See above
	 */
	public Length getDistance(UnitPoint other) {
		if(!other.getUnit().equals(getUnit())) {
			// TODO: Do some transformation
			throw new IllegalArgumentException();
		}
		Point2D p1 = new Point2D.Double(x.getValue(), y.getValue());
		Point2D p2 = new Point2D.Double(other.x.getValue(), other.y.getValue());
		return new LengthI(p1.distance(p2), getUnit());
	}
	
	/**
	 * Moves the point to another location; see {@link Point2D#setLocation(double, double)}
	 * @param x The x value
	 * @param y The y value
	 */
	public void setLocation(double x, double y) {
		this.x.setValue(x);
		this.y.setValue(y);
	}
}
