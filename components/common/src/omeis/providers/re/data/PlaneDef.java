/*
 * omeis.providers.re.data.PlaneDef
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeis.providers.re.data;



//Java imports
import java.io.Serializable;

//Third-party libraries

//Application-internal dependencies

/** 
 * Identifies a <i>2D</i>-plane in the <i>XYZ</i> moving frame of the 
 * <i>3D</i> stack.
 * Tells which plane the wavelengths to render belong to.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/17 17:49:34 $)
 * </small>
 * @since OME2.2
 */
public class PlaneDef 
    implements Serializable
{
    
	/** The serial number */
	private static final long serialVersionUID = -3200013163481587159L;

	/** Flag to identify an <i>XY</i>-plane. */
	public static final int		XY = 0 ;
	
	/** Flag to identify an <i>YZ</i>-plane. */
	public static final int		ZY = 1 ;
	
	/** Flag to identify an <i>XZ</i>-plane. */
	public static final int		XZ = 2 ;
	
    
	/** 
     * Tells which kind of plane this object identifies.  
     * Set to one of the {@link #XY}, {@link #XZ} or {@link #ZY} constants 
     * defined by this class.  
     */
	private int	slice;
	
	/** 
     * Selects a plane in the set identified by {@link #slice}. 
	 * Only relevant in the context of an <i>YZ</i>-plane.
	 */
	private int	x;
	
    /** 
     * Selects a plane in the set identified by {@link #slice}. 
     * Only relevant in the context of an <i>XZ</i>-plane.
     */
	private int	y;
	
    /** 
     * Selects a plane in the set identified by {@link #slice}. 
     * Only relevant in the context of an <i>XY</i> plane.
     */
	private int	z;
	
	/** The timepoint. */
	private int	t;
	
    
	/**
	 * Creates a new instance.
     * This new object will identify the plane belonging to the set specified
     * by <code>slice</code> and <code>t</code> and having a <code>0</code>
     * index.  Call the appropriate <code>setXXX</code> method to set another 
     * index. 
	 * 
	 * @param slice		Tells which kind of plane this object identifies.  
     *                  Must be one of the {@link #XY}, {@link #XZ}, {@link #ZY}
     *                  constants defined by this class.
	 * @param t			The selected timepoint.
     * @throws IllegalArgumentException If bad arguments are passed in.
	 */
	public PlaneDef(int slice, int t)
	{
        switch (slice) {
            case XY:
            case ZY:
            case XZ:
                this.slice = slice;
                break;
            default:
                throw new IllegalArgumentException("Wrong slice identifier: " +
                                                slice+".");
        }  
        if (t < 0)
            throw new IllegalArgumentException("Negative timepoint: "+t+".");
		this.t = t;
	}

	/**  
     * Selects a plane in the set identified by this object. 
     * Can only be called in the context of an <i>ZY</i>-plane.
     * 
     * @param x The plane index.
     * @throws IllegalArgumentException If bad arguments are passed in or this 
     *                                  is not an <i>ZY</i>-plane.
     */
	public void setX(int x)
	{
		//Verify X normal.
        if (ZY != slice)
            throw new IllegalArgumentException(
                    "X index can only be set for ZY planes.");
        if (x < 0)
            throw new IllegalArgumentException("Negative index: "+x+".");
		this.x = x;
	}

    /**  
     * Selects a plane in the set identified by this object. 
     * Can only be called in the context of an <i>XZ</i>-plane.
     * 
     * @param y The plane index.
     * @throws IllegalArgumentException If bad arguments are passed in or this 
     *                                  is not an <i>XZ</i>-plane. 
     */
	public void setY(int y)
	{
		//Verify Y normal.
        if (XZ != slice)
            throw new IllegalArgumentException(
                    "Y index can only be set for XZ planes.");
        if (y < 0)
            throw new IllegalArgumentException("Negative index: "+y+".");
		this.y = y;
	}
	
    /**  
     * Selects a plane in the set identified by this object. 
     * Can only be called in the context of an <i>XY</i>-plane.
     * 
     * @param z The plane index. 
     * @throws IllegalArgumentException If bad arguments are passed in or this 
     *                                  is not an <i>XY</i>-plane.
     */
	public void setZ(int z)
	{
	    //Verify Z normal.
        if (XY != slice)
            throw new IllegalArgumentException(
                    "Z index can only be set for XY planes.");
        if (z < 0)
            throw new IllegalArgumentException("Negative index: "+z+".");
		this.z = z;
	}

    /**
     * Returns an identifier to tell which kind of plane this object identifies.  
     *  
     * @return  One of the {@link #XY}, {@link #XZ} or {@link #ZY} constants 
     *          defined by this class.
     */
	public int getSlice() { return slice; }

	/**
     * Returns the timepoint.
     * 
	 * @return See above.
	 */
	public int getT() { return t; }

    /**
     * Returns the index of the plane in the set identified by this object. 
     * Only relevant in the case of an <i>YZ</i>-plane.
     * 
     * @return See above.
     */
	public int getX() { return x; }

    /**
     * Returns the index of the plane in the set identified by this object. 
     * Only relevant in the case of an <i>XZ</i>-plane.
     * 
     * @return See above.
     */
	public int getY() { return y; }

    /**
     * Returns the index of the plane in the set identified by this object. 
     * Only relevant in the case of an <i>XY</i>-plane.
     * 
     * @return See above.
     */
	public int getZ() { return z; }

    /**
     * Overridden to reflect equality of abstract values (data object) as 
     * opposite to object identity.
     * @see Object#equals(Object)
     */
	public boolean equals(Object o)
    {
	    boolean isEqual = false;
        if (o != null && o instanceof PlaneDef) {  //Else can't be equal.
            PlaneDef other = (PlaneDef) o;
            if (other.slice == slice && other.t == t) {  //Else can't be equal.
                switch (slice) {
                    case XY:
                        isEqual = (other.z == z);
                        break;
                    case ZY:
                        isEqual = (other.x == x);
                        break;
                    case XZ:
                        isEqual = (other.y == y);
                        break;
                }
            }
        }
        return isEqual;
    }
	
    /**
     * Overridden to reflect equality of abstract values (data object) as 
     * opposite to object identity.
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        //We return f(u, t) = (u % 2^15)*2^15 + (t % 2^15), where u is one out
        //of x, y, z depending on slice.
        int u = -1, two15 = 0x8000;  //0x8000 = 2^15
        switch (slice) {
            case XY:
                u = z;
                break;
            case ZY:
                u = x;
                break;
            case XZ:
                u = y;
                break;
        }
        return ((u%two15)*two15 + (t%two15));
    }
    /* NOTE:  The hash code function that we picked, besides being easy and
     * fast to calculate, has this very nice property:
     * 
     *      (u1, t1) = (u2, t2)    <=>    f(u1, t1) = f(u2, t2)
     * 
     * as long as 0 <= u1, t1, u2, t2 < 2^15.
     * Because for all pratical purposes we can assume a pixels set contains
     * less than 2^15 timepoints and less than 2^15 sections in each stack, 
     * then the above implies in practice:
     *  
     *      a.equals(b)    <=>    a.hashCode() == b.hashCode()
     * 
     * where a and b are two instances of PlaneDef and provided that state 
     * didn't change across invocations of the equals and hashCode methods.
     * So we can basically assume this functions avoids collisions.  Even
     * though this is not a requirement for the hashCode contract, avoiding
     * collisions makes PlaneDef very "hash table friedly" :)
     */
    
    /** 
     * Overrides generic <code>toString</code> method to provide a specific
     * string representation of this object.
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        switch (slice) {
            case XY:
                buf.append("Type: XY, ");
                buf.append("z="+z);
                break;
            case ZY:
                buf.append("Type: ZY, ");
                buf.append("x="+x);
                break;
            case XZ:
                buf.append("Type: XZ, ");
                buf.append("y="+y);
                break;
        }
        buf.append(", timepoint: "+t);
        return buf.toString();
    }
    
}
