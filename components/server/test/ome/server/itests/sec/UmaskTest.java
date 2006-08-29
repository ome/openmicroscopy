package ome.server.itests.sec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;
import ome.util.IdBlock;

@Test( groups = {"ticket:182", "ticket:307"} )
public class UmaskTest extends AbstractManagedContextTest
{

	// ~ Tickets
	// =========================================================================
	
	public void testSoftIsRemovedOnTransientDetails() throws Exception {
		Project p = new Project();
		p.setName("ticket:307");
		assertNull( p.getDetails().getPermissions());
		p = iUpdate.saveAndReturnObject(p);
		assertNotNull( p.getDetails().getPermissions());
		assertFalse( p.getDetails().getPermissions().isSet(Flag.SOFT));
	}
	
}
