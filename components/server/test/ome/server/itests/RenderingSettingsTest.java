package ome.server.itests;

import ome.api.IRenderingSettings;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class RenderingSettingsTest extends AbstractManagedContextTest {

	private IRenderingSettings rsx;

	// =========================================================================

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		rsx = factory.getRenderingSettingsService();
	}

	@Test
	public void testApply() {
		Long from = 1L;
		Long to = 2L;

		rsx.applySettingsToImage(from, to);
		rsx.applySettingsToProject(from, to);
		rsx.applySettingsToDataset(from, to);
		rsx.applySettingsToCategory(from, to);
	}

	@Test
	public void testReset() {
		Long id = 2L;
		rsx.resetDefaultsToImage(id);
		rsx.resetDefaultsToCategory(id);
		rsx.resetDefaultsToDataSet(id);
	}
}
