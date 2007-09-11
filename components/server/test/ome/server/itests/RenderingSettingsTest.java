package ome.server.itests;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.quantum.QuantumFactory;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class RenderingSettingsTest extends AbstractManagedContextTest {

	private IRenderingSettings rsx;

	private IPixels pix;

	// =========================================================================
	// ~ Testng Adapter
	// =========================================================================
	@Override
	@Configuration(beforeTestMethod = true)
	public void adaptSetUp() throws Exception {
		super.setUp();
	}

	@Override
	@Configuration(afterTestMethod = true)
	public void adaptTearDown() throws Exception {
		super.tearDown();
	}

	// =========================================================================

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		rsx = factory.getRenderingSettingsService();

	}

	@Test
	public void test() {
		Long from = 1L;
		Long to = 2L;

		System.err.println("RESULT" + rsx.applySettingsToImage(from, to));

	}
}
