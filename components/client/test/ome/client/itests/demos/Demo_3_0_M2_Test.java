package ome.client.itests.demos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.*;

import javax.sql.DataSource;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawPixelsStore;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

@Test(
// "ignored" because it should only be run manually
groups = { "ignore", "manual", "client", "integration", "demo", "3.0", "3.0-M2" })
public class Demo_3_0_M2_Test extends TestCase {

	private static Log TESTLOG = LogFactory.getLog("TEST-"
			+ Demo_3_0_M2_Test.class.getName());

	static int Xmax = 1024, Ymax = 1024, Zmax = 24, Tmax = 120, Cmax = 3;

	ServiceFactory sf;

	IQuery iQuery;

	IUpdate up;

	DataSource ds;

	SimpleJdbcTemplate jdbc;

	@Configuration(beforeTestClass = true)
	public void config() {
		TESTLOG.info("INIT");
		sf = new ServiceFactory("ome.client.test");
		iQuery = sf.getQueryService();
		up = sf.getUpdateService();

		ds = (DataSource) sf.getContext().getBean("dataSource");
		jdbc = new SimpleJdbcTemplate(ds);

		TESTLOG.info("PSQL/bug649");
		try {
			iQuery.get(Experimenter.class, 0L);
		} catch (Exception e) {
			// ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
		}

	}

	@Test
	public void testRenderingEngineDataSavedOnlyManuallys() throws Exception {
		Pixels pix = ObjectFactory.createPixelGraph(null);
		pix.setSizeX(16);
		pix.setSizeY(16);
		pix.setSizeZ(1);
		pix.setSizeT(1);
		pix.setSizeC(1);
		pix = up.saveAndReturnObject(pix);

		RenderingEngine re = sf.createRenderingEngine();
		RawPixelsStore raw = sf.createRawPixelsStore();
		raw.setPixelsId(pix.getId());
		raw.calculateMessageDigest();
		TESTLOG.info(raw.getRowSize());
		TESTLOG.info("WAITING...");
		Thread.sleep(30L * 1000L);
	}

}
