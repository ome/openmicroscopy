package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Repository implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** persistent field */
    private String imageServerUrl;

    /** nullable persistent field */
    private String path;

    /** nullable persistent field */
    private Boolean isLocal;

    /** full constructor */
    public Repository(Integer attributeId, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, String imageServerUrl, String path, Boolean isLocal) {
        this.attributeId = attributeId;
        this.moduleExecution = moduleExecution;
        this.imageServerUrl = imageServerUrl;
        this.path = path;
        this.isLocal = isLocal;
    }

    /** default constructor */
    public Repository() {
    }

    /** minimal constructor */
    public Repository(Integer attributeId, String imageServerUrl) {
        this.attributeId = attributeId;
        this.imageServerUrl = imageServerUrl;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public org.openmicroscopy.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
