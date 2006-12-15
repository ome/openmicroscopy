/*
 * ome.parameters.Filter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;

//Java imports
import java.io.Serializable;

//Third-party libraries

//Application-internal dependencies

/** 
 * parameter to generally reduce the size of a query result set.    
 * 
 * @author  <br>Josh Moore&nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 3.0-M2
 * @see ome.parameters.Page
 * @see ome.parameters.Order
 * @see 
 */
public class Filter implements Serializable
{

    /**
     * flag determining if a {@link ome.services.query.Query} will attempt 
     * to return a single value <em>if supported</em>.
     */
    private boolean unique = false;
    
    private long id_owner, id_group;
    private Page page = new Page( 0, Integer.MAX_VALUE );

    // ~ Flags
    // =========================================================================
    /** 
     * state that this Filter should only return a single value if possible. 
     * By default, a Filter will make no assumptions regarding the uniquesness
     * of a query. 
     */
    public Filter unique()
    {
        unique = true;
        return this;
    }

    /**
     * check uniqueness for this query. Participating queries will attempt to
     * call <code>uniqueResult</code> rather than <code>list</code>. This may
     * throw a {@link ome.conditions.ValidationException} on execution. 
     */
    public boolean isUnique()
    {
        return unique;
    }
    
    // ~ Owner
    // =========================================================================
    public Filter owner( long ownerId )
    { 
        id_owner = ownerId;
        return this;
    }
    
    public long owner()
    {
        return id_owner;
    }
    
    public Filter group( long groupId )
    {
        id_group = groupId;
        return this;
    }
    
    public long group()
    {
        return id_group;
    }
    
    // ~ Page
    // =========================================================================
    
    public Filter page( int offset, int limit )
    {
        page = new Page( offset, limit );
        return this;
    }
    
    public int firstResult()
    {
        return page.offset();
    }
    
    public int maxResults()
    {
        return page.limit();
    }
    
    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 60649802598825408L;

}
