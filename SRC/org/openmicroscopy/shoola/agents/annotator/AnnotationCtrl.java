/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotationCtrl
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
 
package org.openmicroscopy.shoola.agents.annotator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.dto.Attribute;
/**
 * Interface for annotator controls.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class AnnotationCtrl
{
    /**
     * Whether or not this state has been committed.
     */
    protected boolean saved = true;
    
    /**
     * The backing annotator.
     */
    protected Annotator annotator;
    
    /**
     * The list of annotations.
     */
    protected List annotationList;
    
    /**
     * The list of attributes (excluding text annotations)
     */
    protected List attributeList;
    
    /**
     * Gets the text annotations associated with a data object.
     * @return A list of textual annotations.
     */
    public List getTextAnnotations()
    {
        return Collections.unmodifiableList(annotationList);
    }
    
    /**
     * Gets the attributes associated with a data object.
     * @return A list of attributes for the target.
     */
    public List getAttributes()
    {
        return Collections.unmodifiableList(attributeList);
    }
    
    /**
     * Sets an annotation at the specified ID to the specified value.
     * @param annotationID The ID of the annotation to modify.
     * @param annotation The new value of the annotation.
     */
    public void setAnnotation(int annotationID, String annotation)
    {
        // TODO: iterate through attributes, cast annotation, set value
    }
    
    /**
     * Sets an attribute at the specified ID to the specified value.
     * @param attributeID The ID of the attribute to modify.
     * @param attribute The new value of the attribute.
     */
    public void setAttribute(int attributeID, Attribute attribute)
    {
        // TODO: iterate through attributes, set value
    }
    
    /**
     * Set the saved state of this attribute/annotation.
     * @param saved Whether or not to mark this as saved.
     * @return
     */
    public void setSaved(boolean saved)
    {
        this.saved = saved;
    }
    
    /**
     * Whether or not this attribute/annotation has been saved.
     * @return See above
     */
    public boolean isSaved()
    {
        return saved;
    }
    
    /**
     * Stores the information in the DB.
     */
    public abstract void save();
    
    /**
     * Closes the window and removes this controller from the annotator's
     * list of controllers to watch.
     */
    public void close()
    {
        annotator.close(this);
    }
}
