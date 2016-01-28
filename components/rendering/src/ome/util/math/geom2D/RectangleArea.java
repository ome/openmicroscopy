/*
 * ome.util.math.geom2D.RectangleArea
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ome.util.mem.Handle;

/**
 * Represents a rectangle in the Euclidean space <b>R</b><sup>2</sup>.
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
public class RectangleArea extends Handle implements PlaneArea {

    /**
     * Constructs a new {@link RectangleAreaAdapter} whose top-left corner is
     * (0, 0) and whose width and height are both zero.
     */
    public RectangleArea() {
        super(new RectangleAreaAdapter());
    }

    /**
     * Constructs a new {@link RectangleAreaAdapter} whose top-left corner is
     * specified as (x, y) and whose width and height are specified by the
     * arguments of the same name.
     * 
     * @param x
     *            The x-coordinate of the top-left corner.
     * @param y
     *            The y-coordinate of the top-left corner.
     * @param width
     *            The width of the rectangle.
     * @param height
     *            The height of the rectangle.
     */
    public RectangleArea(int x, int y, int width, int height) {
        super(new RectangleAreaAdapter(x, y, width, height));
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int width, int height) {
        breakSharing();
        ((RectangleAreaAdapter) getBody()).setBounds(x, y, width, height);
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#scale(double)
     */
    public void scale(double factor) {
        breakSharing();
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        Rectangle r = adapter.getBounds();
        adapter.setBounds((int) (r.x * factor), (int) (r.y * factor),
                (int) (r.width * factor), (int) (r.height * factor));
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#getPoints()
     */
    public PlanePoint[] getPoints() {
        return ((RectangleAreaAdapter) getBody()).getPoints();
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#onBoundaries(double, double)
     */
    public boolean onBoundaries(double x, double y) {
        return ((RectangleAreaAdapter) getBody()).onBoundaries(x, y);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(double, double)
     */
    public boolean contains(double x, double y) {
        return ((RectangleAreaAdapter) getBody()).contains(x, y);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(double, double, double, double)
     */
    public boolean contains(double x, double y, double w, double h) {
        return ((RectangleAreaAdapter) getBody()).contains(x, y, w, h);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#intersects(double, double, double, double)
     */
    public boolean intersects(double x, double y, double w, double h) {
        return ((RectangleAreaAdapter) getBody()).intersects(x, y, w, h);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getBounds()
     */
    public Rectangle getBounds() {
        return ((RectangleAreaAdapter) getBody()).getBounds();
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(Point2D)
     */
    public boolean contains(Point2D p) {
        return ((RectangleAreaAdapter) getBody()).contains(p);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getBounds2D()
     */
    public Rectangle2D getBounds2D() {
        return ((RectangleAreaAdapter) getBody()).getBounds2D();
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(Rectangle2D)
     */
    public boolean contains(Rectangle2D r) {
        return ((RectangleAreaAdapter) getBody()).contains(r);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#intersects(Rectangle2D)
     */
    public boolean intersects(Rectangle2D r) {
        return ((RectangleAreaAdapter) getBody()).intersects(r);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getPathIterator(AffineTransform)
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return ((RectangleAreaAdapter) getBody()).getPathIterator(at);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getPathIterator(AffineTransform, double)
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return ((RectangleAreaAdapter) getBody()).getPathIterator(at, flatness);
    }

}
