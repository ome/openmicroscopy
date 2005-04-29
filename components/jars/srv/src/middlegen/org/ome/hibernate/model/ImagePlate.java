package org.ome.hibernate.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ImagePlate implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private String well;

    /** nullable persistent field */
    private Integer sample;

    /** nullable persistent field */
    private Integer plate;

    /** persistent field */
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ImagePlate(Integer attributeId, String well, Integer sample, Integer plate, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.well = well;
        this.sample = sample;
        this.plate = plate;
        this.image = image;
    }

    /** default constructor */
    public ImagePlate() {
    }

    /** minimal constructor */
    public ImagePlate(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
    }

    public Integer getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getWell() {
        return this.well;
    }

    public void setWell(String well) {
        this.well = well;
    }

    public Integer getSample() {
        return this.sample;
    }

    public void setSample(Integer sample) {
        this.sample = sample;
    }

    public Integer getPlate() {
        return this.plate;
    }

    public void setPlate(Integer plate) {
        this.plate = plate;
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
