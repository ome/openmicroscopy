/*
 * org.openmicroscopy.shoola.env.data.model.AnnotationData
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

package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies

/** 
 * DON'T CODE AGAINST IT. WILL BE MODIFIED WHEN WE REVIEW THE DATA MODEL
 * 
 * Collapses in one object the ImageAnnotation and DatasetAnnotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnnotationData
{
    
    /** 
     * Default value, if theZ or theT equals the DEFAULT value, the values 
     *  aren't taken into account when we update or create a new 
     * ImageAnnotation.
     */
    public static final int     DEFAULT = -1;
    private int                 theZ;
    
    private int                 theT;
    
    /** Attribute ID in DB. */
    private final int   id;
    
    private Timestamp   date;
    private String      annotation;
    
    /** Information on the user who wrote the annotation. */
    private int         ownerID;
    private String      ownerFirstName;
    private String      ownerLastName;
    
    public AnnotationData(int id, int ownerID, Timestamp date)
    {
        this.id = id;
        this.ownerID = ownerID;
        this.date = date;
        setDefault();
    }
    
    public AnnotationData(int id, int ownerID, String ownerFirstName, String
                          ownerLastName, String annotation, Timestamp date)
    {
        this.id = id;
        this.ownerID = ownerID;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.annotation = annotation;
        this.date = date;
        setDefault();
    }

    public String getAnnotation() { return annotation; }
    
    public void setAnnotation(String annotation)
    {
        this.annotation = annotation;
    }
    
    public Timestamp getDate() { return date; }
    
    public int getID() { return id; }
    
    public String getOwnerFirstName() { return ownerFirstName; }
    
    public int getOwnerID() { return ownerID; }
    
    public String getOwnerLastName() { return ownerLastName; }
    
    public void setOwnerLastName(String s) { ownerLastName = s; }
    
    public void setOwnerFirstName(String s) { ownerFirstName = s; }
    
    public int getTheT() { return theT; }
    
    public void setTheT(int theT) { this.theT = theT;}
    
    public int getTheZ() { return theZ; }
    
    public void setTheZ(int theZ) {this.theZ = theZ; }
    
    private void setDefault()
    {
        this.theZ = DEFAULT;
        this.theT = DEFAULT;
    }
    
}
