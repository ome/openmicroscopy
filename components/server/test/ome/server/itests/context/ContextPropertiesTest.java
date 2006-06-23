package ome.server.itests.context;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.model.meta.Experimenter;

@Test(
        groups = {"integration","ignore"}
)
public class ContextPropertiesTest extends AbstractDependencyInjectionSpringContextTests
{

    HibernateTemplate template;
    
    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception
    {
        super.tearDown();
    }
    // =========================================================================
    
    @Override
    protected String[] getConfigLocations()
    {
        return new String[]{
                "ome/services/out-of-container.xml",
                "ome/services/hibernate.xml",
                "ome/server/itests/context/ContextPropertiesTest.xml"}; 
        // sets initial url (for *first* call)
    }
    
    @Test
    public void test_bad_db() throws Exception
    {
        template = doGetTemplate("jdbc:postgresql://localhost/blah");
        testTemplate(template,"bad db");
    }
    
    @Test
    public void test_bad_server() throws Exception
    {
        template = doGetTemplate("jdbc:postgresql://blah");
        testTemplate(template,"bad server");
    }

    @Test
    public void test_wrong_db_type() throws Exception
    {
        template = doGetTemplate("jdbc:mysql://localhost/ome-meta");
        testTemplate(template,"bad db type");
    }
    
    @Test
    public void test_bad_protocol() throws Exception
    {
        template = doGetTemplate("jdbxx:postgresql://localhost/ome-meta");
        testTemplate(template,"bad protocol");
    }

    // ~ Private helpers
    // =========================================================================
    
    private HibernateTemplate doGetTemplate(String url)
    {
        HibernateTemplate ht = 
            (HibernateTemplate) applicationContext.getBean("hibernateTemplate");
        setUrl(url);
        this.setDirty();
        return ht;
    }
    
    // sets url for SUBSEQUENT call
    private void setUrl(String url)
    {
        System.getProperties().setProperty("hibernate.connection.url",url);
    }

    private void testTemplate(HibernateTemplate template, String msg)
    {        
        try {
            template.get(Experimenter.class,0);
            fail("Should not work with:"+msg);
        } catch (Exception e) {
            // ok.
        }
    }

}
