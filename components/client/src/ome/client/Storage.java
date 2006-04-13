/* ome.client.Storage
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
package ome.client;

//Java imports
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;

/**
 * cache-like storage mechanism. This can eventually be replaced by a more
 * sophisticated, possibly memory-saving implementation. 
 *  
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME3.0
 */

public class Storage
{
    
    // implementation may change!
    
    /** maps from transient entitites to future replacements (with ID) */
    protected Map created = new LinkedHashMap();
    
    /** maps from {@link Key} to persistent entities */
    protected Map persistent = new LinkedHashMap();
    
    /** maps from {@link Key} to deleteable entities */
    protected Map deleted = new LinkedHashMap();
    
    /** maps from {@link Key} to edited entities */
    protected Map dirty = new LinkedHashMap();

    // COPY

    /** copied array of created entities. 
     * @return The keys of the map (i.e. transient
     *      entities) are returned.
     */
    public IObject[] copyCreatedEntities()
    {
        Collection c = created.keySet();
        return (IObject[]) c.toArray( new IObject[c.size( )] );
    }

    /** copied array of tracked entities */
    public IObject[] copyPersistentEntities()
    {
        Collection c = persistent.values();
        return (IObject[]) c.toArray( new IObject[c.size( )] );
    }

    /** copied array of dirty entities */
    public IObject[] copyDirtyEntities()
    {
        Collection c = dirty.values();
        return (IObject[]) c.toArray( new IObject[c.size( )] );
    }

    /** copied array of deleted entities */
    public IObject[] copyDeletedEntities()
    {
        Collection c = deleted.values();
        return (IObject[]) c.toArray( new IObject[c.size( )] );
    }

    
    // GET

    /** is entity scheduled for creation. Here the <em>key</em> of the map 
     * is checked
     */
    public boolean isTransient( IObject iObject)
    {
        if (iObject == null) return false;
        return created.containsKey(iObject);
    }
    
    /** does entity have a replacement. Null and non-transient objects
     * are silently ignored (returns false).
     * @param iObject entity to test
     */
    public boolean hasReplacement( IObject iObject )
    {
        return isTransient( iObject ) && created.get( iObject ) != null;
    }
    
    /** returns the replacement for this transient object. This must be 
     * set by {@link #replaceTransient(IObject, IObject)}
     * @param iObject
     * @return replacement entity. May be null. 
     *      Test with {@link #hasReplacement(IObject)}  
     */
    public IObject getReplacement( IObject iObject )
    {
        if (iObject == null) return null;
        return (IObject) created.get( iObject );
    }
    
    /** get tracked version of entity  */
    public IObject findPersistent( Class klass, Long id )
    {
        if (klass == null || id == null) return null;
        return (IObject) persistent.get( new Key(klass, id) );
    }

    /** is entity scheduled currently tracked */
    public boolean isPersistent( Class klass, Long id)
    {
        if (klass == null || id == null) return false;
        return persistent.containsKey( new Key(klass,id) );
    }
    
    /** is entity scheduled for deletion */
    public boolean isDeleted( Class klass, Long id )
    {
        if (klass == null || id == null) return false;
        return deleted.containsKey( new Key(klass,id) );
    }
    
    /** is entity scheduled for updated */
    public boolean isDirty( Class klass, Long id )
    {
        if (klass == null || id == null) return false;
        return dirty.containsKey( new Key(klass,id) );
       
    }
    
    // PUT

    /** schedule entity for insertion */
    public void storeTransient(IObject iObject)
    {
        if (iObject == null) return;
        if (iObject.getId() != null) 
            throw new IllegalArgumentException("IObject has id; can't be new.");
        created.put( iObject, null );
    }

    /** mark replacement for transient entity */
    public void replaceTransient(IObject iObject, IObject replacement)
    {
        if ( iObject == null) return;
        created.put( iObject, replacement);
    }
    
    /** track changes to this entity. Useful for preventing OptimisticLock
     * exceptions
     */
    public void storePersistent(IObject iObject)
    {
        if (iObject == null) return;
        persistent.put( new Key(iObject), iObject);
    }
        
    /** schedule an entity for update */
    public void storeDirty( IObject iObject )
    {
        if (iObject == null) return;
        dirty.put( new Key(iObject), iObject);
    }

    /** schedule an entity for deletion */
    public void storeDeleted( IObject iObject)
    {
        if (iObject == null) return;
        deleted.put( new Key(iObject), iObject);
    }
    
    /** LSID-type key. Usess {@link java.lang.Class} and {@link java.lang.Long}
     * values to define a key to an entity.
     */
    public static class Key 
    {

        /** type of the entity. */
        Class c;
        
        /** persistent ID */ 
        Long id;
        
        // hide default constructor
        private Key() {}
        
        /** creates a Key based on this {@link IObject} 
         * @param iObject Not null. iObject.getId() != null
         */
        public Key(IObject iObject)
        {
            if (iObject == null || iObject.getId() == null)
                throw new IllegalArgumentException("IObject must be in the PERSISTENT state.");
            
            this.c = iObject.getClass();
            this.id = iObject.getId();
            
            
        }
        
        /** standard constructor for creating a Key instance */
        public Key(Class klass, Long entityId)
        {
            if ( klass == null || entityId == null )
                throw new IllegalArgumentException("Class and id arguments for Key may not be null.");
            
            this.c = klass;
            this.id = entityId;
        }
        
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Key))            
                return false;
            
            Key other = (Key) obj;

            if  (
                    // second Josh Bloch suggestion for often == 
                    (this.c == other.c || ( this.c != null && c.equals(other.c)))
                     &&
                     // first Josh Bloch suggestion
                    (this == null ? other.id == null : this.id.equals(other.id))
                )
                return true;
            
            return false;
            
        }
        
        public int hashCode()
        {
            long _id = this.id.longValue();
            int result = 11;
            result = result * 17 + this.c.hashCode();
            result = result * 19 + (int) (_id^(_id>>>32));
            return result;
        }
        
    }
    
}

