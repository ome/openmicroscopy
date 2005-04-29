package org.ome.hibernate.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class CategoryGroup implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** full constructor */
    public CategoryGroup(Integer attributeId, String name, String description) {
        this.attributeId = attributeId;
        this.name = name;
        this.description = description;
    }

    /** default constructor */
    public CategoryGroup() {
    }

    /** minimal constructor */
    public CategoryGroup(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
