/*
 * pojos.WellData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package pojos;



//Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.util.CBlock;

/** 
 * The data that makes up an <i>OME</i> Well 
 * and the Experimenter that owns this Well.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class WellData 
	extends DataObject
{

    /** Identifies the {@link Well#WELLSAMPLE} field. */
    //public final static String WELL_SAMPLE_LINKS = Well.WELLSAMPLE;
    
    /**
     * All the well samples that are linked to this Well.
     * The elements of this set are {@link WellSampleData} objects. 
     * If this Well is not linked to any Well Sample.
     * then this set will be empty &#151; but never <code>null</code>.
     */
    private List<WellSampleData>    wellSamples;
    
    /** The {@link PlateData plate} containing this well. */
    private PlateData 				plate;
    
	/** Creates a new instance. */
    public WellData()
    {
        setDirty(true);
        setValue(new Well());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param well Back pointer to the {@link Well} model object. 
     * 			   Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public WellData(Well well)
    {
        if (well == null) 
            throw new IllegalArgumentException("Object cannot null.");
        setValue(well);
    }
    
    /**
     * Returns the external description of the well.
     * 
     * @return See above.
     */
    public String getExternalDescription()
    {
        return asWell().getExternalDescription();
    }
    
    /**
     * Returns a human readable identifier for the screening status
     * e.g. empty, positive control etc.
     * 
     * @return See above.
     */
    public String getWellType() { return asWell().getType(); }
    
    /**
     * Sets the external description of the well.
     * 
     * @param description The description of the well.
     */
    public void setExternalDescription(String description)
    {
        setDirty(true);
        asWell().setExternalDescription(description);
    }
    
    /**
     * Returns the number of annotations linked to the object,
     * key: id of the user, value: number of annotation.
     * The map may be <code>null</code> if no annotation.
     * 
     * @return See above.
     */
    public Map<Long, Long> getAnnotationsCounts()
    {
    	return asWell().getAnnotationLinksCountPerOwner(); 
    }
    
    /**
     * Returns the number of reagents linked to the object,
     * key: id of the user, value: number of annotation.
     * The map may be <code>null</code> if no annotation.
     * 
     * @return See above.
     */
    public Map<Long, Long> getReagentsCounts()
    {
    	return asWell().getReagentLinksCountPerOwner();
    }
    
    /**
     * Returns the column used to indicate the location of the well on the grid.
     * 
     * @return See above.
     */
    public Integer getColumn() { return asWell().getColumn(); }
    
    /**
     * Returns the row used to indicate the location of the well on the grid.
     * 
     * @return See above.
     */
    public Integer getRow() { return asWell().getRow(); }
    
    /**
     * Returns the plate containing this Well.
     * 
     * @return See above.
     */
    public PlateData getPlate() 
    {
    	if (plate == null) plate = new PlateData(asWell().getPlate());
    	return plate;
    }
    
//  Lazy loaded Links
    /**
     * Returns the well samples linked to the well.
     * 
     * @return See above.
     */
    public List<WellSampleData> getWellSamples()
    {
        if (wellSamples == null && asWell().sizeOfWellSamples() >= 0) {
        	wellSamples = new ArrayList<WellSampleData>(
        			asWell().collectWellSamples(
        			new CBlock<WellSampleData>() {
                public WellSampleData call(IObject object) {
                    return new WellSampleData((WellSample) object);
                }
            }));
        }
        return wellSamples == null ? null : 
        	new ArrayList<WellSampleData>(wellSamples);
    }
    
//  Link mutations

    /**
     * Sets the well samples linked to the well.
     * 
     * @param value The set of well samples.
     */
    public void setWellSamples(List<WellSampleData> value)
    {
        List<WellSampleData> currentValue = getWellSamples();
        SetMutator<WellSampleData> 
        	m = new SetMutator<WellSampleData>(currentValue, value);

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
