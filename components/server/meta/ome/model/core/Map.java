package ome.model.core;

import ome.util.BaseModelUtils;


import java.util.*;




/**
 *        @hibernate.class
 *         table="map"
 *     
 */
public class
Map 
implements java.io.Serializable ,
ome.api.OMEModel {

    // Fields    

     private Integer id;
     private Pixel pixel;
     private Roi5D roi5d;
     private RoiSet roiSet;


    // Constructors

    /** default constructor */
    public Map() {
    }
    
    /** constructor with id */
    public Map(Integer id) {
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
     * 	        value="map_seq"
     *         
     */
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="set"         
     *         
     */
    public RoiSet getRoiSet() {
        return this.roiSet;
    }
    
    public void setRoiSet(RoiSet roiSet) {
        this.roiSet = roiSet;
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
