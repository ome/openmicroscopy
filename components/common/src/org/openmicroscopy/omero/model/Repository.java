package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Repository implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** persistent field */
    private String imageServerUrl;

    /** nullable persistent field */
    private String path;

    /** nullable persistent field */
    private Boolean isLocal;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** persistent field */
    private Set thumbnails;

    /** persistent field */
    private Set imagePixels;

    /** full constructor */
    public Repository(Integer attributeId, String imageServerUrl, String path, Boolean isLocal, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set thumbnails, Set imagePixels) {
        this.attributeId = attributeId;
        this.imageServerUrl = imageServerUrl;
        this.path = path;
        this.isLocal = isLocal;
        this.moduleExecution = moduleExecution;
        this.thumbnails = thumbnails;
        this.imagePixels = imagePixels;
    }

    /** default constructor */
    public Repository() {
    }

    /** minimal constructor */
    public Repository(Integer attributeId, String imageServerUrl, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set thumbnails, Set imagePixels) {
        this.attributeId = attributeId;
        this.imageServerUrl = imageServerUrl;
        this.moduleExecution = moduleExecution;
        this.thumbnails = thumbnails;
        this.imagePixels = imagePixels;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getImageServerUrl() {
        return this.imageServerUrl;
    }

    public void setImageServerUrl(String imageServerUrl) {
        this.imageServerUrl = imageServerUrl;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getIsLocal() {
        return this.isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public org.openmicroscopy.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public Set getThumbnails() {
        return this.thumbnails;
    }

    public void setThumbnails(Set thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Set getImagePixels() {
        return this.imagePixels;
    }

    public void setImagePixels(Set imagePixels) {
        this.imagePixels = imagePixels;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
