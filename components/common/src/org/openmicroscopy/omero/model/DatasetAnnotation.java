package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class DatasetAnnotation implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String content;

    /** nullable persistent field */
    private Boolean valid;

    /** persistent field */
    private org.openmicroscopy.omero.model.Dataset dataset;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public DatasetAnnotation(Integer attributeId, String content, Boolean valid, org.openmicroscopy.omero.model.Dataset dataset, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.content = content;
        this.valid = valid;
        this.dataset = dataset;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public DatasetAnnotation() {
    }

    /** minimal constructor */
    public DatasetAnnotation(Integer attributeId, org.openmicroscopy.omero.model.Dataset dataset, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.dataset = dataset;
        this.moduleExecution = moduleExecution;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public org.openmicroscopy.omero.model.Dataset getDataset() {
        return this.dataset;
    }

    public void setDataset(org.openmicroscopy.omero.model.Dataset dataset) {
        this.dataset = dataset;
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
        if ( !(other instanceof DatasetAnnotation) ) return false;
        DatasetAnnotation castOther = (DatasetAnnotation) other;
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
