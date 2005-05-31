package org.openmicroscopy.omero.model;

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

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public CategoryGroup(Integer attributeId, String name, String description, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.name = name;
        this.description = description;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public CategoryGroup() {
    }

    /** minimal constructor */
    public CategoryGroup(Integer attributeId, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.moduleExecution = moduleExecution;
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

}
