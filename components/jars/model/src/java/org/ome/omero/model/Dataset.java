package org.ome.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Dataset implements Serializable {

    /** identifier field */
    private Integer datasetId;

    /** persistent field */
    private boolean locked;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private org.ome.omero.model.Group group;

    /** persistent field */
    private org.ome.omero.model.Experimenter experimenter;

    /** persistent field */
    private Set moduleExecutions;

    /** persistent field */
    private Set datasetAnnotations;

    /** persistent field */
    private Set images;

    /** persistent field */
    private Set projects;

    /** full constructor */
    public Dataset(Integer datasetId, boolean locked, String name, String description, org.ome.omero.model.Group group, org.ome.omero.model.Experimenter experimenter, Set moduleExecutions, Set datasetAnnotations, Set images, Set projects) {
        this.datasetId = datasetId;
        this.locked = locked;
        this.name = name;
        this.description = description;
        this.group = group;
        this.experimenter = experimenter;
        this.moduleExecutions = moduleExecutions;
        this.datasetAnnotations = datasetAnnotations;
        this.images = images;
        this.projects = projects;
    }

    /** default constructor */
    public Dataset() {
    }

    /** minimal constructor */
    public Dataset(Integer datasetId, boolean locked, String name, org.ome.omero.model.Group group, org.ome.omero.model.Experimenter experimenter, Set moduleExecutions, Set datasetAnnotations, Set images, Set projects) {
        this.datasetId = datasetId;
        this.locked = locked;
        this.name = name;
        this.group = group;
        this.experimenter = experimenter;
        this.moduleExecutions = moduleExecutions;
        this.datasetAnnotations = datasetAnnotations;
        this.images = images;
        this.projects = projects;
    }

    public Integer getDatasetId() {
        return this.datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public org.ome.omero.model.Group getGroup() {
        return this.group;
    }

    public void setGroup(org.ome.omero.model.Group group) {
        this.group = group;
    }

    public org.ome.omero.model.Experimenter getExperimenter() {
        return this.experimenter;
    }

    public void setExperimenter(org.ome.omero.model.Experimenter experimenter) {
        this.experimenter = experimenter;
    }

    public Set getModuleExecutions() {
        return this.moduleExecutions;
    }

    public void setModuleExecutions(Set moduleExecutions) {
        this.moduleExecutions = moduleExecutions;
    }

    public Set getDatasetAnnotations() {
        return this.datasetAnnotations;
    }

    public void setDatasetAnnotations(Set datasetAnnotations) {
        this.datasetAnnotations = datasetAnnotations;
    }

    public Set getImages() {
        return this.images;
    }

    public void setImages(Set images) {
        this.images = images;
    }

    public Set getProjects() {
        return this.projects;
    }

    public void setProjects(Set projects) {
        this.projects = projects;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("datasetId", getDatasetId())
            .toString();
    }

}
