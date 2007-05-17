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
	private ROI					parent;
	private	Coord3D				coord;
	private Rectangle2D			boundingBox;
	
	private ROIFigure			figure;
	
	/**
	 * Annotations are stored according to a key, object mapping, just like the 
	 * attribute objects of JHotDraw. 
	 */
	private HashMap<AnnotationKey, Object> annotations = new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private HashSet<AnnotationKey> forbiddenAnnotations;
    
	public ROIShape(ROI parent, Coord3D coord, ROIShape shape)
	{
		this.parent = parent;
		this.coord = coord;
		this.boundingBox = (Rectangle2D) shape.getBoundingBox().clone();
		this.figure = (ROIFigure) shape.getFigure().clone();
	}
	
	public ROIShape(ROI parent, Coord3D coord, ROIFigure figure, Rectangle2D boundingBox)
	{
		this.parent = parent;
		this.coord = coord;
		this.figure = figure;
		this.figure.setROIShape(this);
		this.figure.setROI(parent);
		this.boundingBox = boundingBox;
	}
	
	public long getID()
	{
		return parent.getID();
	}
	
	public Coord3D getCoord3D()
	{
		return coord;
	}
	
	public Rectangle2D getBoundingBox()
	{
		return boundingBox;
	}
	
	public ROIFigure getFigure()
	{
		return figure;
	}
	
	public ROI getROI()
	{
		return parent;
	}
       
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
    
    public void setAnnotationEnabled(AnnotationKey key, boolean b) 
    {
        if (forbiddenAnnotations == null) 
        	forbiddenAnnotations = new HashSet<AnnotationKey>();
        if (b) 
        	forbiddenAnnotations.remove(key);
         else 
        	forbiddenAnnotations.add(key);
    }
    
    public boolean isAnnotationEnabled(AnnotationKey key) 
    {
        return forbiddenAnnotations == null || ! forbiddenAnnotations.contains(key);
    }
    
    public void basicSetAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
            basicSetAnnotation(entry.getKey(), entry.getValue());
    }
    
    public void setAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
            setAnnotation(entry.getKey(), entry.getValue());
    }
    
    public Map<AnnotationKey, Object> getAnnotation() 
    {
        return new HashMap<AnnotationKey,Object>(annotations);
    }
    
    /**
     * Sets an annotation of the ROIShape.
     * AnnotationKey name and semantics are defined by the class implementing
     * the ROIShape interface.
     */
    public void basicSetAnnotation(AnnotationKey key, Object newValue) 
    {
        if (forbiddenAnnotations == null 
        	|| ! forbiddenAnnotations.contains(key)) 
        	annotations.put(key, newValue);
    }
    
    /**
     * Gets an annotation from the ROIShape.
     */
    public Object getAnnotation(AnnotationKey key) 
    {
        return hasAnnotation(key) ? annotations.get(key) : key.getDefaultValue();
    }
    
    protected AnnotationKey getAnnotationKey(String name) 
    {
        return AnnotationKeys.supportedAnnotationMap.get(name);
    }
    
    /**
     * Applies all annotation of this ROIShape to that ROIShape.
     */
    protected void applyAnnotationsTo(ROIShape that) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : annotations.entrySet()) 
        {
            that.setAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    public void removeAnnotation(AnnotationKey key) 
    {
        if (hasAnnotation(key)) 
        {
            Object oldValue = getAnnotation(key);
            annotations.remove(key);
        }
    }
    
    public boolean hasAnnotation(AnnotationKey key) 
    {
        return annotations.containsKey(key);
    }

}


