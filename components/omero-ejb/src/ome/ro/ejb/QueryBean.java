/* ome.ro.ejb.QueryBean
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
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;

//Third-party imports

//Application-internal dependencies
import ome.api.IQuery;


@Stateless
@Remote(IQuery.class)
@Local(IQuery.class)
public class QueryBean extends AbstractBean implements IQuery
{

    IQuery delegate;
    
    public QueryBean(){
        super();
        delegate = (IQuery) ctx.getBean("queryService");
    }
    
    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }

    public Object getById(Class klazz, long id)
    {
        return delegate.getById(klazz, id);
    }

    public List getListByExample(Object example)
    {
        return delegate.getListByExample(example);
    }

    public List getListByFieldEq(Class klazz, String field, Object value)
    {
        return delegate.getListByFieldEq(klazz, field, value);
    }

    public List getListByFieldILike(Class klazz, String field, String value)
    {
        return delegate.getListByFieldILike(klazz, field, value);
    }

    public List getListByMap(Class klazz, Map constraints)
    {
        return delegate.getListByMap(klazz, constraints);
    }

    public Object getUniqueByExample(Object example)
    {
        return delegate.getUniqueByExample(example);
    }

    public Object getUniqueByFieldEq(Class klazz, String field, Object value)
    {
        return delegate.getUniqueByFieldEq(klazz, field, value);
    }

    public Object getUniqueByFieldILike(Class klazz, String field, String value)
    {
        return delegate.getUniqueByFieldILike(klazz, field, value);
    }

    public Object getUniqueByMap(Class klazz, Map constraints)
    {
        return delegate.getUniqueByMap(klazz, constraints);
    }

    public void persist(Object[] objects)
    {
        delegate.persist(objects);
    }

    public List queryList(String query, Object[] params)
    {
        return delegate.queryList(query, params);
    }

    public List queryListMap(String query, Map params)
    {
        return delegate.queryListMap(query, params);
    }

    public Object queryUnique(String query, Object[] params)
    {
        return delegate.queryUnique(query, params);
    }

    public Object queryUniqueMap(String query, Map params)
    {
        return delegate.queryUniqueMap(query, params);
    }

}
