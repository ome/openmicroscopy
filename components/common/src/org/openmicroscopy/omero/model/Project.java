package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Project implements Serializable {

    /** identifier field */
    private Integer projectId;

    /** nullable persistent field */
    private String view;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private org.openmicroscopy.omero.model.Group group;

    /** persistent field */
    private org.openmicroscopy.omero.model.Experimenter experimenter;

    /** persistent field */
    private Set datasets;

    /** full constructor */
    public Project(Integer projectId, String view, String name, String description, org.openmicroscopy.omero.model.Group group, org.openmicroscopy.omero.model.Experimenter experimenter, Set datasets) {
        this.projectId = projectId;
        this.view = view;
        this.name = name;
        this.description = description;
        this.group = group;
        this.experimenter = experimenter;
        this.datasets = datasets;
    }

    /** default constructor */
    public Project() {
    }

    /** minimal constructor */
    public Project(Integer projectId, String name, org.openmicroscopy.omero.model.Group group, org.openmicroscopy.omero.model.Experimenter experimenter, Set datasets) {
        this.projectId = projectId;
        this.name = name;
        this.group = group;
        this.experimenter = experimenter;
        this.datasets = datasets;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getView() {
        return this.view;
    }

    public void setView(String view) {
        this.view = view;
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

    public org.openmicroscopy.omero.model.Group getGroup() {
        return this.group;
    }

    public void setGroup(org.openmicroscopy.omero.model.Group group) {
        this.group = group;
    }

    public org.openmicroscopy.omero.model.Experimenter getExperimenter() {
        return this.experimenter;
    }

    public void setExperimenter(org.openmicroscopy.omero.model.Experimenter experimenter) {
        this.experimenter = experimenter;
    }

    public Set getDatasets() {
        return this.datasets;
    }

    public void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("projectId", getProjectId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Project) ) return false;
        Project castOther = (Project) other;
        return new EqualsBuilder()
            .append(this.getProjectId(), castOther.getProjectId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getProjectId())
            .toHashCode();
    }

}
