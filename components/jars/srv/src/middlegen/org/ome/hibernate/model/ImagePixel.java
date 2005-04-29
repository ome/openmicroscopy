package org.ome.hibernate.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImagePixel implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer repository;

    /** nullable persistent field */
    private String pixelType;

    /** nullable persistent field */
    private Integer sizeY;

    /** nullable persistent field */
    private Integer sizeZ;

    /** nullable persistent field */
    private String fileSha1;

    /** nullable persistent field */
    private Integer sizeT;

    /** nullable persistent field */
    private Long imageServerId;

    /** nullable persistent field */
    private Integer sizeX;

    /** nullable persistent field */
    private Integer sizeC;

    /** persistent field */
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ImagePixel(Integer attributeId, Integer repository, String pixelType, Integer sizeY, Integer sizeZ, String fileSha1, Integer sizeT, Long imageServerId, Integer sizeX, Integer sizeC, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.repository = repository;
        this.pixelType = pixelType;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.fileSha1 = fileSha1;
        this.sizeT = sizeT;
        this.imageServerId = imageServerId;
        this.sizeX = sizeX;
        this.sizeC = sizeC;
        this.image = image;
    }

    /** default constructor */
    public ImagePixel() {
    }

    /** minimal constructor */
    public ImagePixel(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getRepository() {
        return this.repository;
    }

    public void setRepository(Integer repository) {
        this.repository = repository;
    }

    public String getPixelType() {
        return this.pixelType;
    }

    public void setPixelType(String pixelType) {
        this.pixelType = pixelType;
    }

    public Integer getSizeY() {
        return this.sizeY;
    }

    public void setSizeY(Integer sizeY) {
        this.sizeY = sizeY;
    }

    public Integer getSizeZ() {
        return this.sizeZ;
    }

    public void setSizeZ(Integer sizeZ) {
        this.sizeZ = sizeZ;
    }

    public String getFileSha1() {
        return this.fileSha1;
    }

    public void setFileSha1(String fileSha1) {
        this.fileSha1 = fileSha1;
    }

    public Integer getSizeT() {
        return this.sizeT;
    }

    public void setSizeT(Integer sizeT) {
        this.sizeT = sizeT;
    }

    public Long getImageServerId() {
        return this.imageServerId;
    }

    public void setImageServerId(Long imageServerId) {
        this.imageServerId = imageServerId;
    }

    public Integer getSizeX() {
        return this.sizeX;
    }

    public void setSizeX(Integer sizeX) {
        this.sizeX = sizeX;
    }

    public Integer getSizeC() {
        return this.sizeC;
    }

    public void setSizeC(Integer sizeC) {
        this.sizeC = sizeC;
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
