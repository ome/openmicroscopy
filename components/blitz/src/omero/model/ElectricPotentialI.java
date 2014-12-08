/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
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

package omero.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import ome.model.ModelBased;
import ome.units.unit.Unit;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.xml.model.enums.EnumerationException;

import omero.model.enums.UnitsElectricPotential;

/**
 * Blitz wrapper around the {@link ome.model.units.ElectricPotential} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class ElectricPotentialI extends ElectricPotential implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("AV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("AV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("AV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("AV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("AV:FV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("AV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("AV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("AV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("AV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("AV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("AV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("AV:NV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("AV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("AV:PV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("AV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("AV:V", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("AV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("AV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("AV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("AV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("CV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CV:V", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DV:V", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FV:NV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FV:PV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FV:V", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HV:V", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KV:V", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROV:V", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MV:V", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("NV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NV:V", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PV:NV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PV:V", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("V:AV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("V:CV", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("V:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("V:DV", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("V:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("V:FV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("V:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("V:HV", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("V:KV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("V:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("V:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("V:MV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("V:NV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("V:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("V:PV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("V:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("V:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("V:YV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("V:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("V:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YV:AV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YV:FV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YV:NV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YV:PV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YV:V", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAV:AV", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAV:CV", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAV:DV", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAV:FV", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAV:HV", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAV:KV", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAV:MV", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAV:NV", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAV:PV", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAV:V", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAV:ZV", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZV:AV", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZV:CV", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZV:DAV", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZV:DV", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZV:EXAV", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZV:FV", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZV:GIGAV", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZV:HV", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZV:KV", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZV:MEGAV", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZV:MICROV", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZV:MV", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZV:NV", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZV:PETAV", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZV:PV", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZV:TERAV", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZV:V", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZV:YOTTAV", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZV:YV", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZV:ZETTAV", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsElectricPotential, String> SYMBOLS;
    static {
        Map<UnitsElectricPotential, String> s = new HashMap<UnitsElectricPotential, String>();
        s.put(UnitsElectricPotential.AV, "aV");
        s.put(UnitsElectricPotential.CV, "cV");
        s.put(UnitsElectricPotential.DAV, "daV");
        s.put(UnitsElectricPotential.DV, "dV");
        s.put(UnitsElectricPotential.EXAV, "EV");
        s.put(UnitsElectricPotential.FV, "fV");
        s.put(UnitsElectricPotential.GIGAV, "GV");
        s.put(UnitsElectricPotential.HV, "hV");
        s.put(UnitsElectricPotential.KV, "kV");
        s.put(UnitsElectricPotential.MEGAV, "MV");
        s.put(UnitsElectricPotential.MICROV, "µV");
        s.put(UnitsElectricPotential.MV, "mV");
        s.put(UnitsElectricPotential.NV, "nV");
        s.put(UnitsElectricPotential.PETAV, "PV");
        s.put(UnitsElectricPotential.PV, "pV");
        s.put(UnitsElectricPotential.TERAV, "TV");
        s.put(UnitsElectricPotential.V, "V");
        s.put(UnitsElectricPotential.YOTTAV, "YV");
        s.put(UnitsElectricPotential.YV, "yV");
        s.put(UnitsElectricPotential.ZETTAV, "ZV");
        s.put(UnitsElectricPotential.ZV, "zV");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsElectricPotential unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new ElectricPotentialI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsElectricPotential makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsElectricPotential
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad ElectricPotential unit: " + unit, e);
        }
    }

    public static ome.units.quantity.ElectricPotential makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units =
                ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.ElectricPotential(d, units);
    }

   /**
    * FIXME: this should likely take a default so that locations which don't
    * want an exception can have
    *
    * log.warn("Using new PositiveFloat(1.0)!", e); return new
    * PositiveFloat(1.0);
    *
    * or similar.
    */
   public static ome.units.quantity.ElectricPotential convert(ElectricPotential t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsElectricPotential.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsElectricPotential units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units2 =
               ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.ElectricPotential(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public ElectricPotentialI() {
        super();
    }

    public ElectricPotentialI(double d, UnitsElectricPotential unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public ElectricPotentialI(double d,
            Unit<ome.units.quantity.ElectricPotential> unit) {
        this(d, ome.model.enums.UnitsElectricPotential.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given ome-xml enum
    */
   public ElectricPotentialI(ElectricPotential value, Unit<ome.units.quantity.ElectricPotential> ul) {
       this(value,
            ome.model.enums.UnitsElectricPotential.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given ome.model enum
    */
   public ElectricPotentialI(double d, ome.model.enums.UnitsElectricPotential ul) {
        this(d, UnitsElectricPotential.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.ElectricPotential}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public ElectricPotentialI(ElectricPotential value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            double[][] coeffs = conversions.get(source + ":" + target);
            if (coeffs == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            double orig = value.getValue();
            double k, p, v;
            if (coeffs.length == 0) {
                v = orig;
            } else if (coeffs.length == 2){
                k = coeffs[0][0];
                p = coeffs[0][1];
                v = Math.pow(k, p);

                k = coeffs[1][0];
                p = coeffs[1][1];
                v += Math.pow(k, p) * orig;
            } else {
                throw new RuntimeException("coefficients of unknown length: " +  coeffs.length);
            }

            setValue(v);
            setUnit(UnitsElectricPotential.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public ElectricPotentialI(ElectricPotential value, UnitsElectricPotential target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public ElectricPotentialI(ome.units.quantity.ElectricPotential value) {
        ome.model.enums.UnitsElectricPotential internal =
            ome.model.enums.UnitsElectricPotential.bySymbol(value.unit().getSymbol());
        UnitsElectricPotential ul = UnitsElectricPotential.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsElectricPotential getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsElectricPotential unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public ElectricPotential copy(Ice.Current ignore) {
        ElectricPotentialI copy = new ElectricPotentialI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.ElectricPotential) {
            ome.model.units.ElectricPotential t = (ome.model.units.ElectricPotential) model;
            this.value = t.getValue();
            this.unit = UnitsElectricPotential.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "ElectricPotential cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsElectricPotential ut = ome.model.enums.UnitsElectricPotential.valueOf(getUnit().toString());
        ome.model.units.ElectricPotential t = new ome.model.units.ElectricPotential(getValue(), ut);
        return t;
    }

    // ~ Java overrides
    // =========================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ElectricPotential(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElectricPotential other = (ElectricPotential) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

