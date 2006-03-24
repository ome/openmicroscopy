package ome.client.ejb.itests;

import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.system.Principal;

public class EjbTest extends TestCase
{

    InitialContext ctx;
    
    protected void setUp() throws Exception
    {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
        env.put(Context.PROVIDER_URL, "jnp://localhost:1099/");
        env.put(Context.SECURITY_PRINCIPAL, new Principal("josh","user","Test"));
        env.put(Context.SECURITY_CREDENTIALS, "ome");
        ctx = new InitialContext(env);
    }
    
    private IUpdate lookupIUpdate(Context ctx) throws NamingException
    {
        String pkg = "omero-app-3.0-alpha-SNAPSHOT/";
        String api = "UpdateBean";
        String und = "/remote";
        IUpdate iUpdate = (IUpdate) ctx.lookup( 
                pkg+api+und);
        return iUpdate;
    }
    
    public void test_withLogin() throws Exception
    {
        IUpdate iUpdate = lookupIUpdate(ctx);
        Project p = new Project();
        p.setName("ejb test:"+new Date());
        iUpdate.saveObject( p );
    }
    
}
