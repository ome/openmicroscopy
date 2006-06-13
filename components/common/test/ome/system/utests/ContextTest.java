package ome.system.utests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanReferenceFactoryBean;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.Test;

import ome.system.OmeroContext;


public class ContextTest extends TestCase
{
    
    public final static String C = "collector";
    public final static String FOO = "foo";
    public final static String BAR = "bar";
    public final static String BAX = "bax";
    
    @Test
    public void test_uniqueNonStaticContext() throws Exception
    {
        Properties p = new Properties();
        OmeroContext c1 = OmeroContext.getClientContext( p );
        OmeroContext c2 = OmeroContext.getClientContext( p );
        OmeroContext c3 = OmeroContext.getClientContext( new Properties() );
        assertTrue( c1 != c2 );
        assertTrue( c1 != c3 );
        assertTrue( c2 != c3 );
    }

    @Test
    public void test_properOmeroContextCtorIsUsed() throws Exception
    {
        StaticApplicationContext beanRef = new StaticApplicationContext();
        ConstructorArgumentValues args = new ConstructorArgumentValues();
        args.addIndexedArgumentValue( 0, new ArrayList() );
        args.addIndexedArgumentValue( 1, Boolean.TRUE );
        args.addIndexedArgumentValue( 2, new RuntimeBeanReference("parent") );
        RootBeanDefinition parent = 
            new RootBeanDefinition(StaticApplicationContext.class,null,null);
        parent.setInitMethodName("refresh");
        beanRef.registerBeanDefinition( "parent", parent );
        beanRef.registerBeanDefinition(
                "test",
                new RootBeanDefinition(CtorOmeroContext.class, args, null));
        beanRef.refresh();
        beanRef.getBean("test");
    }
    
    @Test( groups = "ticket:116")
    public void test_refreshOrdering() throws Exception
    {

        // checks for refresh calls.
        Collector c = new Collector();
        
        // a way to access this
        Map map  = new HashMap();
        map.put(C,c);
        ConstructorArgumentValues args = new ConstructorArgumentValues();
        args.addGenericArgumentValue( map );

        // set our static variable
        System.getProperties().setProperty( FOO, BAR );
        
        // our parent who has the PropertyPlaceholder
        StaticApplicationContext parent = new StaticApplicationContext( );
        parent.registerBeanDefinition( 
                "parentListener",
                new RootBeanDefinition(ParentListener.class, args, null));
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        parent.addBeanFactoryPostProcessor( ppc );
        parent.refresh();
        parent.publishEvent( new ContextRefreshedEvent( parent ));
        assertTrue( c.parentRefreshed );

        // Ok that worked now moving on.
        c.reset();
        // and see if reset works
        assertFalse( c.parentRefreshed );
        assertFalse( c.childRefreshed );

        // our child who depends on the PropertyPlaceholders in the parent
        StaticApplicationContext child = new StaticApplicationContext(parent);
        child.registerBeanDefinition(
                "childListener",
                new RootBeanDefinition(ChildListener.class, args, null));

        ConstructorArgumentValues stringArgs = new ConstructorArgumentValues();
        stringArgs.addGenericArgumentValue("${foo}"); 
        child.registerBeanDefinition(
                "string",
                new RootBeanDefinition(String.class, stringArgs, null));
        child.refresh();
        assertTrue( c.childRefreshed );
        assertFalse( c.parentRefreshed );
        
        // ignoring for the moment
        // assertTrue( (String) child.getBean("string"), BAR.equals( child.getBean( "string" )));
        
    }

    @Test( groups = "ticket:116")
    public void test_refreshOrdering_withOmeroContext() throws Exception
    {
        
        OmeroContext ctx = OmeroContext.getInstance(ContextTest.class.getName());
        Map map  = (Map) ctx.getBean("map");
        Collector c = (Collector) map.get(ContextTest.C);
        // initial test
        c.reset();
        assertFalse( c.childRefreshed );
        assertFalse( c.parentRefreshed );
        // test refresh()
        ctx.refresh();
        assertTrue( c.childRefreshed );
        assertFalse( c.parentRefreshed );
        // test refreshAll
        c.reset();
        ctx.refreshAll();
        map = (Map) ctx.getBean("map");         // have to re-get the collector 
        c = (Collector) map.get(ContextTest.C); // since a new one was created.
        assertTrue( c.childRefreshed );         // on parent refresh.
        assertTrue( c.parentRefreshed );
    }
        
}

class Collector {
    public void reset() {
        parentRefreshed = false;
        childRefreshed = false;
    }
    public boolean parentRefreshed = false;
    public boolean childRefreshed = false;
}

abstract class AbstractListener 
implements ApplicationListener, ApplicationContextAware {
        protected Map m;
        protected ApplicationContext ctx;
        public AbstractListener( Map map ){ m = map; }
        public boolean isRefresh( ApplicationEvent event) {
            if ( event instanceof ContextRefreshedEvent 
                    && ((ContextRefreshedEvent) event)
                    .getApplicationContext().equals( ctx ))
            {
                
                return true;
            }
            return false;
        }
        public void setApplicationContext(
                ApplicationContext applicationContext) throws BeansException
        {
            this.ctx = applicationContext;
        }

}

class ParentListener extends AbstractListener
{
    public ParentListener( Map map ) { super(map); }
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (isRefresh(event))
        ((Collector) m.get(ContextTest.C)).parentRefreshed = true;
    }
}

class ChildListener extends AbstractListener
{
    public ChildListener( Map map ) { super(map); }
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (isRefresh(event))
        ((Collector) m.get(ContextTest.C)).childRefreshed = true;
    }
}

class CtorOmeroContext extends OmeroContext {

    public CtorOmeroContext
    (String configLocation) 
    throws BeansException
    {
        super(configLocation);
        throw new RuntimeException("don't use single string");
    }
    
    public CtorOmeroContext
    (String[] configLocations) 
    throws BeansException
    {
        super(configLocations);
        throw new RuntimeException("don't use string array");
    }

    public CtorOmeroContext
    (String[] configLocations, boolean refresh) 
    throws BeansException
    {
        super(configLocations,refresh);
        throw new RuntimeException("don't use string array, boolean");
    }

    public CtorOmeroContext
    (String[] configLocations, ApplicationContext parent) 
    throws BeansException
    {
        super(configLocations,parent);
        throw new RuntimeException("don't use string array, parent");
    }
    
    public CtorOmeroContext
    (String[] configLocations, boolean refresh, ApplicationContext parent) 
    throws BeansException
    {
        super(configLocations,refresh,parent);
    }
    
}