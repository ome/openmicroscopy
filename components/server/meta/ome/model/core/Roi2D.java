package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="roi2d"
 *     
 */
public abstract class
Roi2D 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Integer version;
     private Roi3D roi3d;
     private Shape shape;


    // Constructors

    /** default constructor */
    public Roi2D() {
    }
    
    /** constructor with id */
    public Roi2D(Integer id) {
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
     * 	        value="roi2d_seq"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="roi3d"         
     *         
     */
    public Roi3D getRoi3d() {
        return this.roi3d;
    }
    
    public void setRoi3d(Roi3D roi3d) {
        this.roi3d = roi3d;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="shape"         
     *         
     */
    public Shape getShape() {
        return this.shape;
    }
    
    public void setShape(Shape shape) {
        this.shape = shape;
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
