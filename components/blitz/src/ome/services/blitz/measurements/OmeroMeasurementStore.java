/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.measurements;

import java.util.List;
import java.util.Map;

import ome.model.IObject;
import omero.grid.TablePrx;

/**
 * Sub-interface of {@link MeasurementStore} with extended life-cycle methods
 * for handling exceptions during saving the measurements to multiple stores.
 * 
 * @since Beta4.1
 */
public interface OmeroMeasurementStore {

    // Original API: should be moved to loci.*

    /**
     * 
     */
    public abstract void initialize(String[] headers, String[] idTypes,
            Class[] types, Map<String, Object> metadata) throws Exception;

    /**
     * Adds an array of rows (Object[]) to the store. These rows might be
     * indexed by an LSID which correlates to an added ROI. For each call to
     * {@link #addRows(Object[][])} a call to {@link #save()} must be made. This
     * allows a single measurement store to be filled with the values from
     * multiple measurement files. To differentiate between the various files,
     * include a File lsid column during the initialization phase.
     * 
     * @param rows
     * @throws Exception
     */
    public abstract void addRows(Object[][] rows) throws Exception;

    public abstract void addCircle(String roiLsid, double x, double y, double r)
            throws Exception;

    public abstract void save() throws Exception;

    // OMERO-specific API

    public abstract void addObject(String lsid, IObject object);

    public abstract void addObjects(Map<String, IObject> objects);

    /**
     * Returns the ids of all Roi instances created during the save method. If
     * Roi creation failed or if {@link #save()} has not been called, this will
     * return null.
     */
    public abstract List<Long> getRoiIds();

    /**
     * Returns the Table proxy which is in use by this service.
     * @return See above.
     */
    public abstract TablePrx getTable();

}