package org.ome.omero.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class DisplayOption implements Serializable {

    /** identifier field */
    private Integer attributeId;

    /** nullable persistent field */
    private Boolean redOn;

    /** nullable persistent field */
    private Integer ZStart;

    /** nullable persistent field */
    private Boolean blueOn;

    /** nullable persistent field */
    private Integer greyChannel;

    /** nullable persistent field */
    private Integer greenChannel;

    /** nullable persistent field */
    private String colorMap;

    /** nullable persistent field */
    private Integer redChannel;

    /** nullable persistent field */
    private Integer ZStop;

    /** nullable persistent field */
    private Float zoom;

    /** nullable persistent field */
    private Integer blueChannel;

    /** nullable persistent field */
    private Integer pixels;

    /** nullable persistent field */
    private Integer TStop;

    /** nullable persistent field */
    private Integer TStart;

    /** nullable persistent field */
    private Boolean greenOn;

    /** nullable persistent field */
    private Boolean displayRgb;

    /** persistent field */
    private org.ome.omero.model.Image image;

    /** persistent field */
    private org.ome.omero.model.ModuleExecution moduleExecution;

    /** full constructor */
    public DisplayOption(Integer attributeId, Boolean redOn, Integer ZStart, Boolean blueOn, Integer greyChannel, Integer greenChannel, String colorMap, Integer redChannel, Integer ZStop, Float zoom, Integer blueChannel, Integer pixels, Integer TStop, Integer TStart, Boolean greenOn, Boolean displayRgb, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
        this.attributeId = attributeId;
        this.redOn = redOn;
        this.ZStart = ZStart;
        this.blueOn = blueOn;
        this.greyChannel = greyChannel;
        this.greenChannel = greenChannel;
        this.colorMap = colorMap;
        this.redChannel = redChannel;
        this.ZStop = ZStop;
        this.zoom = zoom;
        this.blueChannel = blueChannel;
        this.pixels = pixels;
        this.TStop = TStop;
        this.TStart = TStart;
        this.greenOn = greenOn;
        this.displayRgb = displayRgb;
        this.image = image;
        this.moduleExecution = moduleExecution;
    }

    /** default constructor */
    public DisplayOption() {
    }

    /** minimal constructor */
    public DisplayOption(Integer attributeId, org.ome.omero.model.Image image, org.ome.omero.model.ModuleExecution moduleExecution) {
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

    public Boolean getRedOn() {
        return this.redOn;
    }

    public void setRedOn(Boolean redOn) {
        this.redOn = redOn;
    }

    public Integer getZStart() {
        return this.ZStart;
    }

    public void setZStart(Integer ZStart) {
        this.ZStart = ZStart;
    }

    public Boolean getBlueOn() {
        return this.blueOn;
    }

    public void setBlueOn(Boolean blueOn) {
        this.blueOn = blueOn;
    }

    public Integer getGreyChannel() {
        return this.greyChannel;
    }

    public void setGreyChannel(Integer greyChannel) {
        this.greyChannel = greyChannel;
    }

    public Integer getGreenChannel() {
        return this.greenChannel;
    }

    public void setGreenChannel(Integer greenChannel) {
        this.greenChannel = greenChannel;
    }

    public String getColorMap() {
        return this.colorMap;
    }

    public void setColorMap(String colorMap) {
        this.colorMap = colorMap;
    }

    public Integer getRedChannel() {
        return this.redChannel;
    }

    public void setRedChannel(Integer redChannel) {
        this.redChannel = redChannel;
    }

    public Integer getZStop() {
        return this.ZStop;
    }

    public void setZStop(Integer ZStop) {
        this.ZStop = ZStop;
    }

    public Float getZoom() {
        return this.zoom;
    }

    public void setZoom(Float zoom) {
        this.zoom = zoom;
    }

    public Integer getBlueChannel() {
        return this.blueChannel;
    }

    public void setBlueChannel(Integer blueChannel) {
        this.blueChannel = blueChannel;
    }

    public Integer getPixels() {
        return this.pixels;
    }

    public void setPixels(Integer pixels) {
        this.pixels = pixels;
    }

    public Integer getTStop() {
        return this.TStop;
    }

    public void setTStop(Integer TStop) {
        this.TStop = TStop;
    }

    public Integer getTStart() {
        return this.TStart;
    }

    public void setTStart(Integer TStart) {
        this.TStart = TStart;
    }

    public Boolean getGreenOn() {
        return this.greenOn;
    }

    public void setGreenOn(Boolean greenOn) {
        this.greenOn = greenOn;
    }

    public Boolean getDisplayRgb() {
        return this.displayRgb;
    }

    public void setDisplayRgb(Boolean displayRgb) {
        this.displayRgb = displayRgb;
    }

    public org.ome.omero.model.Image getImage() {
        return this.image;
    }

    public void setImage(org.ome.omero.model.Image image) {
        this.image = image;
    }

    public org.ome.omero.model.ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }

    public void setModuleExecution(org.ome.omero.model.ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("attributeId", getAttributeId())
            .toString();
    }

}
