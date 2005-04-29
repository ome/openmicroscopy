package org.ome.hibernate.model;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Feature implements Serializable {

    /** identifier field */
    private Integer featureId;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private String tag;

    /** persistent field */
    private org.ome.hibernate.model.Image image;

    /** persistent field */
    private org.ome.hibernate.model.Feature feature;

    /** persistent field */
    private Set features;

    /** full constructor */
    public Feature(Integer featureId, String name, String tag, org.ome.hibernate.model.Image image, org.ome.hibernate.model.Feature feature, Set features) {
        this.featureId = featureId;
        this.name = name;
        this.tag = tag;
        this.image = image;
        this.feature = feature;
        this.features = features;
    }

    /** default constructor */
    public Feature() {
    }

    /** minimal constructor */
    public Feature(Integer featureId, String tag, org.ome.hibernate.model.Image image, org.ome.hibernate.model.Feature feature, Set features) {
        this.featureId = featureId;
        this.tag = tag;
        this.image = image;
        this.feature = feature;
        this.features = features;
    }

    public Integer getFeatureId() {
        return this.featureId;
    }

    public void setFeatureId(Integer featureId) {
        this.featureId = featureId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public org.ome.hibernate.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.hibernate.model.Image image) {
        this.image = image;
    }

    public org.ome.hibernate.model.Feature getFeature() {
        return this.feature;
    }

    public void setFeature(org.ome.hibernate.model.Feature feature) {
        this.feature = feature;
    }

    public Set getFeatures() {
        return this.features;
    }

    public void setFeatures(Set features) {
        this.features = features;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("featureId", getFeatureId())
            .toString();
    }

}
