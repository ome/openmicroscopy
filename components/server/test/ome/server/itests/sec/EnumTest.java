package ome.server.itests.sec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.test.annotation.ExpectedException;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.conditions.SecurityViolation;
import ome.model.core.Image;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
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

@Test(groups = { "ticket:140", "ticket:145", "ticket:226",
		"security", "filter" })
public class EnumTest extends AbstractManagedContextTest {

	Experimenter e = new Experimenter();

    @Configuration(beforeTestClass = true)
    public void createData() throws Exception{
		setUp();
		
		loginRoot();
		
		e = new Experimenter();
		e.setOmeName(UUID.randomUUID().toString());
		e.setFirstName("enums");
		e.setLastName("enums");
		e = factory.getAdminService().createUser(e);
	
		tearDown();
    }

	@Test
	public void testEnumsAreReloaded() throws Exception {
		
		loginUser(e.getOmeName());
		
		DimensionOrder test = new DimensionOrder();
		test.setValue("XYZCT");
		factory.getUpdateService().saveObject(test);
	
	}

}