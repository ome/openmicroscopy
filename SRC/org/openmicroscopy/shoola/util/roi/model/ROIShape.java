/*
 * org.openmicroscopy.shoola.util.roi.model.ROIShape 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.model;

//Java imports
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIShape 
{
	/** The ROI containing the ROIShape. */
	private ROI					parent;
	
	/** The plane on which the ROIShape resides. */
	private	Coord3D				coord;
	
	/** The bounding box of the ROIShape. */
	private Rectangle2D			boundingBox;
	
	/** The ROIFigure that represents the on screen presence of the ROIShape. */
	private ROIFigure			figure;
	
	/**
	 * Annotations are stored according to a key, object mapping, just like the 
	 * attribute objects of JHotDraw. 
	 */
	private Map<AnnotationKey, Object> annotations = new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private Set<AnnotationKey> forbiddenAnnotations;
    

	/**
	 * Create the ROIShape with parent ROI on plane coord and figure aggregated.
	 * @param parent see above.
	 * @param coord see above.
	 * @param shape see above.
	 */
	public ROIShape(ROI parent, Coord3D coord, ROIShape shape)
	{
		this.parent = parent;
		this.coord = coord;
		this.boundingBox = (Rectangle2D) shape.getBoundingBox().clone();
		this.figure = (ROIFigure) shape.getFigure().clone();
		this.figure.setROIShape(this);
		this.figure.setROI(parent);
	}
	
	/**
	 * Create the ROIShape with parent ROI on plane coord and figure aggregated.
	 * The ROIShape bounds are set by boundingBox.
	 * @param parent see above.
	 * @param coord see above.
	 * @param figure see above.
	 * @param boundingBox see above.
	 */
	public ROIShape(ROI parent, Coord3D coord, ROIFigure figure, Rectangle2D boundingBox)
	{
		this.parent = parent;
		this.coord = coord;
		this.figure = figure;
		this.figure.setROIShape(this);
		this.figure.setROI(parent);
		this.boundingBox = boundingBox;
	}
	
	/**
	 * Get the id of the ROI the ROIShape belongs to.
	 * @return see above.
	 */
	public long getID()
	{
		return parent.getID();
	}
	
	/**
	 * The plane on which the ROIShape belongs.
	 * @return see above.
	 */
	public Coord3D getCoord3D()
	{
		return coord;
	}
	
	/**
	 * Returns the z-section.
	 * 
	 * @return See above.
	 */
	public int getZ() { return coord.getZSection(); }
	
	/**
	 * Returns the timepoint.
	 * 
	 * @return See above.
	 */
	public int getT() { return coord.getTimePoint(); }
	
	/**
	 * Get the bounding box of the ROIShape.
	 * @return see above.
	 */
	public Rectangle2D getBoundingBox()
	{
		return boundingBox;
	}
	
	/**
	 * Get the ROIFigure which represents the ROIShape.
	 * @return see above.
	 */
	public ROIFigure getFigure()
	{
		return figure;
	}
	
	/** 
	 * Get the ROI containing this ROIShape.
	 * @return see above.
	 */
	public ROI getROI()
	{
		return parent;
	}
       
	/**
	 * Set the annotation of the ROIShape with key to value.
	 * @param key see above.
	 * @param newValue see above.
	 */
    public void setAnnotation(AnnotationKey key, Object newValue) {
        if (forbiddenAnnotations == null
                || ! forbiddenAnnotations.contains(key)) {
            
            Object oldValue = annotations.get(key);
            if (! annotations.containsKey(key)
            || oldValue != newValue
                    || oldValue != null && newValue != null && ! oldValue.equals(newValue)) {
                basicSetAnnotation(key, newValue);
            }
        }
    }
    
    /**
     * Set the ROIShape to allow annotation of type key if param b true.
     * @param key see above.
     * @param b see above.
     */
    public void setAnnotationEnabled(AnnotationKey key, boolean b) 
    {
        if (forbiddenAnnotations == null) 
        	forbiddenAnnotations = new HashSet<AnnotationKey>();
        if (b) 
        	forbiddenAnnotations.remove(key);
         else 
        	forbiddenAnnotations.add(key);
    }
    
    /**
     * Is the annotation with key allowed.
     * @param key see above.
     * @return see above.
     */
    public boolean isAnnotationEnabled(AnnotationKey key) 
    {
        return forbiddenAnnotations == null || ! forbiddenAnnotations.contains(key);
    }
    
    /**
     * Set the annotations in the ROIShape from the map.
     * @param map see above.
     */
    public void basicSetAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
            basicSetAnnotation(entry.getKey(), entry.getValue());
    }
    
    /**
     * Set the annoations of the ROIShape from the map provided.
     * @param map see above.
     */
    public void setAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
            setAnnotation(entry.getKey(), entry.getValue());
    }
    
    /**
     * Get all the annotations of the ROIShape.
     * @return see above.
     */
    public Map<AnnotationKey, Object> getAnnotation() 
    {
        return new HashMap<AnnotationKey,Object>(annotations);
    }
    
    /**
     * Sets an annotation of the ROIShape.
     * AnnotationKey name and semantics are defined by the class implementing
     * the ROIShape interface.
     * @param key see above.
     * @param newValue see above.
     */
    public void basicSetAnnotation(AnnotationKey key, Object newValue) 
    {
        if (forbiddenAnnotations == null 
        	|| ! forbiddenAnnotations.contains(key)) 
        	annotations.put(key, newValue);
    }
    
    /**
     * Gets an annotation from the ROIShape.
     * @param key see above.
     * @return see above.
     */
    public Object getAnnotation(AnnotationKey key) 
    {
        return hasAnnotation(key) ? annotations.get(key) : key.getDefaultValue();
    }
    
    /**
     * Get the annotation key with name.
     * @param name see above.
     * @return see above.
     */
    protected AnnotationKey getAnnotationKey(String name) 
    {
        return AnnotationKeys.supportedAnnotationMap.get(name);
    }
    
    /**
     * Applies all annotation of this ROIShape to that ROIShape.
     * @param that the ROIShape to take values from.
     */
    protected void applyAnnotationsTo(ROIShape that) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : annotations.entrySet()) 
        {
            that.setAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Remove the annotation key from map.
     * @param key see above.
     */
    public void removeAnnotation(AnnotationKey key) 
    {
        if (hasAnnotation(key)) 
        {
             annotations.remove(key);
        }
    }
    
    /**
     * Has the ROIShape got the annotation key.
     * @param key see above.
     * @return see above.
     */
    public boolean hasAnnotation(AnnotationKey key) 
    {
        return annotations.containsKey(key);
    }

}


