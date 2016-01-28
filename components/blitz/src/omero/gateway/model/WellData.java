/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static omero.rtypes.*;
import omero.RInt;
import omero.RString;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;

/**
 * The data that makes up an <i>OME</i> Well and the Experimenter that owns
 * this Well.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class WellData extends DataObject {

    /**
     * All the well samples that are linked to this Well. The elements of this
     * set are {@link WellSampleData} objects. If this Well is not linked to any
     * Well Sample. then this set will be empty &#151; but never
     * <code>null</code>.
     */
    private List<WellSampleData> wellSamples;

    /** The {@link PlateData plate} containing this well. */
    private PlateData plate;

    /** Creates a new instance. */
    public WellData() {
        setDirty(true);
        setValue(new WellI());
    }

    /**
     * Creates a new instance.
     *
     * @param well
     *            Back pointer to the {@link Well} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public WellData(Well well) {
        if (well == null) {
            throw new IllegalArgumentException("Object cannot be null.");
        }
        setValue(well);
    }

    /**
     * Returns the external description of the well.
     *
     * @return See above.
     */
    public String getExternalDescription() {
        RString d = asWell().getExternalDescription();
        return d == null ? null : d.getValue();
    }

    /**
     * Returns the status of the well.
     *
     * @return See above.
     */
    public String getStatus() {
        RString d = asWell().getStatus();
        return d == null ? null : d.getValue();
    }

    /**
     * Returns a human readable identifier for the screening status e.g. empty,
     * positive control, etc.
     *
     * @return See above.
     */
    public String getWellType() {
        RString t = asWell().getType();
        return t == null ? null : t.getValue();
    }

    /**
     * Sets a human readable identifier for the screening status e.g. empty,
     * positive control, etc.
     *
     * @param type The value to set.
     */
    public void setWellType(String type)
    {
        setDirty(true);
        asWell().setType(type == null ? null : rstring(type));
    }

    /**
     * Returns the red component of the color associated to the well,
     * or <code>-1</code>.
     *
     * @return See above.
     */
    public int getRed()
    {
        RInt value = asWell().getRed();
        if (value == null) return -1;
        return value.getValue();
    }

    /**
     * Sets the red component of the color associated to the well.
     *
     * @param red The value to set.
     */
    public void setRed(Integer red)
    {
        setDirty(true);
        if (red == null) {//reset the color
            asWell().setRed(null);
            asWell().setGreen(null);
            asWell().setBlue(null);
            asWell().setAlpha(null);
        } else {
            if (red < 0) red = 0;
            if (red > 255) red = 255;
            asWell().setRed(rint(red));
        }
    }

    /**
     * Returns the green component of the color associated to the well,
     * or <code>-1</code>.
     *
     * @return See above.
     */
    public int getGreen()
    {
        RInt value = asWell().getGreen();
        if (value == null) return -1;
        return value.getValue();
    }

    /**
     * Sets the green component of the color associated to the well.
     *
     * @param green The value to set.
     */
    public void setGreen(Integer green)
    {
        setDirty(true);
        if (green == null) {
            asWell().setRed(null);
            asWell().setGreen(null);
            asWell().setBlue(null);
            asWell().setAlpha(null);
        } else {
            if (green < 0) green = 0;
            if (green > 255) green = 255;
            asWell().setGreen(rint(green));
        }
    }

    /**
     * Returns the blue component of the color associated to the well,
     * or <code>-1</code>.
     *
     * @return See above.
     */
    public int getBlue()
    {
        RInt value = asWell().getBlue();
        if (value == null) return -1;
        return value.getValue();
    }

    /**
     * Sets the blue component of the color associated to the well.
     *
     * @param blue The value to set.
     */
    public void setBlue(Integer blue)
    {
        setDirty(true);
        if (blue == null) {
            asWell().setRed(null);
            asWell().setGreen(null);
            asWell().setBlue(null);
            asWell().setAlpha(null);
        } else {
            if (blue < 0) blue = 0;
            if (blue > 255) blue = 255;
            asWell().setBlue(rint(blue));
        }
    }

    /**
     * Returns the alpha component of the color associated to the well,
     * or <code>-1</code>.
     *
     * @return See above.
     */
    public int getAlpha()
    {
        RInt value = asWell().getAlpha();
        if (value == null) return -1;
        return value.getValue();
    }

    /**
     * Sets the alpha component of the color associated to the well.
     *
     * @param alpha The value to set.
     */
    public void setAlpha(Integer alpha)
    {
        setDirty(true);
        if (alpha == null) {
            asWell().setRed(null);
            asWell().setGreen(null);
            asWell().setBlue(null);
            asWell().setAlpha(null);
        } else {
            if (alpha < 0) alpha = 0;
            if (alpha > 255) alpha = 255;
            asWell().setAlpha(rint(alpha));
        }
    }

    /**
     * Sets the external description of the well.
     *
     * @param description
     *            The description of the well.
     */
    public void setExternalDescription(String description) {
        setDirty(true);
        asWell().setExternalDescription(
                description == null ? null : rstring(description));
    }

    /**
     * Sets the status of the well.
     *
     * @param status The status of the well.
     */
    public void setStatus(String status) {
        setDirty(true);
        asWell().setStatus(
                status == null ? null : rstring(status));
    }

    /**
     * Returns the number of annotations linked to the object, key: id of the
     * user, value: number of annotation. The map may be <code>null</code> if
     * no annotation.
     *
     * @return See above.
     */
    public Map<Long, Long> getAnnotationsCounts() {
        return asWell().getAnnotationLinksCountPerOwner();
    }

    /**
     * Returns the number of reagents linked to the object, key: id of the user,
     * value: number of annotation. The map may be <code>null</code> if no
     * annotation.
     *
     * @return See above.
     */
    public Map<Long, Long> getReagentsCounts() {
        return asWell().getReagentLinksCountPerOwner();
    }

    /**
     * Returns the column used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    public Integer getColumn() {
        RInt c = asWell().getColumn();
        return c == null ? null : c.getValue();
    }

    /**
     * Returns the row used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    public Integer getRow() {
        RInt r = asWell().getRow();
        return r == null ? null : r.getValue();
    }

    /**
     * Returns the plate containing this Well.
     *
     * @return See above.
     */
    public PlateData getPlate() {
        if (plate == null) {
            plate = new PlateData(asWell().getPlate());
        }
        return plate;
    }

    // Lazy loaded Links
    /**
     * Returns the well samples linked to the well.
     *
     * @return See above.
     */
    public List<WellSampleData> getWellSamples() {
        if (wellSamples == null && asWell().sizeOfWellSamples() >= 0) {
            wellSamples = new ArrayList<WellSampleData>();
            List<WellSample> samples = asWell().copyWellSamples();
            for (WellSample sample : samples) {
                if (sample != null)
                    wellSamples.add(new WellSampleData(sample));
            }
        }
        return wellSamples == null ? null : new ArrayList<WellSampleData>(
                wellSamples);
    }

    // Link mutations

    /**
     * Sets the well samples linked to the well.
     *
     * @param value
     *            The set of well samples.
     */
    public void setWellSamples(List<WellSampleData> value) {
        List<WellSampleData> currentValue = getWellSamples();
        SetMutator<WellSampleData> m = new SetMutator<WellSampleData>(
                currentValue, value);

        while (m.moreDeletions()) {
            setDirty(true);
            asWell().removeWellSample(m.nextDeletion().asWellSample());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asWell().addWellSample(m.nextAddition().asWellSample());
        }
        wellSamples = m.result();
    }

}
