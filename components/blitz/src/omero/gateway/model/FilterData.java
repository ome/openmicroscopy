/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import ome.model.units.BigResult;
import omero.RDouble;
import omero.RString;
import omero.model.Filter;
import omero.model.FilterType;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.TransmittanceRange;
import omero.model.enums.UnitsLength;

/**
 * Object hosting a filter.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FilterData
    extends DataObject
{

    /**
     * Creates a new instance.
     *
     * @param filter The filter to host. Mustn't be <code>null</code>.
     */
    public FilterData(Filter filter)
    {
        if (filter == null)
            throw new IllegalArgumentException("Filter cannot null.");
        setValue(filter);
    }

    /**
     * Returns the cut in value or <code>null</code>.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getCutIn(UnitsLength unit) throws BigResult
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null)
            return null;
        Length l = range.getCutIn();
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Returns the cut in value or <code>null</code>.
     *
     * @return See above.
     * @deprecated Replaced by {@link #getCutIn(UnitsLength)}
     */
    @Deprecated
    public Integer getCutIn()
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) return null;
        Length value = range.getCutIn();
        if (value == null) return null;
        return (int) value.getValue();
    }

    /**
     * Returns the cut in tolerance value or <code>null</code>.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getCutInTolerance(UnitsLength unit) throws BigResult
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) 
            return null;
        Length l = range.getCutInTolerance();
        if (l==null)
            return null;
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Returns the cut in tolerance value or <code>null</code>.
     *
     * @return See above.
     * @deprecated Replaced by {@link #getCutInTolerance(UnitsLength)}
     */
    @Deprecated
    public Integer getCutInTolerance()
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) return null;
        Length value = range.getCutInTolerance();
        if (value == null) return null;
        return (int) value.getValue();
    }

    /**
     * Returns the cut out value or <code>null</code>.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getCutOut(UnitsLength unit) throws BigResult
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null)
            return null;
        Length l = range.getCutOut();
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Returns the cut out value or <code>null</code>.
     *
     * @return See above.
     * @deprecated Replaced by {@link #getCutOut(UnitsLength)}
     */
    @Deprecated
    public Integer getCutOut()
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) return null;
        Length value = range.getCutOut();
        if (value == null) return null;
        return (int) value.getValue();
    }

    /**
     * Returns the cut out tolerance value or <code>null</code>.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getCutOutTolerance(UnitsLength unit) throws BigResult
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) 
            return null;
        Length l = range.getCutOutTolerance();
        if (l==null)
            return null;
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Returns the cut out tolerance value or <code>null</code>.
     *
     * @return See above.
     * @deprecated Replaced by {@link #getCutOutTolerance(UnitsLength)}
     */
    @Deprecated
    public Integer getCutOutTolerance()
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) return null;
        Length value = range.getCutOutTolerance();
        if (value == null) return null;
        return (int) value.getValue();
    }

    /**
     * Returns the cut out tolerance value or <code>-1</code>.
     *
     * @return See above.
     */
    public Double getTransmittance()
    {
        Filter f = (Filter) asIObject();
        TransmittanceRange range = f.getTransmittanceRange();
        if (range == null) return null;
        RDouble value = range.getTransmittance();
        if (value == null) return null;
        return value.getValue();
    }

    /**
     * Returns the manufacturer.
     *
     * @return See above.
     */
    public String getManufacturer()
    {
        Filter f = (Filter) asIObject();
        RString value = f.getManufacturer();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the model.
     *
     * @return See above.
     */
    public String getModel()
    {
        Filter f = (Filter) asIObject();
        RString value = f.getModel();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the lot number.
     *
     * @return See above.
     */
    public String getLotNumber()
    {
        Filter f = (Filter) asIObject();
        RString value = f.getLotNumber();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the serial number.
     *
     * @return See above.
     */
    public String getSerialNumber()
    {
        Filter f = (Filter) asIObject();
        RString value = f.getSerialNumber();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the type. One out of a predefined list.
     *
     * @return See above.
     */
    public String getType()
    {
        Filter f = (Filter) asIObject();
        FilterType type = f.getType();
        if (type == null) return "";
        return type.getValue().getValue();
    }

    /**
     * Returns the wheel.
     *
     * @return See above.
     */
    public String getFilterWheel()
    {
        Filter f = (Filter) asIObject();
        RString value = f.getFilterWheel();
        if (value == null) return "";
        return value.getValue();
    }

}
