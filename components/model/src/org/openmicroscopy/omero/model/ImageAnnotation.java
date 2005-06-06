package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImageAnnotation implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer theT;

    /** nullable persistent field */
    private String content;

    /** nullable persistent field */
    private Integer theC;

    /** nullable persistent field */
    private Integer theZ;

    /** nullable persistent field */
    private Boolean valid;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public ImageAnnotation(Integer attributeId, Integer theT, String content, Integer theC, Integer theZ, Boolean valid, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.theT = theT;
        this.content = content;
        this.theC = theC;
        this.theZ = theZ;
        this.valid = valid;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public ImageAnnotation() {
    }

    /** minimal constructor */
    public ImageAnnotation(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
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

    public Integer getTheT() {
        return this.theT;
    }

    public void setTheT(Integer theT) {
        this.theT = theT;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTheC() {
        return this.theC;
    }

    public void setTheC(Integer theC) {
        this.theC = theC;
    }

    public Integer getTheZ() {
        return this.theZ;
    }

    public void setTheZ(Integer theZ) {
        this.theZ = theZ;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public org.openmicroscopy.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.openmicroscopy.omero.model.Image image) {
        this.image = image;
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
