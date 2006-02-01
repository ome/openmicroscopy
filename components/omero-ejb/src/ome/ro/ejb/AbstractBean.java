/* ome.ro.ejb.AbstractBean
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

//Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;

//Application-internal dependencies

public class AbstractBean 
{
    
    private static Log log = LogFactory.getLog(AbstractBean.class);

    private BeanFactoryLocator   bfl = SingletonBeanFactoryLocator.getInstance();

    private BeanFactoryReference bf  = bfl.useBeanFactory("ome");
    
    protected ApplicationContext  ctx = (ApplicationContext) bf.getFactory();

    public AbstractBean()
    {
        log.debug("Creating:\n"+getLogString());
    }
    
    public void destroy()
    {
        bf.release();
        log.debug("Destroying:\n"+getLogString());
    }
    
    protected String getLogString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean ");
        sb.append(this);
        sb.append("\n With BeanFactory ");
        sb.append(bf);
        return sb.toString();
    }
    
}
