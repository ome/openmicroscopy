package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Classification implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Float confidence;

    /** nullable persistent field */
    private Boolean valid;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.Category category;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public Classification(Integer attributeId, Float confidence, Boolean valid, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Category category, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.confidence = confidence;
        this.valid = valid;
        this.image = image;
        this.category = category;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public Classification() {
    }

    /** minimal constructor */
    public Classification(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Category category, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.image = image;
        this.category = category;
        this.moduleExecution = moduleExecution;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Float getConfidence() {
        return this.confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public org.openmicroscopy.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.openmicroscopy.omero.model.Image image) {
        this.image = image;
    }

    public org.openmicroscopy.omero.model.Category getCategory() {
        return this.category;
    }

    public void setCategory(org.openmicroscopy.omero.model.Category category) {
        this.category = category;
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
        if ( !(other instanceof Classification) ) return false;
        Classification castOther = (Classification) other;
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
