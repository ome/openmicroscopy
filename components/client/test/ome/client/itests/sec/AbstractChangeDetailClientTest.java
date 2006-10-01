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
public class AbstractChangeDetailClientTest extends AbstractSecurityTest
{
	
	private String user_name  = "USER:"+UUID.randomUUID().toString();
	private String other_name = "OTHER:"+UUID.randomUUID().toString();
	private String world_name = "WORLD:"+UUID.randomUUID().toString();
	private String pi_name    = "PI:"+UUID.randomUUID().toString();
	private String pi_group   = "PIGRP:"+UUID.randomUUID().toString();
	
    protected Login asRoot, asUser, asOther, asWorld, asPI;

    protected Experimenter toRoot, toUser, toOther, toWorld, toPI;
    
    protected ExperimenterGroup toSystem, toUserGroup, toOtherGroup, toPIGroup;

    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestClass = true)
    public void createUsersAndImages() throws Exception
    {
        init();

        // TODO USE ROLES HERE
        toRoot = new Experimenter( 0L, false );        
        toSystem = new ExperimenterGroup( 0L, false ); 
        toUserGroup = new ExperimenterGroup( 1L, false ); 

        toUser = new Experimenter();
        toUser.setFirstName("test");
        toUser.setLastName("test");
        toUser.setOmeName(user_name);
        toUser = new Experimenter( rootAdmin.createUser( toUser ), false );
        
        toOther = new Experimenter();
        toOther.setFirstName("test");
        toOther.setLastName("test");
        toOther.setOmeName(other_name);
        toOther = new Experimenter( rootAdmin.createUser(toOther), false );

        toWorld = new Experimenter();
        toWorld.setFirstName("test");
        toWorld.setLastName("test");
        toWorld.setOmeName(world_name);
        toWorld = new Experimenter( rootAdmin.createUser(toWorld), false );

        toPI = new Experimenter();
        toPI.setFirstName("test");
        toPI.setLastName("test");
        toPI.setOmeName(pi_name);
        toPI = new Experimenter( rootAdmin.createUser(toPI), false );
        
        toOtherGroup = new ExperimenterGroup();
        toOtherGroup.setName(UUID.randomUUID().toString());
        toOtherGroup = new ExperimenterGroup( rootAdmin.createGroup( toOtherGroup ), false );

        toPIGroup = new ExperimenterGroup();
        toPIGroup.setName(pi_group);
        toPIGroup = new ExperimenterGroup( rootAdmin.createGroup( toPIGroup ), false );

        rootAdmin.addGroups(toUser, toPIGroup);
        rootAdmin.addGroups(toOther, toPIGroup);
        rootAdmin.addGroups(toPI, toPIGroup);
        rootAdmin.setGroupOwner(toPIGroup, toPI);
        
        asRoot  = super.rootLogin;
        asUser  = new Login( user_name,  "ome", pi_group, "Test" );
        asOther = new Login( other_name, "ome", pi_group, "Test" );
        asWorld = new Login( world_name, "ome" );
        asPI    = new Login( pi_name,    "ome" );
   
    }

    // ~ Helpers
    // =========================================================================
    protected Long managedImage( Login login )
    {
        ServiceFactory services = new ServiceFactory( login );
        Image i = new Image();
        i.setName( "test" );
        i = (Image) services.getUpdateService().saveAndReturnObject( i );
        // They need to actual belong to the right people
        assertEquals( rootAdmin.lookupExperimenter(login.getName()).getId(), 
        		rootQuery.get(Image.class, i.getId()).getDetails().getOwner().getId());
        return i.getId();
    }
    
    protected void createAsUserToOwner(
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
    
    protected void updateAsUserToOwner(
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

    protected void createAsUserToGroup(
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
    
    protected void updateAsUserToGroup(
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
