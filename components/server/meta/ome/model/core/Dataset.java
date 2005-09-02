package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="dataset"
 *     
 */
public class
Dataset 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer datasetId;
     private Integer version;
     private Integer groupId;
     private String name;
     private String description;
     private Set imageDatasetLinks;


    // Constructors

    /** default constructor */
    public Dataset() {
    }
    
    /** constructor with id */
    public Dataset(Integer datasetId) {
        this.datasetId = datasetId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="sequence"
     *             type="java.lang.Integer"
     *             column="dataset_id"
     *            @hibernate.generator-param
     * 	        name="sequence"
     * 	        value="dataset_seq"
     *         
     */
    public Integer getDatasetId() {
        return this.datasetId;
    }
    
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="dataset_id"
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
