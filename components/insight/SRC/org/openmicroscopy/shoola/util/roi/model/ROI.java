/*
 * org.openmicroscopy.shoola.util.roi.model.ROI 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.attachment.Attachment;
import org.openmicroscopy.shoola.util.roi.model.attachment.AttachmentKey;
import org.openmicroscopy.shoola.util.roi.model.attachment.AttachmentMap;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * The ROI object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ROI
{
	
	/** Default size of the ROI Map. */
	final 	static 	int						DEFAULTMAPSIZE = 101;
	
	/** The id of the ROI. */
	private long							id;
	
	/** Flag indicating the figure can/cannot be deleted.*/
	private boolean deletable;
	
	/** Flag indicating the figure can/cannot be annotated.*/
	private boolean annotatable;
	
	/** Flag indicating the figure can/cannot be edited.*/
	private boolean editable;
	
	/** Is the object a server-side or client-side object. */
	private boolean clientSide;
	
	/** The TreeMap containing the ROI shapes of the ROI. */ 
	private TreeMap<Coord3D, ROIShape> 				roiShapes;
	
	/** The Attachments in the ROI. */
	private  AttachmentMap 							attachments;
	
	/** Annotations in the ROI. */
	private Map<AnnotationKey, Object> 	annotations 
										= new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private Set<AnnotationKey> forbiddenAnnotations;

	/** The identifier of the owner. */
	private long ownerID;

	/** 
	 * Initializes the ROI with id and construct the TreeMap to contain 
	 * the ROIShapes of the ROI and there mapping the coord3D they exist on.
	 * 
	 * @param id id of the ROI.
	 * @param shape The ROIShape being constructed with the ROI.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	private void init(long id, boolean clientSide,
			boolean editable, boolean deletable, boolean annotatable)
	{
		ownerID = -1;
		this.id = id;
		this.clientSide = clientSide;
		this.deletable = deletable;
   		this.annotatable = annotatable;
   		this.editable = editable;
		roiShapes = new TreeMap<Coord3D, ROIShape>(new Coord3D());
		attachments = new AttachmentMap();
	}
	
    /**
     * Construct the ROI with id.
     * @param id see above.
     * @param clientSide
     * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
     */
	public ROI(long id, boolean clientSide, boolean editable, boolean deletable,
			boolean annotatable)
	{
		init(id, clientSide, editable, deletable, annotatable);
	}
	
	/**
	 * Constructs the ROI with id on coordinate and initial ROIShape shape.
	 * 
	 * @param id The ID of the ROI.
	 * @param coord The coordinate of the ROIShape being constructed with the 
	 * ROI. 
	 * @param shape The ROIShape being constructed with the ROI.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public ROI(long id, boolean clientSide, Coord3D coord, ROIShape shape,
			boolean editable, boolean deletable, boolean annotatable)
	{
		init(id, clientSide, editable, deletable, annotatable);
		roiShapes.put(coord, shape);
	}
	
	/**
	 * Add an attachment to the ROI. 
	 * @param key the key of the attachment.
	 * @param attachment the value of the attachment. 
	 */
	public void addAttachment(AttachmentKey key, Attachment attachment)
	{
		attachments.addAttachment(key, attachment);
	}
	
	/** 
	 * Get the attachment on the ROi with key 
	 * @param key see above.
	 * @return see above.
	 */
	public Attachment getAttachment(AttachmentKey key)
	{
		return attachments.getAttachment(key);
	}
	
	/**
	 * Get the map of all attachments. 
	 * @return see above.
	 */
	public AttachmentMap getAttachmentMap()
	{
		return attachments;
	}
	

	
	/**
	 * Gets the ROI id.
	 * 
	 * @return see above.
	 */
	public long getID() { return id; }
	
	/**
	 * Returns <code>true</code> if the object a clientSide object.
	 * 
	 * @return See above.
	 */
	public boolean isClientSide() { return clientSide; }
	
	/** 
	 * Gets the range of the T sections this ROI spans. 
	 * 
	 * @return string. see above.
	 */
	public String getTRange()
	{
		
		Coord3D low = roiShapes.firstKey();
		Coord3D high = roiShapes.lastKey();
		int s = low.getTimePoint();
		int e = high.getTimePoint();
		if (s < 0 || e < 0) return "";
		s++;
		e++;
		if (s == e) return ""+s;
		return s+"-"+e;
		
	}
	
	/** 
	 * Gets the range of the z-sections this ROI spans. 
	 * 
	 * @return string. see above.
	 */
	public String getZRange()
	{
		Coord3D low = roiShapes.firstKey();
		Coord3D high = roiShapes.lastKey();
		int s = low.getZSection();
		int e = high.getZSection();
		if (s < 0 || e < 0) return "";
		s++;
		e++;
		if (s == e) return ""+s;
		return s+"-"+e;
	}
	
	/** 
	 * Gets the range of the shapes this ROI contains.
	 *  
	 * @return string. see above.
	 */
	public String getShapeTypes()
	{
		HashMap<String,Integer> shapeTypes = new HashMap<String, Integer>();
		Iterator<ROIShape> shapeIterator = roiShapes.values().iterator();
		ROIShape shape;
		String type;
		while (shapeIterator.hasNext())
		{
			shape = shapeIterator.next();
			type = shape.getFigure().getType();
			if (shapeTypes.containsKey(type))
			{
				int value  = shapeTypes.get(type)+1;
				shapeTypes.put(type, value);
			}
			else
				shapeTypes.put(type, Integer.valueOf(1));
		}
		
		Iterator<String> typeIterator = shapeTypes.keySet().iterator();
		boolean first = true;
		StringBuffer buffer = new StringBuffer();
		while (typeIterator.hasNext())
		{
			type = typeIterator.next();
			if (!first)
			{
				buffer.append(",");
				first = false;
			}
			buffer.append(type);
		}
		return buffer.toString();
	}

	/**
	 * Returns <code>true</code> if this ROI's roiShapes visible, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isVisible()
	{
		boolean visible = false;
		Iterator<ROIShape> shapeIterator = roiShapes.values().iterator();
		ROIShape shape;
		while (shapeIterator.hasNext())
		{
			shape = shapeIterator.next();
			visible = visible | shape.getFigure().isVisible();
		}
		return visible;
	}
	
	/** 
	 * Return <code>true</code> if the ROI contains a ROIShape on coordinates,
	 * <code>false</code> otherwise.
	 * 
	 * @param coord see above.
	 * @return see above.
	 */
	public boolean containsShape(Coord3D coord)
	{
		return roiShapes.containsKey(coord);
	}
	
	/** 
	 * Return <code>true</code> if the ROI contains a ROIShape on [start, end],
	 * <code>false</code> otherwise.
	 * 
	 * @param start see above.
	 * @param end see above.
	 * @return see above.
	 */
	public boolean containsShape(Coord3D start, Coord3D end)
	{
		//for(int c = start.c; c < end.c ; c++)
		for (int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
			for (int z = start.getZSection(); z < end.getZSection() ; z++)
				if (!roiShapes.containsKey(new Coord3D(z, t)))
					return false;
		return true;
	}
	
	/**
	 * Gets the TreeMap containing the ROIShapes.
	 * 
	 * @return see above.
	 */
	public TreeMap<Coord3D, ROIShape> getShapes() { return roiShapes; }
	
	/**
	 * Gets the ROIShape on plane coordinates.
	 * 
	 * @param coord see above.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROI has no ROIShape on 
	 * coordinates.
	 */
	public ROIShape getShape(Coord3D coord) 
		throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
		return roiShapes.get(coord);
	}
	
	/**
	 * Gets the figure on plane coordinates.
	 * 
	 * @param coord see above.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROI has no ROIShape on 
	 * coordinates.
	 */
	public ROIFigure getFigure(Coord3D coord) 
		throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
		return getShape(coord).getFigure();
	}
	
	/**
	 * Returns all the figures.
	 * 
	 * @return See above.
	 */
	public List<ROIFigure> getAllFigures()
	{
		List<ROIFigure> figures = new ArrayList<ROIFigure>();
		Collection<ROIShape> set = roiShapes.values();
		Iterator<ROIShape> i = set.iterator();
		while (i.hasNext()) {
			figures.add(i.next().getFigure());
		}
		return figures;
	}
	
	/**
	 * Adds ROIShape shape to the ROI. If the ROI already has a shape at 
	 * coordinates an exception will be thrown.
	 * @param shape see above. 
	 * @throws ROICreationException see above. 
	 */
	public void addShape(ROIShape shape) 
		throws ROICreationException
	{
		if (roiShapes.containsKey(shape.getCoord3D()))
			throw new ROICreationException();
		roiShapes.put(shape.getCoord3D(), shape);
	}

	/** 
	 * Deletes the ROIShape on coordinates from the ROI.
	 * 
	 * @param coord see above.
	 * @throws NoSuchROIException Throw exception if the ROI does not contain
	 * an ROIShape on plane coordinates.
	 */
	public void deleteShape(Coord3D coord) 
		throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
			roiShapes.remove(coord);
	}

	/**
	 * Sets the value off the annotation with key.
	 * 
	 * @param key see above.
	 * @param newValue see above.
	 */
    public void setAnnotation(AnnotationKey key, Object newValue)
    {
        if (forbiddenAnnotations == null
                || ! forbiddenAnnotations.contains(key)) {
            Object oldValue = annotations.get(key);
            if (!annotations.containsKey(key) || oldValue != newValue
                    || oldValue != null && newValue != null &&
                    ! oldValue.equals(newValue)) {
                basicSetAnnotation(key, newValue);
            }
        }
    }
    
    /**
     * Sets an annotation to be enabled if the passed value is 
     * <code>true</code>.
     * 
     * @param key see above.
     * @param b see above.
     */
    public void setAnnotationEnabled(AnnotationKey key, boolean b) 
    {
        if (forbiddenAnnotations == null) 
        {
        	forbiddenAnnotations = new HashSet<AnnotationKey>();
        }
        if (b) 
        {
        	forbiddenAnnotations.remove(key);
        } else 
        {
        	forbiddenAnnotations.add(key);
        }
    }
    
    /** 
     * Return <code>true</code> if the annotation with key is allowed,
     * <code>false</code> otherwise.
     * 
     * @param key see above.
     * @return see above.
     */
    public boolean isAnnotationEnabled(AnnotationKey key) 
    {
        return forbiddenAnnotations == null || ! 
        	forbiddenAnnotations.contains(key);
    }
    
    /** 
     * Sets the map with the elements of the map. 
     * 
     * @param map see above.
     */
    public void basicSetAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
        {
            basicSetAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Sets the annotation map of the ROI to map
     * @param map see above.
     */
    public void setAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
        {
            setAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Returns the annotation map for the ROI.
     * 
     * @return see above.
     */
    public Map<AnnotationKey, Object> getAnnotation() 
    {
        return new HashMap<AnnotationKey,Object>(annotations);
    }
    
    /**
     * Sets an annotation of the ROI.
     * AnnotationKey name and semantics are defined by the class implementing
     * the ROI interface.
     */
    public void basicSetAnnotation(AnnotationKey key, Object newValue) 
    {
        if (forbiddenAnnotations == null
                || ! forbiddenAnnotations.contains(key)) 
        {
        	annotations.put(key, newValue);
        }
    }
    
    /**
     * Returns <code>true</code> if the key has an annotation, 
     * <code>false</code> otherwise.
     * 
     * @param key The key to handle.
     * @return See above.
     */
    public boolean hasAnnotation(String key)
    {
    	Iterator<AnnotationKey> i = annotations.keySet().iterator();
    	AnnotationKey annotationKey;
    	while (i.hasNext())
    	{
    		annotationKey = i.next(); 
    		if (annotationKey.getKey().equals(key))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Gets an annotation from the ROI.
     * @return annotation.
     */
    public Object getAnnotation(AnnotationKey key) 
    {
        return hasAnnotation(key) ? annotations.get(key) : key.getDefaultValue();
    }
    
    /** 
     * Returns the annotation key for element string. 
     * 
     * @param name see above.
     * @return  see above.
     */
    protected AnnotationKey getAnnotationKey(String name) 
    {
        return AnnotationKeys.supportedAnnotationMap.get(name);
    }
    
    /**
     * Applies all annotation of this ROI to that ROI.
     * @param that the ROIShape to get annotation from. 
     */
    protected void applyAnnotationsTo(ROIShape that) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : annotations.entrySet()) 
        {
            that.setAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Removes annotation with key.
     *  
     * @param key see above.
     */
    public void removeAnnotation(AnnotationKey key) 
    {
        if (hasAnnotation(key)) 
        {
            //Object oldValue = getAnnotation(key);
            annotations.remove(key);
        }
    }
    
    /**
     * Return <code>true</code> if the ROI has the an annotation with key,
     * <code>false</code> otherwise.
     * 
     * @param key the key of the annotation.
     * @return see above.
     */
    public boolean hasAnnotation(AnnotationKey key) 
    {
        return annotations.containsKey(key);
    }

	/**
	 * Sets the identifier of the owner.
	 * 
	 * @param ownerID the identifier of the owner.
	 */
	public void setOwnerID(long ownerID) { this.ownerID = ownerID; }
	
	/**
	 * Returns the identifier of the owner.
	 * 
	 * @return See above.
	 */
	public long getOwnerID() { return ownerID; }
	
	/** 
	 * Indicates if the ROI can be annotated if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canAnnotate() { return annotatable; }

	/** 
	 * Indicates if the ROI can be deleted if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canDelete() { return deletable; }

	/** 
	 * Indicates if the ROI can be edited if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canEdit() { return editable; }
}
