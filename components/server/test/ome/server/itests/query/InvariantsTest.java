package ome.server.itests.query;

import java.util.List;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import ome.api.IQuery;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;
import ome.server.itests.ConfigHelper;


public class InvariantsTest
        extends AbstractManagedContextTest
{

    public void testExperimenterShouldAlwaysExist() throws Exception
    {
    
        Experimenter root = (Experimenter) 
            iQuery.getUniqueByFieldEq(Experimenter.class,"id",0L);
        
        assertNotNull("Root has to be define.",root);
        // FIXME assertNotNull("And it should have details",root.getDetails());
        
        List<Experimenter> l = iQuery.queryList("from Experimenter",null);
        
        assertTrue("If root is defined, can't be empty",l.size()>0);
        
    }
    
}
