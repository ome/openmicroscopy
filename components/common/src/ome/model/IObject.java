/*
 * ome.model.IObject
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.model;

//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;
import ome.util.Filterable;
import ome.util.Validation;

/**
 * central model interface. All entities that the backend can persist to the DB
 * implement this interface. (Note: value objects like 
 * {@link ome.model.internal.Details} get saved to the DB, but only embedded in
 * other entites.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *               <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * @author josh
 *
 */
public interface IObject extends Filterable{
	
    /** 
     * primary key of this object. Before the session is flushed, this
     * value <em>may be</em> null. 
     * 
     * @return Long primary key. May be null.
     */
	public Long getId();
    
    /**
     * usually unneeded. Ids are managed by the backend.
     * @param id Long value for this id. 
     */
    public void setId(Long id);
    
    // ~ Security
    // =========================================================================
    /** 
     * value (i.e. not entity) which is available on all rows in the database.
     * Low-level "details" such as security, ownership, auditing are managed 
     * here. 
     */
	public Details getDetails();
    
    /** when setting details, it is important to realize that most of the values
     * are managed by the backend and may be replaced. For example, a user does
     * not have permission to change the owner of an object, not even when owned 
     * by that user. 
     */
    public void setDetails(Details details);
    
    //  ~ Lifecycle
    // =========================================================================
    /** transient field (not stored in the DB) which specifies whether this 
     * object has been loaded from the DB or is only a wrapper around the ID.
     */
    public boolean isLoaded();
    
    /** set the loaded field to false, and set all non-ID fields to null. 
     * Subsequent calls to all accessors other than getId/setId will throw an 
     * ApiUsageException
     * @throws ApiUsageException
     */
    public void unload() throws ApiUsageException;
   
    // ~ Validation
    // =========================================================================    
    /** calls the class-specific validator for this instance and returns
     * the value from {@link Validation#isValid()}
     */
    public boolean isValid();
    
    /** calls the class-specific validator for this instance and returns
     * the {@link Validation} object.
     * @return Validation collecting parameter.
     */
    public Validation validate();
    
    // ~ For dynamic/generic programming
    // =========================================================================    
    /** retrieves a value from this instance. Values for <code>field</code>
     * which match a field of this instance will be delegated to the accessors.
     * Otherwise, values will be retrieved from a lazy-loaded map filled by calls
     * to {@link #putAt(String, Object)}
     */
    public Object retrieve(String field);
    
    /** stores a value in this instance. Values for <code>field</code> which 
     * match a field of this instance will be delegated to the accessors.
     * Otherwise, values will be stored in a lazy-loaded map.
     * 
     * @param field Field name
     * @param value Any object to be stored.
     */
    public void putAt(String field, Object value);
    
    /** returns a Set of field names that belong to this class */ 
    public Set fields();
    
    // ~ Graph information
    // =========================================================================
    /** retrieves the {@link GraphHolder} for this entity. If the GraphHolder
     * has not been actively set, a new one will be instatiated. 
     * @return Non-null GraphHolder
     */
    public GraphHolder getGraphHolder();
}
