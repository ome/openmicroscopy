package ome.client.itests.sec;


import javax.ejb.EJBException;

import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security","ticket:52", "chown" }
)
public class ChownClientTest extends AbstractChangeDetailClientTest
{

    // design parameters:
    //  1. new or existing object (belonging to root, user, or other)
    //  2. as user or root
    //  3. changing to root, user, or other 
    
    // ~ AS USER TO ROOT
    // =========================================================================
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_NewImageAsUserChownToROOT() throws Exception
    {
        createAsUserToOwner( asUser, toRoot );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_UserImageAsUserChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asUser, toRoot );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_OtherImageAsUserChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asUser, toRoot );
    }
    
    @Test
    // This should be ok since it already belongs to root.
    public void test_RootImageAsUserChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asUser, toRoot );
    }

    // ~ AS USER TO USER
    // =========================================================================
    @Test
    public void test_NewImageAsUserChownToUSER() throws Exception
    {
        createAsUserToOwner( asUser, toUser );
    }
    
    @Test
    public void test_UserImageAsUserChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asUser, toUser );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_OtherImageAsUserChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asUser, toUser );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_RootImageAsUserChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asUser, toUser );
    }
    
    // ~ AS USER TO OTHER
    // =========================================================================
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_NewImageAsUserChownToOTHER() throws Exception
    {
        createAsUserToOwner( asUser, toOther );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_UserImageAsUserChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asUser, toOther );
    }
    
    @Test
    public void test_OtherImageAsUserChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asUser, toOther );
    }
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_RootImageAsUserChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asUser, toOther );
    }

    
    // ~ AS ROOT TO USER
    // =========================================================================
    @Test
    public void test_NewImageAsRootChownToUSER() throws Exception
    {
        createAsUserToOwner( asRoot, toUser );
    }
    
    @Test
    public void test_UserImageAsRootChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asRoot, toUser );
    }
    
    @Test
    public void test_OtherImageAsRootChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asRoot, toUser );
    }
    
    @Test
    public void test_RootImageAsRootChownToUSER() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asRoot, toUser );
    }

    
    // ~ AS ROOT TO OTHER
    // =========================================================================
    @Test
    public void test_NewImageAsRootChownToOTHER() throws Exception
    {
        createAsUserToOwner( asRoot, toOther );
    }
    
    @Test
    public void test_UserImageAsRootChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asRoot, toOther );
    }
    
    @Test
    public void test_OtherImageAsRootChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asRoot, toOther );
    }
    
    @Test
    public void test_RootImageAsRootChownToOTHER() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asRoot, toOther );
    }

    // ~ AS ROOT TO ROOT
    // =========================================================================
    
    @Test
    public void test_NewImageAsRootChownToROOT() throws Exception
    {
        createAsUserToOwner( asRoot, toRoot );
    }
    
    @Test
    public void test_UserImageAsRootChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asUser), asRoot, toRoot );
    }
    
    @Test
    public void test_OtherImageAsRootChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asOther), asRoot, toRoot );
    }
    
    @Test
    public void test_RootImageAsRootChownToROOT() throws Exception
    {
        updateAsUserToOwner( managedImage(asRoot), asRoot, toRoot );
    }
    
}
