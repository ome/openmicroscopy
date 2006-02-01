/* ome.ro.ejb.UpdateBean
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

package ome.ro.ejb;

//Java imports
import java.util.Collection;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;

//Third-party imports

//Application-internal dependencies
import ome.api.IUpdate;
import ome.model.IObject;

@Stateless
@Remote(IUpdate.class)
@Local(IUpdate.class)
public class UpdateBean extends AbstractBean implements IUpdate
{

    IUpdate delegate;
    
    public UpdateBean(){
        super();
        delegate = (IUpdate) ctx.getBean("updateService");
    }
    
    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }

    public void saveArray(IObject[] arg0)
    {
        delegate.saveArray(arg0);
    }

    public IObject[] saveAndReturnArray(IObject[] graph)
    {
        return delegate.saveAndReturnArray(graph);
    }

    public Collection saveAndReturnCollection(Collection graph)
    {
        return delegate.saveAndReturnCollection(graph);
    }

    public Map saveAndReturnMap(Map map)
    {
        return delegate.saveAndReturnMap(map);
    }

    public IObject saveAndReturnObject(IObject graph)
    {
        return delegate.saveAndReturnObject(graph);
    }

    public void saveObject(IObject graph)
    {
        delegate.saveObject(graph);
    }

    public void saveCollection(Collection arg0)
    {
        delegate.saveCollection(arg0);
    }

    public void saveMap(Map arg0)
    {
        delegate.saveMap(arg0);
    }

}
