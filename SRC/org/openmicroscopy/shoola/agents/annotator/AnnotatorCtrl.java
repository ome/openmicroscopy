/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl
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

package org.openmicroscopy.shoola.agents.annotator;

//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * 
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
public class AnnotatorCtrl
{
    
    private Annotator       abstraction;
    
    private AnnotatorUIF    presentation;
    
    AnnotatorCtrl(Annotator abstraction)
    {
        this.abstraction = abstraction;
    }
    
    void setPresentation(AnnotatorUIF presentation)
    {
        this.presentation = presentation;
        //Add a window listener
        if (presentation != null)
            presentation.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) { close(); }
            });
    }
    
    public Registry getRegistry() { return abstraction.getRegistry(); }

    /** Forward event to the {@link Annotator abstraction}. */
    public void close() { abstraction.close(); }

    public void saveEnabled(boolean b)
    {
        presentation.bar.saveEnabled(b);
    }

    public void buttonsEnabled(boolean b)
    {
        presentation.bar.buttonsEnabled(b);
    }
    
    /** Forward event to the {@link Annotator abstraction}. */
    public List getOwnerAnnotation(int index)
    {
        return abstraction.getOwnerAnnotation(index);
    }
    
    /** 
     * Delete the annotation, only possible is the annotation was previously in
     * the DB.
     */
    public void delete()
    {
        abstraction.delete(presentation.pane.getAnnotationData());
    }
    
    /** Update or create a new annotation. */
    public void save(int index)
    {
        if (presentation.pane.isCreation()) 
            abstraction.create(presentation.pane.getAnnotation(), index);
        else 
            abstraction.update(presentation.pane.getAnnotationData(), index);
    }
    
    /** Forward event to the {@link Annotator abstraction}. */ 
    public void viewImage(int z, int t) { abstraction.viewImage(z, t); }
    
    /** Forward event to the {@link Annotator abstraction}. */ 
    public void viewImage() { abstraction.viewImage(); }
    
    public int getUserIndex() { return abstraction.getUserIndex(); }
    
    public int getAnnotationIndex() { return abstraction.getAnnotationIndex(); }
    
}