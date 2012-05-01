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
     * simple constructor. All color components will be set to 0;
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

    /**
     * Returns the <code>red</code> component of the color.
     * 
     * @return See above.
     */
    public int getRed()
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>green</code> component of the color.
     * 
     * @return See above.
     */
    public int getGreen()
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>blue</code> component of the color.
     * 
     * @return See above.
     */
    public int getBlue()
    {
    	return 0; //To be implemented.
    }
    
    /**
     * Returns the <code>alpha</code> component of the color.
     * 
     * @return See above.
     */
    public int getAlpha()
    {
    	return 0; //To be implemented.
    }
    
    protected void setColor(int color) {
        this.color = color;
    }

}
