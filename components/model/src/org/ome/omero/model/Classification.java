package org.ome.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Classification implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer category;

    /** nullable persistent field */
    private Float confidence;

    /** nullable persistent field */
    private Boolean valid;

    /** persistent field */
    private org.ome.omero.model.Image image;

    /** persistent field */
    private org.ome.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public Classification(Integer attributeId, Integer category, Float confidence, Boolean valid, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.category = category;
        this.confidence = confidence;
        this.valid = valid;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public Classification() {
    }

    /** minimal constructor */
    public Classification(Integer attributeId, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
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

    public Integer getCategory() {
        return this.category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Float getConfidence() {
        return this.confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
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
