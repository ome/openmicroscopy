package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Experimenter implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String omeName;

    /** nullable persistent field */
    private String email;

    /** nullable persistent field */
    private String firstname;

    /** nullable persistent field */
    private String password;

    /** nullable persistent field */
    private org.openmicroscopy.omero.model.Group group;

    /** nullable persistent field */
    private String dataDir;

    /** nullable persistent field */
    private Integer moduleExecutionId;

    /** nullable persistent field */
    private String lastname;

    /** nullable persistent field */
    private String institution;

    /** persistent field */
    private Set datasets;

    /** persistent field */
    private Set groupsByLeader;

    /** persistent field */
    private Set groupsByContact;

    /** persistent field */
    private Set images;

    /** persistent field */
    private Set projects;

    /** persistent field */
    private Set moduleExecutions;

    /** full constructor */
    public Experimenter(Integer attributeId, String omeName, String email, String firstname, String password, org.openmicroscopy.omero.model.Group group, String dataDir, Integer moduleExecutionId, String lastname, String institution, Set datasets, Set groupsByLeader, Set groupsByContact, Set images, Set projects, Set moduleExecutions) {
        this.attributeId = attributeId;
        this.omeName = omeName;
        this.email = email;
        this.firstname = firstname;
        this.password = password;
        this.group = group;
        this.dataDir = dataDir;
        this.moduleExecutionId = moduleExecutionId;
        this.lastname = lastname;
        this.institution = institution;
        this.datasets = datasets;
        this.groupsByLeader = groupsByLeader;
        this.groupsByContact = groupsByContact;
        this.images = images;
        this.projects = projects;
        this.moduleExecutions = moduleExecutions;
    }

    /** default constructor */
    public Experimenter() {
    }

    /** minimal constructor */
    public Experimenter(Integer attributeId, Set datasets, Set groupsByLeader, Set groupsByContact, Set images, Set projects, Set moduleExecutions) {
        this.attributeId = attributeId;
        this.datasets = datasets;
        this.groupsByLeader = groupsByLeader;
        this.groupsByContact = groupsByContact;
        this.images = images;
        this.projects = projects;
        this.moduleExecutions = moduleExecutions;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getOmeName() {
        return this.omeName;
    }

    public void setOmeName(String omeName) {
        this.omeName = omeName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public org.openmicroscopy.omero.model.Group getGroup() {
        return this.group;
    }

    public void setGroup(org.openmicroscopy.omero.model.Group group) {
        this.group = group;
    }

    public String getDataDir() {
        return this.dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public Integer getModuleExecutionId() {
        return this.moduleExecutionId;
    }

    public void setModuleExecutionId(Integer moduleExecutionId) {
        this.moduleExecutionId = moduleExecutionId;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getInstitution() {
        return this.institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Set getDatasets() {
        return this.datasets;
    }

    public void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    public Set getGroupsByLeader() {
        return this.groupsByLeader;
    }

    public void setGroupsByLeader(Set groupsByLeader) {
        this.groupsByLeader = groupsByLeader;
    }

    public Set getGroupsByContact() {
        return this.groupsByContact;
    }

    public void setGroupsByContact(Set groupsByContact) {
        this.groupsByContact = groupsByContact;
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

    public Set getModuleExecutions() {
        return this.moduleExecutions;
    }

    public void setModuleExecutions(Set moduleExecutions) {
        this.moduleExecutions = moduleExecutions;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
