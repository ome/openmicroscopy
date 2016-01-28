/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.util.Iterator;
import java.util.List;

import omero.model.Dichroic;
import omero.model.FilterSetEmissionFilterLink;
import omero.model.FilterSetExcitationFilterLink;
import omero.RString;
import omero.model.FilterSet;

/** 
 * Object hosting a filterSet.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FilterSetData
    extends DataObject
{

    /** The collection of emission filters. */
    private List<FilterData> emissionFilters;

    /** The collection of excitation filters. */
    private List<FilterData> excitationFilters;

    /**
     * Creates a new instance.
     *
     * @param filterSet The filterSet to host. Mustn't be <code>null</code>.
     */
    public FilterSetData(FilterSet filterSet)
    {
        if (filterSet == null)
            throw new IllegalArgumentException("filterSet cannot null.");
        setValue(filterSet);
    }

    /**
     * Returns the manufacturer.
     *
     * @return See above.
     */
    public String getManufacturer()
    {
        FilterSet f = (FilterSet) asIObject();
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
        FilterSet f = (FilterSet) asIObject();
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
        FilterSet f = (FilterSet) asIObject();
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
        FilterSet f = (FilterSet) asIObject();
        RString value = f.getSerialNumber();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Returns the collections of emission filters if any.
     *
     * @return See above.
     */
    public List<FilterData> getEmissionFilters()
    {
        FilterSet f = (FilterSet) asIObject();
        if (emissionFilters == null && f.sizeOfEmissionFilterLink() > 0) {
            emissionFilters = new ArrayList<FilterData>();
            List<FilterSetEmissionFilterLink> l = f.copyEmissionFilterLink();
            Iterator<FilterSetEmissionFilterLink> i = l.iterator();
            while (i.hasNext()) {
                emissionFilters.add(new FilterData(i.next().getChild()));
            }
        }
        return emissionFilters;
    }

    /**
     * Returns the collections of excitation filters if any.
     *
     * @return See above.
     */
    public List<FilterData> getExcitationFilters()
    {
        FilterSet f = (FilterSet) asIObject();
        if (excitationFilters == null && f.sizeOfExcitationFilterLink() > 0) {
            excitationFilters = new ArrayList<FilterData>();
            List<FilterSetExcitationFilterLink> l = f.copyExcitationFilterLink();
            Iterator<FilterSetExcitationFilterLink> i = l.iterator();
            while (i.hasNext()) {
                excitationFilters.add(new FilterData(i.next().getChild()));
            }
        }
        return excitationFilters;
    }

    /**
     * Returns the dichroic if any.
     *
     * @return See above.
     */
    public DichroicData getDichroic()
    {
        FilterSet f = (FilterSet) asIObject();
        Dichroic d = f.getDichroic();
        if (d == null) return null;
        return new DichroicData(d);
    }

}
