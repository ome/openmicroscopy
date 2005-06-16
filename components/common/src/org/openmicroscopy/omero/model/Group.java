package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Group implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private org.openmicroscopy.omero.model.Experimenter leader;

    /** persistent field */
    private org.openmicroscopy.omero.model.Experimenter contact;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** persistent field */
    private Set datasets;

    /** persistent field */
    private Set images;

    /** persistent field */
    private Set projects;

    /** persistent field */
    private Set experimenters;

    /** persistent field */
    private Set moduleExecutions;

    /** persistent field */
    private Set imageInfos;

    /** full constructor */
    public Group(Integer attributeId, String name, org.openmicroscopy.omero.model.Experimenter leader, org.openmicroscopy.omero.model.Experimenter contact, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set datasets, Set images, Set projects, Set experimenters, Set moduleExecutions, Set imageInfos) {
        this.attributeId = attributeId;
        this.name = name;
        this.leader = leader;
        this.contact = contact;
        this.moduleExecution = moduleExecution;
        this.datasets = datasets;
        this.images = images;
        this.projects = projects;
        this.experimenters = experimenters;
        this.moduleExecutions = moduleExecutions;
        this.imageInfos = imageInfos;
    }

    /** default constructor */
    public Group() {
    }

    /** minimal constructor */
    public Group(Integer attributeId, org.openmicroscopy.omero.model.Experimenter leader, org.openmicroscopy.omero.model.Experimenter contact, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set datasets, Set images, Set projects, Set experimenters, Set moduleExecutions, Set imageInfos) {
        this.attributeId = attributeId;
        this.leader = leader;
        this.contact = contact;
        this.moduleExecution = moduleExecution;
        this.datasets = datasets;
        this.images = images;
        this.projects = projects;
        this.experimenters = experimenters;
        this.moduleExecutions = moduleExecutions;
        this.imageInfos = imageInfos;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public org.openmicroscopy.omero.model.Experimenter getLeader() {
        return this.leader;
    }

    public void setLeader(org.openmicroscopy.omero.model.Experimenter leader) {
        this.leader = leader;
    }

    public org.openmicroscopy.omero.model.Experimenter getContact() {
        return this.contact;
    }

    public void setContact(org.openmicroscopy.omero.model.Experimenter contact) {
        this.contact = contact;
    }

    public org.openmicroscopy.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public Set getDatasets() {
        return this.datasets;
    }

    public void setDatasets(Set datasets) {
        this.datasets = datasets;
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

    public Set getExperimenters() {
        return this.experimenters;
    }

    public void setExperimenters(Set experimenters) {
        this.experimenters = experimenters;
    }

    public Set getModuleExecutions() {
        return this.moduleExecutions;
    }

    public void setModuleExecutions(Set moduleExecutions) {
        this.moduleExecutions = moduleExecutions;
    }

    public Set getImageInfos() {
        return this.imageInfos;
    }

    public void setImageInfos(Set imageInfos) {
        this.imageInfos = imageInfos;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Group) ) return false;
        Group castOther = (Group) other;
        return new EqualsBuilder()
            .append(this.getAttributeId(), castOther.getAttributeId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getAttributeId())
            .toHashCode();
    }

}
