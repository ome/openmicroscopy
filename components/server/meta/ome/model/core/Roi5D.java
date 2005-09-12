package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="roi5d"
 *     
 */
public class
Roi5D 
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
     private Pixel pixel;
     private Set maps;
     private Set roi4ds;


    // Constructors

    /** default constructor */
    public Roi5D() {
    }
    
    /** constructor with id */
    public Roi5D(Integer id) {
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
     * 	        value="roi5d_seq"
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
     *            @hibernate.column name="pixel"         
     *         
     */
    public Pixel getPixel() {
        return this.pixel;
    }
    
    public void setPixel(Pixel pixel) {
        this.pixel = pixel;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *            @hibernate.collection-key
     *             column="roi5d"
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

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *            @hibernate.collection-key
     *             column="roi5d"
     *            @hibernate.collection-one-to-many
     *             class="ome.model.core.Roi4D"
     *         
     */
    public Set getRoi4ds() {
        return this.roi4ds;
    }
    
    public void setRoi4ds(Set roi4ds) {
        this.roi4ds = roi4ds;
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
