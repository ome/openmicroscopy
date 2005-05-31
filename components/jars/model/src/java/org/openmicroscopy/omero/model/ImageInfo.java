package org.openmicroscopy.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImageInfo implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Integer experiment;

    /** nullable persistent field */
    private Integer groupId;

    /** nullable persistent field */
    private Integer instrument;

    /** nullable persistent field */
    private Integer objective;

    /** persistent field */
    private org.openmicroscopy.omero.model.Image image;

    /** persistent field */
    private org.openmicroscopy.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public ImageInfo(Integer attributeId, Integer experiment, Integer groupId, Integer instrument, Integer objective, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.experiment = experiment;
        this.groupId = groupId;
        this.instrument = instrument;
        this.objective = objective;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public ImageInfo() {
    }

    /** minimal constructor */
    public ImageInfo(Integer attributeId, org.openmicroscopy.omero.model.Image image, org.openmicroscopy.omero.model.ModuleExecution moduleExecution) {
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

    public Integer getExperiment() {
        return this.experiment;
    }

    public void setExperiment(Integer experiment) {
        this.experiment = experiment;
    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Integer instrument) {
        this.instrument = instrument;
    }

    public Integer getObjective() {
        return this.objective;
    }

    public void setObjective(Integer objective) {
        this.objective = objective;
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
