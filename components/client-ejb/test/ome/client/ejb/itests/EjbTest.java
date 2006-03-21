package ome.client.ejb.itests;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.containers.Project;

public class EjbTest extends TestCase
{

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
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
        env.setProperty(Context.PROVIDER_URL, "jnp://localhost:1099/");
        env.setProperty(Context.SECURITY_PRINCIPAL, "josh");
        env.setProperty(Context.SECURITY_CREDENTIALS, "moore");
        InitialContext ctx = new InitialContext(env);
        ctx.addToEnvironment( "group", "ome");
        env.setProperty("group2","ome2");
        ctx.bind("group3","ome3");
        IUpdate iUpdate = lookupIUpdate(ctx);
        iUpdate.saveObject( new Project() );
    }

    public void testSimpleCall() throws Exception
    {
        Context ctx = new InitialContext();
        IUpdate iUpdate = lookupIUpdate(ctx);
        iUpdate.saveObject( new Project() );
    }    
    
}
