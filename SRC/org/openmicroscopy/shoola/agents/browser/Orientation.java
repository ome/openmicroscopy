/*
 * org.openmicroscopy.shoola.agents.browser.Orientation
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

/**
 * An enumerated type that encapsulates the eight cardinal directions plus
 * center.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public final class Orientation
{
  // dummy differentiation variable
  private int value;
  
  // singleton constructor
  private Orientation(int value)
  {
    this.value = value;
  }
  
  /**
   * Symbolic center orientation.
   */
  public static final Orientation CTR = new Orientation(-1);
  
  /**
   * Symbolic north (top) orientation.
   */
  public static final Orientation N = new Orientation(0);
  
  /**
   * Symbolic northeast (top-right) orientation.
   */
  public static final Orientation NE = new Orientation(45);
  
  /**
   * Symbolic east (right) orientation;
   */
  public static final Orientation E = new Orientation(90);
  
  /**
   * Symbolic southeast (bottom-right) orientation.
   */
  public static final Orientation SE = new Orientation(135);
  
  /**
   * Symbolic south (bottom) orientation.
   */
  public static final Orientation S = new Orientation(180);
  
  /**
   * Symbolic southwest (bottom-left) orientation.
   */
  public static final Orientation SW = new Orientation(225);
  
  /**
   * Symbolic west (left) orientation.
   */
  public static final Orientation W = new Orientation(270);
  
  /**
   * Symbolic northwest (top-left) orientation.
   */
  public static final Orientation NW = new Orientation(315);
  
  /**
   * Returns the canonical value of the orientation, for ease of use
   * in switch statements.
   * @return The canonical value of the orientation.
   */
  public int getValue()
  {
    return value;
  }
  
  /**
   * Equality by reference override.
   */
  public boolean equals(Object o)
  {
    return this == o;
  }
  
  /**
   * Equals override begets hashCode override (maintain contract)
   */
  public int hashCode()
  {
    return value;
  }
}
