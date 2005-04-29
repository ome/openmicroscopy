package org.ome.hibernate.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImageDimension implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Float pixelSizeC;

    /** nullable persistent field */
    private Float pixelSizeT;

    /** nullable persistent field */
    private Float pixelSizeX;

    /** nullable persistent field */
    private Float pixelSizeY;

    /** nullable persistent field */
    private Float pixelSizeZ;

    /** persistent field */
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ImageDimension(Integer attributeId, Float pixelSizeC, Float pixelSizeT, Float pixelSizeX, Float pixelSizeY, Float pixelSizeZ, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.pixelSizeC = pixelSizeC;
        this.pixelSizeT = pixelSizeT;
        this.pixelSizeX = pixelSizeX;
        this.pixelSizeY = pixelSizeY;
        this.pixelSizeZ = pixelSizeZ;
        this.image = image;
    }

    /** default constructor */
    public ImageDimension() {
    }

    /** minimal constructor */
    public ImageDimension(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Float getPixelSizeC() {
        return this.pixelSizeC;
    }

    public void setPixelSizeC(Float pixelSizeC) {
        this.pixelSizeC = pixelSizeC;
    }

    public Float getPixelSizeT() {
        return this.pixelSizeT;
    }

    public void setPixelSizeT(Float pixelSizeT) {
        this.pixelSizeT = pixelSizeT;
    }

    public Float getPixelSizeX() {
        return this.pixelSizeX;
    }

    public void setPixelSizeX(Float pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    public Float getPixelSizeY() {
        return this.pixelSizeY;
    }

    public void setPixelSizeY(Float pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    public Float getPixelSizeZ() {
        return this.pixelSizeZ;
    }

    public void setPixelSizeZ(Float pixelSizeZ) {
        this.pixelSizeZ = pixelSizeZ;
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
