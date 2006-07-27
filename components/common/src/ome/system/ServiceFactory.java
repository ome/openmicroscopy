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
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
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

    protected OmeroContext ctx;

    protected String getPrefix()
    {
    	return "managed:";
    }
    
    protected String getDefaultContext()
    {
    	return OmeroContext.CLIENT_CONTEXT;
    }
    
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
    	if ( getDefaultContext() != null )
    	{
    		this.ctx = OmeroContext.getInstance(getDefaultContext());	
    	}
    }
    
    /** 
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context},
     * passing in the {@link Properties} representation of the {@link Login}
     * for configuration.
     * @see Login#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory( Login login ){
        this.ctx = OmeroContext.getClientContext( login.asProperties() );
    }

    /** 
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context},
     * passing in the {@link Properties} representation of the {@link Server}
     * for configuration.
     * @see Server#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory( Server server ){
        this.ctx = OmeroContext.getClientContext( server.asProperties() );
    }

    /** 
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context},
     * passing in the {@link Properties} representation of both the 
     * {@link Server} and the {@link Login} for configuration.
     * @see Login#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory( Server server, Login login ){
    	Properties s = server.asProperties();
    	Properties l = login.asProperties();
    	s.putAll(l);
    	this.ctx = OmeroContext.getClientContext( s );
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

    // ~ Stateless services
    // =========================================================================
    
    public IAdmin getAdminService(){
    	return getServiceByClass(IAdmin.class);
    }
    
    public IAnalysis getAnalysisService(){
    	return getServiceByClass(IAnalysis.class);
    }
    
    public IPixels getPixelsService(){
        return getServiceByClass(IPixels.class);
    }
    
    public IPojos getPojosService(){
        return getServiceByClass(IPojos.class);
    }
    
    public IQuery getQueryService(){
        return getServiceByClass(IQuery.class);
    }

    public ITypes getTypesService(){
        return getServiceByClass(ITypes.class);
    }
    
    public IUpdate getUpdateService(){
        return getServiceByClass(IUpdate.class);
    }
    
    // ~ Stateful services
    // =========================================================================

    public RawPixelsStore createRawPixelsStore(){
        return getServiceByClass(RawPixelsStore.class);
    }
    
    public RenderingEngine createRenderingEngine(){
        return getServiceByClass(RenderingEngine.class);
    }
    
    // ~ Helpers
	// =========================================================================

    protected <T extends ServiceInterface> T getServiceByClass(Class<T> klass)
    {
    	return klass.cast(this.ctx.getBean(getPrefix()+klass.getName()));
    }
}
