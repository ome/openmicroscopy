package org.openmicroscopy.omero.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Category implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private org.openmicroscopy.omero.model.CategoryGroup categoryGroup;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** persistent field */
    private Set classifications;

    /** full constructor */
    public Category(Integer attributeId, String name, String description, org.openmicroscopy.omero.model.CategoryGroup categoryGroup, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set classifications) {
        this.attributeId = attributeId;
        this.name = name;
        this.description = description;
        this.categoryGroup = categoryGroup;
        this.moduleExecution = moduleExecution;
        this.classifications = classifications;
    }

    /** default constructor */
    public Category() {
    }

    /** minimal constructor */
    public Category(Integer attributeId, org.openmicroscopy.omero.model.CategoryGroup categoryGroup, org.openmicroscopy.omero.model.ModuleExecution moduleExecution, Set classifications) {
        this.attributeId = attributeId;
        this.categoryGroup = categoryGroup;
        this.moduleExecution = moduleExecution;
        this.classifications = classifications;
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

    public org.openmicroscopy.omero.model.CategoryGroup getCategoryGroup() {
        return this.categoryGroup;
    }

    public void setCategoryGroup(org.openmicroscopy.omero.model.CategoryGroup categoryGroup) {
        this.categoryGroup = categoryGroup;
    }

    public org.openmicroscopy.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public Set getClassifications() {
        return this.classifications;
    }

    public void setClassifications(Set classifications) {
        this.classifications = classifications;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
