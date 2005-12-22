package ome.client.ejb.itests;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import ome.ro.ejb.Generic;

public class EjbTest extends TestCase
{

    public void testSimpleCall() throws Exception
    {
        
        Context ctx = new InitialContext();
        Generic g = (Generic) ctx.lookup(Generic.class.getName());
        g.run("from Object");
        
    }
    
}
