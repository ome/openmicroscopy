/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 Glencoe Software, Inc.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeis.providers.re.utests;

import java.util.ArrayList;
import java.util.List;

import ome.annotations.RolesAllowed;
import ome.model.display.ChannelBinding;
import omeis.providers.re.lut.LutProvider;
import omeis.providers.re.lut.LutReader;


/**
 * Test lookup table provider implementation.
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.4.1
 */
public class TestLutProvider implements LutProvider {

    /* (non-Javadoc)
     * @see omeis.providers.re.lut.LutProvider#getLutReaders(ome.model.display.ChannelBinding[])
     */
    @RolesAllowed("user")
    public List<LutReader> getLutReaders(ChannelBinding[] channelBindings) {
        List<LutReader> lutReaders = new ArrayList<LutReader>();
        for (ChannelBinding cb : channelBindings) {
            if (cb.getActive()) {
                lutReaders.add(null);
            }
        }
        return lutReaders;
    }

}
