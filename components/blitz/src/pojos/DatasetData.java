/*
 * pojos.DatasetData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

//Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import static omero.rtypes.rstring;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.LongAnnotation;
import omero.model.ProjectDatasetLink;

/**
 * The data that makes up an <i>OME</i> Dataset along with links to its
 * contained Images and enclosing Project as well as the Experimenter that owns
 * this Dataset.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date: 2005/05/09
 *          19:50:41 $) </small>
 * @since OME2.2
 */
public class DatasetData extends DataObject {

    /** Identifies the {@link Dataset#NAME} field. */
    public final static String NAME = DatasetI.NAME;

    /** Identifies the {@link Dataset#DESCRIPTION} field. */
    public final static String DESCRIPTION = DatasetI.DESCRIPTION;

    /** Identifies the {@link Dataset#IMAGELINKS} field. */
    public final static String IMAGE_LINKS = DatasetI.IMAGELINKS;

    /** Identifies the {@link Dataset#PROJECTLINKS} field. */
    public final static String PROJECT_LINKS = DatasetI.PROJECTLINKS;

    /** Identifies the {@link Dataset#ANNOTATIONLINKS} field. */
    public final static String ANNOTATIONS = DatasetI.ANNOTATIONLINKS;

    /**
     * All the Images contained in this Dataset. The elements of this set are
     * {@link ImageData} objects. If this Dataset contains no Images, then this
     * set will be empty &#151; but never <code>null</code>.
     */
    private Set<ImageData> images;

    /**
     * All the Projects that contain this Dataset. The elements of this set are
     * {@link ProjectData} objects. If this Dataset is not contained in any
     * Project, then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<ProjectData> projects;

    /**
     * All the annotations related to this Dataset. The elements of the set are
     * {@link AnnotationData} objetcs. If this Dataset hasn't been annotated,
     * then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<AnnotationData> annotations;

    /**
     * The number of annotations attached to this Dataset. This field may be
     * <code>null</code> meaning no count retrieved, and it may be less than
     * the actual number if filtered by user.
     */
    private Long annotationCount;

    /** Creates a new instance. */
    public DatasetData() {
        setDirty(true);
        setValue(new DatasetI());
    }

    /**
     * Creates a new instance.
     * 
     * @param dataset
     *            Back pointer to the {@link Dataset} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public DatasetData(Dataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(dataset);
    }

    // IMMUTABLES

    /**
     * Sets the name of the dataset.
     * 
     * @param name
     *            The name of the dataset. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asDataset().setName(rstring(name));
    }

    /**
     * Returns the name of the dataset.
     * 
     * @return See above.
     */
    public String getName() {
        omero.RString n = asDataset().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null.");
        }
        return n.getValue();
    }

    /**
     * Sets the description of the dataset.
     * 
     * @param description
     *            The description of the dataset.
     */
    public void setDescription(String description) {
        setDirty(true);
        asDataset().setDescription(rstring(description));
    }

    /**
     * Returns the description of the dataset.
     * 
     * @return See above.
     */
    public String getDescription() {
        omero.RString d = asDataset().getDescription();
        return d == null ? null : d.getValue();
    }

    /**
     * Returns the number of annotations linked to the object, key: id of the
     * user, value: number of annotation. The map may be <code>null</code> if
     * no annotation.
     * 
     * @return See above.
     */
    public Map<Long, Long> getAnnotationsCounts() {
        return asDataset().getAnnotationLinksCountPerOwner();
    }

    // Lazy loaded links

    /**
     * Returns a set of images contained in the dataset.
     * 
     * @return See above.
     */
    public Set getImages() {
        if (images == null && asDataset().sizeOfImageLinks() >= 0) {
            List<DatasetImageLink> imageLinks = asDataset().copyImageLinks();
            images = new HashSet<ImageData>();
            for (DatasetImageLink link : imageLinks) {
                images.add(new ImageData(link.getChild()));
            }
        }
        return images == null ? null : new HashSet(images);
    }

    /**
     * Returns a set of projects containing the dataset.
     * 
     * @return See above.
     */
    public Set getProjects() {
        if (projects == null && asDataset().sizeOfProjectLinks() >= 0) {
            projects = new HashSet();
            List<ProjectDatasetLink> projectLinks = asDataset()
                    .copyProjectLinks();
            for (ProjectDatasetLink link : projectLinks) {
                projects.add(new ProjectData(link.getParent()));
            }
        }

        return projects == null ? null : new HashSet(projects);
    }

    // Link mutations

    /**
     * Sets the images contained in this dataset.
     * 
     * @param newValue
     *            The set of images.
     */
    public void setImages(Set<ImageData> newValue) {
        Set<ImageData> currentValue = getImages();
        SetMutator<ImageData> m = new SetMutator<ImageData>(currentValue,
                newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().unlinkImage(m.nextDeletion().asImage());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asDataset().linkImage(m.nextAddition().asImage());
        }

        images = new HashSet<ImageData>(m.result());
    }

    /**
     * Sets the projects containing the dataset.
     * 
     * @param newValue
     *            The set of projects.
     */
    public void setProjects(Set<ProjectData> newValue) {
        Set<ProjectData> currentValue = getProjects();
        SetMutator<ProjectData> m = new SetMutator<ProjectData>(currentValue,
                newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().unlinkProject(m.nextDeletion().asProject());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asDataset().linkProject(m.nextAddition().asProject());
        }

        projects = new HashSet<ProjectData>(m.result());
    }

    // SETS
    /**
     * Returns the annotations related to this dataset. Not sure we are going to
     * keep this method.
     * 
     * @return See Above
     */
    public Set getAnnotations() {
        if (annotations == null) {
            int size = asDataset().sizeOfAnnotationLinks();
            if (size >= 0) {
                annotations = new HashSet<AnnotationData>(size);
                List<DatasetAnnotationLink> links = asDataset()
                        .copyAnnotationLinks();
                for (DatasetAnnotationLink link : links) {
                    Annotation a = link.getChild();
                    if (a instanceof CommentAnnotation) {
                        annotations.add(new TextualAnnotationData(
                                (CommentAnnotation) a));
                    } else if (a instanceof LongAnnotation) {
                        annotations.add(new RatingAnnotationData(
                                (LongAnnotation) a));
                    }
                }
            }
        }
        return annotations == null ? null : new HashSet<AnnotationData>(
                annotations);
    }

    /**
     * Sets the annotations related to this dataset.
     * 
     * @param newValue
     *            The set of annotations.
     */
    public void setAnnotations(Set newValue) {
        Set<AnnotationData> currentValue = getAnnotations();
        SetMutator<AnnotationData> m = new SetMutator<AnnotationData>(
                currentValue, newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asDataset().unlinkAnnotation(m.nextDeletion().asAnnotation());
            annotationCount = annotationCount == null ? null : new Long(
                    annotationCount.longValue() - 1);
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asDataset().linkAnnotation(m.nextAddition().asAnnotation());
            annotationCount = annotationCount == null ? null : new Long(
                    annotationCount.longValue() + 1);
        }

        annotations = new HashSet<AnnotationData>(m.result());
    }

}
