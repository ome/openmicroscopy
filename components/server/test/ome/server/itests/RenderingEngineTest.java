package ome.server.itests;

import java.util.concurrent.locks.ReadWriteLock;

import omeis.providers.re.RenderingEngine;

public class RenderingEngineTest extends AbstractManagedContextTest
{

    RenderingEngine            re;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        re = (RenderingEngine) applicationContext.getBean("renderService");
    }

    public void test_simple_usage() throws Exception
    {
        re.lookupPixels(1L);
        re.lookupRenderingDef(1L);
    }
    
    public void test_multi_txs() throws Exception
    {
        ReadWriteLock rwl = null;
        fail();
    }

}
