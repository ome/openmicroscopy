/*
 * pojos.WellSampleData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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



// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.model.units.BigResult;
import omero.RTime;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import omero.model.enums.UnitsLength;

/**
 * The data that makes up an <i>OME</i> WellSample along with links to its
 * images and the Experimenter that owns this WellSample.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since 3.0-Beta3
 */
public class WellSampleData extends DataObject {

    /**
     * The image that is linked to this well sample. The element is a
     * {@link ImageData} object. If this well sample is not linked to any Image,
     * then this object will be <code>null</code>.
     */
    private ImageData image;

    /** Creates a new instance. */
    public WellSampleData() {
        setDirty(true);
        setValue(new WellSampleI());
       
    }

    /**
     * Creates a new instance.
     * 
     * @param wellSample
     *            Back pointer to the {@link WellSample} model object. Mustn't
     *            be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public WellSampleData(WellSample wellSample) {
        if (wellSample == null) {
            throw new IllegalArgumentException("Object cannot be null.");
        }
        setValue(wellSample);
    }

    /**
     * Returns the image related to that sample if any. Possible values are
     * <code>0</code> or <code>1</code>.
     * 
     * @return See above.
     */
    public ImageData getImage() {
        if (image == null) {
            image = new ImageData(asWellSample().getImage());
        }
        return image;
    }

    /**
     * Sets the image linked to this well sample.
     * 
     * @param newValue
     *            The image to set.
     */
    public void setImage(ImageData newValue) {
        if (newValue == null) {
            return;
        }
        setDirty(true);
        asWellSample().setImage(newValue.asImage());
    }

    /**
     * Returns the position X.
     * 
     * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred 
     */
    public Length getPositionX(UnitsLength unit) throws BigResult
    {
    	Length value = asWellSample().getPosX();
    	if (value == null) 
    		return new LengthI(0, UnitsLength.REFERENCEFRAME);
    	return unit == null ? value : new LengthI(value, unit);
    }	
    
    /**
     * Returns the position X.
     * 
     * @return See above.
     * @deprecated Replaced by {@link #getPositionX(UnitsLength)}
     */
    @Deprecated
    public double getPositionX()
    {
    	Length value = asWellSample().getPosX();
    	if (value == null) return 0;
    	return value.getValue();
    }
    
    /**
     * Returns the position Y.
     * 
     * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getPositionY(UnitsLength unit) throws BigResult
    {
    	Length value = asWellSample().getPosY();
    	if (value == null)
    		return new LengthI(0, UnitsLength.REFERENCEFRAME);
    	return unit == null ? value : new LengthI(value, unit);
    }
    
    /**
     * Returns the position Y.
     * 
     * @return See above.
     * @deprecated Replaced by {@link #getPositionY(UnitsLength)}
     */
    @Deprecated
    public double getPositionY()
    {
    	Length value = asWellSample().getPosY();
    	if (value == null) return 0;
    	return value.getValue();
    }
    
    /**
     * Returns the time at which the field was acquired.
     * 
     * @return See above.
     */
    public long getStartTime()
    {
    	RTime value = asWellSample().getTimepoint();
    	if (value == null) return 0;
    	return value.getValue();
    }

}
