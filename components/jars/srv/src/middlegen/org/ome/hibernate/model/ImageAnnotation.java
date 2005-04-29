package org.ome.hibernate.model;

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

    /** nullable persistent field */
    private Long timestamp;

    /** nullable persistent field */
    private Integer experimenter;

    /** persistent field */
    private org.ome.hibernate.model.Image image;

    /** full constructor */
    public ImageAnnotation(Integer attributeId, Integer theT, String content, Integer theC, Integer theZ, Boolean valid, Long timestamp, Integer experimenter, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.theT = theT;
        this.content = content;
        this.theC = theC;
        this.theZ = theZ;
        this.valid = valid;
        this.timestamp = timestamp;
        this.experimenter = experimenter;
        this.image = image;
    }

    /** default constructor */
    public ImageAnnotation() {
    }

    /** minimal constructor */
    public ImageAnnotation(Integer attributeId, org.ome.hibernate.model.Image image) {
        this.attributeId = attributeId;
        this.image = image;
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

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getExperimenter() {
        return this.experimenter;
    }

    public void setExperimenter(Integer experimenter) {
        this.experimenter = experimenter;
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
