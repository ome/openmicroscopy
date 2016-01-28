/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jhotdraw.draw.AttributeKey;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

import omero.gateway.model.ShapeData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ROIShape
{

    /** The server side object associated to this node.*/
    private ShapeData data;

	/** The id of the ROIShape. */
	private long id;
	
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
	private Map<AnnotationKey, Object> annotations = 
		new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private Set<AnnotationKey> forbiddenAnnotations;
    
    /**
     * Copies the attributes from one figure to another.
     * 
     * @param src The figure to copy from.
     * @param dst The figure to copy to.
     */
	private void copyAttributes(ROIFigure src, ROIFigure dst)
	{
		Map<AttributeKey, Object> map = src.getAttributes();
		Entry entry;
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			dst.setAttribute((AttributeKey) entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Creates the ROIShape with parent ROI on plane coordinate and 
	 * figure aggregated.
	 * 
	 * @param parent see above.
	 * @param coord see above.
	 * @param shape see above.
	 */
	public ROIShape(ROI parent, Coord3D coord, ROIShape shape)
	{
		if (shape == null)
			throw new IllegalArgumentException("No Shape specified.");
		if (parent == null)
			throw new IllegalArgumentException("No ROI specified.");
		ROIFigure src = (ROIFigure) shape.getFigure();
		if (src == null)
			throw new IllegalArgumentException("No Figure associated to shape.");
		this.parent = parent;
		this.coord = coord;
		boundingBox = (Rectangle2D) shape.getBoundingBox().clone();
		figure = (ROIFigure) src.clone();
		figure.setROIShape(this);
		figure.setROI(parent);
		copyAttributes(src, figure);
	}
	
	/**
	 * Creates the ROIShape with parent ROI on plane coordinates and 
	 * figure aggregated. The ROIShape bounds are set by boundingBox.
	 * 
	 * @param parent see above.
	 * @param coord see above.
	 * @param figure see above.
	 * @param boundingBox see above.
	 */
	public ROIShape(ROI parent, Coord3D coord, ROIFigure figure, 
			Rectangle2D boundingBox)
	{
		if (figure == null)
			throw new IllegalArgumentException("No Figure specified.");
		if (parent == null)
			throw new IllegalArgumentException("No ROI specified.");
		this.parent = parent;
		this.coord = coord;
		this.figure = figure;//(ROIFigure) figure.clone(); //shouldn't be clone.
		this.figure.setROIShape(this);
		this.figure.setROI(parent);
		this.boundingBox = boundingBox;
		copyAttributes(figure, this.figure);
	}
	
	/**
	 * This id will only be used by server objects.
	 * @return See above.
	 */
	public long getROIShapeID() { return id; }

	/**
     * This id will only be used by server objects.
     * @param id The id of the 
     */
    public void setROIShapeID(long id) { this.id = id; }

	/**
	 * Returns the server side shape associated to this object or
	 * <code>null</code>.
	 *
	 * @return See above.
	 */
	public ShapeData getData() { return data; }

	/**
	 * Sets the server side shape associated to this object or
	 * <code>null</code>.
	 */
    public void setData(ShapeData data) { this.data = data; }

	/**
	 * Get the id of the ROI the ROIShape belongs to.
	 * @return see above.
	 */
	public long getID() { return parent.getID(); }
	
	/**
	 * The plane on which the ROIShape belongs.
	 * @return see above.
	 */
	public Coord3D getCoord3D() { return coord; }
	
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
	public Rectangle2D getBoundingBox() { return boundingBox; }
	
	/**
	 * Get the ROIFigure which represents the ROIShape.
	 * @return see above.
	 */
	public ROIFigure getFigure() { return figure; }
	
	/** 
	 * Get the ROI containing this ROIShape.
	 * @return see above.
	 */
	public ROI getROI() { return parent; }
       
	/**
	 * Set the annotation of the ROIShape with key to value.
	 * @param key see above.
	 * @param newValue see above.
	 */
    public void setAnnotation(AnnotationKey key, Object newValue)
    {
        if (forbiddenAnnotations == null
                || ! forbiddenAnnotations.contains(key)) {
            
            Object oldValue = annotations.get(key);
            if (! annotations.containsKey(key)
            || oldValue != newValue
                    || oldValue != null && newValue != null && ! 
                    oldValue.equals(newValue)) {
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
        return forbiddenAnnotations == null || 
        !forbiddenAnnotations.contains(key);
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
        	annotations.remove(key);
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

    /**
     * Copies the shape.
     *
     * @param plane The plane to copy.
     * @return See above.
     */
    public ROIShape copy(Coord3D plane)
    {
        if (plane == null) plane = this.coord;
        return new ROIShape(this.parent, plane, this.figure, this.boundingBox);
    }
}
