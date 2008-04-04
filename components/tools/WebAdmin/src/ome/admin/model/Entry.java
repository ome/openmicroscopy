/*
 * ome.admin.model.Entry
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.model;

/**
 * It's model for {@link ome.admin.controller.ITypesEnumController}
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class Entry {

    /**
     * Long id
     */
    private Long id;

    /**
     * String value
     */
    private String value;

    /**
     * Gets id
     * 
     * @return Long
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets id
     * 
     * @param id -
     *            Long
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets value
     * 
     * @return {@link java.lang.String}
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets value
     * 
     * @param value -
     *            {@link java.lang.String}
     */
    public void setValue(String value) {
        this.value = value;
    }

}
