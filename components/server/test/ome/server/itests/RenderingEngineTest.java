package ome.server.itests;

import java.util.concurrent.locks.ReadWriteLock;

import org.testng.annotations.Test;

import omeis.providers.re.RenderingEngine;

@Test( enabled = false, groups = {"broken", "ticket:119","ticket:120" } )
public class RenderingEngineTest extends AbstractManagedContextTest
{

    RenderingEngine            re;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        re = (RenderingEngine) applicationContext.getBean("renderService");
    }

    @Test
    public void test_simple_usage() throws Exception
    {
        re.lookupPixels(1L);
        re.lookupRenderingDef(1L);
    }
    
    @Test( groups = {"unfinished","ignore"} )
    public void test_multi_txs() throws Exception
    {
        ReadWriteLock rwl = null;
    }

}
