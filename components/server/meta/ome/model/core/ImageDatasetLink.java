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
ome.api.OMEModel {

    // Fields    

     private Integer imageDatasetLinkId;
     private Integer version;
     private Dataset dataset;
     private Image image;


    // Constructors

    /** default constructor */
    public ImageDatasetLink() {
    }
    
    /** constructor with id */
    public ImageDatasetLink(Integer imageDatasetLinkId) {
        this.imageDatasetLinkId = imageDatasetLinkId;
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
    public Integer getImageDatasetLinkId() {
        return this.imageDatasetLinkId;
    }
    
    public void setImageDatasetLinkId(Integer imageDatasetLinkId) {
        this.imageDatasetLinkId = imageDatasetLinkId;
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
