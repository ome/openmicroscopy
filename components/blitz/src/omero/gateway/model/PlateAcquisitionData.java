/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import static omero.rtypes.rstring;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;

/**
 * The data that makes up an <i>OME</i> PlateAcquisition.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PlateAcquisitionData
    extends DataObject
{

    /** The default text used when displaying the label.*/
    private static final String DEFAULT_TEXT = "Run ";

    /** The Id of the plate this plate acquisition is for. */
    private long refPlateId;

    /** Creates a new instance. */
    public PlateAcquisitionData()
    {
        setDirty(true);
        setValue(new PlateAcquisitionI());
        refPlateId = -1L;
    }

    /**
     * Creates a new instance.
     *
     * @param value Back pointer to the {@link Plate} model object. Mustn't be
     *            <code>null</code>.
     */
    public PlateAcquisitionData(PlateAcquisition value)
    {
        if (value == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(value);
        refPlateId = -1L;
    }

    /**
     * Returns the id of the plate of reference.
     *
     * @return See above.
     */
    public long getRefPlateId() { return refPlateId; }

    /**
     * Sets the id of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    public void setRefPlateId(long refPlateId) { this.refPlateId = refPlateId; }

    /**
     * Returns the name of the plate acquisition.
     *
     * @return See above.
     */
    public String getName()
    {
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        if (acq == null) return "";
        omero.RString n = acq.getName();
        if (n == null) return "";
        return n.getValue();
    }

    /**
     * Returns the description of the plate acquisition.
     *
     * @return See above.
     */
    public String getDescription()
    {
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        if (acq == null) return "";
        omero.RString n = acq.getDescription();
        if (n == null) return "";
        return n.getValue();
    }

    /**
     * Sets the name of the acquisition.
     *
     * @param name The value to set.
     */
    public void setName(String name)
    {
        if (name == null || name.length() == 0) return;
        setDirty(true);
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        acq.setName(rstring(name));
    }

    /**
     * Sets the name of the acquisition.
     *
     * @param description The value to set.
     */
    public void setDescription(String description)
    {
        if (StringUtils.isBlank(description)) return;
        setDirty(true);
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        acq.setDescription(rstring(description));
    }

    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    public Timestamp getStartTime()
    {
        PlateAcquisition sa = (PlateAcquisition) asIObject();
        if (sa == null) return null;
        omero.RTime time = sa.getStartTime();
        if (time == null) return null;
        return new Timestamp(time.getValue());
    }

    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    public Timestamp getEndTime()
    {
        PlateAcquisition sa = (PlateAcquisition) asIObject();
        if (sa == null) return null;
        omero.RTime time = sa.getEndTime();
        if (time == null) return null;
        return new Timestamp(time.getValue());
    }

    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    public String getLabel()
    {
        String name = getName();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        Timestamp time = getStartTime();
        String start = "";
        String end = "";
        if (time != null) {
            start = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT).format(time);
        }

        String[] values = start.split(" ");
        String date = "";
        String dateEnd = "";
        if (values.length > 1) {
            date = values[0];
            start = start.substring(date.length()+1);
        } 
        time = getEndTime();
        if (time != null) {
            end = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT).format(time);
        }

        values = end.split(" ");
        if (values.length > 1) {
            if (!date.equals(values[0]))
                dateEnd = values[0];
            end = end.substring(values[0].length()+1);
        }
        String value = "";
        if (start.length() == 0 && end.length() == 0)
            return DEFAULT_TEXT+getId();
        if (date.length() == 0 && end.length() != 0)
            return dateEnd+" "+end;
        if (dateEnd.length() == 0) {
            value = date+" "+start;
            if (end.length() > 0) value += " - "+end;
        } else {
            value = date+" "+start+" - "+dateEnd+" "+end;
        }
        if (value.length() > 0) return value;
        return DEFAULT_TEXT+getId();
    }

    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    public int getMaximumFieldCount()
    {
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        if (acq == null) return -1;
        omero.RInt n = acq.getMaximumFieldCount();
        if (n == null) return -1;
        return n.getValue();
    }

    /**
     * Returns the number of annotations linked to the object, key: id of the
     * user, value: number of annotation. The map may be <code>null</code> if
     * no annotation.
     *
     * @return See above.
     */
    public Map<Long, Long> getAnnotationsCounts()
    {
        PlateAcquisition acq = (PlateAcquisition) asIObject();
        if (acq == null) return null;
        return acq.getAnnotationLinksCountPerOwner();
    }

}
