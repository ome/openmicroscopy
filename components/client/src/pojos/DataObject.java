/*
 * pojos.DataObject
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package pojos;

//Java imports
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/** 
 * Abstract superclass for objects that hold <i>OMEDS</i> data.
 * Delegates getters and setters to <code>IObject</code> instances. Modifications
 * are propagated. <b>Not thread-safe.</b>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public abstract class DataObject
{

    public static Set asPojos( Set iObjects )
    {
        Set result = new HashSet();
        for (Iterator it = iObjects.iterator(); it.hasNext();)
        {
            IObject obj = (IObject) it.next();
            DataObject converted = asPojo( obj );
            result.add( converted );
        }
        return result;
    }

    public static Map asPojos( Map iObjects )
    {
        Map result = new HashMap( );
        for (Iterator it = iObjects.keySet().iterator(); it.hasNext();)
        {
            Object key = it.next();
            Object value = iObjects.get( key );
            
            DataObject convertedKey = null, convertedValue = null;
            if ( key instanceof IObject )
                convertedKey = asPojo( (IObject) iObjects.get( key ));

            if ( value instanceof IObject )
                convertedValue = asPojo( (IObject) iObjects.get( value ));
            
            result.put
                (
                        null == convertedKey ? key : convertedKey,
                        null == convertedValue ? value : convertedValue
                );
        }
        return result;
    }
    
    public static DataObject asPojo( IObject obj )
    {
        DataObject converted = null;
        if ( obj instanceof Project )
            converted = new ProjectData( (Project)  obj );
        else if ( obj instanceof Dataset )
            converted = new DatasetData( (Dataset)  obj );
        else if ( obj instanceof DatasetAnnotation )
            converted = new AnnotationData( (DatasetAnnotation)  obj );
        else if ( obj instanceof Image )
            converted = new ImageData( (Image)  obj );
        else if ( obj instanceof ImageAnnotation )
            converted = new AnnotationData( (ImageAnnotation)  obj );
        else if ( obj instanceof Image )
            converted = new CategoryGroupData( (CategoryGroup)  obj );
        else if ( obj instanceof Category )
            converted = new CategoryData( (Category)  obj );
        else if ( obj instanceof Pixels )
            converted = new PixelsData( (Pixels)  obj );
        else if ( obj instanceof Experimenter )
            converted = new ExperimenterData( (Experimenter)  obj );
        else if ( obj instanceof ExperimenterGroup )
            converted = new GroupData( (ExperimenterGroup)  obj );
        else
            if ( obj != null )
                throw new IllegalArgumentException(
                        "Unknown type: "+obj.getClass().getName());
        return converted;
    }
    
    /** delegate IObject */ 
    private IObject value = null; 

    /** lazily-loaded owner */
    private ExperimenterData owner = null;

    /** NOTE: IObject-views are mutable. we can't ensure non-dirtiness, 
     * only non-cleanness
     */
    private boolean dirty = false;

    protected void setValue( IObject value )
    {
        if ( value == null )
            throw new IllegalArgumentException( 
                    "IObject delegate for DataObject cannot be null.");
        
        this.value = value;
    }
    
    /** if true, setter value has modified the value of the stored IObject */
    public boolean isDirty()
    {
        return dirty;
    }

    protected void setDirty( boolean dirty )
    {
        this.dirty = dirty;
    }

    // Common properties
    
    /** database id of the IObject or -1 if null */
    public long getId()
    {
        return value.getId() == null ? -1 : value.getId().longValue();
    }
    
    public void setId(long id)
    {
        setDirty( true );
        value.setId( new Long( id ));
    }
    
    protected int getVersion()
    {
        if ( value instanceof IMutable)
        {
            IMutable m = (IMutable)  value;
            return m.getVersion() == null ? 0 : m.getVersion().intValue();
        } else {
            return 0;
        }
    }
    
    protected void setVersion( int version )
    {
        if ( value instanceof IMutable)
        {
            IMutable m = (IMutable)  value;
            setDirty( true );
            m.setVersion( new Integer( version ));
        }
    }
    
    public boolean isLoaded()
    {
        return value.isLoaded();
    }
    
    protected Set getFiltered()
    {
        return new HashSet( value.getDetails().filteredSet() );
    }
    
    public boolean isFiltered( String fieldName )
    {
        if ( fieldName == null ) return false;
        return value.getDetails().filteredSet() == null 
            ? false 
                    : value.getDetails().filteredSet().contains( fieldName );
    }
    
    public ExperimenterData getOwner()
    {
        if ( owner == null )
        {
            owner = new ExperimenterData( asIObject().getDetails().getOwner() );
        }
        return owner;
    }

    public String toString() {
        return getClass().getName()+" (id="+getId()+")";
    }

    // ~ Helpers
    // =========================================================================
    
    protected int nullSafe( Integer i )
    {
        return i == null ? 0 : i.intValue();
    }
    
    protected long nullSafe( Long l )
    {
        return l == null ? 0L : l.longValue();
    }
    
    protected double nullSafe( Double d )
    {
        return d == null ? 0.0 : d.doubleValue();
    }
    
    protected float nullSafe( Float f )
    {
        return f == null ? 0.0f : f.floatValue();
    }
    
    protected Timestamp timeOfEvent( Event event )
    {
        if (event==null) return null;
        if (!event.isLoaded()) return null;
        if (event.getTime()==null) return null;
        return new Timestamp( event.getTime().getTime() );
    }

    protected boolean nullDetails()
    {
        return asIObject().getDetails() == null;
    }
    
    protected Details getDetails()
    {
        return asIObject().getDetails();
    }
    
    protected Integer getCount( String countName )
    {
        Object count = null;
        
        if (! nullDetails() && countName != null 
                && getDetails().getCounts() != null)
            count = getDetails().getCounts().get( countName );
    
        if ( count instanceof Integer )
            return (Integer) count;

        return null;
 
    }
    
    // ~ VIEWS
    // =========================================================================
    // These methods should never a null value 
    // since single setter checks for null. 
    
    /** 
     * not null; no exceptions. 
     * @return not null IObject
     */
    public IObject asIObject()
    {
        return value;
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Experimenter asExperimenter()
    {
        return (Experimenter) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public ExperimenterGroup asGroup()
    {
        return (ExperimenterGroup) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public ImageAnnotation asImageAnnotation()
    {
        return (ImageAnnotation) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public DatasetAnnotation asDatasetAnnotation()
    {
        return (DatasetAnnotation) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Image asImage()
    {
        return (Image) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Dataset asDataset()
    {
        return (Dataset) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Category asCategory()
    {
        return (Category) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public CategoryGroup asCategoryGroup()
    {
        return (CategoryGroup) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Project asProject()
    {
        return (Project) asIObject();
    }
    /** not null; may through class-cast exception
     * @throws ClassCastException
     * @return not null IObject 
     */
    public Pixels asPixels()
    {
        return (Pixels) asIObject();
    }
    
}

/** Encapsulates the logic to update one set based on the changes made to a copy
 * of that set. Provides two iterators for acting on the changes:
 * <code>
 * SetMutator m = new SetMutator( oldSet, updatedSet );
 * while ( m.hasDeletions() )
 * {
 *      DataObject d = m.nextDeletion();
 * }
 * 
 * while ( m.hasAdditions() )
 * {
 *      DataObject d = m.nextAddition();
 * }
 * return m.result();
 * </code>
 */
class SetMutator {
 
    private Set _old, _new, del, add;
    private Iterator r, a;

    /** null-safe constructor */
    public SetMutator( Set originalSet, Set targetSet )
    {
        
        _old = originalSet == null ? new HashSet() : new HashSet( originalSet );
        _new = targetSet == null ? new HashSet() : new HashSet( targetSet );

        del = new HashSet( _old );
        del.removeAll( _new );
        r = del.iterator();
        
        add = new HashSet( _new );
        add.removeAll( _old );
        a = add.iterator();
        
    }

    public boolean moreDeletions()
    {
        return r.hasNext();
    }
    
    public DataObject nextDeletion()
    {
        return (DataObject) r.next();
    }
    
    public boolean moreAdditions()
    {
        return a.hasNext();
    }
    
    public DataObject nextAddition()
    {
        return (DataObject) a.next();
    }

    public Set result()
    {
        return new HashSet( _new );
    }
    
}
