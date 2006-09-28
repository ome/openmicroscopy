package ome.server.itests;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.*;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import junit.framework.TestCase;


@Test( groups = {"mockable","security","integration"} )
public class LoginTest extends TestCase
{

    protected void login(String user, String group, String eventType)
    {
        sec.login( new Principal( user, group, eventType ));
    }
    
    protected OmeroContext ctx;
    protected ServiceFactory sf;
    protected IQuery q;
    protected IUpdate u;
    protected SecuritySystem sec;

    @Configuration( beforeTestClass = true )
    public void config(){
        ctx = OmeroContext.getManagedServerContext();
        sf = new ServiceFactory( ctx );
        q = sf.getQueryService();
        u = sf.getUpdateService();
        sec = (SecuritySystem) ctx.getBean("securitySystem");
    }
    
  @Test
    public void testNoLoginThrowsException() throws Exception
    {
      sec.logout();
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
        assertTrue( sec.isEmptyEventContext() );
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
    
}
