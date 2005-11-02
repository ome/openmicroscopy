/*
 * pojos.ImageData
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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ome.api.OMEModel;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.ImagePixel;
import ome.model.ModuleExecution;
import ome.util.ModelMapper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import sun.security.krb5.internal.crypto.m;

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Image along with links to its
 * Pixels, enclosing Datasets, and the Experimenter that owns this Image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/09 19:50:41 $)
 * </small>
 * @since OME2.2
 */
public class ImageData
    implements DataObject
{

    /** The Image ID. */
    private int      id;
    
    /** 
     * The Image's name.
     * This field may not be <code>null</code>.  
     */
    private String   name;
    
    /** The Image's description. */
    private String   description;
    
    /**
     * The creation timestamp.
     * That is the time at which the Image was created.
     * This field may not be <code>null</code>.
     */
    private Timestamp  created;
    
    /**
     * The insertion timestamp.
     * That is the time at which the Image was inserted into the DB.
     * This field may not be <code>null</code>.
     */
    private Timestamp  inserted;
    
    /**
     * The default image data associated to this Image.
     * An <i>OME</i> Image can be associated to more than one 5D pixels set
     * (that is, the raw image data) if all those sets are derived from an
     * initial image file.  An example is a deconvolved image and the original
     * file: those two pixels sets would be represented by the same <i>OME</i>
     * Image.  
     * In the case there's more than one pixels set, this field identifies the
     * pixels that are used by default for analysis and visualization.  If the
     * Image only has one pixels set, then this field just points to that set.
     * This field may not be <code>null</code>.
     */
    private PixelsData defaultPixels;
    
    /**
     * All the Pixels that belong to this Image.
     * The elements of this set are {@link PixelsData} objects.
     * This field may not be <code>null</code> nor empty.  As a minimum, it
     * will contain the {@link #defaultPixels default} Pixels.
     * 
     * @see #defaultPixels
     */
    private Set        allPixels;
    
    /** 
     * All the Datasets that contain this Image.
     * The elements of this set are {@link DatasetData} objects.  If this
     * Image is not contained in any Dataset, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      datasets;
    
    /**
     * All the annotations related to this Image.
     * The elements of the set are {@link AnnotationData} objetcs.
     * If this Image hasn't been annotated, then this set will be empty
     * &#151; but never <code>null</code>. 
     */
    private Set      annotations;
    
    /** 
     * The Experimenter that owns this Dataset.
     * This field may not be <code>null</code>.  
     */
    private ExperimenterData owner;
    
    public void copy(OMEModel model, ModelMapper mapper) {
    	if (model instanceof Image) {
			Image i = (Image) model;
			this.setId(mapper.nullSafeInt(i.getImageId()));
			this.setName(i.getName());
			this.setDescription(i.getDescription());
			this.setCreated(mapper.date2timestamp(i.getCreated()));
			this.setInserted(mapper.date2timestamp(i.getInserted()));
			this.setDefaultPixels((PixelsData)mapper.findTarget(i.getImagePixel()));
			this.setAllPixels((Set) mapper.findCollection(i.getImagePixels()));
			this.setDatasets((Set) mapper.findCollection(i.getDatasets()));
			this.setAnnotations((Set) mapper.findCollection(i.getImageAnnotations()));
			this.setOwner((ExperimenterData) mapper.findTarget(i.getExperimenter()));
		} else {
			throw new IllegalArgumentException("ImageData copies only from Image");
		}
    }

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setInserted(Timestamp inserted) {
		this.inserted = inserted;
	}

	public Timestamp getInserted() {
		return inserted;
	}

	public void setDefaultPixels(PixelsData defaultPixels) {
		this.defaultPixels = defaultPixels;
	}

	public PixelsData getDefaultPixels() {
		return defaultPixels;
	}

	public void setAllPixels(Set allPixels) {
		this.allPixels = allPixels;
	}

	public Set getAllPixels() {
		return allPixels;
	}

	public void setDatasets(Set datasets) {
		this.datasets = datasets;
	}

	public Set getDatasets() {
		return datasets;
	}

	public void setAnnotations(Set annotations) {
		this.annotations = annotations;
	}

	public Set getAnnotations() {
		return annotations;
	}

	public void setOwner(ExperimenterData owner) {
		this.owner = owner;
	}

	public ExperimenterData getOwner() {
		return owner;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
				.append("name", this.name)
				.toString();
	}
}
