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

import ome.api.OMEModel;
import ome.model.DatasetAnnotation;
import ome.model.ImageAnnotation;
import ome.model.ModuleExecution;
import ome.util.ModelMapper;
import sun.security.krb5.internal.crypto.t;

//Third-party libraries

//Application-internal dependencies

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
    public int          id;
    
    /** The annotation textual description. */
    public String       text;
    
    /**
     * Timestamp indicating the last time the annotation was modified.
     * This field may not be <code>null</code>.
     */
    public Timestamp    lastModified;
    
    /**
     * The object this annotation refers to, for example Image or Dataset.
     * This field may not be <code>null</code>.
     */
    public DataObject   annotatedObject;
    
    /** 
     * The Experimenter that wrote this annotation.
     * This field may not be <code>null</code>.  
     */
    public ExperimenterData owner;
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof ImageAnnotation) {
			ImageAnnotation iann = (ImageAnnotation) model;
			this.id=mapper.nullSafeInt(iann.getAttributeId());
			this.text=iann.getContent();
			ModuleExecution mex = iann.getModuleExecution();
			if (mex!=null){
				this.lastModified=mapper.date2timestamp(mex.getTimestamp());
				this.owner=(ExperimenterData) mapper.findTarget(mex.getExperimenter());
			}
			this.annotatedObject=(DataObject) mapper.findTarget(iann.getImage());
    	} else if (model instanceof DatasetAnnotation) {
			DatasetAnnotation dann = (DatasetAnnotation) model;
			this.id=mapper.nullSafeInt(dann.getAttributeId());
			this.text=dann.getContent();
			ModuleExecution mex = dann.getModuleExecution();
			if (mex!=null){
				this.lastModified=mapper.date2timestamp(mex.getTimestamp());
				this.owner=(ExperimenterData) mapper.findTarget(mex.getExperimenter());
			}
			this.annotatedObject=(DataObject) mapper.findTarget(dann.getDataset());
		} else {
			throw new IllegalArgumentException("AnnotationData can only copy from ImageAnnotation and DatasetAnnotations");
		}
    }
    
}
