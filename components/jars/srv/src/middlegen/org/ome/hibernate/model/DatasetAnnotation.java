package org.ome.hibernate.model;

import java.io.Serializable;
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
    private org.ome.hibernate.model.Dataset dataset;

    /** full constructor */
    public DatasetAnnotation(Integer attributeId, String content, Boolean valid, org.ome.hibernate.model.Dataset dataset) {
        this.attributeId = attributeId;
        this.content = content;
        this.valid = valid;
        this.dataset = dataset;
    }

    /** default constructor */
    public DatasetAnnotation() {
    }

    /** minimal constructor */
    public DatasetAnnotation(Integer attributeId, org.ome.hibernate.model.Dataset dataset) {
        this.attributeId = attributeId;
        this.dataset = dataset;
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

    public org.ome.hibernate.model.Dataset getDataset() {
        return this.dataset;
    }

    public void setDataset(org.ome.hibernate.model.Dataset dataset) {
        this.dataset = dataset;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
