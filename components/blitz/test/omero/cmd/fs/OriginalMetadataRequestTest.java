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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ome.io.nio.PixelsService;
import ome.services.blitz.test.AbstractServantTest;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.RString;
import omero.RType;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.OriginalMetadataResponse;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@Test(groups = "integration")
public class OriginalMetadataRequestTest extends AbstractServantTest {

    private static final ImmutableMap<String, String> expectedGlobalMetadata;
    private static final ImmutableMap<String, String> expectedSeriesMetadata;

    static {
        Builder<String, String> builder;
        builder = ImmutableMap.builder();
        builder.put("a=b", "c");
        builder.put("(a=b)", "c");
        builder.put("a", "(b=c)");
        builder.put("{a=b", "c}");
        builder.put("{(a=b)", "c}");
        builder.put("{a", "(b=c)}");
        builder.put("(p=q", "r");
        builder.put("p=q", "r)");
        builder.put("((p=q)", "r");
        builder.put("p", "(q=r))");
        builder.put("p q", "r s");
        expectedGlobalMetadata = builder.build();
        builder = ImmutableMap.builder();
        builder.put("ein må lære seg å krype før ein lærer å gå", "learn to walk before you can run");
        builder.put("money doesn't grow on trees", "pengar växer inte på träd");
        builder.put("аб", "вг");
        expectedSeriesMetadata = builder.build();
    }

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
						status.currentStep = j;
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

    /**
     * Test that pre-FS original_metadata.txt files are parsed as expected,
     * including selection of which "=" to split at, and non-ASCII characters.
     * @throws FileNotFoundException if the test INI-style file is not accessible
     */
    @Test
    public void testMetadataParsing() throws FileNotFoundException {
        final OriginalMetadataRequestI request = new OriginalMetadataRequestI(null);
        request.init(new Helper(request, new Status(), null, null, null));
        request.parseOriginalMetadataTxt(ResourceUtils.getFile("classpath:original_metadata.txt"));
        request.buildResponse(0, null);
        final OriginalMetadataResponse response = (OriginalMetadataResponse) request.getResponse();
        final Map<String, String> actualGlobalMetadata = new HashMap<String, String>();
        for (final Entry<String, RType> keyValue : response.globalMetadata.entrySet()) {
            actualGlobalMetadata.put(keyValue.getKey(), ((RString) keyValue.getValue()).getValue());
        }
        Assert.assertTrue(CollectionUtils.isEqualCollection(expectedGlobalMetadata.entrySet(), actualGlobalMetadata.entrySet()));
        final Map<String, String> actualSeriesMetadata = new HashMap<String, String>();
        for (final Entry<String, RType> keyValue : response.seriesMetadata.entrySet()) {
            actualSeriesMetadata.put(keyValue.getKey(), ((RString) keyValue.getValue()).getValue());
        }
        Assert.assertTrue(CollectionUtils.isEqualCollection(expectedSeriesMetadata.entrySet(), actualSeriesMetadata.entrySet()));
    }
}
