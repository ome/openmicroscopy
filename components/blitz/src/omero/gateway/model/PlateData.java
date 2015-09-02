/*
 * pojos.PlateData 
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
package omero.gateway.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static omero.rtypes.*;
import ome.model.units.BigResult;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateI;
import omero.model.ScreenPlateLink;
import omero.model.enums.UnitsLength;

/**
 * The data that makes up an <i>OME</i> Plate along with links to its contained
 * Well and enclosing Screen as well as the Experimenter that owns this Plate.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since 3.0-Beta3
 */
public class PlateData extends DataObject {

	/** Indicates that the column or row is a number starting from 1. */
	public final static int	   ASCENDING_NUMBER = 0;
	
	/** Indicates that the column or row is a letter starting from A. */
	public final static int	   ASCENDING_LETTER = 1;
	
	/** Indicates that the column or row is a letter starting from 26 or 16. */
	public final static int	   DESCENDING_NUMBER = 2;
	
	/** Indicates that the column or row is a letter starting from Z or P. */
	public final static int	   DESCENDING_LETTER = 3;
	
    /** Identifies the {@link Plate#NAME} field. */
    public final static String NAME = PlateI.NAME;

    /** Identifies the {@link Plate#DESCRIPTION} field. */
    public final static String DESCRIPTION = PlateI.DESCRIPTION;

    /** Identifies the {@link Plate#WELLS} field. */
    public final static String WELLS = PlateI.WELLS;

    /** Identifies the {@link Plate#SCREENLINKS} field. */
    public final static String SCREEN_LINKS = PlateI.SCREENLINKS;

    /** Identifies the {@link Plate#ANNOTATIONLINKS} field. */
    public final static String ANNOTATIONS = PlateI.ANNOTATIONLINKS;

    /** Indicates that the convention is to use <code>letter</code>. */
    private final static String LETTER_CONVENTION = "letter";
    
    /** Indicates that the convention is to use <code>number</code>. */
    private final static String NUMBER_CONVENTION = "number";
    
    /**
     * All the Screens that contain this Plate. The elements of this set are
     * {@link ScreenData} objects. If this Plate is not contained in any
     * Screen, then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<ScreenData> screens;

    /**
     * All the Plate Acquisition related to this Plate. The elements of this
     * set are {@link PlateAcquisitionData} objects.
     * If this Plate does contained in any PlateAcquisition, 
     * then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<PlateAcquisitionData> plateAcquisitions;

    /**
     * Returns the index corresponding to the passed value.
     * 
     * @param value The value to handle.
     * @return See above.
     */
    private int getSequenceIndex(String value)
    {
    	if (LETTER_CONVENTION.equals(value)) return ASCENDING_LETTER;
    	else if (NUMBER_CONVENTION.equals(value)) return ASCENDING_NUMBER;
    	//TODO
    	return -1;
    }
    
    /** Creates a new instance. */
    public PlateData()
    {
        setDirty(true);
        setValue(new PlateI());
    }

