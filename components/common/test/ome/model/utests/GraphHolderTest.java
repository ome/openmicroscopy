package ome.model.utests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.testng.annotations.*;

import com.sun.corba.se.pept.encoding.InputObject;

import ome.model.acquisition.DyeLaser;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightSource;
import ome.model.internal.GraphHolder;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;

import junit.framework.TestCase;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

public class GraphHolderTest extends TestCase {

	GraphHolder gh;

	@Test
	public void testShouldNeverBeNull() throws Exception {
		DyeLaser dl = new DyeLaser();
		assertNotNull(dl.getGraphHolder());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(dl);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		DyeLaser test = (DyeLaser) ois.readObject();
		baos.close();
		oos.close();
		bais.close();
		ois.close();
		
		assertNotNull(test.getGraphHolder());
	}
}
