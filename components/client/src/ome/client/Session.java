/*
 * ome.client.Session
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

//Third-party libraries

//Application-internal dependencies
import ome.api.IUpdate;
import ome.model.IMutable;
import ome.model.IObject;
import ome.system.ServiceFactory;

/** 
 * client-side unit-of-work which uses the provided 
 * {@link ome.system.ServiceFactory} for synchronizing with the server. 
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
public class Session
{

    /** represents an entity in an invalid state */
    public final static int INVALID = -1;
    
    /** represents a new object (no ID) */
    public final static int TRANSIENT = 0;
    
    /** represents a persistent object (with ID) */
    public final static int PERSISTENT = 1;

    /** represents a persistent object scheduled for deletion */
    public final static int DELETED = 2;
    
    /** is this session active. If false, all calls other than {@link #close()}
     * will fail.
     */
    protected boolean closed = false;
    
    /** time of last modification. Initialized at Session creation to
     * the current JVM time. */
    protected long lastModification = System.currentTimeMillis();
    
    /** used for querying the server (lazy-loading) and 
     * synchronizing values to the database. Passed in through the public
     * constructors. Will never be null.
     */
    protected ServiceFactory serviceFactory;

    /** central cache-like data structure which keeps track of the entities
     * in their various states. Created in constructor. 
     */ 
    protected Storage storage;

    /** conflict resolution strategy. Default is set in constructor. */
    protected ConflictResolver conflictResolver;
    
    /** default constructor is private. We need a ServiceFactory! */ 
    private Session() {}
    
    /** constructor taking a {@link ServiceFactory} which is used for 
     * server communication.
     * @param factory Valid ServiceFactory instance. Not null.
     */
    public Session( ServiceFactory factory )
    {
        if ( factory == null )
            throw new IllegalArgumentException("ServiceFactory may not be null");
        
        this.serviceFactory = factory;
        this.storage = new Storage();
        this.conflictResolver = new ThrowsConflictResolver();
    }
    
    // ~ Unit-of-Work methods
    // =========================================================================

    /** returns the currently active instance that matches the query. 
     * In the case of a deleted object, a null will be returned.
     * @param klass Not null.
     * @param id Not null. 
     */
    public IObject find(Class klass, Long id)
    {
        errorIfClosed();
        
        if ( klass == null || id == null )
            throw new IllegalArgumentException("Class and id may not be null.");
        
        if ( storage.isDeleted( klass, id )) return null;
        
        return storage.findPersistent( klass, id );
    }
    
    /** returns time at which the last modification took place.
     * (Time as returned by {@link System#currentTimeMillis()}.
     * @return time of last modification
     */
    public long lastModification()
    {
        errorIfClosed();
        return lastModification;
    }
    
    /** add a transient ( <code>new()</code> ) or persistent instance to the
     * session. Often this will be done by another infrastructure class. Invalid
     * (version == null) entities will cause an IllegalArgumentException. 
     * Version conflicts will throw a 
     * {@link java.util.ConcurrentModificationException}
     * 
     * @param iObject entity reference to be added to session. Null values
     *      are silently ignored. Values without ids will be scheduled for
     *      creation. Values <em>with</em> ids but <em>without</em> versions
     *      are illegal.
     * @see pojos.DataObject
     * @throws java.util.ConcurrentModificationException if an updated object
     *      is registered while an existing version is dirty.
     */
    public void register(IObject iObject)
    {
        errorIfClosed();
        
        // Silently ignore.
        if (iObject == null) return;
        switch ( getState(iObject) )
        {
        case TRANSIENT :
            storage.storeTransient( iObject ); break;
        case PERSISTENT :
            iObject = checkForConflicts( iObject );
            storage.storePersistent( iObject ); break;
        case INVALID :
            throw new IllegalArgumentException(NULL_VERSION);
        default :
            /* silently ignore? */ break;
        }
        lastModification = System.currentTimeMillis();
    }
    
    /** marks an object as dirty. On the next synchronization with the 
     * database, it will be updated. Non-persistent entites will cause 
     * an IllegalArgumentException
     * @param iObject a persistent entity. Null values are silently ignored.
     */
    public void markDirty( IObject iObject )
    {
        errorIfClosed();
        
        if (iObject == null) return;
        switch ( getState(iObject) )
        {
        case PERSISTENT :
            storage.storeDirty( iObject ); break;
        default :
            throw new IllegalArgumentException(NULL_VERSION);
        }
    }
    
    /** registers a persistent entity for deletion. Non-persistent entites 
     * will cause an IllegalArgumentException
     * @param iObject a persistent entity. Null values are
     *      silently ignored.
     * @DEV.TODO should we also  unregister a transient entity if persistent.
     */
    public void delete( IObject iObject )
    {
        errorIfClosed();
        
        if (iObject == null) return;
        switch ( getState(iObject) )
        {
        case PERSISTENT :
            storage.storeDeleted( iObject ); break;
        default :
            throw new IllegalArgumentException(NULL_VERSION);
        }
        lastModification = System.currentTimeMillis();
    }
    
    /** synchronizes the session state with the server. All actions are
     * purged; each action in a given type (INSERT, UPDATE, DELETE) is performed
     * in the order that it was performed on the session.
     */
    public void flush()
    {
        errorIfClosed();
        
        IUpdate iUpdate = serviceFactory.getUpdateService();
        
        // get a copy of all our entities
        IObject[] insert = storage.copyCreatedEntities();
        IObject[] update = storage.copyDirtyEntities();
        IObject[] delete = storage.copyDeletedEntities();
        
        IObject[] inserted = iUpdate.saveAndReturnArray( insert );
        assert insert.length == inserted.length : "Differing sizes returned.";
        
        for ( int i = 0; i < inserted.length; i++ )
        {
            storage.replaceTransient(insert[i],inserted[i]);
        }
        
        IObject[] updated = iUpdate.saveAndReturnArray( update );
        assert update.length == updated.length : "Differing sizes returned.";
       
        for ( int i = 0; i < updated.length; i++ )
        {
            storage.storePersistent( updated[i] );
        }
        
        for ( int i = 0; i < delete.length; i++ )
        {
            iUpdate.deleteObject( delete[i] );    
        }
        
        
    }
    
    /** at any time, but especially after flushing or on 
     * {@link #lastModification() modification} an object can be presented for
     * <em>check out</em>. Transient and persistent entities are simply replaced
     * by a newer version, if available. Deleted entities are replaced with null.
     * @param iObject an object for which a replacement might be in the session.
     *      Nulls are silently ignored.
     */
    public IObject checkOut( IObject iObject )
    {
        errorIfClosed();
        
        if (iObject == null) return null;
        
        Class k = iObject.getClass();
        
        // entity is new. Check if its been updated. 
        if (getState(iObject) == TRANSIENT)
        {
            return storage.hasReplacement( iObject ) 
                ? storage.getReplacement( iObject ) 
                    : iObject;
        }
        
        // not transient; must have an id.
        
        Long id = iObject.getId();
        
        if (storage.isDeleted(k,id))
            return null;

        if (storage.isPersistent(k,id))
            return storage.findPersistent(k,id);

        // if nothing found, return the same.
        return iObject;
        
    }
    
    /** releases references to stored entities. All session method calls other
     * than {@link #close} will
     */
    public void close()
    {
        storage = null;
        closed = true;
    }
    // ~ Helper methods
    // =========================================================================
    
    /** determines if there is a version conflict that needs to be resolved. If
     * so, handling is given over to a {@link ConflictResolver}. Here, we 
     * know that the possibleReplacement is {@link #PERSISTENT} and so don't
     * have to worry too much about checking its state.
     * @param possibleReplacement a {@link #PERSISTENT} entity.
     * @see ThrowsConflictResolver
     */
    protected IObject checkForConflicts( IObject possibleReplacement )
    {
        Class k = possibleReplacement.getClass();
        Long id = possibleReplacement.getId();
        IObject registeredVersion = storage.findPersistent( k,id );
        
        if ( storage.isDirty( k,id )) 
            return conflictResolver
                .resolveConflict( registeredVersion, possibleReplacement );
        
        if ( storage.isDeleted( k,id ))
            return conflictResolver
                .resolveConflict( registeredVersion, possibleReplacement );

        return possibleReplacement;

    }
    
    /** determines the state (as identified by the public final static variables
     * on Session: {@link Session#PERSISTENT}, {@link Session#TRANSIENT}, ...
     * @param iObject state to test. Not null.
     */
    protected int getState(IObject iObject)
    {
        if ( iObject == null )
            throw new IllegalArgumentException("Entities to test may not be null");
        
        if ( iObject.getId() == null)
            return TRANSIENT;
        
        if ( iObject instanceof IMutable )
        {
            IMutable iMutable = (IMutable) iObject;
            if ( iMutable.getVersion() == null )
                return INVALID;
        }
        
        return PERSISTENT;
    }
    
    /** used on most instance methods to protected against corruption once
     * the {@link #close()} method is called.
     */
    protected void errorIfClosed()
    {
        if (closed)
            throw new IllegalStateException("Session "+toString()+" is closed.");
    }
    
    private final static String NULL_VERSION = "Invalid entity. Is it missing a version?";
    
}
