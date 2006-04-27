package ome.client.itests;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.system.ServiceFactory;


public class VersionIncrementTest extends TestCase
{
        ServiceFactory sf = new ServiceFactory();
        IQuery iQuery = sf.getQueryService();
        IUpdate iUpdate = sf.getUpdateService();

        Project p = new Project(), p2;
        Dataset d = new Dataset(), d2;

        protected void setUp() throws Exception
        {
            p.setName(NAME);
            d.setName(NAME);
            p.linkDataset( d );
            
            p = (Project) iUpdate.saveAndReturnObject( p );
            d = (Dataset) p.linkedDatasetIterator().next();

            p.setName( p.getName()+" updated.");
            p2 = (Project) iUpdate.saveAndReturnObject( p );
            d2 = (Dataset) p2.linkedDatasetIterator().next();
        }
        public final static String NAME = "vers++"+new java.util.Date();
        
        public void test_link_versions_shouldnt_increase() throws Exception
        {
            assertTrue( d.getVersion().equals( d2.getVersion() ));
        }

        public void test_if_version_increases_exception() throws Exception
        {
            d.setName( d.getName() + "updated.");
            try {
                iUpdate.saveAndReturnObject( d );
                fail("Should have thrown");
            } catch (Exception e) {
                // good
            }
            
        }
        
        public void test_if_versions_do_increase_let_me_override() throws Exception
        {
            d.setName( d.getName() + "updated.");
            d.setVersion( d2.getVersion() );
            iUpdate.saveAndReturnObject( d );
            
        }
        
}
