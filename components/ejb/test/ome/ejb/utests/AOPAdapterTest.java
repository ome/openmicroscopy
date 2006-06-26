package ome.ejb.utests;

import java.lang.reflect.Method;
import java.util.Collections;

import javax.interceptor.InvocationContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.*;

import ome.ro.ejb.AOPAdapter;

@Test(
        groups = {"integration","aop","ejb"}
)
public class AOPAdapterTest extends MockObjectTestCase
{

    // This overrides the ejb/resources/ejb.properties settings
    static {
        System.getProperties().setProperty("container.status","out-of-container");
    }
    
    Mock mockInvocation;
    InvocationContext invocation;
    
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void setUp() throws Exception { 
        super.setUp();
        newInvocation();
    }

    @Configuration(afterTestMethod = true)
    public void tearDown() throws Exception {
        super.verify();
        invocation = null;
        super.tearDown(); 
    }
    // =========================================================================

    protected void newInvocation()
    {
        mockInvocation = mock(InvocationContext.class);
        invocation = (InvocationContext) mockInvocation.proxy();
    }

    @Test
    public void testOwnContext() throws Throwable
    {
        StaticApplicationContext context = new StaticApplicationContext();
        // the bean 
        BeanDefinition beanDef = new RootBeanDefinition(Bean.class,null,null);
        context.registerBeanDefinition("bean",beanDef);
        // the interceptor
        BeanDefinition handlerDef = new RootBeanDefinition(SimpleInterceptor.class,null,null);
        context.registerBeanDefinition("handler",handlerDef);
        // the proxy factory
        MutablePropertyValues mpv = new MutablePropertyValues();
        mpv.addPropertyValue("interceptorNames",Collections.singleton("handler"));
        mpv.addPropertyValue("proxyInterfaces",Collections.singleton(IBean.class.getName()));
        mpv.addPropertyValue("target",new RuntimeBeanReference("bean"));
        BeanDefinition factoryDef = new RootBeanDefinition(ProxyFactoryBean.class,null,mpv);
        context.registerBeanDefinition("factory",factoryDef);
        
        context.refresh();
        
        // setup mock expectations
        mockInvocation.expects( atLeastOnce() ).method( "getBean" )
            .will( returnValue( null ));
        Method method = Bean.class.getMethod("call");
        mockInvocation.expects( atLeastOnce() ).method( "getMethod" )
            .will( returnValue( method ));
        mockInvocation.expects( atLeastOnce() ).method( "getParameters" )
            .will( returnValue( new Object[]{} ));
        mockInvocation.expects( atLeastOnce() ).method( "proceed" )
        .will( returnValue( null ));
        
        // do it
        ProxyFactoryBean factory = (ProxyFactoryBean) context.getBean("&factory");
        MethodInvocation test = AOPAdapter.create( factory, invocation );
        test.proceed();
        
        // checks
        assertTrue(
                "This handler should get called.",
                ((SimpleInterceptor)context.getBean("handler")).invoked );
        assertFalse(
                "Bean should not get called but rather the target of the mockInvocation",
                ((Bean)context.getBean("bean")).invoked);
        super.verify();
        
    }
    
}

class SimpleInterceptor implements MethodInterceptor {
    public boolean invoked = false;
    public Object invoke(MethodInvocation arg0) throws Throwable
    {
        invoked = true;
        return arg0.proceed();
    }
}

interface IBean {
    void call();
}

class Bean implements IBean {
    public boolean invoked = false;
    public void call()
    {
        invoked = true;
    }
}