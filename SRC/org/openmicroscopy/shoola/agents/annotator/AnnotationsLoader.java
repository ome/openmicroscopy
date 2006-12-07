/*
 * org.openmicroscopy.shoola.agents.util.annotator.AnnotationsLoader 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.annotator;



//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.view.Annotator;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Retrieves the annotations linked to the annotated <code>DataObject</code>s.
 * This class calls the <code>loadAnnotations</code> method in the 
 * <code>AnnotatorView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AnnotationsLoader 
	extends AnnotatorLoader
{

	/** Collection of <code>DataObject</code>s id. */
	private Set				nodeIds;
	
	/** The type of annotation. */
	private Class			type;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for.
	 * @param objects		Collection of <code>DataObject</code>s to 
							retrieve the annotations for.
	 * @param type			The type of annotation.
	 */
	public AnnotationsLoader(Annotator viewer, Set objects, Class type)
	{
		super(viewer);
		if (!checkAnnotationType(type))
			throw new IllegalArgumentException("Type not supported: "+type);
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No data objects specifed.");
		nodeIds = new HashSet(objects.size());
		Iterator i = objects.iterator();
		while (i.hasNext()) {
			nodeIds.add(new Long(((DataObject) i.next()).getId()));
		}
		this.type = type;
	}
	
	/** 
     * Cancels the data loading. 
     * @see AnnotatorLoader#laod()
     */
	public void load()
	{
		handle = aView.loadAnnotations(type, nodeIds, this);
	}
	
	/** 
     * Cancels the data loading. 
     * @see AnnotatorLoader#cancel()
     */
	public void cancel() { handle.cancel(); }
	
    /**
     * Feeds the result back to the viewer.
     * @see AnnotatorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Annotator.DISCARDED) return; 
        viewer.setAnnotations((Map) result);
    }
	
}
