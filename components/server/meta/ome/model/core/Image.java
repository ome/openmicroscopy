package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="image"
 *     
 */
public class
Image 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer imageId;
     private Integer version;
     private Integer pixelsId;
     private String name;
     private String description;
     private Integer groupId;
     private String imageGuid;
     private Set imageDatasetLinks;


    // Constructors

    /** default constructor */
    public Image() {
    }
    
    /** constructor with id */
    public Image(Integer imageId) {
        this.imageId = imageId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="sequence"
     *             type="java.lang.Integer"
     *             column="image_id"
     *            @hibernate.generator-param
     * 	        name="sequence"
     * 	        value="image_seq"
     *         
     */
    public Integer getImageId() {
        return this.imageId;
    }
    
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
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
     *      *            @hibernate.property
     *             column="pixels_id"
     *             length="4"
     *         
     */
    public Integer getPixelsId() {
        return this.pixelsId;
    }
    
    public void setPixelsId(Integer pixelsId) {
        this.pixelsId = pixelsId;
    }

    /**
     *      *            @hibernate.property
     *             column="name"
     *             length="256"
     *             not-null="true"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="description"
     *             length="256"
     *         
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      *            @hibernate.property
     *             column="group_id"
     *             length="4"
     *         
     */
    public Integer getGroupId() {
        return this.groupId;
    }
    
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     *      *            @hibernate.property
     *             column="image_guid"
     *             length="256"
     *         
     */
    public String getImageGuid() {
        return this.imageGuid;
    }
    
    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="image_id"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.ImageDatasetLink"
     *         
     */
    public Set getImageDatasetLinks() {
        return this.imageDatasetLinks;
    }
    
    public void setImageDatasetLinks(Set imageDatasetLinks) {
        this.imageDatasetLinks = imageDatasetLinks;
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
