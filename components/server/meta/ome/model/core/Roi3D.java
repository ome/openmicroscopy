package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="roi3d"
 *     
 */
public abstract class
Roi3D 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Integer version;
     private Roi4D roi4d;
     private Set roi2ds;


    // Constructors

    /** default constructor */
    public Roi3D() {
    }
    
    /** constructor with id */
    public Roi3D(Integer id) {
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
     * 	        value="roi3d_seq"
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
     *            @hibernate.column name="roi4d"         
     *         
     */
    public Roi4D getRoi4d() {
        return this.roi4d;
    }
    
    public void setRoi4d(Roi4D roi4d) {
        this.roi4d = roi4d;
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
