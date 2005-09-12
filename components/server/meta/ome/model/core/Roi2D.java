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
