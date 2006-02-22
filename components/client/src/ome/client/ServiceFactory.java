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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.system.OmeroContext;

import omeis.providers.re.RenderingEngine;

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

    public OmeroContext ctx;
    
    public ServiceFactory(){
        this.ctx = OmeroContext.getClientContext();
        
        Map auth = (Map) ctx.getBean("auth");
        //  try {
        //      getRemoteAuthenticationManager().attemptAuthentication((String)auth.
        //    get("name"),(String)auth.get("pass"));
        //  } catch (AuthenticationException authEx){
        //        throw new RemoteAuthenticationException(authEx.getMessage());         
        //  }

    }
    
    public ServiceFactory(String contextName){
        this.ctx = OmeroContext.getInstance(contextName);
    }

//	public RemoteAuthenticationManager getRemoteAuthenticationManager(){
//        return (RemoteAuthenticationManager) this.ctx.getBean("remoteAuthenticationFacade");
//    }
    
    public IPixels getPixelsService(){
        return (IPixels) this.ctx.getBean("pixelsService");
    }
    
    public IPojos getPojosService(){
        return (IPojos) this.ctx.getBean("pojosService");
    }
    
    public IQuery getQueryService(){
        return (IQuery) this.ctx.getBean("queryService");
    }
    
    public IUpdate getUpdateService(){
        return (IUpdate) this.ctx.getBean("updateService");
    }
    
    public RenderingEngine getRenderingService(){
        return (RenderingEngine) this.ctx.getBean("renderingService");
    }
    
}
