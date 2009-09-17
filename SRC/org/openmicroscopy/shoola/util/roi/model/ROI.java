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

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
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
public class ROI
{
	/** Default size of the ROI Map. */
	final 	static 	int						DEFAULTMAPSIZE = 101;
	
	/** The id of the ROI. */
	private long							id;
	
	/** The TreeMap containing the ROI shapes of the ROI. */ 
	TreeMap<Coord3D, ROIShape> 				roiShapes;
	
	/** The Attachments in the ROI. */
	AttachmentMap 							attachments;
	
	/** Annotations in the ROI. */
	private Map<AnnotationKey, Object> 	annotations 
										= new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private Set<AnnotationKey> forbiddenAnnotations;
    
    /**
     * Construct the ROI with id.
     * @param id see above.
     */
	public ROI(long id)	
	{
		init(id);
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
	 * Construct the ROI with id on coord and initial ROIShape shape.
	 * @param id the ID of the ROI.
	 * @param coord the coord of the ROIShape being constructed with the ROI. 
	 * @param shape the ROIShape being constructed with the ROI. 
	 */
	public ROI(long id, Coord3D coord, ROIShape shape)
	{
		init(id);
		roiShapes.put(coord, shape);
	}
	
	/** 
	 * initialise the ROI with id and construct the TreeMap to contain 
	 * the ROIShapes of the ROI and there mapping the coord3D they exist on.
	 * @param id id of the ROI.
	 */
	private void init(long id)
	{
		this.id = id;
		roiShapes = new TreeMap<Coord3D, ROIShape>(new Coord3D());
	}
	
	/**
	 * Get the ROI id.
	 * @return see above.
	 */
	public long getID()
	{
		return id;
	}
	
	/** Get the range of the T sections this ROI spans. 
	 * @return string. see above.
	 */
	public String getTRange()
	{
		Coord3D low = roiShapes.firstKey();
		Coord3D high = roiShapes.lastKey();
		return "["+(low.getTimePoint()+1)+","+(high.getTimePoint()+1)+"]";
	}
	
	/** Get the range of the timepoints this ROI spans. 
	 * @return string. see above.
	 */
	public String getZRange()
	{
		Coord3D low = roiShapes.firstKey();
		Coord3D high = roiShapes.lastKey();
		return "["+(low.getZSection()+1)+","+(high.getZSection()+1)+"]";
	}
	
	/** Get the range of the shapes this ROI contains. 
	 * @return string. see above.
	 */
	public String getShapeTypes()
	{
		String shapes = "";
		HashMap<String,Integer> shapeTypes = new HashMap<String, Integer>();
		Iterator<ROIShape> shapeIterator = roiShapes.values().iterator();
		ROIShape shape;
		String type;
		while(shapeIterator.hasNext())
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
		
		while (typeIterator.hasNext())
		{
			type = typeIterator.next();
			if(!first)
			{
				shapes = shapes + ",";
				first = false;
			}
			shapes = shapes+type;
		}
		shapes = shapes + "";
		return shapes;
	}

	/**
	 * Is this ROI's roiShapes visible.
	 * @return see above.
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
	 * Return true if the ROI contains a ROIShape on coord.
	 * @param coord see above.
	 * @return see above.
	 */
	public boolean containsKey(Coord3D coord)
	{
		return roiShapes.containsKey(coord);
	}
	
	/** 
	 * Return true if the ROI contains a ROIShape on [start, end].
	 * @param start see above.
	 * @param end see above.
	 * @return see above.
	 */
	public boolean containsKey(Coord3D start, Coord3D end)
	{
		//for(int c = start.c; c < end.c ; c++)
		for (int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
			for (int z = start.getZSection(); z < end.getZSection() ; z++)
				if (!roiShapes.containsKey(new Coord3D(z, t)))
					return false;
		return true;
	}
	
	/**
	 * Get the TreeMap containing the ROIShapes.
	 * @return see above.
	 */
	public TreeMap<Coord3D, ROIShape> getShapes()
	{
		return roiShapes;
	}
	
	/**
	 * Get the ROIShape on plane coord.
	 * @param coord see above.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROI has no ROIShape on 
	 * coord.
	 */
	public ROIShape getShape(Coord3D coord) throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
		return roiShapes.get(coord);
	}
	
	/**
	 * Get the figure on plane coord.
	 * @param coord see above.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROI has no ROIShape on 
	 * coord.
	 */
	public ROIFigure getFigure(Coord3D coord) throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
		return getShape(coord).getFigure();
	}
	
	/**
	 * Add ROIShape shape to the ROI. If the ROI already has a shape at coord
	 * an exception will be thrown.
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
	 * Delete the ROIShape on coord from the ROI.
	 * @param coord see above.
	 * @throws NoSuchROIException Throw exception if the ROI does not contain
	 * an ROIShape on plane coord.
	 */
	public void deleteShape(Coord3D coord) throws NoSuchROIException
	{
		if (!roiShapes.containsKey(coord))
			throw new NoSuchROIException("ROI " + id + " does not contain " +
					"ROIShape on Coord " + coord.toString());
			roiShapes.remove(coord);
	}

	/**
	 * Set the value off the annotation with key.
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
     * Set an annotation to be enabled if b true.
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
     * Return true if the annotation with key is allowed. 
     * 
     * @param key see above.
     * @return see above.
     */
    public boolean isAnnotationEnabled(AnnotationKey key) 
    {
        return forbiddenAnnotations == null || ! forbiddenAnnotations.contains(key);
    }
    
    /** 
     * Set the map with the elements of the map. 
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
     * set the annotaiton map of the ROI to map
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
     * get the anotaiton map for the ROI.
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
     * Gets an annotation from the ROI.
     * @return annotation.
     */
    public Object getAnnotation(AnnotationKey key) 
    {
        return hasAnnotation(key) ? annotations.get(key) : key.getDefaultValue();
    }
    
    /** get the annotation key for element string. 
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
     * Remove annotation with key 
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
     * Return true if the ROI has the an annotation with key. 
     * @param key the key of the annotation.
     * @return see above.
     */
    public boolean hasAnnotation(AnnotationKey key) 
    {
        return annotations.containsKey(key);
    }

}


