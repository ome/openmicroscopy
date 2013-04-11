/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ome.io.nio.PixelsService;
import ome.services.blitz.test.AbstractServantTest;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.OriginalMetadataResponse;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class OriginalMetadataRequestTest extends AbstractServantTest {

	@Override
	@BeforeClass
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected OriginalMetadataResponse assertRequest(
			final OriginalMetadataRequestI req, Map<String, String> ctx) {

		final Status status = new Status();

		@SuppressWarnings("unchecked")
		List<Object> rv = (List<Object>) user.ex.execute(ctx, user
				.getPrincipal(), new Executor.SimpleWork(this, "testRequest") {
			@Transactional(readOnly = false)
			public List<Object> doWork(Session session, ServiceFactory sf) {

				// from HandleI.steps()
				List<Object> rv = new ArrayList<Object>();

				Helper helper = new Helper((Request) req, status,
						getSqlAction(), session, sf);
				req.init(helper);

				int j = 0;
				while (j < status.steps) {
					try {
						rv.add(req.step(j));
					} catch (Cancel c) {
						throw c;
					} catch (Throwable t) {
						throw helper.cancel(new ERR(), t, "bad-step", "step",
								"" + j);
					}
					j++;
				}

				return rv;
			}
		});

		// Post-process
		for (int step = 0; step < status.steps; step++) {
			Object obj = rv.get(step);
			req.buildResponse(step, obj);
		}

		Response rsp = req.getResponse();
		if (rsp instanceof ERR) {
			fail(rsp.toString());
		}

		return (OriginalMetadataResponse) rsp;
	}

	@Test
	public void testFileset() throws Exception {
		OriginalMetadataRequestI req = new OriginalMetadataRequestI(
				(PixelsService) user.ctx.getBean("/OMERO/Pixels"));
		req.imageId = makeImage(); // FAILING
		OriginalMetadataResponse rsp = assertRequest(req, null);
	}

}
