/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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

package ome.services.ldapsync;

import java.io.File;

import javax.naming.directory.ModificationItem;

import ome.services.ldap.IntegrationFixture;
import ome.system.OmeroContext;

public class SyncFixture
    extends IntegrationFixture
{

    public SyncFixture(File ctxFile, OmeroContext ctx) {
        super(ctxFile, ctx);
    }

    /**
     * In order to insert our logic into the {@link #testLdiffFile(File)}
     * method, we're going to override assertPasses.
     */
    @Override
    public void assertGoodPasses()
        throws Exception
    {
        super.assertGoodPasses();

        for (Modification mod : getBeansOfType(Modification.class).values())
        {
            // After the modification, an exception will be thrown
            // if the proper response from OMERO is not encountered.
            mod.modify(this);
        }

    }

    /**
     * All testing takes place in the {@link Modification}.
     */
    @Override
    public void assertBadFails()
    {
        // no-op

    }

    public void modifyAttributes(String string, ModificationItem[] mods)
    {
        data.modifyAttributes(string, mods);
    }
}
