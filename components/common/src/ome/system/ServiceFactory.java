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
import ome.api.IConfig;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IThumb;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
import ome.model.internal.Permissions;
import ome.system.OmeroContext;

import omeis.providers.re.RenderingEngine;

/** 
 * Entry point for all client calls. Provides methods to 
 * obtain proxies for all remote facades. 
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     OmeroContext
 * @since   3.0
 */
public class ServiceFactory {

	/** the {@link OmeroContext context instance} which this 
	 * {@link ServiceFactory} uses to look up all of its state.
	 */
    protected OmeroContext ctx;
    
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

    // ~ Accessors
	// =========================================================================
    
    /** sets the umask on the {@link Principal} instance which will be passed
     * to the server-side on each invocation. 
     */
    public void setUmask( Permissions mask )
    {
    	if (!ctx.containsBean("principal"))
    	{
    		throw new UnsupportedOperationException("The context for this " +
    				"ServiceFactory does not contain a Principal on which " +
    				"the umask can be set.");
    	}
    	Principal p = (Principal) ctx.getBean("principal");
    	p.setUmask(mask);
    }
    
    // ~ Stateless services
    // =========================================================================
    
    public IAdmin getAdminService(){
    	return getServiceByClass(IAdmin.class);
    }
    
    public IAnalysis getAnalysisService(){
    	return getServiceByClass(IAnalysis.class);
    }

    public IConfig getConfigService(){
    	return getServiceByClass(IConfig.class);
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
    
    public IThumb getThumbnailService(){
    	return getServiceByClass(IThumb.class);
    }
    
    // ~ Stateful services
    // =========================================================================

    /** create a new {@link RawPixelsStore} proxy. This proxy will have to be
     * initialized using {@link RawPixelsStore#setPixelsId(long)}
     */
    public RawPixelsStore createRawPixelsStore(){
        return getServiceByClass(RawPixelsStore.class);
    }
    
    /** create a new {@link RenderingEngine} proxy. This proxy will have to be 
     * initialized using {@link RenderingEngine#lookupPixels(long)} and
     * {@link RenderingEngine#load()}
     */
    public RenderingEngine createRenderingEngine(){
        return getServiceByClass(RenderingEngine.class);
    }
    
    // ~ Helpers
	// =========================================================================

    /** looks up services based on the current {@link #getPrefix() prefix} 
     * and the class name of the service type.
     */
    protected <T extends ServiceInterface> T getServiceByClass(Class<T> klass)
    {
    	return klass.cast(this.ctx.getBean(getPrefix()+klass.getName()));
    }
    
    /** used by {@link #getServiceByClass(Class)} to find the correct service
     * proxy in the {@link #ctx}
     * 
     * @return a {@link String}, usually "internal:" or "managed:"
     */
    protected String getPrefix()
    {
    	return "managed:";
    }
    
    /** used when no {@link OmeroContext context} name is provided to the 
     * constructor. Subclasses can override to allow for easier creation. 
     * 
     * @return name of default context as found in beanRefContext.xml.
     */
    protected String getDefaultContext()
    {
    	return OmeroContext.CLIENT_CONTEXT;
    }
}
