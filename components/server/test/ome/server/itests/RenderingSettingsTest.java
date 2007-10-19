package ome.server.itests;

import java.util.Collections;
import java.util.List;

import ome.api.IRenderingSettings;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.RenderingDef;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

public class RenderingSettingsTest extends AbstractManagedContextTest {

	private IRenderingSettings rsx;

	private Pixels p1;

	private Pixels p2;

	// =========================================================================

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		rsx = factory.getRenderingSettingsService();

		p1 = makePixels();
		p2 = makePixels();

	}

	private Pixels makePixels() {
		Pixels p = ObjectFactory.createPixelGraph(null);
		p.setDefaultPixels(true);

		RenderingDef rdef = ObjectFactory.createRenderingDef();
		CodomainMapContext enhancement = ObjectFactory
				.createPlaneSlicingContext();
		ChannelBinding binding = ObjectFactory.createChannelBinding();

		List enhancements = Collections.singletonList(enhancement);
		List bindings = Collections.singletonList(binding);

		rdef.setWaveRendering(bindings);
		rdef.setSpatialDomainEnhancement(enhancements);

		p.addRenderingDef(rdef);
		p = factory.getUpdateService().saveAndReturnObject(p);
		return p;

	}

	@Test
	public void testApply() {

		Long from = p1.getId();
		Long to = p2.getId();

		rsx.applySettingsToPixel(from, to);

		Image img = iQuery.get(Image.class, to);
		rsx.resetDefaultsInImage(img.getId());
	}

	@Test
	public void testApplyToCollections() {

		Project p1 = new Project();
		p1.setName("prtest1");
		Project p2 = new Project();
		p2.setName("prtest2");
		Dataset d1 = new Dataset();
		d1.setName("dstest1");
		Dataset d2 = new Dataset();
		d2.setName("dstest2");
		p1.linkDataset(d1);
		p2.linkDataset(d2);

		// and using proxies works
		p1 = iUpdate.saveAndReturnObject(p1);
		//p1 = new Project(p1.getId(), false);
		p2 = iUpdate.saveAndReturnObject(p2);
		//p2 = new Project(p2.getId(), false);

		d1 = iUpdate.saveAndReturnObject(d1);
		//d1 = new Dataset(d1.getId(), false);
		d2 = iUpdate.saveAndReturnObject(d2);
		//d2 = new Dataset(d2.getId(), false);
		
		ProjectDatasetLink link1 = new ProjectDatasetLink();
		link1.link(p1, d1);
		ProjectDatasetLink link2 = new ProjectDatasetLink();
		link2.link(p2, d2);

		DatasetImageLink ilink1 = new DatasetImageLink();
		ilink1.link(d1, iQuery.get(Image.class, p1.getId()));
		DatasetImageLink ilink2 = new DatasetImageLink();
		ilink2.link(d2, iQuery.get(Image.class, p2.getId()));

		iUpdate.saveAndReturnObject(link1);
		iUpdate.saveAndReturnObject(link2);

		iUpdate.saveAndReturnObject(ilink1);
		iUpdate.saveAndReturnObject(ilink2);

		rsx.applySettingsToDataset(d1.getId(), d2.getId());
		rsx.applySettingsToProject(p1.getId(), p2.getId());

	}

}
