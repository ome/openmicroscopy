package ome.client.ejb.itests;

import javax.naming.Context;
import javax.naming.InitialContext;

import ome.api.IQuery;

import junit.framework.TestCase;

public class EjbTest extends TestCase
{

    public void testSimpleCall() throws Exception
    {
        
        Context ctx = new InitialContext();
        IQuery g = (IQuery) ctx.lookup(IQuery.class.getName());
        g.queryList("from Object",null);
        
    }
    
}
