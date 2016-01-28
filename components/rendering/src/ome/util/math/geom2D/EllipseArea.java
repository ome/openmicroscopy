/*
 * ome.util.math.geom2D.EllipseArea
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
 * Represents an ellipse in the Euclidean space <b>R</b><sup>2</sup>.
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
public class EllipseArea extends Handle implements PlaneArea {

    /**
     * Creates a new instance.
     * 
     * @param x
     *            The x-coordinate of the top-left corner.
     * @param y
     *            The y-coordinate of the top-left corner.
     * @param width
     *            The width of the ellipse.
     * @param height
     *            The height of the ellipse.
     */
    public EllipseArea(float x, float y, float width, float height) {
        super(new EllipseAreaAdapter(x, y, width, height));
    }

    /**
     * Implemented as specified in the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int width, int height) {
        breakSharing();
        ((EllipseAreaAdapter) getBody()).setBounds(x, y, width, height);
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#scale(double)
     */
    public void scale(double factor) {
        breakSharing();
        EllipseAreaAdapter adapter = (EllipseAreaAdapter) getBody();
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
        return ((EllipseAreaAdapter) getBody()).getPoints();
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * 
     * @see PlaneArea#onBoundaries(double, double)
     */
    public boolean onBoundaries(double x, double y) {
        return ((EllipseAreaAdapter) getBody()).onBoundaries(x, y);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(double, double)
     */
    public boolean contains(double x, double y) {
        return ((EllipseAreaAdapter) getBody()).contains(x, y);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(double, double, double, double)
     */
    public boolean contains(double x, double y, double w, double h) {
        return ((EllipseAreaAdapter) getBody()).contains(x, y, w, h);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#intersects(double, double, double, double)
     */
    public boolean intersects(double x, double y, double w, double h) {
        EllipseAreaAdapter adapter = (EllipseAreaAdapter) getBody();
        return adapter.intersects(x, y, w, h);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getBounds()
     */
    public Rectangle getBounds() {
        return ((EllipseAreaAdapter) getBody()).getBounds();
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(Point2D)
     */
    public boolean contains(Point2D p) {
        return ((EllipseAreaAdapter) getBody()).contains(p);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getBounds2D()
     */
    public Rectangle2D getBounds2D() {
        return ((EllipseAreaAdapter) getBody()).getBounds2D();
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#contains(Rectangle2D)
     */
    public boolean contains(Rectangle2D r) {
        return ((EllipseAreaAdapter) getBody()).contains(r);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#intersects(Rectangle2D)
     */
    public boolean intersects(Rectangle2D r) {
        return ((EllipseAreaAdapter) getBody()).intersects(r);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getPathIterator(AffineTransform)
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return ((EllipseAreaAdapter) getBody()).getPathIterator(at);
    }

    /**
     * Required by the {@link java.awt.Shape Shape} I/F.
     * 
     * @see java.awt.Shape#getPathIterator(AffineTransform, double)
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return ((EllipseAreaAdapter) getBody()).getPathIterator(at, flatness);
    }

}
