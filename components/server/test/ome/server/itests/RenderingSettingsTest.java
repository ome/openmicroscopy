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

		/*RenderingDef rdFrom = rsx.getRenderingSettings(from);
		QuantumDef qDefFrom = rdFrom.getQuantization();
		System.err.println("From " + qDefFrom.getBitResolution() + " "
				+ qDefFrom.getCdStart() + " " + qDefFrom.getCdEnd());
		List wavesFrom = rdFrom.getWaveRendering();
		ChannelBinding bFrom;
		for (Iterator i = wavesFrom.iterator(); i.hasNext();) {
			bFrom = (ChannelBinding) i.next();
			System.err.println("CHB From" + bFrom.getActive() + " "
					+ bFrom.getCoefficient() + " "
					+ bFrom.getFamily().getValue() + " "
					+ bFrom.getInputStart() + " " + bFrom.getInputEnd() + " "
					+ bFrom.getNoiseReduction() + " "
					+ bFrom.getColor().getAlpha() + " "
					+ bFrom.getColor().getBlue() + " "
					+ bFrom.getColor().getGreen() + " "
					+ bFrom.getColor().getRed());
		}

		RenderingDef rdToOld = rsx.getRenderingSettings(to);
		QuantumDef qDef1 = rdToOld.getQuantization();
		System.err.println("To old " + qDef1.getBitResolution() + " "
				+ qDef1.getCdStart() + " " + qDef1.getCdEnd());
		List wavesToOld = rdToOld.getWaveRendering();
		ChannelBinding bToOld;
		for (Iterator i = wavesToOld.iterator(); i.hasNext();) {
			bToOld = (ChannelBinding) i.next();
			System.err.println("CHB Old " + bToOld.getActive() + " "
					+ bToOld.getCoefficient() + " "
					+ bToOld.getFamily().getValue() + " "
					+ bToOld.getInputStart() + " " + bToOld.getInputEnd() + " "
					+ bToOld.getNoiseReduction() + " "
					+ bToOld.getColor().getAlpha() + " "
					+ bToOld.getColor().getBlue() + " "
					+ bToOld.getColor().getGreen() + " "
					+ bToOld.getColor().getRed());
		}*/

		//System.err.println("RESALT" + rsx.applySettingsToImage(from, to));

		System.err.println("RESALT" + rsx.getRenderingSettings(1L));
		
		/*RenderingDef rdToNew = rsx.getRenderingSettings(to);
		QuantumDef qDef2 = rdToNew.getQuantization();
		System.err.println("To new " + qDef2.getBitResolution() + " "
				+ qDef2.getCdStart() + " " + qDef2.getCdEnd());

		List wavesToNew = rdToNew.getWaveRendering();
		ChannelBinding bToNew;
		for (Iterator i = wavesToNew.iterator(); i.hasNext();) {
			bToNew = (ChannelBinding) i.next();
			System.err.println("CHB New " + bToNew.getActive() + " "
					+ bToNew.getCoefficient() + " "
					+ bToNew.getFamily().getValue() + " "
					+ bToNew.getInputStart() + " " + bToNew.getInputEnd() + " "
					+ bToNew.getNoiseReduction() + " "
					+ bToNew.getColor().getAlpha() + " "
					+ bToNew.getColor().getBlue() + " "
					+ bToNew.getColor().getGreen() + " "
					+ bToNew.getColor().getRed());
		}
*/
	}
}
