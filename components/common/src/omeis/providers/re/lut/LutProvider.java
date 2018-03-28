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
package omeis.providers.re.lut;

import java.util.List;

import ome.api.ServiceInterface;
import ome.model.display.ChannelBinding;


/**
 * Interface for lookup table providers.
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.4.1
 */
public interface LutProvider extends ServiceInterface {

    /**
     * Returns the list of lookup table readers that can be used by a set
     * of channel bindings.
     *
     * @param channelBindings Array of channel bindings to constrain the
     * lookup tables readers by.
     * @return See above. The size of the collection will be equal to, and
     * ordered by, the number of active channel bindings. <code>null</code>
     * will be inserted when either no lookup table has been requested or
     * can be found for that channel binding.
     */
    List<LutReader> getLutReaders(ChannelBinding[] channelBindings);

}
