package ome.server.utests.sec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.logic.QueryImpl;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.AdminAction;
import ome.security.BasicSecuritySystem;
import ome.security.JBossLoginModule;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.system.SimpleEventContext;
import ome.testing.MockServiceFactory;
import ome.tools.hibernate.OmeroInterceptor;
import ome.tools.hibernate.SecurityFilter;
import ome.util.IdBlock;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class OmeroInterceptorTest extends SecuritySystemTest {
	
	// TODO subclassing here duplicates the tests. Need abstract super class.

	OmeroInterceptor oi;
	
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        oi = new OmeroInterceptor(sec);
    }
    
	
    // ~ TESTS
	// =========================================================================
    
	@Test
	public void testSQLDoesntNeedFrom() throws Exception {
		String t;
		t = oi.onPrepareStatement("select p");
		t = oi.onPrepareStatement("select p from Project p");
		t = oi.onPrepareStatement("select p from Project p where\nx");
		t = oi.onPrepareStatement("select p from Project p where\n(x");
		t = oi.onPrepareStatement("select p from Project p where(x");
		String s =
		"select dataset0_.id as id142_, dataset0_.owner_id as owner2_142_, dataset0_.group_id as group3_1"+
		"42_, dataset0_.creation_id as creation4_142_, dataset0_.update_id as update5_142_, dataset0_.permissions as permissi6_142_, dataset0_.vers"+
		"ion as version142_, dataset0_.name as name142_, dataset0_.description as descript9_142_ from dataset dataset0_ where "+
		"( "+
		 "? OR "+
		 "(dataset0_.group_id in (?, ?)) OR "+
		 "(dataset0_.owner_id = ? AND (cast(dataset0_.permissions as bit(64)) & cast(1024 as bit(64))) = cast(1024 as bit(64))) OR "+
		 "(dataset0_.group_id in (?, ?) AND (cast(dataset0_.permissions as bit(64)) & cast(64 as bit(64))) = cast(64 as bit(64))) OR "+
		 "((cast(dataset0_.permissions as bit(64)) & cast(4 as bit(64))) = cast(4 as bit(64))) "+
		") and (dataset0_.id in (select projectdat1_.child from projectdatasetlink projectdat1_ where projectdat1_.parent=?)) limit ?";
		t = oi.onPrepareStatement(s);
	}
		
}
