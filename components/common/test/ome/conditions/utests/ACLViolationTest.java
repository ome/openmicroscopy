package ome.conditions.utests;

import org.testng.annotations.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.conditions.acl.ACLDeleteViolation;
import ome.conditions.acl.ACLLoadViolation;
import ome.conditions.acl.ACLViolation;
import ome.conditions.acl.CollectedACLViolations;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;

import junit.framework.TestCase;


public class ACLViolationTest extends TestCase
{

    @Test
    @ExpectedExceptions( CollectedACLViolations.class )
    public void testCollectionACLViolationToStringTest() throws Exception {
		CollectedACLViolations coll = new CollectedACLViolations("test");
		ACLViolation[] array = {
				new ACLLoadViolation(Image.class,1L,"can't load img"),
				new ACLDeleteViolation(Project.class,2L,"can't delete prj")
		};
		for (int i = 0; i < array.length; i++) {
			coll.addViolation(array[i]);
		}
		coll.setStackTrace(new CollectedACLViolations(null).getStackTrace());
		throw coll;
	}

  
}
