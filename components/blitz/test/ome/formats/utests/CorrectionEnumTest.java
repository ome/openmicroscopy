package ome.formats.utests;

import java.util.HashMap;

import ome.formats.enums.handler.EnumHandlerFactory;
import ome.formats.enums.handler.EnumerationHandler;
import omero.model.CorrectionI;
import omero.model.IObject;
import omero.model.Correction;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CorrectionEnumTest extends TestCase
{
	private EnumerationHandler handler;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		EnumHandlerFactory factory = new EnumHandlerFactory();
		handler = factory.getHandler(Correction.class);
	}

	@Test
	public void testPlanApoMatch()
	{
		HashMap<String, IObject> enumerations = new HashMap<String, IObject>();
		enumerations.put("PlanApo", new CorrectionI());
		IObject enumeration = handler.findEnumeration(enumerations, "PlApo");
		assertNotNull(enumeration);
		enumeration = handler.findEnumeration(enumerations, "PlApo");
		assertNotNull(enumeration);
		enumeration = handler.findEnumeration(enumerations, "  PlApo");
		assertNotNull(enumeration);
		enumeration = handler.findEnumeration(enumerations, "PlApo   ");
		assertNotNull(enumeration);
		enumeration = handler.findEnumeration(enumerations, "PlanApochromat");
		assertNotNull(enumeration);
		enumeration = handler.findEnumeration(enumerations, "PlanApo");
		assertNotNull(enumeration);
	}
}
