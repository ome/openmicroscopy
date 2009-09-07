/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.measurements;

import java.util.Map;

import ome.model.IObject;
import omero.ServerError;
import omero.ValidationException;
import omero.grid.Column;

/**
 * Wrapper around an array of columns for facilitating instantiation and
 * mutation.
 * 
 * @since Beta4.1
 */
class SmartColumns {

    Map<String, IObject> lsidMap;
    String[] idTypes;
    Column[] cols;
    Helper[] helpers;

    SmartColumns(Map<String, IObject> lsidMap, String[] headers,
            String[] idTypes, Class[] types) throws ServerError {

        if (types == null || headers == null) {
            throw new omero.ApiUsageException(null, null,
                    "Required parameter is null");
        }

        if ((types.length + idTypes.length) != headers.length) {
            throw new omero.ApiUsageException(null, null, String.format(
                    "types.length (%s) != headers.length (%s)", types.length,
                    headers.length));
        }

        this.lsidMap = lsidMap;
        this.idTypes = idTypes;
        cols = new Column[headers.length];
        helpers = new Helper[headers.length];

        for (int i = 0; i < idTypes.length; i++) {
            if (idTypes[i].contains("Roi")) {
                helpers[i] = Helper.fromLsid(idTypes[i]);
                cols[i] = helpers[i].newInstance();
            } else {
                throw new ValidationException(null, null, "Unknown lsid type: "
                        + idTypes[i]);
            }
            cols[i].name = headers[i];
        }

        for (int i = 0; i < types.length; i++) {
            int j = i + idTypes.length;
            helpers[j] = Helper.fromClass(types[i]);
            cols[j] = helpers[j].newInstance();
            cols[j].name = headers[j];
        }

    }

    public void fill(Object[][] data) {

        //
        // Now that we have ids for all of the objects, we can parse the
        // data[][] into columns and pass to the table instance.
        //

        // Initialize the columns
        for (int colIdx = 0; colIdx < cols.length; colIdx++) {
            Helper helper = helpers[colIdx];
            Column col = cols[colIdx];
            helper.setSize(col, data.length);
        }

        for (int rowIdx = 0; rowIdx < data.length; rowIdx++) {
            Object[] row = data[rowIdx];
            // LSIDS
            for (int colIdx = 0; colIdx < idTypes.length; colIdx++) {
                Column col = cols[colIdx];
                Helper helper = helpers[colIdx];
                String lsid = (String) data[rowIdx][colIdx];
                IObject obj = lsidMap.get(lsid);
                Object value = null;
                if (obj != null) {
                    value = obj.getId();
                }
                helper.setValue(col, rowIdx, value);
            }

            // VALUES
            for (int colIdx = idTypes.length; colIdx < cols.length; colIdx++) {
                Column col = cols[colIdx];
                Helper helper = helpers[colIdx];
                helper.setValue(col, rowIdx, data[rowIdx][colIdx]);
            }
        }

    }

    public Column[] asArray() {
        return cols;
    }
}