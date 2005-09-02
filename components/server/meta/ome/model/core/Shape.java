package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="shape"
 *     
 */
public abstract class
Shape 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Set roi2ds;


    // Constructors

    /** default constructor */
    public Shape() {
    }
    
    /** constructor with id */
    public Shape(Integer id) {
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
     * 	        value="shape_seq"
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
     *            @hibernate.collection-key
     *             column="roi2d"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.Roi2D"
     *         
     */
    public Set getRoi2ds() {
        return this.roi2ds;
    }
    
    public void setRoi2ds(Set roi2ds) {
        this.roi2ds = roi2ds;
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
