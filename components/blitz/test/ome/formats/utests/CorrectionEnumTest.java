/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
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
