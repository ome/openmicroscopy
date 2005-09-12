package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="image_dataset_link"
 *     
 */
public class
ImageDatasetLink 
implements java.io.Serializable ,
ome.api.OMEModel
, ome.NewModel 
{

    // Fields    

     private Integer id;
     private Integer version;
     private byte[] permissions;
     private ome.model.meta.Experimenter owner;
     private ome.model.meta.Event creationEvent;
     private ome.model.meta.Event updateEvent;
     private Dataset dataset;
     private Image image;


    // Constructors

    /** default constructor */
    public ImageDatasetLink() {
    }
    
    /** constructor with id */
    public ImageDatasetLink(Integer id) {
        this.id = id;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="sequence"
     *             type="java.lang.Integer"
     *             column="image_dataset_link_id"
     *            @hibernate.generator-param
     * 	        name="sequence"
     * 	        value="image_dataset_link_seq"
     *         
     */
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *      *            @hibernate.version
     *             column="version"
     *             unsaved-value="negative"
     *         
     */
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 
     */
    public byte[] getPermissions() {
        return this.permissions;
    }
    
    public void setPermissions(byte[] permissions) {
        this.permissions = permissions;
    }

    /**
     * 
     */
    public ome.model.meta.Experimenter getOwner() {
        return this.owner;
    }
    
    public void setOwner(ome.model.meta.Experimenter owner) {
        this.owner = owner;
    }

    /**
     * 
     */
    public ome.model.meta.Event getCreationEvent() {
        return this.creationEvent;
    }
    
    public void setCreationEvent(ome.model.meta.Event creationEvent) {
        this.creationEvent = creationEvent;
    }

    /**
     * 
     */
    public ome.model.meta.Event getUpdateEvent() {
        return this.updateEvent;
    }
    
    public void setUpdateEvent(ome.model.meta.Event updateEvent) {
        this.updateEvent = updateEvent;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="dataset_id"         
     *         
     */
    public Dataset getDataset() {
        return this.dataset;
    }
    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="image_id"         
     *         
     */
    public Image getImage() {
        return this.image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }





	/** utility methods. Container may re-assign this. */	
	protected static BaseModelUtils _utils = 
		new BaseModelUtils();
	public BaseModelUtils getUtils(){
		return _utils;
	}
	public void setUtils(BaseModelUtils utils){
		_utils = utils;
	}



}
