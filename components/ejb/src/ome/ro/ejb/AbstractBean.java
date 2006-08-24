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
import javax.ejb.PrePassivate;
import javax.ejb.SessionContext;
import javax.interceptor.InvocationContext;

//Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactoryBean;

//Application-internal dependencies
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;

public class AbstractBean 
{
    
    private static Log log = LogFactory.getLog(AbstractBean.class);

    protected transient OmeroContext  applicationContext;

    protected transient ServiceFactory serviceFactory;
    
    protected transient SecuritySystem securitySystem;
    
    protected transient LocalQuery localQuery;
    
    protected transient LocalUpdate localUpdate;
    
    protected @Resource SessionContext sessionContext;

    public void create()
    {
        applicationContext = OmeroContext.getManagedServerContext();
        securitySystem = (SecuritySystem) applicationContext.getBean("securitySystem");
        serviceFactory = new InternalServiceFactory( applicationContext );
        localQuery = (LocalQuery) serviceFactory.getQueryService();
        localUpdate = (LocalUpdate) serviceFactory.getUpdateService();
 
        log.debug("Created:\n"+getLogString());
    }
    
    protected void passivationNotAllowed()
    {
    		throw new InternalException(String.format(
    				"Passivation should have been disabled for this Stateful Session Beans (%s).\n" +
    				"Please contact the Omero development team for how to ensure that passivation\n" +
    				"is disabled on your application server.",this.getClass().getName()));
    }
    
    public void destroy()
    {
        log.debug("Destroying:\n"+getLogString());
    }

    protected void login( )
    {
        Principal p;
        if ( sessionContext.getCallerPrincipal() instanceof Principal )
        {
            p = (Principal) sessionContext.getCallerPrincipal();
            securitySystem.login(p);
            if ( log.isDebugEnabled() )
                log.debug( "Running with user: "+p.getName() );
        }
        else
        {
            throw new ApiUsageException(
                    "ome.system.Principal instance must be provided on login.");
        }
        
    }
    
    protected void logout( )
    {
        securitySystem.logout();
    }
    
    protected Object wrap( 
    		InvocationContext context, 
    		Class<? extends ServiceInterface> factoryClass ) throws Exception
    {
        try {
            login();
            String factoryName = "&managed:"+factoryClass.getName();
            AOPAdapter adapter = 
            AOPAdapter.create( 
                    (ProxyFactoryBean) applicationContext.getBean(factoryName),
                    context );
            return adapter.proceed( );   
        } catch (Throwable t) {
            throw translateException( t );
        } finally {
            logout();
        }

    }
    
    protected Exception translateException( Throwable t )
    {
        if ( Exception.class.isAssignableFrom( t.getClass() ))
        {
            return (Exception) t;
        } else {
            InternalException ie = new InternalException( t.getMessage() );
            ie.setStackTrace( t.getStackTrace() );
            return ie;
        }
    }
    
    // ~ Helpers
    // =========================================================================
    
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
