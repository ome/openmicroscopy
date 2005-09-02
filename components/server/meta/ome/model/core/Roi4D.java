package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="roi4d"
 *     
 */
public abstract class
Roi4D 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Integer version;
     private Roi5D roi5d;
     private Set roi3ds;


    // Constructors

    /** default constructor */
    public Roi4D() {
    }
    
    /** constructor with id */
    public Roi4D(Integer id) {
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
     * 	        value="roi4d_seq"
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
     *            @hibernate.column name="roi5d"         
     *         
     */
    public Roi5D getRoi5d() {
        return this.roi5d;
    }
    
    public void setRoi5d(Roi5D roi5d) {
        this.roi5d = roi5d;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *            @hibernate.collection-key
     *             column="roi3d"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.Roi3D"
     *         
     */
    public Set getRoi3ds() {
        return this.roi3ds;
    }
    
    public void setRoi3ds(Set roi3ds) {
        this.roi3ds = roi3ds;
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
