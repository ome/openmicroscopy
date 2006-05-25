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
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

//Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;

public class AbstractBean 
{
    
    private static Log log = LogFactory.getLog(AbstractBean.class);

    protected OmeroContext  applicationContext;

    protected EventContext  eventContext;
    
    // java:comp.ejb3/EJBContext
    protected @Resource SessionContext sessionContext; 
    //protected @Resource(mappedName="UserTransaction") UserTransaction ut;
    //protected @Resource(mappedName="security/subject") Subject subject;
    
    public AbstractBean()
    {
        applicationContext = OmeroContext.getManagedServerContext();
        eventContext = (EventContext) applicationContext.getBean("eventContext");
        log.debug("Created:\n"+getLogString());
    }

    @AroundInvoke 
    public Object around( InvocationContext ctx ) throws Exception
    {
        Principal p;
        if ( sessionContext.getCallerPrincipal() instanceof Principal )
        {
            p = (Principal) sessionContext.getCallerPrincipal();
        }
        else
        {
            throw new ApiUsageException(
                    "ome.system.Principal instance must be provided on login.");
        }
                
        if ( log.isDebugEnabled() )
            log.debug( "Running with user: "+p.getName() );
        
        try {
            eventContext.setPrincipal( p );
            return ctx.proceed();    
        } finally {
            eventContext.setPrincipal( null );
        }
        
    }
    
    public void destroy()
    {
        log.debug("Destroying:\n"+getLogString());
    }
    
    protected String getLogString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean ");
        sb.append(this);
        sb.append("\n with Context ");
        sb.append(applicationContext);
        return sb.toString();
    }
    
}
