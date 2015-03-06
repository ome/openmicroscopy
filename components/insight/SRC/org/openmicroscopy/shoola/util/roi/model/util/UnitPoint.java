package org.openmicroscopy.shoola.util.roi.model.util;


import java.awt.geom.Point2D;

import ome.model.units.BigResult;
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
			// check if units are compatible:
			try {
				new LengthI(y, x.getUnit());
			} catch (Exception e) {
				throw new IllegalArgumentException("Units "
						+ LengthI.lookupSymbol(x.getUnit()) + " and "
						+ LengthI.lookupSymbol(y.getUnit())
						+ " are not compatible!");
			}
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
		// make sure all vars have the same unit, taking x as base unit
		try {
			Length myX = new LengthI(x, x.getUnit());
			Length myY = new LengthI(y, x.getUnit());
			Length otherX = new LengthI(other.x, x.getUnit());
			Length otherY = new LengthI(other.y, x.getUnit());

			Point2D p1 = new Point2D.Double(myX.getValue(), myY.getValue());
			Point2D p2 = new Point2D.Double(otherX.getValue(), otherY.getValue());
			return new LengthI(p1.distance(p2), getUnit());
		} catch (BigResult result) {
			// FIXME: temporarily just returning inifinity.
			return new LengthI(Double.POSITIVE_INFINITY, getUnit());
		}
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
