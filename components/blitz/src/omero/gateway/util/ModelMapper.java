/*
 * org.openmicroscopy.shoola.env.data.util.ModelMapper
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package omero.gateway.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RMap;
import omero.RString;
import omero.RType;
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
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
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
import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileAnnotationLink;
import omero.model.OriginalFileAnnotationLinkI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
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
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DoubleAnnotationData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.MapAnnotationData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.XMLAnnotationData;

/** 
 * Helper class to map {@link DataObject}s into their corresponding
 * {@link IObject}s.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
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
        if (object == null) return;
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
        if (link instanceof WellAnnotationLink)
            return ((WellAnnotationLink) link).getChild();
        if (link instanceof PlateAcquisitionAnnotationLink)
            return ((PlateAcquisitionAnnotationLink) link).getChild();
        if (link instanceof AnnotationAnnotationLink)
            return ((AnnotationAnnotationLink) link).getChild();
        return null;
    }

    /**
     * Returns the child from the passed link.
     *
     * @param link The link to handle.
     * @return See above.
     */
    public static IObject getParentFromLink(IObject link)
    {
        if (link == null) return null;
        if (link instanceof ProjectAnnotationLink)
            return ((ProjectAnnotationLink) link).getParent();
        if (link instanceof DatasetAnnotationLink)
            return ((DatasetAnnotationLink) link).getParent();
        if (link instanceof ImageAnnotationLink)
            return ((ImageAnnotationLink) link).getParent();
        if (link instanceof PlateAnnotationLink)
            return ((PlateAnnotationLink) link).getParent();
        if (link instanceof ScreenAnnotationLink)
            return ((ScreenAnnotationLink) link).getParent();
        if (link instanceof WellAnnotationLink)
            return ((WellAnnotationLink) link).getParent();
        if (link instanceof PlateAcquisitionAnnotationLink)
            return ((PlateAcquisitionAnnotationLink) link).getParent();
        if (link instanceof AnnotationAnnotationLink)
            return ((AnnotationAnnotationLink) link).getParent();
        return null;
    }

    /**
     * Links the  {@link IObject child} to its {@link IObject parent}.
     * 
     * @param child The child to handle.
     * @param parent The parent to handle.
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
            Project unloadedProject;
            if (parent.getId() == null) unloadedProject = (Project) parent;
            else
                unloadedProject = new ProjectI(parent.getId().getValue(),
                        false);
            Dataset unloadedDataset;
            if (child.getId() == null) unloadedDataset = (Dataset) child;
            else
                unloadedDataset = new DatasetI(child.getId().getValue(),
                        false);
            ProjectDatasetLink l = new ProjectDatasetLinkI();
            l.link(unloadedProject, unloadedDataset);
            return l;
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset unloadedDataset;
            if (parent.getId() == null) unloadedDataset = (Dataset) parent;
            else
                unloadedDataset = new DatasetI(parent.getId().getValue(),
                        false);
            Image unloadedImage = new ImageI(child.getId().getValue(), false);

            DatasetImageLink l = new DatasetImageLinkI();
            l.link(unloadedDataset, unloadedImage);
            return l;
        } else if (parent instanceof Screen) {
            if (!(child instanceof Plate))
                throw new IllegalArgumentException("Child not valid.");

            Screen unloadedScreen;
            if (parent.getId() == null) unloadedScreen = (Screen) parent;
            else
                unloadedScreen = new ScreenI(parent.getId().getValue(),
                        false);
            Plate unloadedPlate;
            if (child.getId() == null) unloadedPlate = (Plate) child;
            else
                unloadedPlate = new PlateI(child.getId().getValue(),
                        false);

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
     * @param child The newly created child.
     * @param parent The parent of the newly created child.
     */
    public static void linkParentToNewChild(IObject child, IObject parent)
    {
        if (parent == null) return;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                "be null.");

        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project p = (Project) parent;
            Dataset d = (Dataset) child;

            List<ProjectDatasetLink> l = d.copyProjectLinks();
            if (l == null) return;
            ProjectDatasetLink link;
            Iterator<ProjectDatasetLink> it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = it.next();
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
            List<DatasetImageLink> l = d.copyDatasetLinks();
            if (l == null) return;
            DatasetImageLink link;
            Iterator<DatasetImageLink> it = l.iterator();
            long id = p.getId().getValue();
            while (it.hasNext()) {
                link = it.next();
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
     * @param child The child to create.
     * @param parent The child's parent.
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
            return g;
        }
        throw new IllegalArgumentException("Child and parent are not " +
                "compatible.");
    }

    /**
     * Unlinks the specified child and the parent and returns the
     * updated child <code>IObject</code>.
     *
     * @param child The child to remove.
     * @param parent The parent of the child.
     * @return See above. 
     */
    public static IObject removeIObject(IObject child, IObject parent)
    {
        if ((child instanceof Dataset) && (parent instanceof Project)) {
            Project mParent = (Project) parent;
            List<ProjectDatasetLink> s = mParent.copyDatasetLinks();
            Iterator<ProjectDatasetLink> i = s.iterator();
            while (i.hasNext()) { 
                mParent.removeProjectDatasetLink(
                        new ProjectDatasetLinkI(i.next().getId(), false));
            }
            return mParent;
        } 
        throw new IllegalArgumentException("DataObject not supported.");
    }

    /**
     * Creates a new annotation <code>IObject</code>.
     *
     * @param annotatedObject The <code>DataObject</code> to annotate.
     *                        Can either be a <code>DatasetData</code>
     *                        or a <code>ImageData</code>. Mustn't be
     *                        <code>null</code>.
     * @param data The annotation to create.
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
     * @param annotatedObject The <code>DataObject</code> to annotate.
     *                        Can either be a <code>DatasetData</code>
     *                        or a <code>ImageData</code>. Mustn't be
     *                        <code>null</code>.
     * @param data The annotation to create.
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
            if (StringUtils.isNotEmpty(ns)) {
                annotation.setNs(omero.rtypes.rstring(ns));
            }
        } else if (data instanceof BooleanAnnotationData) {
            annotation = new BooleanAnnotationI();
            annotation.setNs(omero.rtypes.rstring(
                    BooleanAnnotationData.INSIGHT_PUBLISHED_NS));
            ((BooleanAnnotation) annotation).setBoolValue(omero.rtypes.rbool(
                    ((BooleanAnnotationData) data).getValue()));
        } else if (data instanceof XMLAnnotationData) {
            annotation = new XmlAnnotationI();
            ((XmlAnnotation) annotation).setTextValue(
                    omero.rtypes.rstring(data.getContentAsString()));
            annotation.setDescription(omero.rtypes.rstring(
                    ((XMLAnnotationData) data).getDescription()));
            String ns = data.getNameSpace();
            if (ns != null && ns.length() > 0) {
                annotation.setNs(omero.rtypes.rstring(ns));
            }
        } else if (data instanceof LongAnnotationData) {
            annotation = new LongAnnotationI();
            ((LongAnnotation) annotation).setLongValue(omero.rtypes.rlong(
                    (Long) data.getContent()));
        } else if (data instanceof DoubleAnnotationData) {
            annotation = new DoubleAnnotationI();
            ((DoubleAnnotation) annotation).setDoubleValue(omero.rtypes.rdouble(
                    (Double) data.getContent()));
        } else if (data instanceof MapAnnotationData) {
            annotation = new MapAnnotationI();
            String ns = data.getNameSpace();
            if (StringUtils.isNotEmpty(ns)) {
                annotation.setNs(omero.rtypes.rstring(ns));
            }
            ((MapAnnotation) annotation).setMapValue((List<NamedValue>) data
                    .getContent());
        }
        return annotation;
    }

    /**
     * Links the annotation to the passed object.
     *
     * @param annotatedObject The object to annotate.
     * @param annotation The annotation to link.
     * @return See above.
     */
    public static IObject linkAnnotation(IObject annotatedObject,
            Annotation annotation)
    {
        if (annotation == null) return null;
        if (annotatedObject instanceof Dataset) {
            DatasetAnnotationLink l = new DatasetAnnotationLinkI();
            l.setParent((Dataset) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Image) {
            ImageAnnotationLink l = new ImageAnnotationLinkI();
            l.setParent((Image) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Project) {
            ProjectAnnotationLink l = new ProjectAnnotationLinkI();
            l.setParent((Project) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Annotation) {
            AnnotationAnnotationLink l = new AnnotationAnnotationLinkI();
            l.setParent((Annotation) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Screen) {
            ScreenAnnotationLink l = new ScreenAnnotationLinkI();
            l.setParent((Screen) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Plate) {
            PlateAnnotationLink l = new PlateAnnotationLinkI();
            l.setParent((Plate) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof PlateAcquisition) {
            PlateAcquisitionAnnotationLink l = 
                    new PlateAcquisitionAnnotationLinkI();
            l.setParent((PlateAcquisition) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof Well) {
            WellAnnotationLink l = new WellAnnotationLinkI();
            l.setParent((Well) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        } else if (annotatedObject instanceof OriginalFile) {
            OriginalFileAnnotationLink l = new OriginalFileAnnotationLinkI();
            l.setParent((OriginalFile) annotatedObject.proxy());
            l.setChild(annotation);
            return l;
        }
        return null;
    }

    /**
     * Returns the annotated IObject related to the specified annotation.
     *
     * @param annotation The annotation.
     * @return See above.
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
        else if (annotation instanceof PlateAcquisitionAnnotationLink)
            return ((PlateAcquisitionAnnotationLink) annotation).getParent();
        return null;
    }

    /**
     * Returns the annotation represented by the specified link
     *
     * @param link The annotation.
     * @return See above.
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
     * @param oldObject The old object.
     * @param newObject The object to fill.
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
            n.setMiddleName(o.getMiddleName());
            n.setInstitution(o.getInstitution());
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
        } else if (oldObject instanceof PlateAcquisition) {
            PlateAcquisition n = (PlateAcquisition) newObject;
            PlateAcquisition o = (PlateAcquisition) oldObject;
            n.setName(o.getName());
            n.setDescription(o.getDescription());
            n.setEndTime(o.getEndTime());
            n.setStartTime(o.getStartTime());
            n.setMaximumFieldCount(o.getMaximumFieldCount());
        }
    }

    /**
     * Converts the passed OMERO type into its corresponding Java type.
     *
     * @param type The type to handle.
     * @return See above.
     */
    public static Object convertRTypeToJava(RType type)
    {
        if (type instanceof RString) return ((RString) type).getValue();
        if (type instanceof RLong) return ((RLong) type).getValue();
        if (type instanceof RBool) return ((RBool) type).getValue();
        if (type instanceof RDouble) return ((RDouble) type).getValue();
        if (type instanceof RFloat) return ((RFloat) type).getValue();
        if (type instanceof RInt) return ((RInt) type).getValue();
        if (type instanceof RList) {
            List<RType> types = ((RList) type).getValue();
            List<Object> l = new ArrayList<Object>(types.size());
            Iterator<RType> i = types.iterator();
            while (i.hasNext()) {
                l.add(convertRTypeToJava(i.next()));
            }
            return l;
        }
        if (type instanceof RMap) {
            Map<String, RType> types = ((RMap) type).getValue();
            Map<String, Object> l = new LinkedHashMap<String, Object>(
                    types.size());
            Entry<String, RType> e;
            Iterator<Entry<String, RType>> i = types.entrySet().iterator();
            while (i.hasNext()) {
                e = i.next();
                l.put(e.getKey(), convertRTypeToJava(e.getValue()));
            }
            return l;
        }
        return "";
    }
}
