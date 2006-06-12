/*
 * ome.system.ServiceFactory
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

package ome.system;

//Java imports
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.ITypes;
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

    private OmeroContext ctx;

    /** public access to the context. This may not always be available, but
     * for this initial phase, it makes some sense. Completely non-dangerous on
     * the client-side.
     *  
     * @deprecated
     */
    public OmeroContext getContext()
    {
        return ctx;
    }
    
    /** default constructor which obtains the global static
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context} from 
     * {@link ome.system.OmeroContext}. This can be done manually by calling
     * {@link ome.system.OmeroContext#getClientContext()}
     * @see OmeroContext#CLIENT_CONTEXT
     * @see OmeroContext#getClientContext()
     */ 
    public ServiceFactory(){
        this.ctx = OmeroContext.getClientContext();
    }
    
    /** 
     * constructor which obtains a new 
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context},
     * passing in the {@link Properties} representation of the Login
     * for configuration.
     * @see Login#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory( Login login ){
        this.ctx = OmeroContext.getClientContext( login.asProperties() );
    }
    
    /** 
     * constructor which obtains a new 
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context},
     * passing in the provided properties for configuration.
     * @see OmeroContext#getClientContext(Properties)
     */
    public ServiceFactory( Properties properties ){
        this.ctx = OmeroContext.getClientContext( properties );
    }

    /** 
     * constructor which uses the provided {@link OmeroContext} for all
     * loookups.
     */ 
    public ServiceFactory( OmeroContext context ){
        this.ctx = context;
    }

    /** 
     * constructor which finds the global static {@link OmeroContext} with the
     * given name.
     * @see OmeroContext#CLIENT_CONTEXT
     * @see OmeroContext#INTERNAL_CONTEXT
     * @see OmeroContext#MANAGED_CONTEXT
     */ 
    public ServiceFactory(String contextName){
        this.ctx = OmeroContext.getInstance(contextName);
    }

    public IAdmin getAdminService(){
        return (IAdmin) this.ctx.getBean("adminService");
    }
    
    public IAnalysis getAnalysisService(){
        return (IAnalysis) this.ctx.getBean("analysisService");
    }
    
    public IPixels getPixelsService(){
        return (IPixels) this.ctx.getBean("pixelsService");
    }
    
    public IPojos getPojosService(){
        return (IPojos) this.ctx.getBean("pojosService");
    }
    
    public IQuery getQueryService(){
        return (IQuery) this.ctx.getBean("queryService");
    }

    public ITypes getTypesService(){
        return (ITypes) this.ctx.getBean("typesService");
    }
    
    public IUpdate getUpdateService(){
        return (IUpdate) this.ctx.getBean("updateService");
    }
    
    public RenderingEngine getRenderingService(){
        return (RenderingEngine) this.ctx.getBean("renderService");
    }
    
}
