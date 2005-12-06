package ome.io.bdb.utests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.IO;
import ome.io.bdb.Datastore;

public class AbstractBdbTest extends AbstractDependencyInjectionSpringContextTests
{

    private static Log log = LogFactory.getLog(AbstractBdbTest.class);

    protected String[] getConfigLocations()
    {
        return new String[] { "bdb.xml" };
    }

    IO        io;

    Datastore db;

    protected void onSetUp() throws Exception
    {
        io = (IO) applicationContext.getBean("ioService");
        db = (Datastore) applicationContext.getBean("ioStore");
    }

    protected void onTearDown() throws Exception
    {
        this.setDirty();
    }
    
}
