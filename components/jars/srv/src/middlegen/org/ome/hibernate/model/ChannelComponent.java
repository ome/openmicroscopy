package org.ome.hibernate.model;

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
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ChannelComponent(Integer attributeId, Integer pixelsId, Integer index, String colorDomain, Integer logicalChannel, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.pixelsId = pixelsId;
        this.index = index;
        this.colorDomain = colorDomain;
        this.logicalChannel = logicalChannel;
        this.image = image;
    }

    /** default constructor */
    public ChannelComponent() {
    }

    /** minimal constructor */
    public ChannelComponent(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
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

    public org.ome.hibernate.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.hibernate.model.Image image) {
        this.image = image;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
