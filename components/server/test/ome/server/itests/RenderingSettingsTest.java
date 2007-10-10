package ome.server.itests;

import java.util.HashSet;
import java.util.Set;

import ome.api.IRenderingSettings;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.core.Image;

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
		rsx.resetDefaultsInImage(id);
		rsx.resetDefaultsInCategory(id);
		rsx.resetDefaultsInDataset(id);
	}
	
	@Test
	public void testSet() {
		Set<Long> s = new HashSet<Long>();
		s.add(1L);
		s.add(2L);
		rsx.resetDefaultsInSet(Image.class, s);
		rsx.resetDefaultsInSet(Category.class, s);
		rsx.resetDefaultsInSet(Dataset.class, s);
	}
	
}
