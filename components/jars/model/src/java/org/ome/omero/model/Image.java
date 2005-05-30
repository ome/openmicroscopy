package org.ome.omero.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Image implements Serializable {

    /** identifier field */
    private Integer imageId;

    /** nullable persistent field */
    private Integer pixelsId;

    /** persistent field */
    private Date inserted;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Date created;

    /** nullable persistent field */
    private String imageGuid;

    /** persistent field */
    private org.ome.omero.model.Group group;

    /** persistent field */
    private org.ome.omero.model.Experimenter experimenter;

    /** persistent field */
    private Set thumbnails;

    /** persistent field */
    private Set classifications;

    /** persistent field */
    private Set displayRois;

    /** persistent field */
    private Set imageInfos;

    /** persistent field */
    private Set imagePixels;

    /** persistent field */
    private Set imagePlates;

    /** persistent field */
    private Set features;

    /** persistent field */
    private Set imageAnnotations;

    /** persistent field */
    private Set moduleExecutions;

    /** persistent field */
    private Set imageDimensions;

    /** persistent field */
    private Set channelComponents;

    /** persistent field */
    private Set displayOptions;

    /** persistent field */
    private Set datasets;

    /** full constructor */
    public Image(Integer imageId, Integer pixelsId, Date inserted, String name, String description, Date created, String imageGuid, org.ome.omero.model.Group group, org.ome.omero.model.Experimenter experimenter, Set thumbnails, Set classifications, Set displayRois, Set imageInfos, Set imagePixels, Set imagePlates, Set features, Set imageAnnotations, Set moduleExecutions, Set imageDimensions, Set channelComponents, Set displayOptions, Set datasets) {
        this.imageId = imageId;
        this.pixelsId = pixelsId;
        this.inserted = inserted;
        this.name = name;
        this.description = description;
        this.created = created;
        this.imageGuid = imageGuid;
        this.group = group;
        this.experimenter = experimenter;
        this.thumbnails = thumbnails;
        this.classifications = classifications;
        this.displayRois = displayRois;
        this.imageInfos = imageInfos;
        this.imagePixels = imagePixels;
        this.imagePlates = imagePlates;
        this.features = features;
        this.imageAnnotations = imageAnnotations;
        this.moduleExecutions = moduleExecutions;
        this.imageDimensions = imageDimensions;
        this.channelComponents = channelComponents;
        this.displayOptions = displayOptions;
        this.datasets = datasets;
    }

    /** default constructor */
    public Image() {
    }

    /** minimal constructor */
    public Image(Integer imageId, Date inserted, String name, Date created, org.ome.omero.model.Group group, org.ome.omero.model.Experimenter experimenter, Set thumbnails, Set classifications, Set displayRois, Set imageInfos, Set imagePixels, Set imagePlates, Set features, Set imageAnnotations, Set moduleExecutions, Set imageDimensions, Set channelComponents, Set displayOptions, Set datasets) {
        this.imageId = imageId;
        this.inserted = inserted;
        this.name = name;
        this.created = created;
        this.group = group;
        this.experimenter = experimenter;
        this.thumbnails = thumbnails;
        this.classifications = classifications;
        this.displayRois = displayRois;
        this.imageInfos = imageInfos;
        this.imagePixels = imagePixels;
        this.imagePlates = imagePlates;
        this.features = features;
        this.imageAnnotations = imageAnnotations;
        this.moduleExecutions = moduleExecutions;
        this.imageDimensions = imageDimensions;
        this.channelComponents = channelComponents;
        this.displayOptions = displayOptions;
        this.datasets = datasets;
    }

    public Integer getImageId() {
        return this.imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getPixelsId() {
        return this.pixelsId;
    }

    public void setPixelsId(Integer pixelsId) {
        this.pixelsId = pixelsId;
    }

    public Date getInserted() {
        return this.inserted;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
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

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getImageGuid() {
        return this.imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }

    public org.ome.omero.model.Group getGroup() {
        return this.group;
    }

    public void setGroup(org.ome.omero.model.Group group) {
        this.group = group;
    }

    public org.ome.omero.model.Experimenter getExperimenter() {
        return this.experimenter;
    }

    public void setExperimenter(org.ome.omero.model.Experimenter experimenter) {
        this.experimenter = experimenter;
    }

    public Set getThumbnails() {
        return this.thumbnails;
    }

    public void setThumbnails(Set thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Set getClassifications() {
        return this.classifications;
    }

    public void setClassifications(Set classifications) {
        this.classifications = classifications;
    }

    public Set getDisplayRois() {
        return this.displayRois;
    }

    public void setDisplayRois(Set displayRois) {
        this.displayRois = displayRois;
    }

    public Set getImageInfos() {
        return this.imageInfos;
    }

    public void setImageInfos(Set imageInfos) {
        this.imageInfos = imageInfos;
    }

    public Set getImagePixels() {
        return this.imagePixels;
    }

    public void setImagePixels(Set imagePixels) {
        this.imagePixels = imagePixels;
    }

    public Set getImagePlates() {
        return this.imagePlates;
    }

    public void setImagePlates(Set imagePlates) {
        this.imagePlates = imagePlates;
    }

    public Set getFeatures() {
        return this.features;
    }

    public void setFeatures(Set features) {
        this.features = features;
    }

    public Set getImageAnnotations() {
        return this.imageAnnotations;
    }

    public void setImageAnnotations(Set imageAnnotations) {
        this.imageAnnotations = imageAnnotations;
    }

    public Set getModuleExecutions() {
        return this.moduleExecutions;
    }

    public void setModuleExecutions(Set moduleExecutions) {
        this.moduleExecutions = moduleExecutions;
    }

    public Set getImageDimensions() {
        return this.imageDimensions;
    }

    public void setImageDimensions(Set imageDimensions) {
        this.imageDimensions = imageDimensions;
    }

    public Set getChannelComponents() {
        return this.channelComponents;
    }

    public void setChannelComponents(Set channelComponents) {
        this.channelComponents = channelComponents;
    }

    public Set getDisplayOptions() {
        return this.displayOptions;
    }

    public void setDisplayOptions(Set displayOptions) {
        this.displayOptions = displayOptions;
    }

    public Set getDatasets() {
        return this.datasets;
    }

    public void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("imageId", getImageId())
            .toString();
    }

}
