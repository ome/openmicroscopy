package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Group implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private Integer moduleExecutionId;

    /** persistent field */
    private org.openmicroscopy.omero.model.Experimenter experimenterByLeader;

    /** persistent field */
    private org.openmicroscopy.omero.model.Experimenter experimenterByContact;

    /** persistent field */
    private Set datasets;

    /** persistent field */
    private Set images;

    /** persistent field */
    private Set projects;

    /** full constructor */
    public Group(Integer attributeId, String name, Integer moduleExecutionId, org.openmicroscopy.omero.model.Experimenter experimenterByLeader, org.openmicroscopy.omero.model.Experimenter experimenterByContact, Set datasets, Set images, Set projects) {
        this.attributeId = attributeId;
        this.name = name;
        this.moduleExecutionId = moduleExecutionId;
        this.experimenterByLeader = experimenterByLeader;
        this.experimenterByContact = experimenterByContact;
        this.datasets = datasets;
        this.images = images;
        this.projects = projects;
    }

    /** default constructor */
    public Group() {
    }

    /** minimal constructor */
    public Group(Integer attributeId, org.openmicroscopy.omero.model.Experimenter experimenterByLeader, org.openmicroscopy.omero.model.Experimenter experimenterByContact, Set datasets, Set images, Set projects) {
        this.attributeId = attributeId;
        this.experimenterByLeader = experimenterByLeader;
        this.experimenterByContact = experimenterByContact;
        this.datasets = datasets;
        this.images = images;
        this.projects = projects;
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

    public Integer getModuleExecutionId() {
        return this.moduleExecutionId;
    }

    public void setModuleExecutionId(Integer moduleExecutionId) {
        this.moduleExecutionId = moduleExecutionId;
    }

    public org.openmicroscopy.omero.model.Experimenter getExperimenterByLeader() {
        return this.experimenterByLeader;
    }

    public void setExperimenterByLeader(org.openmicroscopy.omero.model.Experimenter experimenterByLeader) {
        this.experimenterByLeader = experimenterByLeader;
    }

    public org.openmicroscopy.omero.model.Experimenter getExperimenterByContact() {
        return this.experimenterByContact;
    }

    public void setExperimenterByContact(org.openmicroscopy.omero.model.Experimenter experimenterByContact) {
        this.experimenterByContact = experimenterByContact;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
