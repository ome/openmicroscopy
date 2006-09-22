package ome.server.utests.sec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.BasicSecuritySystem;
import ome.system.Principal;
import ome.testing.MockServiceFactory;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;

public abstract class AbstractBasicSecuritySystemTest extends MockObjectTestCase {

	MockServiceFactory sf;
	BasicSecuritySystem sec; 
	
	// login information
	Principal p;
	
	// "current" details
	Experimenter user;
	ExperimenterGroup group;
	EventType type;
	Event event;
	List<Long> leaderOfGroups, memberOfGroups;
	
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sf = new MockServiceFactory();
        sec = new BasicSecuritySystem(sf);
           
    }
    
    protected void prepareMocksWithUserDetails(boolean readOnly)
    {
        // login
    	p = new Principal("test","test","test");
        sec.login(p);
        
        // context 
		user = new Experimenter(1L);
		group = new ExperimenterGroup(1L);
		type = new EventType(1L);
		event = new Event(1L);

		user.linkExperimenterGroup(group);
		leaderOfGroups = Collections.singletonList(1L);
		memberOfGroups = Collections.singletonList(1L);

		prepareMocks(readOnly);
    }

    protected void prepareMocksWithRootDetails(boolean readOnly)
    {
        // login
    	p = new Principal("root","system","internal");
    	sec.login(p);
    	
        // context 
		user = new Experimenter(0L);
		group = new ExperimenterGroup(0L);
		type = new EventType(0L);
		event = new Event(0L);

		user.linkExperimenterGroup(group);
		leaderOfGroups = Collections.singletonList(0L);
		memberOfGroups = Arrays.asList(0L,1L);
		prepareMocks(readOnly);
    }
    
    protected void prepareMocks(boolean readOnly)
    {
        // prepare mocks
		sf.mockAdmin = mock(LocalAdmin.class);
		sf.mockTypes = mock(ITypes.class);
		sf.mockUpdate = mock(LocalUpdate.class);
    
		sf.mockAdmin.expects(atLeastOnce()).method("userProxy")
			.will( returnValue( user ));
		sf.mockAdmin.expects(atLeastOnce()).method("groupProxy")
			.will( returnValue( group ));
		sf.mockAdmin.expects(atLeastOnce()).method("getMemberOfGroupIds")
		.will( returnValue( memberOfGroups ));
		sf.mockAdmin.expects(atLeastOnce()).method("getLeaderOfGroupIds")
			.will( returnValue( leaderOfGroups ));
		sf.mockTypes.expects(atLeastOnce()).method("getEnumeration")
			.will(returnValue( type ));
		if (!readOnly)
		{
			sf.mockUpdate.expects(atLeastOnce()).method("saveAndReturnObject")
				.will(returnValue( event ));
		}
    }
    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        sec.clearCurrentDetails();
        super.tearDown();
    }

}
