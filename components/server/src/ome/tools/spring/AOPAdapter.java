/* ome.tools.spring.AOPAdapter
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

package ome.tools.spring;

//Java imports
import java.lang.reflect.Method;
import java.util.List;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

//Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AdvisorChainFactoryUtils;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

//Application-internal dependencies

/** adapts between Spring AOP and JEE AOP. AOPadapter can be used to share a 
 * single service implementation between both Spring and JavaEE. This is
 * achieved by applying the stack of AOP interceptors defined in Spring and 
 * having them applied during the {@link AroundInvoke} JavaEE interceptor.
 */
public class AOPAdapter extends ReflectiveMethodInvocation
{
    
    private static Log log = LogFactory.getLog(AOPAdapter.class);

    /** The {@link javax.interceptor.InvocationContext} which 
     * is passed into an {@link AroundInvoke}-annotated method.  
     */ 
    private InvocationContext invocation;
    
    @Override
    /** invokes {@link javax.interceptor.InvocationContext#proceed() proceed} on
     * the {@link InvocationContext} passed into this instance.
     */ 
    protected Object invokeJoinpoint() throws Throwable
    {
        return invocation.proceed();
    }

    /** 
     * because of hidden constructors, a static factory method is needed to 
     * create the AOPAdapter.
     */
    public static AOPAdapter create( 
            ProxyFactoryBean factory, InvocationContext context )
    {
        return new AOPAdapter(
                context, 
                proxy( factory ),
                target( context ),
                method( context ),
                args( context ),
                targetClass( factory ),
                interceptors( factory, context ));
    }
    
    /** simple override of the 
     * {@link org.springframework.aop.framework.ReflectiveMethodInvocation}
     * contructor which initializes the two fields on AOPAdapter.
     */
    public AOPAdapter( InvocationContext context, 
            Object proxy, Object target, Method method, Object[] arguments,
            Class targetClass, List interceptorsAndDynamicMethodMatchers )
    {
        super(proxy,target,method,arguments,targetClass,
        		interceptorsAndDynamicMethodMatchers);  
        this.invocation = context;
    }

    // ~ Static helpers for creation.
    // =========================================================================
    
    protected static Object proxy( ProxyFactoryBean factory )
    {
        return null;
    }
    
    protected static Object target( InvocationContext context )
    {
        return context.getBean();
    }
    
    protected static Method method( InvocationContext context )
    {
        return context.getMethod();
    }
    
    protected static Object[] args( InvocationContext context )
    {
        return context.getParameters();
    }
    
    protected static Class targetClass( ProxyFactoryBean factory )
    {
        return factory.getObjectType();
    }
    
    protected static List interceptors( ProxyFactoryBean factory, 
            InvocationContext context )
    {
        return AdvisorChainFactoryUtils
            .calculateInterceptorsAndDynamicInterceptionAdvice(
                factory, 
                proxy( factory ), 
                method( context ), 
                targetClass( factory ));
    }
    
    
}

