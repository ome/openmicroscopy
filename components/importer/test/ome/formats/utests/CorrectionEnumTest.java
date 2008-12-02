package ome.formats.utests;

import java.util.HashMap;

import ome.formats.enums.handler.EnumHandlerFactory;
import ome.formats.enums.handler.EnumerationHandler;
import ome.model.IEnum;
import ome.model.enums.Correction;
import junit.framework.TestCase;

public class CorrectionEnumTest extends TestCase
{
	private EnumerationHandler handler;
	
	@Override
	protected void setUp() throws Exception
	{
		EnumHandlerFactory factory = new EnumHandlerFactory();
		handler = factory.getHandler(Correction.class);
	}

	public void testPlanApoMatch()
	{
		HashMap<String, IEnum> enumerations = new HashMap<String, IEnum>();
		enumerations.put("PlanApo", new Correction());
		IEnum enumeration = handler.findEnumeration(enumerations, "PlApo");
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
