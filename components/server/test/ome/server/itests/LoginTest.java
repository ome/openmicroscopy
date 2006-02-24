package ome.server.itests;

import java.util.Properties;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.security.CurrentDetails;
import ome.system.OmeroContext;

import junit.framework.TestCase;


public class LoginTest extends TestCase
{

    protected void login(String user, String group, String eventType)
    {
        Properties p = System.getProperties();
        p.setProperty("omero.username",user);
        p.setProperty("omero.groupname",group);
        p.setProperty("omero.eventtype",eventType);
    }

    @Override
    protected void setUp() throws Exception
    {
        serverSideLogout();
    }

    protected void serverSideLogout()
    {
        CurrentDetails.setOwner(null);
        CurrentDetails.setGroup(null);
        CurrentDetails.setCreationEvent(null);
    }
    
    protected OmeroContext ctx = OmeroContext.getManagedServerContext();
    protected IQuery q = (IQuery) ctx.getBean("queryService");
    protected IUpdate u = (IUpdate) ctx.getBean("updateService");
    
    public void testNoLoginThrowsException() throws Exception
    {
        try {
            q.getById(Experimenter.class,0l);
            fail("Non-logged-in call allowed!");
        } catch (RuntimeException e){
            // ok.
        }
    }
    
    public void testLoggedInAllowed() throws Exception
    {
        login("root","system","Test");
        q.getById(Experimenter.class,0l);
    }
    
    public void testLoginWithInvalidThrowsException() throws Exception
    {
        try {
            login("unknown2349akljf9q283","system","Test");
            q.getById(Experimenter.class,0l);
            fail("Login allowed with unknown user.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
        
        try {
            login("unknown","baba9o38023984019","Test");
            q.getById(Experimenter.class,0l);
            fail("Login allowed with unknown group.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
            
        try {
            login("root","system","blarg23498239048230");
            q.getById(Experimenter.class,0l);
            fail("Login allowed with unknown type.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
            
        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
    }
    
}
