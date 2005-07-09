package org.openmicroscopy.omero.shoolaadapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;

public class PojoOmeroServiceReflectionProxy implements InvocationHandler { 

    private ServiceFactory services = new ServiceFactory();
    private 	Object target = services.getHierarchyBrowsingService();    
 
	public static PojoOmeroService createProxy( ) {
		PojoOmeroServiceReflectionProxy posp = new PojoOmeroServiceReflectionProxy();
		return (PojoOmeroService) Proxy.newProxyInstance( 
				posp.target.getClass().getClassLoader(),
				new Class[]{PojoOmeroService.class},
				posp );     
		}     
		
	private PojoOmeroServiceReflectionProxy( ) {
	}     

public Object invoke( Object proxy, Method method, Object[] args )          
  throws Throwable {
	Object result = null;         
	try {
		result = method.invoke( target, args );       
	} catch (InvocationTargetException e) {
		// throw new !!! FIXME
	}
	
	Method adapt;
	//adapt=PojoAdapterUtils.class.getMethod("adapt"+method.getName(),)
	
	//Object adapted = PojoAdapterUtils.
	
	return result;
} 
}

