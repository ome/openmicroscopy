package ome.server.itests.sec;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.SecurityViolation;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import ome.server.itests.AbstractManagedContextTest;


@Test(groups = { "ticket:156", "ticket:157", "security", "filter" })
public class SystemTypesTest extends AbstractManagedContextTest {

	static String ticket156 = "ticket:156";

	Experimenter e = new Experimenter();

    @Configuration(beforeTestClass = true)
    public void createData() throws Exception{
    	setUp();
		
		loginRoot();
		
		e = new Experimenter();
		e.setOmeName(UUID.randomUUID().toString());
		e.setFirstName(ticket156);
		e.setLastName(ticket156);
		e = factory.getAdminService().getExperimenter(
				factory.getAdminService().createUser(e));
	
		tearDown();
    }
    
    // ~ Admin types
	// =========================================================================

	@Test
	@ExpectedExceptions( SecurityViolation.class )
	public void testCannotCreateExperimenter() throws Exception {
		
		loginUser(e.getOmeName());
		
		Experimenter test = new Experimenter();
		test.setOmeName(UUID.randomUUID().toString());
		test.setFirstName(ticket156);
		test.setLastName(ticket156);
		factory.getUpdateService().saveObject(test);
	}

	@Test
	@ExpectedExceptions( SecurityViolation.class )
	public void testCannotCreateGroup() throws Exception {
		
		loginUser(e.getOmeName());
		
		ExperimenterGroup test = new ExperimenterGroup();
		test.setName(UUID.randomUUID().toString());
		factory.getUpdateService().saveObject(test);
	}
		
	// ~ Events
	// =========================================================================
	
	@Test
	@ExpectedExceptions( SecurityViolation.class )
	public void testCannotCreateEvents() throws Exception {
		
		loginUser(e.getOmeName());
		
		Event test = new Event();
		test.setTime( new Timestamp( System.currentTimeMillis()) );
		test.setStatus("hi");
		test.setType(new EventType(0L,false));
		test.setExperimenter(new Experimenter(0L,false));
		test.setExperimenterGroup(new ExperimenterGroup(0L,false));
		factory.getUpdateService().saveObject(test);
	}
	
	@Test
	@ExpectedExceptions( SecurityViolation.class )
	public void testCannotCreateEventLogs() throws Exception {
		
		loginUser(e.getOmeName());
		
		EventLog test = new EventLog();
		test.setAction("BOINK");
		test.setEvent(new Event(0L,false));
		test.setEntityId(1L);
		test.setEntityType("ome.model.Something");
		factory.getUpdateService().saveObject(test);
	}
		
	// ~ Enums
	// =========================================================================
	
	@Test
	@ExpectedExceptions( SecurityViolation.class )
	public void testCannotCreateEnumsWithIUpdate() throws Exception {
		
		loginUser(e.getOmeName());

		AcquisitionMode test = new AcquisitionMode();
		test.setValue("ticket:157");
		factory.getUpdateService().saveObject(test);
	}

	@Test
	public void testRootCanCreateEnumsWithIUpdate() throws Exception {

		loginRoot();

		AcquisitionMode test = new AcquisitionMode();
		test.setValue("ticket:157/"+uuid());
		factory.getUpdateService().saveObject(test);
	}
	
	@Test
	public void testCanCreateEnumsWithITypes() throws Exception {
		
		loginUser(e.getOmeName());

		AcquisitionMode test = new AcquisitionMode();
		test.setValue("ticket:157/"+uuid());
		factory.getTypesService().createEnumeration(test);
	}
	
}