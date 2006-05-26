package ome.server.itests.query;

import java.util.List;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.testng.annotations.Test;

import ome.api.IQuery;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.server.itests.ConfigHelper;


public class InvariantsTest
        extends AbstractManagedContextTest
{

    @Test
    public void testExperimenterShouldAlwaysExist() throws Exception
    {
    
        Experimenter root = (Experimenter) 
            iQuery.findByQuery(Experimenter.class.getName(),
                    new Parameters().addId(0L));
        
        assertNotNull("Root has to be defined.",root);
        // FIXME assertNotNull("And it should have details",root.getDetails());
        
        List<Experimenter> l = iQuery.findAllByQuery("from Experimenter",null);
        
        assertTrue("If root is defined, can't be empty",l.size()>0);
        
    }
    
}
