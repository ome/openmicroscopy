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

import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

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
    protected static final int NEW_ID = -1;
    /**
     * Whether or not this state has been committed.
     */
    protected boolean saved = true;
    
    /**
     * The backing annotator.
     */
    protected Annotator annotator;
    
    /**
     * The list of annotations. (as strings)
     */
    protected List annotationList;
    
    /**
     * The list of attributes (excluding text annotations)
     */
    protected List attributeList;
    
    /**
     * Returns the description of the object being annotated.
     * @return
     */
    public abstract String getTargetDescription();
    
    /**
     * Gets the text annotations associated with a data object.
     * @return A list of textual annotations.
     */
    public abstract List getTextAnnotations();
    
    /**
     * Gets the attributes associated with a data object.
     * @return A list of attributes for the target.
     */
    public List getAttributes()
    {
        return Collections.unmodifiableList(attributeList);
    }
    
    /**
     * Get the annotation in (annotation ID-ordered) index.
     * @param index The index of the annotation to load.
     */
    public abstract String getAnnotation(int index);

    /**
     * Create a new annotation.
     * @param annotation
     */
    public abstract void newAnnotation(String annotation);
    
    /**
     * Sets an annotation at the specified ID to the specified value.  If
     * the annotation is new, the annotationID should be NEW_ID.
     * @param annotationID The ID of the annotation to modify.
     * @param annotation The new value of the annotation.
     */
    public abstract void setAnnotation(int annotationIndex, String annotation);
    
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
     * Prompt termination.  Expected return values: 
     * @return
     */
    public boolean canExit()
    {
        if(!isSaved())
        {
            Object[] options = {"Save","Don't Save","Cancel"};
            int outcome = JOptionPane.showOptionDialog(null,
                                         "Would you like to save this annotation?",
                                         "Save Annotation",
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options,
                                         options[0]);
            if(outcome == JOptionPane.YES_OPTION)
            {
                save();
                return true;
            }
            else if(outcome == JOptionPane.NO_OPTION)
            {
                return true;
            }
            else if(outcome == JOptionPane.CANCEL_OPTION)
            {
                return false;
            }
            else return true; // something's f'ed up; just bail?
        }
        else return true;
    }
    
    /**
     * Stores the information in the DB.
     * @return Whether or not the save was successful.
     */
    public abstract boolean save();
    
    /**
     * Tells the annotator that we are done.
     */
    public void close()
    {
        annotator.close(this);
    }
}
