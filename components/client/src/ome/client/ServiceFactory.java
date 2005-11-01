/*
 * ome.client.ServiceFactory
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

package ome.client;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.net.URL;
import java.util.Map;

import net.sf.acegisecurity.AuthenticationException;
import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.rcp.RemoteAuthenticationException;
import net.sf.acegisecurity.providers.rcp.RemoteAuthenticationManager;

import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import ome.api.HierarchyBrowsing;
import ome.api.Pojos;

/** 
 * Entry point for all client calls. Provides methods to 
 * obtain proxies for all remote facades. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class ServiceFactory {

	public final static String SPRING_CONF_FILE = "ome/client/spring.xml";
    public ApplicationContext ctx;
    
	public ServiceFactory(){
        URL path = ServiceFactory.class.getClassLoader().getResource(SPRING_CONF_FILE);
        if (path==null){
            throw new RuntimeException("Client jar corrupted. Can't find internal configuration file:\n"+SPRING_CONF_FILE);
        }
        try {
			ctx = new FileSystemXmlApplicationContext(path.toString());
		} catch (Exception e) {
			throw new RuntimeException("Can't load file: "+path,e);
		}
		
		Map auth = (Map) ctx.getBean("auth");
		try {
			getRemoteAuthenticationManager().attemptAuthentication((String)auth.get("name"),(String)auth.get("pass"));
		} catch (AuthenticationException authEx){
            throw new RemoteAuthenticationException(authEx.getMessage());			
		}

	}

	public RemoteAuthenticationManager getRemoteAuthenticationManager(){
        return (RemoteAuthenticationManager) this.ctx.getBean("remoteAuthenticationFacade");
    }
	
    public HierarchyBrowsing getHierarchyBrowsingService(){
        return (HierarchyBrowsing) this.ctx.getBean("hierarchyBrowsingFacade");
    }
    
    public Pojos getPojosService(){
        return (Pojos) this.ctx.getBean("pojosFacade");
    }

    
    
}
