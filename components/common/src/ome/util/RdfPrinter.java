/*
 * ome.util.RdfPrinter
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

package ome.util;


//Java imports
import java.util.Collection;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies


/** walks an object graph producing RDF-like output.
 */
public class RdfPrinter extends ContextFilter {
 
    StringBuilder sb = new StringBuilder();
    
    public String getRdf(){
        return sb.toString();
    }
    
    void space(){
        sb.append(" ");
    }
    
    void newline(){
        sb.append("\n");
    }
    
    protected void entry(String field, Object o){
        if (!Collection.class.isAssignableFrom(currentContext().getClass()) )
        {
            sb.append(currentContext());
            space();
            sb.append(field);
            space();
            sb.append(o);
            newline();
        }
    }
    
    public Object filter(String fieldId, Object o)
    {
        entry(fieldId,o);
        return super.filter(fieldId,o);
    }
    
    public Filterable filter(String fieldId, Filterable f)
    {
        entry(fieldId,f);
        return super.filter(fieldId,f);
    }
    
    public Collection filter(String fieldId, Collection c)
    {
        sb.append(currentContext() == null ? "" : currentContext());
        space();
        sb.append(fieldId);
        space();
        sb.append(" [ ");
        Collection result;
        if (c != null && c.size() > 0)
        {
            for (Iterator it = c.iterator(); it.hasNext();)
            {
                sb.append(it.next());
                
            }
        } else {
        }
        sb.append(" ] \n");
        return super.filter(fieldId,c);
    }
}
