/*
 * ome.adapters.pojos.OmeroEntry
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

package ome.adapters.pojos;

//Java imports
import java.net.MalformedURLException;

//Third-party libraries

//Application-internal dependencies
import ome.api.Pojos;
import ome.client.ServiceFactory;
import ome.client.SpringHarness;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

/** 
 * Entry point for all Shoola calls. Provides methods to 
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
public class OmeroEntry {

	public OmeroEntry(){
	}
	
	public OmeroEntry(String host, int port){
		
		if (null == host || host.equals("")){
			throw new IllegalArgumentException("Host name cannot be empty");//TODO
		}
		
		if (port < 0){
			throw new IllegalArgumentException("Port cannot be negative.");//TODO
		}
		
		String url = constructUrl(host,port);
		resetAllFacades(url);
	}
	
    public Pojos getPojoOmeroService(){
        return new ServiceFactory().getPojosService();
    }
    
    private String constructUrl(String host, int port){
    		return "http://"+host+":"+port+"/omero"+"/";//FIXME put omero.context in spring.properties (rename omero.properties)
    }
    
    //TODO make this code more flexible (see client!:/spring.xml
    private void resetAllFacades(String url){
   		String[] beans=SpringHarness.ctx.getBeanDefinitionNames();
   		for (int i=0;i<beans.length;i++) {
   			if (beans[i].endsWith("Facade")){
   				HessianProxyFactoryBean fb = (HessianProxyFactoryBean) SpringHarness.ctx.getBean(SpringHarness.ctx.FACTORY_BEAN_PREFIX+beans[i]);
   				String oldUrl = fb.getServiceUrl();
   				String service = oldUrl.substring(oldUrl.lastIndexOf("/"));
   				fb.setServiceUrl(url+service);
   				try {
   					fb.afterPropertiesSet();
   				} catch (MalformedURLException e) {
   					throw new OmeroException("Improperly formed url.",e); // TODO
   				}
   			}
   		}
    }
}
