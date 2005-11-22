package ome.server.itests;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.testing.OMEData;

public class OMEDataTest extends AbstractDependencyInjectionSpringContextTests
{

    protected String[] getConfigLocations()
    {
        return ConfigHelper.getDataConfigLocations();
    }

    DataSource ds;

    public void setDataSource(DataSource source)
    {
        ds = source;
    }

    public void testDefaultOMEData()
    {
        OMEData data = new OMEData();
        data.setDataSource(ds);
        List l;
        Object o;

        l = data.get("unknown.value");
        assertNull(l);
        
        l = data.get("simple.test");
        for (Iterator it = l.iterator(); it.hasNext();)
        {
            Long n = (Long) it.next();
            assertTrue("simple.test is 1..5", n.longValue() > 0
                    && n.longValue() < 6);
        }

        l = data.get("project.count");
        assertTrue(l + " should have one element", l.size() == 1);
        assertTrue(l + "'s single element should be a number, not"
                + l.get(0).getClass(), l.get(0) instanceof Number);

        o = data.getFirst("project.count");
        assertTrue("getFirst and get(0) should return the same value", l.get(0)
                .equals(o));

        l = data.get("project.ids");
        assertTrue("database should have project ids", l.size() > 0);
        assertTrue("all project ids should be number, not"
                + l.get(0).getClass(), l.get(0) instanceof Number);

    }

}
