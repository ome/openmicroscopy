/*
 * ome.model.internal.GraphHolder
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
package ome.model.internal;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.model.IObject;

/**
 * holds information regarding the graph to which an {@link ome.model.IObject}
 * belongs.
 * 
 * {@link GraphHolder#hasToken()}, {@link GraphHolder#tokenMatches(Token)}, 
 * and {@link GraphHolder#setToken(Token, Token)} are final so that subclasses
 * cannot intercept tokens.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *               <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * @author josh
 */
public final class GraphHolder
{

    private IObject replacement;

    /** a replacement is a <em>managed</em> entity instance which has the same
     * primary key as this instance. Storing this value here allows for several
     * optimizations.
     * 
     * @return entity 
     */
    public IObject getReplacement()
    {
        return replacement;
    }


    /** used mostly by {@link ome.api.IUpdate}. Improper use of this method 
     * may cause erratic behavior.
     * 
     * @param replacement
     */

    public void setReplacement( IObject replacement )
    {
        this.replacement = replacement;
    }
    
    private Token token;
 
    /** tests if this {@link GraphHolder} contains a {@link Token} reference.
     */
    public final boolean hasToken( )
    {
    	return this.token != null;
    }
    
    /** check the {@link Token} for the {@link IObject} represented by this 
     * {@link GraphHolder}. This can be seen to approximate "ownership" of this
     * Object within the JVM.
     * 
     * @return true only if the two instances are identical.
     */
    public final boolean tokenMatches( Token token )
    {
    	return this.token == token;
    }

    /** set the {@link Token} for this {@link GraphHolder} but only if you 
     * posses the current {@link Token}. The first call to {@link #setToken(Token, Token)}
     * will succeed when {@link #token} is null.
     * 
     * @param previousToken
     * @param newToken
     */
    public final void setToken( Token previousToken, Token newToken )
    {
    	if ( token == null || previousToken == token )
    	{
    		this.token = newToken;
    	}
    	
    	else 
    	{
    		throw new SecurityViolation("Tokens do not match.");
    	}
    }
}
