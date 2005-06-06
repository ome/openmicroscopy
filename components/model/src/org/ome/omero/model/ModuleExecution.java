package org.ome.omero.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class ModuleExecution implements Serializable {

    /** identifier field */
    private Integer moduleExecutionId;

    /** nullable persistent field */
    private String status;

    /** nullable persistent field */
    private Double attributeDbTime;

    /** persistent field */
    private boolean virtualMex;

    /** nullable persistent field */
    private String inputTag;

    /** nullable persistent field */
    private String iteratorTag;

    /** persistent field */
    private String dependence;

    /** nullable persistent field */
    private Double attributeSortTime;

    /** nullable persistent field */
    private String errorMessage;

    /** nullable persistent field */
    private Date timestamp;

    /** nullable persistent field */
    private Double attributeCreateTime;

    /** nullable persistent field */
    private String newFeatureTag;

    /** nullable persistent field */
    private Double totalTime;

    /** persistent field */
    private org.ome.omero.model.Image image;

    /** persistent field */
    private org.ome.omero.model.Dataset dataset;

    /** persistent field */
    private org.ome.omero.model.Experimenter experimenter;

    /** persistent field */
    private Set thumbnails;

    /** persistent field */
    private Set classifications;

    /** persistent field */
    private Set categories;

    /** persistent field */
    private Set displayRois;

    /** persistent field */
    private Set imageInfos;

    /** persistent field */
    private Set imagePixels;

    /** persistent field */
    private Set imagePlates;

    /** persistent field */
    private Set categoryGroups;

    /** persistent field */
    private Set imageAnnotations;

    /** persistent field */
    private Set datasetAnnotations;

    /** persistent field */
    private Set imageDimensions;

    /** persistent field */
    private Set channelComponents;

    /** persistent field */
    private Set displayOptions;

    /** full constructor */
    public ModuleExecution(Integer moduleExecutionId, String status, Double attributeDbTime, boolean virtualMex, String inputTag, String iteratorTag, String dependence, Double attributeSortTime, String errorMessage, Date timestamp, Double attributeCreateTime, String newFeatureTag, Double totalTime, org.ome.omero.model.Image image, org.ome.omero.model.Dataset dataset, org.ome.omero.model.Experimenter experimenter, Set thumbnails, Set classifications, Set categories, Set displayRois, Set imageInfos, Set imagePixels, Set imagePlates, Set categoryGroups, Set imageAnnotations, Set datasetAnnotations, Set imageDimensions, Set channelComponents, Set displayOptions) {
        this.moduleExecutionId = moduleExecutionId;
        this.status = status;
        this.attributeDbTime = attributeDbTime;
        this.virtualMex = virtualMex;
        this.inputTag = inputTag;
        this.iteratorTag = iteratorTag;
        this.dependence = dependence;
        this.attributeSortTime = attributeSortTime;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
        this.attributeCreateTime = attributeCreateTime;
        this.newFeatureTag = newFeatureTag;
        this.totalTime = totalTime;
        this.image = image;
        this.dataset = dataset;
        this.experimenter = experimenter;
        this.thumbnails = thumbnails;
        this.classifications = classifications;
        this.categories = categories;
        this.displayRois = displayRois;
        this.imageInfos = imageInfos;
        this.imagePixels = imagePixels;
        this.imagePlates = imagePlates;
        this.categoryGroups = categoryGroups;
        this.imageAnnotations = imageAnnotations;
        this.datasetAnnotations = datasetAnnotations;
        this.imageDimensions = imageDimensions;
        this.channelComponents = channelComponents;
        this.displayOptions = displayOptions;
    }

    /** default constructor */
    public ModuleExecution() {
    }

    /** minimal constructor */
    public ModuleExecution(Integer moduleExecutionId, boolean virtualMex, String dependence, org.ome.omero.model.Image image, org.ome.omero.model.Dataset dataset, org.ome.omero.model.Experimenter experimenter, Set thumbnails, Set classifications, Set categories, Set displayRois, Set imageInfos, Set imagePixels, Set imagePlates, Set categoryGroups, Set imageAnnotations, Set datasetAnnotations, Set imageDimensions, Set channelComponents, Set displayOptions) {
        this.moduleExecutionId = moduleExecutionId;
        this.virtualMex = virtualMex;
        this.dependence = dependence;
        this.image = image;
        this.dataset = dataset;
        this.experimenter = experimenter;
        this.thumbnails = thumbnails;
        this.classifications = classifications;
        this.categories = categories;
        this.displayRois = displayRois;
        this.imageInfos = imageInfos;
        this.imagePixels = imagePixels;
        this.imagePlates = imagePlates;
        this.categoryGroups = categoryGroups;
        this.imageAnnotations = imageAnnotations;
        this.datasetAnnotations = datasetAnnotations;
        this.imageDimensions = imageDimensions;
        this.channelComponents = channelComponents;
        this.displayOptions = displayOptions;
    }

    public Integer getModuleExecutionId() {
        return this.moduleExecutionId;
    }

    public void setModuleExecutionId(Integer moduleExecutionId) {
        this.moduleExecutionId = moduleExecutionId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAttributeDbTime() {
        return this.attributeDbTime;
    }

    public void setAttributeDbTime(Double attributeDbTime) {
        this.attributeDbTime = attributeDbTime;
    }

    public boolean isVirtualMex() {
        return this.virtualMex;
    }

    public void setVirtualMex(boolean virtualMex) {
        this.virtualMex = virtualMex;
    }

    public String getInputTag() {
        return this.inputTag;
    }

    public void setInputTag(String inputTag) {
        this.inputTag = inputTag;
    }

    public String getIteratorTag() {
        return this.iteratorTag;
    }

    public void setIteratorTag(String iteratorTag) {
        this.iteratorTag = iteratorTag;
    }

    public String getDependence() {
        return this.dependence;
    }

    public void setDependence(String dependence) {
        this.dependence = dependence;
    }

    public Double getAttributeSortTime() {
        return this.attributeSortTime;
    }

    public void setAttributeSortTime(Double attributeSortTime) {
        this.attributeSortTime = attributeSortTime;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getAttributeCreateTime() {
        return this.attributeCreateTime;
    }

    public void setAttributeCreateTime(Double attributeCreateTime) {
        this.attributeCreateTime = attributeCreateTime;
    }

    public String getNewFeatureTag() {
        return this.newFeatureTag;
    }

    public void setNewFeatureTag(String newFeatureTag) {
        this.newFeatureTag = newFeatureTag;
    }

    public Double getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime(Double totalTime) {
        this.totalTime = totalTime;
    }

    public org.ome.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.omero.model.Image image) {
        this.image = image;
    }

    public org.ome.omero.model.Dataset getDataset() {
        return this.dataset;
    }

    public void setDataset(org.ome.omero.model.Dataset dataset) {
        this.dataset = dataset;
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

    public Set getCategories() {
        return this.categories;
    }

    public void setCategories(Set categories) {
        this.categories = categories;
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

    public Set getCategoryGroups() {
        return this.categoryGroups;
    }

    public void setCategoryGroups(Set categoryGroups) {
        this.categoryGroups = categoryGroups;
    }

    public Set getImageAnnotations() {
        return this.imageAnnotations;
    }

    public void setImageAnnotations(Set imageAnnotations) {
        this.imageAnnotations = imageAnnotations;
    }

    public Set getDatasetAnnotations() {
        return this.datasetAnnotations;
    }

    public void setDatasetAnnotations(Set datasetAnnotations) {
        this.datasetAnnotations = datasetAnnotations;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("moduleExecutionId", getModuleExecutionId())
            .toString();
    }

}
