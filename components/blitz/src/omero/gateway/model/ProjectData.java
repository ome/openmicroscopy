/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package omero.gateway.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static omero.rtypes.rstring;
import omero.model.Dataset;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;

/**
 * The data that makes up an <i>OME</i> Project along with links to its
 * contained Datasets and the Experimenter that owns this Project.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ProjectData extends DataObject {

    /** Identifies the {@link Project#NAME} field. */
    public final static String NAME = ProjectI.NAME;

    /** Identifies the {@link Project#DESCRIPTION} field. */
    public final static String DESCRIPTION = ProjectI.DESCRIPTION;

    /** Identifies the {@link Project#DATASETLINKS} field. */
    public final static String DATASET_LINKS = ProjectI.DATASETLINKS;

    /**
     * All the Datasets that are contained this Project. The elements of this
     * set are {@link DatasetData} objects. If this Project does not contained
     * in any Dataset, then this set will be empty &#151; but never
     * <code>null</code>.
     */
    private Set<DatasetData> datasets;

    /** Creates a new instance. */
    public ProjectData() {
        setDirty(true);
        setValue(new ProjectI());
    }

    /**
     * Creates a new instance.
     * 
     * @param project
     *            Back pointer to the {@link Project} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public ProjectData(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(project);
    }

    /**
     * Sets the name of the project.
     * 
     * @param name
     *            The name of the project. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asProject().setName(rstring(name));
    }

    /**
     * Returns the name of the project.
     * 
     * @return See above.
     */
    public String getName() {
        omero.RString n = asProject().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null");
        }
        return n.getValue();
    }

    /**
     * Sets the description of the project.
     * 
     * @param description
     *            The description of the project.
     */
    public void setDescription(String description) {
        setDirty(true);
        asProject().setDescription(rstring(description));
    }

    /**
     * Returns the description of the project.
     * 
     * @return See above.
     */
    public String getDescription() {
        omero.RString d = asProject().getDescription();
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
        return asProject().getAnnotationLinksCountPerOwner();
    }

    // Lazy loaded Links
    /**
     * Returns the datasets contained in this project.
     * 
     * @return See above.
     */
    public Set<DatasetData> getDatasets() {
        if (datasets == null && asProject().sizeOfDatasetLinks() >= 0) {
            datasets = new HashSet<DatasetData>();
            List<ProjectDatasetLink> links = asProject().copyDatasetLinks();
            for (ProjectDatasetLink link : links) {
                datasets.add(new DatasetData(link.getChild()));
            }
        }
        return datasets == null ? null : new HashSet<DatasetData>(datasets);
    }

    // Link mutations

    /**
     * Sets the datasets contained in this project.
     * 
     * @param newValue
     *            The set of datasets.
     */
    public void setDatasets(Set<DatasetData> newValue) {
        //first clear the links
        asProject().clearDatasetLinks();
        if (newValue == null) return;
        Iterator<DatasetData> i = newValue.iterator();
        ProjectDatasetLink link;
        Project p = asProject();
        while (i.hasNext()) {
            link = new ProjectDatasetLinkI();
            link.link((Project) p.proxy(), (Dataset) i.next().asDataset());
            p.addProjectDatasetLink(link);
        }
        datasets = null;
        datasets = getDatasets();
    }

}
