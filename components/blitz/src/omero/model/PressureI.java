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

import omero.model.enums.UnitsPressure;

/**
 * Blitz wrapper around the {@link ome.model.units.Pressure} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PressureI extends Pressure implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, double[][]> conversions;
    static {
        Map<String, double[][]> c = new HashMap<String, double[][]>();

        c.put("APA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("APA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("APA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("APA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("APA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("APA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("APA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("APA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("APA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("APA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("APA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("APA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("APA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("APA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("APA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("APA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("APA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("APA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("APA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("APA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("CPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("NPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("PA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("PA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("PA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("PA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("ZPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsPressure, String> SYMBOLS;
    static {
        Map<UnitsPressure, String> s = new HashMap<UnitsPressure, String>();
        s.put(UnitsPressure.APA, "aPa");
        s.put(UnitsPressure.ATM, "atm");
        s.put(UnitsPressure.BAR, "bar");
        s.put(UnitsPressure.CBAR, "cbar");
        s.put(UnitsPressure.CPA, "cPa");
        s.put(UnitsPressure.DAPA, "daPa");
        s.put(UnitsPressure.DBAR, "dbar");
        s.put(UnitsPressure.DPA, "dPa");
        s.put(UnitsPressure.EXAPA, "EPa");
        s.put(UnitsPressure.FPA, "fPa");
        s.put(UnitsPressure.GIGAPA, "GPa");
        s.put(UnitsPressure.HPA, "hPa");
        s.put(UnitsPressure.KBAR, "kBar");
        s.put(UnitsPressure.KPA, "kPa");
        s.put(UnitsPressure.MBAR, "mbar");
        s.put(UnitsPressure.MEGABAR, "Mbar");
        s.put(UnitsPressure.MEGAPA, "MPa");
        s.put(UnitsPressure.MICROPA, "µPa");
        s.put(UnitsPressure.MMHG, "mm Hg");
        s.put(UnitsPressure.MPA, "mPa");
        s.put(UnitsPressure.MTORR, "mTorr");
        s.put(UnitsPressure.NPA, "nPa");
        s.put(UnitsPressure.PA, "Pa");
        s.put(UnitsPressure.PETAPA, "PPa");
        s.put(UnitsPressure.PPA, "pPa");
        s.put(UnitsPressure.PSI, "psi");
        s.put(UnitsPressure.TERAPA, "TPa");
        s.put(UnitsPressure.TORR, "Torr");
        s.put(UnitsPressure.YOTTAPA, "YPa");
        s.put(UnitsPressure.YPA, "yPa");
        s.put(UnitsPressure.ZETTAPA, "ZPa");
        s.put(UnitsPressure.ZPA, "zPa");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsPressure unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PressureI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsPressure makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsPressure
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Pressure unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Pressure makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Pressure> units =
                ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Pressure(d, units);
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
   public static ome.units.quantity.Pressure convert(Pressure t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsPressure.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsPressure units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Pressure> units2 =
               ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Pressure(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public PressureI() {
        super();
    }

    public PressureI(double d, UnitsPressure unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public PressureI(double d,
            Unit<ome.units.quantity.Pressure> unit) {
        this(d, ome.model.enums.UnitsPressure.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome-xml enum
    */
   public PressureI(Pressure value, Unit<ome.units.quantity.Pressure> ul) {
       this(value,
            ome.model.enums.UnitsPressure.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given ome.model enum
    */
   public PressureI(double d, ome.model.enums.UnitsPressure ul) {
        this(d, UnitsPressure.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Pressure}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public PressureI(Pressure value, String target) {
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
            setUnit(UnitsPressure.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public PressureI(Pressure value, UnitsPressure target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public PressureI(ome.units.quantity.Pressure value) {
        ome.model.enums.UnitsPressure internal =
            ome.model.enums.UnitsPressure.bySymbol(value.unit().getSymbol());
        UnitsPressure ul = UnitsPressure.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsPressure getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPressure unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Pressure copy(Ice.Current ignore) {
        PressureI copy = new PressureI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Pressure) {
            ome.model.units.Pressure t = (ome.model.units.Pressure) model;
            this.value = t.getValue();
            this.unit = UnitsPressure.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Pressure cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsPressure ut = ome.model.enums.UnitsPressure.valueOf(getUnit().toString());
        ome.model.units.Pressure t = new ome.model.units.Pressure(getValue(), ut);
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
        return "Pressure(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pressure other = (Pressure) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