    /**
     * Creates a new instance.
     * 
     * @param plate Back pointer to the {@link Plate} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public PlateData(Plate plate)
    {
        if (plate == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(plate);
    }

    // IMMUTABLES

    /**
     * Sets the name of the plate.
     * 
     * @param name The name of the plate. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asPlate().setName(rstring(name));
    }

    /**
     * Returns the name of the plate.
     * 
     * @return See above.
     */
    public String getName()
    {
        omero.RString n = asPlate().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have bee null.");
        }
        return n.getValue();
    }

    /**
     * Sets the description of the plate.
     * 
     * @param description The description of the plate.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asPlate().setDescription(rstring(description));
    }

    /**
     * Returns the description of the plate.
     * 
     * @return See above.
     */
    public String getDescription()
    {
        omero.RString d = asPlate().getDescription();
        return d == null ? null : d.getValue();
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
        return asPlate().getAnnotationLinksCountPerOwner();
    }

    /**
     * Returns a set of screens containing the plate.
     * 
     * @return See above.
     */
    public Set<ScreenData> getScreens() 
    {
        if (screens == null && asPlate().sizeOfScreenLinks() >= 0) {
            screens = new HashSet<ScreenData>();
            List<ScreenPlateLink> links = asPlate().copyScreenLinks();
            for (ScreenPlateLink link : links) {
                screens.add(new ScreenData(link.getParent()));
            }
        }

        return screens == null ? null : new HashSet<ScreenData>(screens);
    }

    /**
     * Sets the screens containing the plate.
     * 
     * @param value The set of screens.
     */
    public void setScreens(Set<ScreenData> value) {
        Set<ScreenData> currentValue = getScreens();
        SetMutator<ScreenData> m = new SetMutator<ScreenData>(currentValue,
                value);

        while (m.moreDeletions()) {
            setDirty(true);
            asPlate().unlinkScreen(m.nextDeletion().asScreen());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asPlate().linkScreen(m.nextAddition().asScreen());
        }

        screens = new HashSet<ScreenData>(m.result());
    }
    
    /**
     * Returns the index indicating how to label a column.
     * 
     * @return See above.
     */
    public int getColumnSequenceIndex()
    {
    	omero.RString value = asPlate().getColumnNamingConvention();
    	if (value == null) return ASCENDING_NUMBER;
    	String v = value.getValue();
    	if (v == null) return ASCENDING_NUMBER;
    	int index =  getSequenceIndex(value.getValue().toLowerCase());
    	if (index == -1) return ASCENDING_NUMBER;
    	return index;
    }
    
    /**
     * Returns the index indicating how to label a row.
     * 
     * @return See above.
     */
    public int getRowSequenceIndex()
    {
    	omero.RString value = asPlate().getRowNamingConvention();
    	if (value == null) return ASCENDING_LETTER;
    	String v = value.getValue();
    	if (v == null) return ASCENDING_LETTER;
    	int index =  getSequenceIndex(v.toLowerCase());
    	if (index == -1) return ASCENDING_LETTER;
    	return index;
    }
    
    /**
     * Returns the currently selected field or <code>0</code>.
     * 
     * @return See above.
     */
    public int getDefaultSample()
    {
    	omero.RInt value = asPlate().getDefaultSample();
    	if (value == null) return 0;
    	return value.getValue();
    }
    
    /**
     * Sets the default sample.
     * 
     * @param value The value to set.
     */
    public void setDefaultSample(int value)
    {
    	if (value < 0) value = 0;
    	asPlate().setDefaultSample(omero.rtypes.rint(value));
    }
    
    /** 
     * Returns the status of the plate.
     * 
     * @return See above.
     */
    public String getStatus()
    {
    	omero.RString value = asPlate().getStatus();
    	if (value == null) return "";
    	return value.getValue();
    }
    
    /** 
     * Returns the external identifier of the plate.
     * 
     * @return See above.
     */
    public String getExternalIdentifier()
    {
    	omero.RString value = asPlate().getExternalIdentifier();
    	if (value == null) return "";
    	return value.getValue();
    }
    
    /** 
     * Returns the type of plate e.g. A 384-Well Plate, 96-Well Plate.
     * 
     * @return See above.
     */
    public String getPlateType()
    {
    	Plate plate = asPlate();
    	int c = 0;
    	int r = 0;
    	omero.RInt v = plate.getColumns();
    	if (v != null) c = v.getValue();
    	v = plate.getRows();
    	if (v != null) r = v.getValue();
    	int value = c*r;
    	if (value <= 0) return "";
    	return value+"-Well Plate";
    }
    
    /**
     * Returns the x-coordinate in 2D-space of the well.
     * 
     * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
     * @return See above
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getWellOriginX(UnitsLength unit) throws BigResult
    {
    	Length value = asPlate().getWellOriginX();
    	if (value == null) 
    		return new LengthI(0, UnitsLength.REFERENCEFRAME);
    	else 
    		return unit == null ? value : new LengthI(value, unit);
    }
    
    /**
     * Returns the x-coordinate in 2D-space of the well.
     * 
     * @return See above
     * @deprecated Replaced by {@link #getWellOriginX(UnitsLength)}
     */
    @Deprecated
    public double getWellOriginX()
    {
    	Length value = asPlate().getWellOriginX();
    	if (value == null) return 0;
    	return value.getValue();
    }
    
    /**
     * Returns the y-coordinate in 2D-space of the well.
     * 
     * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
     * @return See above
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getWellOriginY(UnitsLength unit) throws BigResult
    {
    	Length value = asPlate().getWellOriginY();
    	if (value == null)
    		return new LengthI(0, UnitsLength.REFERENCEFRAME);
    	else
    		return unit == null ? value : new LengthI(value, unit);
    }
    
    /**
     * Returns the y-coordinate in 2D-space of the well.
     * 
     * @return See above
     * @deprecated Replaced by {@link #getWellOriginY(UnitsLength)}
     */
    @Deprecated
    public double getWellOriginY()
    {
    	Length value = asPlate().getWellOriginY();
    	if (value == null) return 0;
    	return value.getValue();
    }
    
    /**
     * Returns the plate acquisitions related to this plate.
     * 
     * @return See above.
     */
    public Set<PlateAcquisitionData> getPlateAcquisitions()
    {
        if (plateAcquisitions == null &&
        		asPlate().sizeOfPlateAcquisitions() >= 0) {
		plateAcquisitions = new HashSet<PlateAcquisitionData>();
            List<PlateAcquisition> links = asPlate().copyPlateAcquisitions();
            for (PlateAcquisition link : links) {
            	plateAcquisitions.add(new PlateAcquisitionData(link));
            }
        }
        return plateAcquisitions == null ? null :
		new HashSet<PlateAcquisitionData>(plateAcquisitions);
    }
    
    /**
     * Sets the plate acquisition linked to the plate.
     * 
     * @param value The set of plate acquisitions.
     */
    public void setPlateAcquisition(Set<PlateAcquisitionData> value)
    {
        Set<PlateAcquisitionData> currentValue = getPlateAcquisitions();
        SetMutator<PlateAcquisitionData> 
        	m = new SetMutator<PlateAcquisitionData>(currentValue, value);

        while (m.moreDeletions()) {
            setDirty(true);
            asPlate().removePlateAcquisition(
            		(PlateAcquisition) m.nextDeletion().asIObject());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asPlate().addPlateAcquisition(
            		(PlateAcquisition) m.nextAddition().asIObject());
        }
        plateAcquisitions = new HashSet<PlateAcquisitionData>(m.result());
    }
    
    /**
     * Sets the external identifier.
     * 
     * @param value The value to set.
     */
    public void setExternalIdentifier(String value)
    {
    	if (value != null && value.trim().length() != 0)
    		asPlate().setExternalIdentifier(omero.rtypes.rstring(value));
    }
    
    /**
     * Sets the status.
     * 
     * @param value The value to set.
     */
    public void setStatus(String value)
    {
    	if (value != null && value.trim().length() != 0)
    		asPlate().setStatus(omero.rtypes.rstring(value));
    }
    
}
