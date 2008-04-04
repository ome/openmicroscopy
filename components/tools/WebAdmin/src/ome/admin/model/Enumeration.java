/*
 * ome.admin.model.Enumeration
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.model;

// Java imports
import java.util.Collections;
import java.util.List;

// Application-internal dependencies
import ome.model.IEnum;

/**
 * It's model for {@link ome.admin.controller.ITypesEnumController}
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class Enumeration {

    /**
     * List provides objects extends IEnum
     */
    private List<? extends IEnum> enumList = Collections.EMPTY_LIST;

    /**
     * String provides name of class
     */
    private String className = null;

    /**
     * Original value
     */
    private boolean oryginalVales = true;

    /**
     * String provides value
     */
    private String event = null;

    /**
     * Gets event
     * 
     * @return {@link java.lang.String}
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets event
     * 
     * @param event
     *            {@link java.lang.String}
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Gets list of objects extends IEnum
     * 
     * @return {@link java.util.List}
     */
    public List<? extends IEnum> getEntryList() {
        return enumList;
    }

    /**
     * Sets list of objects extends IEnum
     * 
     * @param enumList
     *            {@link java.util.List}
     */
    public void setEntryList(List<? extends IEnum> enumList) {
        this.enumList = enumList;
    }

    /**
     * Gets class name
     * 
     * @return {@link java.lang.String}
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets class name
     * 
     * @param className
     *            {@link java.lang.String}
     */
    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isOriginalVales() {
        return oryginalVales;
    }

    public void setOriginalVales(boolean oryginalVales) {
        this.oryginalVales = oryginalVales;
    }

}
