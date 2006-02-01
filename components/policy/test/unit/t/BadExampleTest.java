package t;

import org.springframework.orm.hibernate3.HibernateTemplate;

import junit.framework.TestCase;


public class BadExampleTest extends TestCase
{

    public void testThreadStart() throws Exception
    {
        // ERROR new Thread().start();
    }

    public void testSpring() throws Exception
    {   
        // ERROR HibernateTemplate ht = new HibernateTemplate();
        // ERROR ht.find("hi");
    }
    
}
