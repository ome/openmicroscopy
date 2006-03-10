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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.adapters.pojos.MapperBlock;
import ome.model.IObject;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Details;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

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
    extends DataObject
{
    
    public final static String NAME = Image.NAME;
    public final static String DESCRIPTION = Image.DESCRIPTION;
    public final static String PIXELS = Image.PIXELS;
    public final static String ANNOTATIONS = Image.ANNOTATIONS;
    public final static String DATASET_LINKS = Image.DATASETLINKS;
    
    
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
    
    /** 
     * The number of annotations attached to this Image.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer annotationCount;
    
    /** 
     * The number of categories attached to this Imaget.
     * This field may be <code>null</code> meaning no count retrieved,
     * and it may be less than the actual number if filtered by user.
     */
    private Integer classificationCount;
    
    public void copy(IObject model, ModelMapper mapper) {
    	if (model instanceof Image) {
			Image i = (Image) model;
            super.copy(model,mapper);
            
            // Details
            if (i.getDetails() != null){
                Details d = i.getDetails();
                this.setCreated(mapper.event2timestamp(d.getCreationEvent()));
                this.setInserted(mapper.event2timestamp(d.getUpdateEvent()));//TODO
                this.setOwner((ExperimenterData) mapper.findTarget(d.getOwner()));
                if ( d.getCounts() != null )
                {
                    Object annotationCount = d.getCounts().get( Image.ANNOTATIONS );
                    if ( annotationCount instanceof Integer )
                        this.setAnnotationCount( (Integer) annotationCount  );
                    
                    Object classificationCount = d.getCounts().get( Image.CATEGORYLINKS );
                    if ( classificationCount instanceof Integer )
                        this.setClassificationCount( (Integer) classificationCount  );
                    
                }
            }

            // Fields
            this.setName(i.getName());
            this.setDescription(i.getDescription());
			this.setDefaultPixels((PixelsData)mapper.findTarget(i.getDefaultPixels()));
            
            // Collections
            MapperBlock block = new MapperBlock( mapper );
            setDatasets( makeSet(
                    i.sizeOfDatasetLinks(),
                    i.eachLinkedDataset( block )));
            setAllPixels( makeSet(
                    i.sizeOfPixels(),
                    i.collectPixels( block )));
            setAnnotations( makeSet(
                    i.sizeOfAnnotations(),
                    i.collectAnnotations( block )));
            
		} else {
			throw new IllegalArgumentException("ImageData copies only from Image");
		}
    }
    
    public IObject newIObject()
    {
        return new Image();
    }
    
    public IObject fillIObject( IObject obj, ReverseModelMapper mapper)
    {
        if ( obj instanceof Image)
        {
            Image i = (Image) obj;
          
            if (super.fill(i)) {
                i.setName(this.getName());
                i.setDescription(this.getDescription());
                i.setDescription(this.getDescription());
    
                Pixels pix = (Pixels) mapper.map(this.getDefaultPixels());
                if ( pix != null) pix.setDefaultPixels( Boolean.TRUE );
                
                if (this.getAllPixels() != null){
                    for (Iterator it = this.getAllPixels().iterator(); it.hasNext();)
                    {
                        PixelsData p = (PixelsData) it.next();
                        i.addToPixels( (Pixels) mapper.map(p) );
                    }
                }
                
                if (this.getAnnotations() != null){
                    for (Iterator it = this.getAnnotations().iterator(); it.hasNext();)
                        {
                        AnnotationData ann = (AnnotationData) it.next();
                        i.addToAnnotations( (ImageAnnotation) mapper.map(ann) );
                        }
                }
                
                if (this.getDatasets() != null){
                    for (Iterator it = this.getDatasets().iterator(); it.hasNext();)
                    {
                        DatasetData d = (DatasetData) it.next();
                        i.linkDataset((Dataset) mapper.map(d));
                    }
                }
            }
            return i;
        } else {
            throw new IllegalArgumentException(
                    "ImageData can only fill Image.");
        }
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

    public Integer getAnnotationCount()
    {
        return annotationCount;
    }
    
    public void setAnnotationCount(Integer annotationCount)
    {
        this.annotationCount = annotationCount;
    }

    public Integer getClassificationCount()
    {
        return classificationCount;
    }

    public void setClassificationCount(Integer classificationCount)
    {
        this.classificationCount = classificationCount;
    }
}
