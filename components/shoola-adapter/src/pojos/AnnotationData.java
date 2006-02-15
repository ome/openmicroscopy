/*
 * pojos.AnnotationData
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

package pojos;


//Java imports
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.internal.Details;
import ome.util.ModelMapper;


/** 
 * Holds a textual annotation of a given data object and a reference to the
 * Experimenter that wrote it.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class AnnotationData
    implements DataObject
{
    
    /** The annotation ID. */
    private long         id;
    
    /** The annotation textual description. */
    private String       text;
    
    /**
     * Timestamp indicating the last time the annotation was modified.
     * This field may not be <code>null</code>.
     */
    private Timestamp    lastModified;
    
    /**
     * The object this annotation refers to, for example Image or Dataset.
     * This field may not be <code>null</code>.
     */
    private DataObject   annotatedObject;
    
    /** 
     * The Experimenter that wrote this annotation.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof ImageAnnotation) {
			ImageAnnotation iann = (ImageAnnotation) model;
			this.setId(mapper.nullSafeLong(iann.getId()));
			this.setText(iann.getContent());
			Details details = iann.getDetails();
			if (details!=null){
				this.setLastModified(mapper.event2timestamp(details.getUpdateEvent()));
				this.setOwner((ExperimenterData) mapper.findTarget(details.getOwner()));
			}
			this.setAnnotatedObject((DataObject) mapper.findTarget(iann.getImage()));
    	} else if (model instanceof DatasetAnnotation) {
			DatasetAnnotation dann = (DatasetAnnotation) model;
			this.setId(mapper.nullSafeLong(dann.getId()));
			this.setText(dann.getContent());
			Details details = dann.getDetails();
			if (details!=null){
				this.setLastModified(mapper.event2timestamp(details.getUpdateEvent()));
				this.setOwner((ExperimenterData) mapper.findTarget(details.getOwner()));
			}
			this.setAnnotatedObject((DataObject) mapper.findTarget(dann.getDataset()));
		} else {
			throw new IllegalArgumentException("AnnotationData can only copy from ImageAnnotation and DatasetAnnotations");
		}
    }

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setAnnotatedObject(DataObject annotatedObject) {
		this.annotatedObject = annotatedObject;
	}

	public DataObject getAnnotatedObject() {
		return annotatedObject;
	}

	public void setOwner(ExperimenterData owner) {
		this.owner = owner;
	}

	public ExperimenterData getOwner() {
		return owner;
	}
	
	public String toString() {
		return getClass().getName()+" (id="+getId()+")";
	}
    
}
