package ome.server.itests.sec;

import ome.conditions.InternalException;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = {"security"})
public class DisablingTest extends AbstractManagedContextTest {

	@Test
	public void testSimpleDisabling() throws Exception {
		loadSucceeds();
		securitySystem.disable("load");
		loadFails();
		securitySystem.enable();
		loadSucceeds();
	}

	@Test
	public void testGetsReset() throws Exception {
		securitySystem.disable("load");
		loadFails(); // this implicitly resets
		assertFalse( securitySystem.isDisabled("load") );
	}
	
	// ~ Helpers
	// =========================================================================

	private void loadSucceeds() {
		iQuery.get(Experimenter.class, 0L);
	}

	private void loadFails() {
		try {
			loadSucceeds();
		} catch (InternalException ie) {
			// good.
		}
	}


}