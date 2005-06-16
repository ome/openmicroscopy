package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ChannelComponent implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer index;

    /** nullable persistent field */
    private String colorDomain;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.ImagePixel imagePixel;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public ChannelComponent(Integer attributeId, Integer index, String colorDomain, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ImagePixel imagePixel, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.index = index;
        this.colorDomain = colorDomain;
        this.image = image;
        this.imagePixel = imagePixel;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public ChannelComponent() {
    }

    /** minimal constructor */
    public ChannelComponent(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ImagePixel imagePixel, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.image = image;
        this.imagePixel = imagePixel;
        this.moduleExecution = moduleExecution;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getColorDomain() {
        return this.colorDomain;
    }

    public void setColorDomain(String colorDomain) {
        this.colorDomain = colorDomain;
    }

    public org.openmicroscopy.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.openmicroscopy.omero.model.Image image) {
        this.image = image;
    }

    public org.openmicroscopy.omero.model.ImagePixel getImagePixel() {
        return this.imagePixel;
    }

    public void setImagePixel(org.openmicroscopy.omero.model.ImagePixel imagePixel) {
        this.imagePixel = imagePixel;
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
        if ( !(other instanceof ChannelComponent) ) return false;
        ChannelComponent castOther = (ChannelComponent) other;
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
