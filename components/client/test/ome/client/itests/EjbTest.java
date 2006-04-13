package ome.client.itests;

import java.util.Date;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.system.ServiceFactory;

public class EjbTest extends TestCase
{

    ServiceFactory sf = new ServiceFactory();

    public void test_withLogin() throws Exception
    {
        IUpdate iUpdate = sf.getUpdateService();
        Project p = new Project();
        p.setName("ejb test:"+new Date());
        iUpdate.saveObject( p );
    }
    
}
