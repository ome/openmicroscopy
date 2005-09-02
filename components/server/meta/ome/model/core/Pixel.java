package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="pixel"
 *     
 */
public class
Pixel 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Set roi5ds;
     private Set maps;


    // Constructors

    /** default constructor */
    public Pixel() {
    }
    
    /** constructor with id */
    public Pixel(Integer id) {
        this.id = id;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="sequence"
     *             type="java.lang.Integer"
     *             column="id"
     *            @hibernate.generator-param
     * 	        name="sequence"
     * 	        value="pixel_seq"
     *         
     */
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="pixel"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.Roi5D"
     *         
     */
    public Set getRoi5ds() {
        return this.roi5ds;
    }
    
    public void setRoi5ds(Set roi5ds) {
        this.roi5ds = roi5ds;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="pixel"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.Map"
     *         
     */
    public Set getMaps() {
        return this.maps;
    }
    
    public void setMaps(Set maps) {
        this.maps = maps;
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
