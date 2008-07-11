/*
 * pojos.PlateData 
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.util.CBlock;

/** 
 * The data that makes up an <i>OME</i> Plate along with links to its
 * contained Well and enclosing Screen as well as the Experimenter 
 * that owns this Plate.
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
public class PlateData 
	extends DataObject
{

	/** Identifies the {@link Plate#NAME} field. */
    public final static String NAME = Plate.NAME;

    /** Identifies the {@link Plate#DESCRIPTION} field. */
    public final static String DESCRIPTION = Plate.DESCRIPTION;

    /** Identifies the {@link Plate#WELLS} field. */
    public final static String WELLS = Plate.WELLS;

    /** Identifies the {@link Plate#SCREENLINKS} field. */
    public final static String SCREEN_LINKS = Plate.SCREENLINKS;

    /** Identifies the {@link Plate#ANNOTATIONLINKS} field. */
    public final static String ANNOTATIONS = Plate.ANNOTATIONLINKS;
    
    /**
     * All the Wells contained in this plate. The elements of this set are
     * {@link WellData} objects. If this Plate contains no Images, then this
     * set will be empty &#151; but never <code>null</code>.
     */
    private Set<ImageData> 		wells;

    /**
     * All the Screens that contain this Plate. The elements of this set are
     * {@link ProjectData} objects. If this Plate is not contained in any
     * Screen, then this set will be empty &#151; but never <code>null</code>.
     */
    private Set<ScreenData>		screens;

    /**
     * The number of annotations attached to this Plate. This field may be
     * <code>null</code> meaning no count retrieved, and it may be less than
     * the actual number if filtered by user.
     */
    private Long 				annotationCount;
    
    /** Creates a new instance. */
    public PlateData()
    {
        setDirty(true);
        setValue(new Plate());
    }

    /**
     * Creates a new instance.
     * 
     * @param plate Back pointer to the {@link Plate} model object. 
     * 				Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public PlateData(Plate plate)
    {
        if (plate == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(plate);
    }
    
    // IMMUTABLES

    /**
     * Sets the name of the plate.
     * 
     * @param name The name of the plate. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null) 
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asPlate().setName(name);
    }

    /**
     * Returns the name of the plate.
     * 
     * @return See above.
     */
    public String getName() { return asPlate().getName(); }

    /**
     * Sets the description of the plate.
     * 
     * @param description The description of the plate.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asPlate().setDescription(description);
    }

    /**
     * Returns the description of the plate.
     * 
     * @return See above.
     */
    public String getDescription() { return asPlate().getDescription(); }
    
    /**
     * Returns the number of annotations linked to the object,
     * key: id of the user, value: number of annotation.
     * The map may be <code>null</code> if no annotation.
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
    public Set getScreens()
    {
        if (screens == null && asPlate().sizeOfScreenLinks() >= 0) {
        	screens = new HashSet(asPlate().eachLinkedScreen(new CBlock() {
                public ScreenData call(IObject object) {
                    return new ScreenData((Screen) object);
                };
            }));
        }

        return screens == null ? null : new HashSet(screens);
    }
    
    /**
     * Sets the screens containing the plate.
     * 
     * @param value The set of screens.
     */
    public void setScreens(Set<ScreenData> value)
    {
        Set<ScreenData> currentValue = getScreens();
        SetMutator<ScreenData>
        	m = new SetMutator<ScreenData>(currentValue, value);

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
    
}
