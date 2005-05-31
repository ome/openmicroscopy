package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ChannelComponent implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer pixelsId;

    /** nullable persistent field */
    private Integer index;

    /** nullable persistent field */
    private String colorDomain;

    /** nullable persistent field */
    private Integer logicalChannel;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public ChannelComponent(Integer attributeId, Integer pixelsId, Integer index, String colorDomain, Integer logicalChannel, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.pixelsId = pixelsId;
        this.index = index;
        this.colorDomain = colorDomain;
        this.logicalChannel = logicalChannel;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public ChannelComponent() {
    }

    /** minimal constructor */
    public ChannelComponent(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
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

    public Integer getPixelsId() {
        return this.pixelsId;
    }

    public void setPixelsId(Integer pixelsId) {
        this.pixelsId = pixelsId;
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

    public Integer getLogicalChannel() {
        return this.logicalChannel;
    }

    public void setLogicalChannel(Integer logicalChannel) {
        this.logicalChannel = logicalChannel;
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

}
