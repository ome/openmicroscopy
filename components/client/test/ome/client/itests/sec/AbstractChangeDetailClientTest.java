package ome.client.itests.sec;

import java.util.UUID;

import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security","ticket:52", "init"} 
)
public class AbstractChangeDetailClientTest extends TestCase
{
    
	private ServiceFactory tmp = new ServiceFactory();
	
    protected Login
    asRoot = (Login) tmp.getContext().getBean("rootLogin"),
    asUser = new Login( UUID.randomUUID().toString(), "ome" ),
    other = new Login( UUID.randomUUID().toString(), "ome" );

    protected Experimenter toRoot, toUser, toOther;
    
    protected ExperimenterGroup toSystem, toUserGroup, toOtherGroup;
    
    protected Long rootImage, userImage, otherImage;

    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestClass = true)
    public void setUp() throws Exception
    {
        super.setUp();
        
        ServiceFactory rootServices = new ServiceFactory( asRoot );
        IQuery iQuery = rootServices.getQueryService();
        IUpdate iUpdate = rootServices.getUpdateService();
        IAdmin iAdmin = rootServices.getAdminService();
        
        try 
        {
            iQuery.get(Experimenter.class,0l);
        } catch (Throwable t){
            // TODO no, no, really. This is ok. (And temporary) 
        }
        
        toRoot = new Experimenter( 0L, false );        
        toSystem = new ExperimenterGroup( 0L, false ); 
        toUserGroup = new ExperimenterGroup( 1L, false ); 
        toOtherGroup = new ExperimenterGroup( 2L, false ); 

        toUser = new Experimenter();
        toUser.setFirstName("test");
        toUser.setLastName("test");
        toUser.setOmeName(asUser.getName());
        toUser = iAdmin.createUser( toUser );
        toUser.unload();
        
        toOther = new Experimenter();
        toOther.setFirstName("test");
        toOther.setLastName("test");
        toOther.setOmeName(other.getName());
        toOther = iAdmin.createUser(toOther);
        toOther.unload();
        
        toOtherGroup = new ExperimenterGroup();
        toOtherGroup.setName(UUID.randomUUID().toString());
        toOtherGroup = iAdmin.createGroup( toOtherGroup );
        toOther.unload();
        
        rootImage = getManageImageId( asRoot );
        userImage = getManageImageId( asUser );
        otherImage = getManageImageId( other );
    }

    // ~ Helpers
    // =========================================================================
    private Long getManageImageId( Login login )
    {
        ServiceFactory services = new ServiceFactory( login );
        Image i = new Image();
        i.setName( "test" );
        i = (Image) services.getUpdateService().saveAndReturnObject( i );
        return i.getId();
    }

    protected <E extends Exception> void verify(Exception e, Class<E>[] exs)
    {	
    	boolean match = false;
		for (Class<E> ex : exs) {
			if (ex.isAssignableFrom(e.getClass()))
			{
				match = true;
			}
		}
		if (!match) throw new RuntimeException("Unexected exception thrown.",e);
    }
    
    protected <E extends Exception> void createAsUserToOwner(
    		Login login, Experimenter owner) 
    throws ValidationException
    {

			ServiceFactory factory = new ServiceFactory(login);
			IUpdate iUpdate = factory.getUpdateService();
			Image i = new Image();
			i.setName("test");
			i.getDetails().setOwner(owner);
			iUpdate.saveObject(i);
     
    }
    
    protected <E extends Exception> void updateAsUserToOwner(
            Long imageId, Login login, Experimenter owner)
    throws ValidationException
    {

			ServiceFactory factory = new ServiceFactory( login );
			IQuery iQuery = factory.getQueryService();
			Image i = (Image) iQuery.get( Image.class, imageId );
			i.getDetails().setOwner( owner );
			IUpdate iUpdate = factory.getUpdateService();
			iUpdate.saveObject( i );

    }

    protected <E extends Exception> void createAsUserToGroup(
    		Login login, ExperimenterGroup group)
    throws ValidationException
    {

			ServiceFactory factory = new ServiceFactory( login );
			IUpdate iUpdate = factory.getUpdateService();
			Image i = new Image();
			i.setName( "test" );
			i.getDetails().setGroup( group );
			iUpdate.saveObject( i );

    }
    
    protected <E extends Exception> void updateAsUserToGroup(
            Long imageId, Login login, ExperimenterGroup group)
    throws ValidationException
    {

			ServiceFactory factory = new ServiceFactory( login );
			IQuery iQuery = factory.getQueryService();
			Image i = (Image) iQuery.get( Image.class, imageId );
			i.getDetails().setGroup( group);
			IUpdate iUpdate = factory.getUpdateService();
			iUpdate.saveObject( i );

    }


    
}
