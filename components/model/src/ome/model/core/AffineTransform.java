/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Transient;

import ome.util.Filter;

/**
 * class responsible for storing a00,a01,a10,a11,a02,a12-style
 * affine transforms in the database.
 *
 * @see <a
 *      href="https://trac.openmicroscopy.org.uk/omero/ticket/8084">ticket:8084</a>
 */
public class AffineTransform implements Serializable, ome.util.Filterable {

    private static final long serialVersionUID = 938403948520934L;

    // ~ Fields
    // =========================================================================

    private double a00, a01, a10, a11, a02, a12;

    // ~ Constructors
    // =========================================================================
    //
    /**
     * simple constructor. All transform components will be set to 0;
     */
    public AffineTransform() {
        this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public AffineTransform(double a00, double a01, double a10, double a11, double a02, double a12) {
        this.a00 = a00;
        this.a01 = a01;
        this.a10 = a10;
        this.a11 = a11;
        this.a02 = a02;
        this.a12 = a12;
    }

    // ~ Overrides
    // =========================================================================

    /**
     * Produces a string like "[a00, a01, a10, a11, a02, a12]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        sb.append("[");
        sb.append(a00);
        sb.append(",");
        sb.append(a01);
        sb.append(",");
        sb.append(a10);
        sb.append(",");
        sb.append(a11);
        sb.append(",");
        sb.append(a02);
        sb.append(",");
        sb.append(a12);
        sb.append("]");
        return sb.toString();
    }

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    @Column(name = "a00", nullable = false)
    public double getA00() {
        return this.a00;
    }

    protected void setA00(int a00) {
        this.a00 = a00;
    }

    @Column(name = "a01", nullable = false)
    public double getA01() {
        return this.a01;
    }

    protected void setA01(int a01) {
        this.a01 = a01;
    }

    @Column(name = "a10", nullable = false)
    public double getA10() {
        return this.a10;
    }

    protected void setA10(int a10) {
        this.a10 = a10;
    }

    @Column(name = "a11", nullable = false)
    public double getA11() {
        return this.a11;
    }

    protected void setA11(int a11) {
        this.a11 = a11;
    }

    @Column(name = "a02", nullable = false)
    public double getA02() {
        return this.a02;
    }

    protected void setA02(int a02) {
        this.a02 = a02;
    }

    @Column(name = "a12", nullable = false)
    public double getA12() {
        return this.a12;
    }

    protected void setA12(int a12) {
        this.a12 = a12;
    }

    /**
     * Does nothing.
     */
    public boolean acceptFilter(Filter filter) {
        return false;
    }

}
