/*
 * ome.client.ThrowsConflictResolver
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
import java.util.ConcurrentModificationException;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;

/** 
 * default strategy implementation which simply throws a 
 * {@link java.util.ConcurrentModificationException} regardless of arguments. 
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
public class ThrowsConflictResolver implements ConflictResolver
{
    public final static String MESSAGE = "Version conflict discovered in session:";
    
    /** 
     * strategy method that, in fact, doesn't resolve any conflicts. But simply
     * throws a {@link ConcurrentModificationException}
     * @param registeredVersion currently registered entity. Ignored.
     * @param possibleReplacement entity which is to be considered for replacement. Ignored.
     * @return does not return.
     * @throws ConcurrentModificationException. Always thrown.
     */
    public IObject resolveConflict( IObject registeredVersion, IObject possibleReplacement )
        throws ConcurrentModificationException
        {
            throw new ConcurrentModificationException
                (message( registeredVersion, possibleReplacement ));
        }
 
    /** produces exception message based on the two inputs */
    protected String message( IObject registeredVersion, IObject possibleReplacement )
    {
        StringBuffer sb = new StringBuffer(MESSAGE.length() + 64);
        sb.append( MESSAGE );
        sb.append( "\nregisteredVersion:\t");
        sb.append( registeredVersion );
        sb.append( " (hash="+registeredVersion.hashCode()+")");
        sb.append( "\npossibleReplacement\t");
        sb.append( possibleReplacement );
        sb.append( " (hash="+possibleReplacement.hashCode()+")");
        return sb.toString();
    }
}
