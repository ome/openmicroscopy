package org.ome.hibernate.model;

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
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ImageInfo(Integer attributeId, Integer experiment, Integer groupId, Integer instrument, Integer objective, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.experiment = experiment;
        this.groupId = groupId;
        this.instrument = instrument;
        this.objective = objective;
        this.image = image;
    }

    /** default constructor */
    public ImageInfo() {
    }

    /** minimal constructor */
    public ImageInfo(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
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

    public org.ome.hibernate.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.hibernate.model.Image image) {
        this.image = image;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
