package org.ome.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Thumbnail implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer repository;

    /** nullable persistent field */
    private String path;

    /** nullable persistent field */
    private String mimeType;

    /** persistent field */
    private org.ome.omero.model.Image image;

    /** persistent field */
    private org.ome.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public Thumbnail(Integer attributeId, Integer repository, String path, String mimeType, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.repository = repository;
        this.path = path;
        this.mimeType = mimeType;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public Thumbnail() {
    }

    /** minimal constructor */
    public Thumbnail(Integer attributeId, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
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

    public Integer getRepository() {
        return this.repository;
    }

    public void setRepository(Integer repository) {
        this.repository = repository;
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

    public org.ome.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.omero.model.Image image) {
        this.image = image;
    }

    public org.ome.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.ome.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
