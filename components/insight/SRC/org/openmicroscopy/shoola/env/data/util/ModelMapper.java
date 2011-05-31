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
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMapI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.OriginalFileAnnotationLink;
import omero.model.OriginalFileAnnotationLinkI;
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
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import omero.model.WellSampleAnnotationLink;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;

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
        if (object.isLoaded())
        	object.unloadCollections();
    }

    /**
     * Returns the child from the passed link.
     * 
     * @param link The link to handle.
     * @return See above.
     */
    public static IObject getChildFromLink(IObject link)
    {
    	if (link == null) return null;
    	if (link instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) link).getChild();
    	if (link instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) link).getChild();
    	if (link instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) link).getChild();
    	if (link instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) link).getChild();
    	if (link instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) link).getChild();
    	if (link instanceof WellSampleAnnotationLink)
    		return ((WellSampleAnnotationLink) link).getChild();
    	if (link instanceof AnnotationAnnotationLink)
    		return ((AnnotationAnnotationLink) link).getChild();
    	return null;
    }
    
    /**
     * Links the  {@link IObject child} to its {@link IObject parent}.
     * 
     * @param child     The child to handle. 
     * @param parent    The parent to handle. 
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
        	return linkAnnotation(parent, (TagAnnotation) child);
        } else if (parent instanceof ExperimenterGroup) {
        	 if (!(child instanceof Experimenter))
                 throw new IllegalArgumentException("Child not valid.");
        	 ExperimenterGroup unloadedGroup = 
        		 new ExperimenterGroupI(parent.getId().getValue(), 
             		false);
        	 Experimenter unloadedExp = 
        		 new ExperimenterI(child.getId().getValue(), false);
             
             GroupExperimenterMapI l = new GroupExperimenterMapI();
             l.link(unloadedGroup, unloadedExp);
             return l;
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
        Iterator it;
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project p = (Project) parent;
            Dataset d = (Dataset) child;
            
            l = d.copyProjectLinks();
            if (l == null) return;
            ProjectDatasetLink link;
            
            it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = (ProjectDatasetLink) it.next();
                if (id == link.getParent().getId().getValue()) {
                	p.addProjectDatasetLink(
                			new ProjectDatasetLinkI(link.getId(), false));
                }
            }
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset p = (Dataset) parent;
            Image d = (Image) child;
            l = d.copyDatasetLinks();
            if (l == null) return;
            DatasetImageLink link;
            it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = (DatasetImageLink) it.next();
                if (id == link.getParent().getId().getValue()) {
                 	p.addDatasetImageLink(
                 			new DatasetImageLinkI(link.getId(), false));
                }
            }
        } else
            throw new IllegalArgumentException("DataObject not supported.");
    }
    
    /**
     * Converts the specified <code>DataObject</code> into its corresponding 
     * <code>IObject</code>.
     * 
     * @param child The child to create.
     * @return The {@link IObject} to create.
     */
    public static IObject createIObject(DataObject child)
    {
    	return createIObject(child, null);
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
        } else if (child instanceof ExperimenterData) {
        	ExperimenterData data = (ExperimenterData) child;
        	Experimenter exp = new ExperimenterI();
        	exp.setFirstName(omero.rtypes.rstring(data.getFirstName()));
        	exp.setLastName(omero.rtypes.rstring(data.getLastName()));
        	exp.setMiddleName(omero.rtypes.rstring(data.getMiddleName()));
        	exp.setEmail(omero.rtypes.rstring(data.getEmail()));
        	exp.setInstitution(omero.rtypes.rstring(data.getInstitution()));
        	return exp;
        } else if (child instanceof GroupData) {
        	GroupData data = (GroupData) child;
        	ExperimenterGroup g = new ExperimenterGroupI();
        	g.setName(omero.rtypes.rstring(data.getName()));
        	//g.setDescription(omero.rtypes.rstring(data.getDescription()));
        	return g;
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
            ProjectDatasetLink link;
            while (i.hasNext()) { 
            	link = (ProjectDatasetLink) i.next();
                mParent.removeProjectDatasetLink(
                		new ProjectDatasetLinkI(link.getId(), false));
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
    	} else if (data instanceof TermAnnotationData) {
    		annotation = new TermAnnotationI();
    		((TermAnnotation) annotation).setTermValue(
					omero.rtypes.rstring(data.getContentAsString()));
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
    	} else if (annotatedObject instanceof OriginalFile) {
    		OriginalFile of = (OriginalFile) annotatedObject;
    		OriginalFileAnnotationLink l = new OriginalFileAnnotationLinkI();
    		l.setParent(of);
    		l.setChild(annotation);
    		return l;
    	}
    	return null;
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
     * Returns the annotation represented by the specified link
     * 
     * @param link  The annotation.
     * @return  See above.
     */
    public static IObject getAnnotationObject(IObject link)
    {
    	if (link instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) link).getChild();
    	else if (link instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) link).getChild();
    	else if (link instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) link).getChild();
    	else if (link instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) link).getChild();
    	else if (link instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) link).getChild();
    	else if (link instanceof WellAnnotationLink)
    		return ((WellAnnotationLink) link).getChild();
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
