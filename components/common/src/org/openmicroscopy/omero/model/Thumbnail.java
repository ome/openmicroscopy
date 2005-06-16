package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Thumbnail implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String path;

    /** nullable persistent field */
    private String mimeType;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.Repository repository;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public Thumbnail(Integer attributeId, String path, String mimeType, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Repository repository, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.path = path;
        this.mimeType = mimeType;
        this.image = image;
        this.repository = repository;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public Thumbnail() {
    }

    /** minimal constructor */
    public Thumbnail(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Repository repository, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.image = image;
        this.repository = repository;
        this.moduleExecution = moduleExecution;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public org.openmicroscopy.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.openmicroscopy.omero.model.Image image) {
        this.image = image;
    }

    public org.openmicroscopy.omero.model.Repository getRepository() {
        return this.repository;
    }

    public void setRepository(org.openmicroscopy.omero.model.Repository repository) {
        this.repository = repository;
    }

    public org.openmicroscopy.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Thumbnail) ) return false;
        Thumbnail castOther = (Thumbnail) other;
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
