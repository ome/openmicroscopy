package ome.server.itests;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.*;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.security.CurrentDetails;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;

import junit.framework.TestCase;


@Test( groups = {"mockable","security","integration"} )
public class LoginTest extends TestCase
{

    protected void login(String user, String group, String eventType)
    {
        ec.setPrincipal( new Principal( user, group, eventType ));
    }
    
    protected OmeroContext ctx;
    protected IQuery q;
    protected IUpdate u;
    protected EventContext ec;

    //@Configuration( beforeTest = true )
    public void config(){
        ctx = OmeroContext.getManagedServerContext();
        q = (IQuery) ctx.getBean("queryService");
        u = (IUpdate) ctx.getBean("updateService");
        ec = (EventContext) ctx.getBean("eventContext");
    }
    
  @Test
    public void testNoLoginThrowsException() throws Exception
    {
      ec.setPrincipal( null );
        try {
            q.find(Experimenter.class,0l);
            fail("Non-logged-in call allowed!");
        } catch (RuntimeException e){
            // ok.
        }
    }
    
  @Test
    public void testLoggedInAllowed() throws Exception
    {
        login("root","system","Test");
        q.find(Experimenter.class,0l);
    }

  @Test
    public void testLoggedOutAfterCall() throws Exception
    {
        login("root","system","Test");
        q.find(Experimenter.class,0l);
        assertNull(CurrentDetails.getOwner());
        assertNull(CurrentDetails.getGroup());
        assertNull(CurrentDetails.getCreationEvent());
    }
    
  @Test
    public void testLoginWithInvalidThrowsException() throws Exception
    {
        try {
            login("unknown2349akljf9q283","system","Test");
            q.find(Experimenter.class,0l);
            fail("Login allowed with unknown user.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
        
        try {
            login("unknown","baba9o38023984019","Test");
            q.find(Experimenter.class,0l);
            fail("Login allowed with unknown group.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
            
        try {
            login("root","system","blarg23498239048230");
            q.find(Experimenter.class,0l);
            fail("Login allowed with unknown type.");
        } catch (RuntimeException r){} 
            // TODO Otherexception
            
        
    }
    
    @Override
  @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
    }
    
}
