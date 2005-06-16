package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImagePixel implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String pixelType;

    /** nullable persistent field */
    private Integer sizeY;

    /** nullable persistent field */
    private Integer sizeZ;

    /** nullable persistent field */
    private String fileSha1;

    /** nullable persistent field */
    private String path;

    /** nullable persistent field */
    private Integer sizeT;

    /** nullable persistent field */
    private Long imageServerId;

    /** nullable persistent field */
    private Integer sizeX;

    /** nullable persistent field */
    private Integer sizeC;

    /** nullable persistent field */
    private Integer bitsPerPixel;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.Repository repository;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** persistent field */
    private Set images;

    /** persistent field */
    private Set displayOptions;

    /** persistent field */
    private Set channelComponents;

    /** full constructor */
    public ImagePixel(Integer attributeId, String pixelType, Integer sizeY, Integer sizeZ, String fileSha1, String path, Integer sizeT, Long imageServerId, Integer sizeX, Integer sizeC, Integer bitsPerPixel, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Repository repository, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set images, Set displayOptions, Set channelComponents) {
        this.attributeId = attributeId;
        this.pixelType = pixelType;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.fileSha1 = fileSha1;
        this.path = path;
        this.sizeT = sizeT;
        this.imageServerId = imageServerId;
        this.sizeX = sizeX;
        this.sizeC = sizeC;
        this.bitsPerPixel = bitsPerPixel;
        this.image = image;
        this.repository = repository;
        this.moduleExecution = moduleExecution;
        this.images = images;
        this.displayOptions = displayOptions;
        this.channelComponents = channelComponents;
    }

    /** default constructor */
    public ImagePixel() {
    }

    /** minimal constructor */
    public ImagePixel(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.Repository repository, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set images, Set displayOptions, Set channelComponents) {
        this.attributeId = attributeId;
        this.image = image;
        this.repository = repository;
        this.moduleExecution = moduleExecution;
        this.images = images;
        this.displayOptions = displayOptions;
        this.channelComponents = channelComponents;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
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

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public Integer getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    public void setBitsPerPixel(Integer bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
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

    public Set getImages() {
        return this.images;
    }

    public void setImages(Set images) {
        this.images = images;
    }

    public Set getDisplayOptions() {
        return this.displayOptions;
    }

    public void setDisplayOptions(Set displayOptions) {
        this.displayOptions = displayOptions;
    }

    public Set getChannelComponents() {
        return this.channelComponents;
    }

    public void setChannelComponents(Set channelComponents) {
        this.channelComponents = channelComponents;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ImagePixel) ) return false;
        ImagePixel castOther = (ImagePixel) other;
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
