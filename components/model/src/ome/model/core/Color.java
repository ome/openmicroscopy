/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.persistence.Column;

import ome.conditions.ApiUsageException;

/**
 * class responsible for storing RGBA-style colors in the database.
 *
 * @see <a
 *      href="https://trac.openmicroscopy.org.uk/omero/ticket/7944">ticket:7944</a>
 */
public class Color implements Serializable {

    private static final long serialVersionUID = 938403948520934L;

    // ~ Fields
    // =========================================================================

    private int color;

    // ~ Constructors
    // =========================================================================
    //
    /**
     * simple contructor. All color components will be set to 0;
     */
    public Color() {
        this(0, 0, 0, 0);
    }

    /**
     * copy constructor. Will create a new {@link Permissions} with the same
     * {@link Right rights} as the argument.
     */
    public Color(int red, int green, int blue, int alpha) {
        // TOOD: use these to create a single instance of ome.xml.Color
    }

    // ~ Overrides
    // =========================================================================

    /**
     * Produces a string like "rgba(24,53,35,)"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        sb.append("rgba(");
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    @Column(name = "color", nullable = false)
    public int getColor() {
        return this.color;
    }

    protected void setColor(int color) {
        this.color = color;
    }

}
