package ome.server.itests.query;

import java.util.List;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import ome.api.IQuery;
import ome.model.meta.Experimenter;
import ome.server.itests.ConfigHelper;


public class InvariantsTest
        extends AbstractTransactionalDataSourceSpringContextTests
{

    @Override
    protected String[] getConfigLocations()
    {
        return ConfigHelper.getConfigLocations();
    }

    protected IQuery _q;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        _q = (IQuery) applicationContext.getBean("queryService");
    }
    
    public void testExperimenterShouldAlwaysExist() throws Exception
    {
    
        Experimenter root = new Experimenter();
        root.setId(0l);
        root = (Experimenter) _q.getUniqueByExample(root);
        
        assertNotNull("Root has to be define.",root);
        assertNotNull("And it should have details",root.getDetails());
        assertNotNull("And that should have counts",root.getDetails().getCounts());
        
        List<Experimenter> l = _q.queryList("from Experimenter",null);
        
        assertTrue("If root is defined, can't be empty",l.size()>0);
        
    }
    
}
