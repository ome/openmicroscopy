/*
 * ome.util.math.geom2D.PlaneArea
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

import java.awt.Shape;

import ome.util.mem.Copiable;

/**
 * Interface that all areas of the Euclidean space <b>R</b><sup>2</sup> must
 * implement.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:32 $) </small>
 * @since OME2.2
 */
public interface PlaneArea extends Copiable, Shape {

    /**
     * Sets the bounding <code>Rectangle</code> of the planeArea to the
     * specified x, y, width, and height.
     * 
     * @param x
     *            The x-coordinate of the top-left corner.
     * @param y
     *            The y-coordinate of the top-left corner.
     * @param width
     *            The width of the area.
     * @param height
     *            The height of the area.
     */
    public void setBounds(int x, int y, int width, int height);

    /**
     * Resets the bounding <code>Rectangle</code> of the planeArea according
     * to the specified scaling factor.
     * 
     * @param factor
     *            The scaling factor.
     */
    public void scale(double factor);

    /**
     * Returns an array of {@link PlanePoint} contained in the PlaneArea.
     * 
     * @return See above.
     */
    public PlanePoint[] getPoints();

    /**
     * Controls if a specified point is on the boundary of the PlaneArea.
     * 
     * @param x
     *            The x-coordinate of the point.
     * @param y
     *            The y-coordinate of the point.
     * @return <code>true</code> if the point is on the boundary,
     *         <code>false</code> otherwise.
     */
    public boolean onBoundaries(double x, double y);

}
