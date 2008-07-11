/*
 * pojos.ScreenData 
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
 * The data that makes up an <i>OME</i> Screen along with links to its
 * contained Plates and the Experimenter that owns this Screen.
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
public class ScreenData 
	extends DataObject
{

	/** Identifies the {@link Screen#NAME} field. */
    public final static String NAME = Screen.NAME;

    /** Identifies the {@link Screen#DESCRIPTION} field. */
    public final static String DESCRIPTION = Screen.DESCRIPTION;

    /** Identifies the {@link Screen#PLATELINKS} field. */
    public final static String PLATE_LINKS = Screen.PLATELINKS;
    
    /**
     * All the Plates that are contained in this Project.
     * The elements of this set are {@link PlateData} objects. 
     * If this Project does contained in any Dataset, 
     * then this set will be empty &#151; but never <code>null</code>.
     */
    private Set plates;

    /** Creates a new instance. */
    public ScreenData()
    {
        setDirty(true);
        setValue(new Screen());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param screen Back pointer to the {@link Screen} model object. 
     * 				 Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public ScreenData(Screen screen)
    {
        if (screen == null) 
            throw new IllegalArgumentException("Object cannot null.");
        setValue(screen);
    }
    
    /**
     * Sets the name of the screen.
     * 
     * @param name The name of the screen. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name)
    {
        if (name == null)
            throw new IllegalArgumentException("The name cannot be null.");
        setDirty(true);
        asScreen().setName(name);
    }

    /**
     * Returns the name of the screen.
     * 
     * @return See above.
     */
    public String getName() { return asScreen().getName(); }

    /**
     * Sets the description of the screen.
     * 
     * @param description The description of the screen.
     */
    public void setDescription(String description)
    {
        setDirty(true);
        asScreen().setDescription(description);
    }

    /**
     * Returns the description of the screen.
     * 
     * @return See above.
     */
    public String getDescription() { return asScreen().getDescription(); }

    /**
     * Returns the number of annotations linked to the object,
     * key: id of the user, value: number of annotation.
     * The map may be <code>null</code> if no annotation.
     * 
     * @return See above.
     */
    public Map<Long, Long> getAnnotationsCounts()
    {
    	return asScreen().getAnnotationLinksCountPerOwner(); 
    }
    
    // Lazy loaded Links
    /**
     * Returns the plates contained in this screen.
     * 
     * @return See above.
     */
    public Set<PlateData> getPlates()
    {
        if (plates == null && asScreen().sizeOfPlateLinks() >= 0) {
        	plates = new HashSet<PlateData>(asScreen().eachLinkedPlate(
        			new CBlock<PlateData>() {
                public PlateData call(IObject object) {
                    return new PlateData((Plate) object);
                }
            }));
        }
        return plates == null ? null : new HashSet<PlateData>(plates);
    }

    // Link mutations

    /**
     * Sets the plates contained in this screen.
     * 
     * @param value The set of plates.
     */
    public void setPlates(Set<PlateData> value)
    {
        Set<PlateData> currentValue = getPlates();
        SetMutator<PlateData> 
        	m = new SetMutator<PlateData>(currentValue, value);

        while (m.moreDeletions()) {
            setDirty(true);
            asScreen().unlinkPlate(m.nextDeletion().asPlate());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asScreen().linkPlate(m.nextAddition().asPlate());
        }
        plates = new HashSet<PlateData>(m.result());
    }
    
}
