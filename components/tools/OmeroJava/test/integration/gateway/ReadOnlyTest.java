/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration.gateway;

import java.util.Map;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the gateway methods {@link omero.gateway.Gateway#getReadOnlyStatus(omero.gateway.SecurityContext)} and
 * {@link omero.gateway.Gateway#isAnyReadOnly(omero.gateway.SecurityContext, String...)}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.6
 */
public class ReadOnlyTest extends GatewayTest {

    private static final SecurityContext ctx = new SecurityContext(-1);

    /**
     * Test that {@code "db"} and {@code "repo"} compose the queryable subsystems among the
     * {@code omero.cluster.read_only.runtime.*} properties from
     * {@link omero.gateway.Gateway#getReadOnlyStatus(omero.gateway.SecurityContext)} and that
     * {@link omero.gateway.Gateway#isAnyReadOnly(omero.gateway.SecurityContext, String...)} returns results consistent with their
     * property values.
     * @throws DSOutOfServiceException unexpected
     */
    @Test
    public void testConsistentResults() throws DSOutOfServiceException {
        final Map<String, Boolean> readOnlyStatus = gw.getReadOnlyStatus(ctx);
        Assert.assertEquals(readOnlyStatus.keySet(), ImmutableSet.of("db", "repo"));

        final boolean isReadOnlyDb   = readOnlyStatus.get("db");
        final boolean isReadOnlyRepo = readOnlyStatus.get("repo");
        Assert.assertEquals(gw.isAnyReadOnly(ctx), false);
        Assert.assertEquals(gw.isAnyReadOnly(ctx, "db"), isReadOnlyDb);
        Assert.assertEquals(gw.isAnyReadOnly(ctx, "repo"), isReadOnlyRepo);
        Assert.assertEquals(gw.isAnyReadOnly(ctx, "db", "repo"), isReadOnlyDb || isReadOnlyRepo);
    }

    /**
     * Test that in the absence of corresponding {@code omero.cluster.read_only.runtime.*} properties as from a pre-5.4.6 server
     * the queryable subsystems are reported as being read-write.
     * @throws DSOutOfServiceException unexpected
     */
    @Test
    public void testBackwardCompatibility() throws DSOutOfServiceException {
        final String absentSubsystem = "absent subsystem";
        Assert.assertFalse(gw.getReadOnlyStatus(ctx).containsKey(absentSubsystem));
        Assert.assertFalse(gw.isAnyReadOnly(ctx, absentSubsystem));
    }
}
