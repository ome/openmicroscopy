package ome.server.itests.sec;

import java.sql.SQLException;
import java.util.ArrayList;
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
import ome.model.core.Image;
import ome.model.enums.AcquisitionMode;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.tools.hibernate.SecurityFilter;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

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
		e = factory.getAdminService().createUser(e);
	
		tearDown();
    }

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
	public void testCannotCreateEnums() throws Exception {
		
		loginUser(e.getOmeName());

		AcquisitionMode test = new AcquisitionMode();
		test.setValue("ticket:157");
		factory.getUpdateService().saveObject(test);
	}

	@Test
	public void testCanCreateEnumsWithITypes() throws Exception {
		
		loginUser(e.getOmeName());

		AcquisitionMode test = new AcquisitionMode();
		test.setValue("ticket:157");
		fail("factory.getTypesService().createEnum()");
	}
	
}