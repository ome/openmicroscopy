/*
 * ome.formats.EnumerationException
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
GPL'd. See License attached to this project
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

package ome.formats;

/**
 * @author "Brian W. Loranger"
 */
public class EnumerationException extends RuntimeException
{

    /** The enumeration class that was used in a failed enumeration lookup. */
    private Class  failureClass;

    /** The enumeration value that was used in a failed enumeration lookup. */
    private String value;

    public EnumerationException(String message, Class klass, String value)
    {
        super(message);
        this.failureClass = klass;
        this.value = value;
    }

    public Class getFailureClass()
    {
        return failureClass;
    }

    public String getValue()
    {
        return value;
    }

    public String toString()
    {
        return getMessage() + "'" + value + "' in '" + failureClass + "'.";
    }
}
