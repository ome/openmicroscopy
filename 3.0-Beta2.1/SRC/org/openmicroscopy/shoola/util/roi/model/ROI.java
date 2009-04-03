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
import java.util.Map;
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
	final 	static 	int						DEFAULTMAPSIZE = 101;
	private long							id;
	
	TreeMap<Coord3D, ROIShape> 				roiShapes;
	AttachmentMap 							attachments;
	private HashMap<AnnotationKey, Object> 	annotations 
										= new HashMap<AnnotationKey,Object>();
	
	/**
     * Forbidden annotations can't be set by the setAnnotation() operation.
     * They can only be changed by basicSetAnnotations().
     */
    private HashSet<AnnotationKey> forbiddenAnnotations;
    
	public ROI(long id)	
	{
		init(id);
	}
	
	public void addAttachment(AttachmentKey key, Attachment attachment)
	{
		attachments.addAttachment(key, attachment);
	}
	
	public Attachment getAttachment(AttachmentKey key)
	{
		return attachments.getAttachment(key);
	}
	
	public AttachmentMap getAttachmentMap()
	{
		return attachments;
	}
	
	public ROI(long id, Coord3D coord, ROIShape shape)
	{
		init(id);
		roiShapes.put(coord, shape);
	}
	
	private void init(long id)
	{
		this.id = id;
		roiShapes = new TreeMap<Coord3D, ROIShape>(new Coord3D());
	}
	
	public long getID()
	{
		return id;
	}
	
	public boolean containsKey(Coord3D coord)
	{
		return roiShapes.containsKey(coord);
	}
	
	public boolean containsKey(Coord3D start, Coord3D end)
	{
		//for(int c = start.c; c < end.c ; c++)
			for(int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
				for(int z = start.getZSection(); z < end.getZSection() ; z++)
					if(!roiShapes.containsKey(new Coord3D(z,t)))
						return false;
		return true;
	}
	
	public TreeMap<Coord3D, ROIShape> getShapes()
	{
		return roiShapes;
	}
	
	public ROIShape getShape(Coord3D coord) throws NoSuchROIException
	{
		try 
		{
			return roiShapes.get(coord);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
	}
	
	public ROIFigure getFigure(Coord3D coord) throws NoSuchROIException
	{
		return getShape(coord).getFigure();
	}
	
	public void addShape(ROIShape shape) 
												throws ROICreationException
	{
		if(roiShapes.containsKey(shape.getCoord3D()))
			throw new ROICreationException();
		roiShapes.put(shape.getCoord3D(), shape);
	}
	
	public void deleteShape(Coord3D coord) throws NoSuchROIException
	{
		try
		{
			roiShapes.remove(coord);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
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
    
    public boolean isAnnotationEnabled(AnnotationKey key) 
    {
        return forbiddenAnnotations == null || ! forbiddenAnnotations.contains(key);
    }
    
    public void basicSetAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
        {
            basicSetAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
    public void setAnnotations(Map<AnnotationKey, Object> map) 
    {
        for (Map.Entry<AnnotationKey, Object> entry : map.entrySet()) 
        {
            setAnnotation(entry.getKey(), entry.getValue());
        }
    }
    
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
     * Applies all annotation of this ROI to that ROI.
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


