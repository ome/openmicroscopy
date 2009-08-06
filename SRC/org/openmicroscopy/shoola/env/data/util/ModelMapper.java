/*
 * org.openmicroscopy.shoola.env.data.util.ModelMapper
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Iterator;
import java.util.List;
//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.UriAnnotation;
import omero.model.UriAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Helper class to map {@link DataObject}s into their corresponding
 * {@link IObject}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ModelMapper
{
   
    /**
     * Unlinks the collections linked to the specified {@link IObject}.
     * 
     * @param object The object.
     */
    public static void unloadCollections(IObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("The object mustn't be null.");
        object.unloadCollections();
        /*
        if (object instanceof Project) {
        	ProjectI p = (ProjectI) object;
        	p.toggleCollectionsLoaded(false);
        } else if (object instanceof Dataset) {
        	DatasetI p = (DatasetI) object;
        	p.toggleCollectionsLoaded(false);
        } else if (object instanceof LongAnnotation) {
        	LongAnnotationI p = (LongAnnotationI) object;
        	p.toggleCollectionsLoaded(false);
        } else if (object instanceof TagAnnotation) {
        	TagAnnotationI p = (TagAnnotationI) object;
        	p.toggleCollectionsLoaded(false);
        } else if (object instanceof UrlAnnotation) {
        	UrlAnnotationI p = (UrlAnnotationI) object;
        	p.toggleCollectionsLoaded(false);
        } else if (object instanceof TextAnnotation) {
        	TextAnnotationI p = (TextAnnotationI) object;
        	p.toggleCollectionsLoaded(false);
        } 
        */
    }

    /**
     * Unlinks the specified child from its parent and returns the link to 
     * remove.
     * 
     * @param child     The child to unlink.
     * @param parent    The child's parent.
     * @return See above
     */
    public static IObject unlinkChildFromParent(IObject child, IObject parent)
    {
        if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Image) child).copyDatasetLinks();
            Iterator i = links.iterator();
            DatasetImageLink link = null;
            long parentID = parent.getId().getValue();
            while (i.hasNext()) {
                link = (DatasetImageLink) i.next();
                if (link.getParent().getId().getValue() == parentID) 
                	break;  
            }
            return link;
        } else if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Project) parent).copyDatasetLinks();
            Iterator i = links.iterator();
            ProjectDatasetLink link = null;
            long childID = child.getId().getValue();
            while (i.hasNext()) {
                link = (ProjectDatasetLink) i.next();
                if (link.getChild().getId().getValue() == childID) {
                    return link;  
                }
            }
            //return link;
        } 
        throw new IllegalArgumentException("Parent not supported.");
    }

    /**
     * Links the  {@link IObject child} to its {@link IObject parent}.
     * 
     * @param child     The child. 
     * @param parent    The parent.
     * @return The link.
     */
    public static IObject linkParentToChild(IObject child, IObject parent)
    {
        if (parent == null) return null;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project unloadedProject = new ProjectI(parent.getId().getValue(), 
            		false);
            Dataset unloadedDataset = new DatasetI(child.getId().getValue(), 
            		false);
            ProjectDatasetLink l = new ProjectDatasetLinkI();
            l.link(unloadedProject, unloadedDataset);
            return l;
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset unloadedDataset = new DatasetI(parent.getId().getValue(), 
            		false);
            Image unloadedImage = new ImageI(child.getId().getValue(), false);
            
            DatasetImageLink l = new DatasetImageLinkI();
            l.link(unloadedDataset, unloadedImage);
            return l;
        } else if (parent instanceof Screen) {
            if (!(child instanceof Plate))
                throw new IllegalArgumentException("Child not valid.");
            Screen unloadedScreen = new ScreenI(parent.getId().getValue(), 
            		false);
            Plate unloadedPlate = new PlateI(child.getId().getValue(), false);
            
            ScreenPlateLink l = new ScreenPlateLinkI();
            l.link(unloadedScreen, unloadedPlate);
            return l;
        } else if (parent instanceof TagAnnotation) {
        	if (!(child instanceof TagAnnotation))
                throw new IllegalArgumentException("Child not valid.");
        	RString ns = ((TagAnnotation) parent).getNs();
        	if (ns == null || !ns.getValue().equals(
        			TagAnnotationData.INSIGHT_TAGSET_NS))
        		return null;
        		//throw new IllegalArgumentException("Parent not valid.");
        	return linkAnnotation(parent, (TagAnnotation) child);
        }
        return null;
    }
    
    /**
     * Links the newly created {@link IObject child} to its
     * {@link IObject parent}. This method should only be invoked to add a 
     * newly created child.
     * 
     * @param child     The newly created child. 
     * @param parent    The parent of the newly created child.
     */
    public static void linkParentToNewChild(IObject child, IObject parent)
    {
        if (parent == null) return;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        List l;
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project p = (Project) parent;
            Dataset d = (Dataset) child;
            
            l = d.copyProjectLinks();
            if (l == null) return;
            ProjectDatasetLink link;
            Iterator it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = (ProjectDatasetLink) it.next();
                if (id == link.getParent().getId().getValue())
                	p.addProjectDatasetLink(link);
            }
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset p = (Dataset) parent;
            Image d = (Image) child;
            l = d.copyDatasetLinks();
            if (l == null) return;
            DatasetImageLink link;
            Iterator it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = (DatasetImageLink) it.next();
                if (id == link.getParent().getId().getValue())
                	p.addDatasetImageLink(link);
            }
        } else
            throw new IllegalArgumentException("DataObject not supported.");
    }
    
    /**
     * Converts the specified <code>DataObject</code> into its corresponding 
     * <code>IObject</code>.
     * 
     * @param child     The child to create.
     * @param parent    The child's parent.
     * @return The {@link IObject} to create.
     */
    public static IObject createIObject(DataObject child, DataObject parent)
    {
        if (child instanceof ProjectData) {
            ProjectData data = (ProjectData) child;
            Project model = new ProjectI();
            model.setName(omero.rtypes.rstring(data.getName()));
            model.setDescription(omero.rtypes.rstring(data.getDescription()));
            return model;
        } else if (child instanceof DatasetData) {
            DatasetData data = (DatasetData) child;
            Dataset model = new DatasetI();
            model.setName(omero.rtypes.rstring(data.getName()));
            model.setDescription(omero.rtypes.rstring(data.getDescription()));
            if (parent != null)
            	model.linkProject(
            			new ProjectI(Long.valueOf(parent.getId()), false));
            return model;
        } else if (child instanceof ImageData) {
            if (!(parent instanceof DatasetData))
                throw new IllegalArgumentException("Parent not valid.");
            ImageData data = (ImageData) child;
            Image model = new ImageI();
            model.setName(omero.rtypes.rstring(data.getName()));
            model.setDescription(omero.rtypes.rstring(data.getDescription()));
            model.linkDataset(new DatasetI(Long.valueOf(parent.getId()), 
                    false));
                
            return model; 
        } else if (child instanceof ScreenData) {
        	ScreenData data = (ScreenData) child;
        	Screen model = new ScreenI();
        	model.setName(omero.rtypes.rstring(data.getName()));
        	model.setDescription(omero.rtypes.rstring(data.getDescription()));
            return model;
        } else if (child instanceof TagAnnotationData) {
        	return createAnnotation((TagAnnotationData) child);
        }
        throw new IllegalArgumentException("Child and parent are not " +
        		"compatible.");
    }
    
    /**
     * Unlinks the specified child and the parent and returns the 
     * updated child <code>IObject</code>.
     * 
     * @param child     The child to remove.
     * @param parent    The parent of the child.
     * @return See above. 
     */
    public static IObject removeIObject(IObject child, IObject parent)
    {
        if ((child instanceof Dataset) && (parent instanceof Project)) {
            Project mParent = (Project) parent;
            List s = mParent.copyDatasetLinks();
            Iterator i = s.iterator();
            while (i.hasNext()) { 
                mParent.removeProjectDatasetLink((ProjectDatasetLink) i.next());
            }
            return mParent;
        } 
        throw new IllegalArgumentException("DataObject not supported.");
    }
   
    /**
     * Creates a new annotation <code>IObject</code>.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          Can either be a <code>DatasetData</code>
     *                          or a <code>ImageData</code>. Mustn't be
     *                          <code>null</code>.
     * @param data              The annotation to create.
     * @return See above.
     */
    public static IObject createAnnotationAndLink(IObject annotatedObject,
                                    AnnotationData data)
    {
    	Annotation annotation = createAnnotation(data);
    	if (annotation == null) return null;
    	return linkAnnotation(annotatedObject, annotation);
    }
    
    /**
     * Creates a new annotation <code>IObject</code>.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          Can either be a <code>DatasetData</code>
     *                          or a <code>ImageData</code>. Mustn't be
     *                          <code>null</code>.
     * @param data              The annotation to create.
     * @return See above.
     */
    public static Annotation createAnnotation(AnnotationData data)
    {
    	Annotation annotation = null;
    	if (data instanceof TextualAnnotationData) {
    		annotation = new CommentAnnotationI();
    		((CommentAnnotation) annotation).setTextValue(omero.rtypes.rstring(
    										data.getContentAsString()));
    	} else if (data instanceof RatingAnnotationData) {
    		int rate = ((RatingAnnotationData) data).getRating();
			if (rate == RatingAnnotationData.LEVEL_ZERO) return null;
    		annotation = new LongAnnotationI();
    		annotation.setNs(omero.rtypes.rstring(
    				RatingAnnotationData.INSIGHT_RATING_NS));
    		((LongAnnotation) annotation).setLongValue(omero.rtypes.rlong(
    										(Long) data.getContent()));
    	} else if (data instanceof URLAnnotationData) {
    		annotation = new UriAnnotationI();
    		try {
    			((UriAnnotation) annotation).setTextValue(
    					omero.rtypes.rstring(data.getContentAsString()));
			} catch (Exception e) { //Need to propagate that.
				return null;
			}
    		
    	} else if (data instanceof TagAnnotationData) {
    		annotation = new TagAnnotationI();
    		((TagAnnotation) annotation).setTextValue(
    				omero.rtypes.rstring(data.getContentAsString()));
    		annotation.setDescription(omero.rtypes.rstring(
    				((TagAnnotationData) data).getTagDescription()));
    		String ns = data.getNameSpace();
    		if (ns != null && ns.length() > 0) {
    			annotation.setNs(omero.rtypes.rstring(ns));
    		}
    	} else if (data instanceof BooleanAnnotationData) {
    		annotation = new BooleanAnnotationI();
    		annotation.setNs(omero.rtypes.rstring(
    				BooleanAnnotationData.INSIGHT_PUBLISHED_NS));
    		((BooleanAnnotation) annotation).setBoolValue(omero.rtypes.rbool(
    				((BooleanAnnotationData) data).getValue()));
    	}
    	return annotation;
    }
    
    /**
     * Links the annotation to the passed object.
     * 
     * @param annotatedObject	The object to annotate.
     * @param annotation		The annotation to link.
     * @return See above.
     */
    public static IObject linkAnnotation(IObject annotatedObject,
    									Annotation annotation) 
    {
    	if (annotation == null) return null;
    	if (annotatedObject instanceof Dataset) {
    		Dataset m = (Dataset) annotatedObject;
    		DatasetAnnotationLink l = new DatasetAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Image) {
    		Image m = (Image) annotatedObject;
    		ImageAnnotationLink l = new ImageAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Project) {
    		Project m = (Project) annotatedObject;
    		ProjectAnnotationLink l = new ProjectAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Annotation) {
    		Annotation ann = (Annotation) annotatedObject;
    		AnnotationAnnotationLink l = new AnnotationAnnotationLinkI();
    		l.setParent(ann);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Screen) {
    		Screen m = (Screen) annotatedObject;
    		ScreenAnnotationLink l = new ScreenAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Plate) {
    		Plate m = (Plate) annotatedObject;
    		PlateAnnotationLink l = new PlateAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Well) {
    		Well m = (Well) annotatedObject;
    		WellAnnotationLink l = new WellAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	}
    	return null;
    }
    
    /**
     * Links the annotated object and its annotation.
     * 
     * @param annotated		The annotated object.
     * @param annotation	The annotation.
     */
    public static void setAnnotatedObject(IObject annotated, 
            IObject annotation)  
    {
    	/*
        if (annotation instanceof ImageAnnotation)
            ((ImageAnnotation) annotation).setImage((Image) annotated);
        if (annotation instanceof DatasetAnnotation)
            ((DatasetAnnotation) annotation).setDataset((Dataset) annotated);
            */
    }
    
    /**
     * Returns the annotated IObject related to the specified annotation.
     * 
     * @param annotation    The annotation.
     * @return  See above.
     */
    public static IObject getAnnotatedObject(IObject annotation)
    {
    	if (annotation instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) annotation).getParent();
    	else if (annotation instanceof AnnotationAnnotationLink)
    		return ((AnnotationAnnotationLink) annotation).getParent();
    	else if (annotation instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) annotation).getParent();
    	else if (annotation instanceof WellAnnotationLink)
    		return ((WellAnnotationLink) annotation).getParent();
    	return null;
    }
    
    /**
     * Returns the annotated IObject related to the specified annotation.
     * 
     * @param annotation    The annotation.
     * @return  See above.
     */
    public static IObject getAnnotationObject(IObject annotation)
    {
    	if (annotation instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) annotation).getChild();
    	else if (annotation instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) annotation).getChild();
    	else if (annotation instanceof WellAnnotationLink)
    		return ((WellAnnotationLink) annotation).getChild();
    	return null;
    }
    
    /**
     * Fills the new IObject with data from the old one.
     * 
     * @param oldObject	The old object.
     * @param newObject	The object to fill.
     */
    public static void fillIObject(IObject oldObject, IObject newObject)
    {
    	if (oldObject == null || newObject == null)
    		throw new IllegalArgumentException("Object cannot be NULL.");
    	if (oldObject.getClass() != newObject.getClass())
    		throw new IllegalArgumentException("Objects should be of the " +
    				"same type.");
    	if (oldObject instanceof Project) {
    		Project n = (Project) newObject;
    		Project o = (Project) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Dataset) {
    		Dataset n = (Dataset) newObject;
    		Dataset o = (Dataset) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Image) {
    		Image n = (Image) newObject;
    		Image o = (Image) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Experimenter) {
    		Experimenter n = (Experimenter) newObject;
    		Experimenter o = (Experimenter) oldObject;
    		n.setEmail(o.getEmail());
    		n.setFirstName(o.getFirstName());
    		n.setLastName(o.getLastName());
    		n.setInstitution(o.getInstitution());
    		//n.setDefaultGroup(o.getDefaultGroup());
    	} else if (oldObject instanceof Screen) {
    		Screen n = (Screen) newObject;
    		Screen o = (Screen) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Plate) {
    		Plate n = (Plate) newObject;
    		Plate o = (Plate) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    		n.setDefaultSample(o.getDefaultSample());
    	} else if (oldObject instanceof Well) {
    		Well n = (Well) newObject;
    		Well o = (Well) oldObject;
    		n.setType(o.getType());
    		n.setExternalDescription(o.getExternalDescription());
    		n.setRed(o.getRed());
    		n.setGreen(o.getGreen());
    		n.setBlue(o.getBlue());
    		n.setAlpha(o.getAlpha());
    	}
    }
  
}
