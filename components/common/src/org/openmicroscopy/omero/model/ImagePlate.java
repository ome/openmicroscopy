package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImagePlate implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String well;

    /** nullable persistent field */
    private Integer sample;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public ImagePlate(Integer attributeId, String well, Integer sample, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.well = well;
        this.sample = sample;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public ImagePlate() {
    }

    /** minimal constructor */
    public ImagePlate(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getWell() {
        return this.well;
    }

    public void setWell(String well) {
        this.well = well;
    }

    public Integer getSample() {
        return this.sample;
    }

    public void setSample(Integer sample) {
        this.sample = sample;
    }

    public org.openmicroscopy.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.openmicroscopy.omero.model.Image image) {
        this.image = image;
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
        if ( !(other instanceof ImagePlate) ) return false;
        ImagePlate castOther = (ImagePlate) other;
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
