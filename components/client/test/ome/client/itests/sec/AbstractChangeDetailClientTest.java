package ome.client.itests.sec;

import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security","ticket:52", "init"} 
)
public class AbstractChangeDetailClientTest extends TestCase
{
    
    protected Login
    asRoot = new Login( "root", "ome" ),
    asUser = new Login( "user1", "ome" ),
    other = new Login( "user2", "ome" );

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
        
        try 
        {
            iQuery.get(Experimenter.class,0l);
        } catch (Throwable t){
            // TODO no, no, really. This is ok. (And temporary) 
        }
        
        toRoot = (Experimenter) iQuery.get( Experimenter.class, 0L );
        
        toUser = (Experimenter) iQuery.findByString( 
                Experimenter.class, "omeName", asUser.getName() );
        
        toOther = (Experimenter) iQuery.findByString( 
                Experimenter.class, "omeName", other.getName() );
        
        toSystem = (ExperimenterGroup) 
        iQuery.get( ExperimenterGroup.class, 0L );
        
        toUserGroup = (ExperimenterGroup) 
        iQuery.get( ExperimenterGroup.class, 1L );

        toOtherGroup = (ExperimenterGroup)
        iQuery.find( ExperimenterGroup.class, 2L );
        
        ExperimenterGroup userGroupProxy = new ExperimenterGroup( 1L, false );
        
        if (toUser == null)
        {
            toUser = new Experimenter();
            toUser.setFirstName("test");
            toUser.setLastName("test");
            toUser.setOmeName(asUser.getName());
            GroupExperimenterMap map = new GroupExperimenterMap();
            map.setDefaultGroupLink( Boolean.TRUE );
            map.link( userGroupProxy, toUser );
            toUser.addGroupExperimenterMap( map,false );
            toUser = (Experimenter) iUpdate.saveAndReturnObject( toUser );
            rootServices.getAdminService().synchronizeLoginCache();
        }
        
        if (toOther == null)
        {
            toOther = new Experimenter();
            toOther.setFirstName("test");
            toOther.setLastName("test");
            toOther.setOmeName(other.getName());
            GroupExperimenterMap map = new GroupExperimenterMap();
            map.setDefaultGroupLink( Boolean.TRUE );
            map.link( userGroupProxy, toOther );
            toOther.addGroupExperimenterMap( map,false );
            toOther = (Experimenter) iUpdate.saveAndReturnObject( toOther );
            rootServices.getAdminService().synchronizeLoginCache();
        }

        if (toOtherGroup == null)
        {
            toOtherGroup = new ExperimenterGroup();
            toOtherGroup.setName("test");
            toOtherGroup = (ExperimenterGroup)
            iUpdate.saveAndReturnObject( toOtherGroup );
            rootServices.getAdminService().synchronizeLoginCache();
        }
        
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

    protected void createAsUserToOwner(Login login, Experimenter owner) 
    throws ValidationException
    {
        ServiceFactory factory = new ServiceFactory( login );
        IUpdate iUpdate = factory.getUpdateService();
        Image i = new Image();
        i.setName( "test" );
        i.getDetails().setOwner( owner );
        iUpdate.saveObject( i );
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

    protected void createAsUserToGroup(Login login, ExperimenterGroup group) 
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
