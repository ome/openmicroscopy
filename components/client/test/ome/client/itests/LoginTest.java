package ome.client.itests;

import org.testng.annotations.*;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import junit.framework.TestCase;

import ome.api.IPojos;
import ome.api.IQuery;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test(groups = { "client", "integration" })
public class LoginTest extends TestCase {

	//	 for this to work, we need an empty local.properties
	@Test( groups = {"ignore","broken"}) 
	@ExpectedExceptions(RuntimeException.class)
	public void test_withPropertiesNull() throws Exception {
		Properties p = new Properties();
		ServiceFactory factory = new ServiceFactory(p);
		IQuery iQuery = factory.getQueryService();
		iQuery.get(Experimenter.class, 0L);
	}

	@Test
	@ExpectedExceptions(ome.conditions.ApiUsageException.class)
	public void test_withLoginNull() throws Exception {
		Login login = new Login(null, "b");
		ServiceFactory factory = new ServiceFactory(login);
		IQuery iQuery = factory.getQueryService();
		iQuery.get(Experimenter.class, 0L);
	}

	@Test
	public void test_withProps() throws Exception {
		Properties p = new Properties();
		p.setProperty("omero.user", "root");
		p.setProperty("omero.pass", "ome");
		ServiceFactory factory = new ServiceFactory(p);
		IQuery iQuery = factory.getQueryService();
		iQuery.get(Experimenter.class, 0L);
	}

	@Test
	public void test_withLogin() throws Exception {
		Login login = new Login("root", "ome");
		ServiceFactory factory = new ServiceFactory(login);
		IQuery iQuery = factory.getQueryService();
		iQuery.get(Experimenter.class, 0L);
	}
	
	@Test( groups = {"ticket:182"})
	public void testLoginWithUmask() throws Exception {
		Login login = new Login("root","ome");
		ServiceFactory factory = new ServiceFactory(login);
		factory.setUmask(Permissions.IMMUTABLE);
		Image i = new Image();
		i.setName(UUID.randomUUID().toString());
		Image test = factory.getUpdateService().saveAndReturnObject(i);
		assertFalse(test.getDetails().getPermissions().isGranted(Role.USER, Right.WRITE));
	}

	@Test( groups = {"ticket:297","broken"})
	public void testLoginWithGUEST() throws Exception {
		ServiceFactory factory = new ServiceFactory( Login.GUEST );
		factory.getQueryService().findAll( Image.class, null );
	}
	
}
