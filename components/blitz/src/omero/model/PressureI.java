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

        c.put("APA:ATM", new double[][]{null});
        c.put("APA:BAR", new double[][]{null});
        c.put("APA:CBAR", new double[][]{null});
        c.put("APA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("APA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("APA:DBAR", new double[][]{null});
        c.put("APA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("APA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("APA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("APA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("APA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("APA:KBAR", new double[][]{null});
        c.put("APA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("APA:MBAR", new double[][]{null});
        c.put("APA:MEGABAR", new double[][]{null});
        c.put("APA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("APA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("APA:MMHG", new double[][]{null});
        c.put("APA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("APA:MTORR", new double[][]{null});
        c.put("APA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("APA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("APA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("APA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("APA:PSI", new double[][]{null});
        c.put("APA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("APA:TORR", new double[][]{null});
        c.put("APA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("APA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("APA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("APA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ATM:APA", new double[][]{null});
        c.put("ATM:BAR", new double[][]{null});
        c.put("ATM:CBAR", new double[][]{null});
        c.put("ATM:CPA", new double[][]{null});
        c.put("ATM:DAPA", new double[][]{null});
        c.put("ATM:DBAR", new double[][]{null});
        c.put("ATM:DPA", new double[][]{null});
        c.put("ATM:EXAPA", new double[][]{null});
        c.put("ATM:FPA", new double[][]{null});
        c.put("ATM:GIGAPA", new double[][]{null});
        c.put("ATM:HPA", new double[][]{null});
        c.put("ATM:KBAR", new double[][]{null});
        c.put("ATM:KPA", new double[][]{null});
        c.put("ATM:MBAR", new double[][]{null});
        c.put("ATM:MEGABAR", new double[][]{null});
        c.put("ATM:MEGAPA", new double[][]{null});
        c.put("ATM:MICROPA", new double[][]{null});
        c.put("ATM:MMHG", new double[][]{null});
        c.put("ATM:MPA", new double[][]{null});
        c.put("ATM:MTORR", new double[][]{null});
        c.put("ATM:NPA", new double[][]{null});
        c.put("ATM:PA", new double[][]{null});
        c.put("ATM:PETAPA", new double[][]{null});
        c.put("ATM:PPA", new double[][]{null});
        c.put("ATM:PSI", new double[][]{null});
        c.put("ATM:TERAPA", new double[][]{null});
        c.put("ATM:TORR", new double[][]{null});
        c.put("ATM:YOTTAPA", new double[][]{null});
        c.put("ATM:YPA", new double[][]{null});
        c.put("ATM:ZETTAPA", new double[][]{null});
        c.put("ATM:ZPA", new double[][]{null});
        c.put("BAR:APA", new double[][]{null});
        c.put("BAR:ATM", new double[][]{null});
        c.put("BAR:CBAR", new double[][]{null});
        c.put("BAR:CPA", new double[][]{null});
        c.put("BAR:DAPA", new double[][]{null});
        c.put("BAR:DBAR", new double[][]{null});
        c.put("BAR:DPA", new double[][]{null});
        c.put("BAR:EXAPA", new double[][]{null});
        c.put("BAR:FPA", new double[][]{null});
        c.put("BAR:GIGAPA", new double[][]{null});
        c.put("BAR:HPA", new double[][]{null});
        c.put("BAR:KBAR", new double[][]{null});
        c.put("BAR:KPA", new double[][]{null});
        c.put("BAR:MBAR", new double[][]{null});
        c.put("BAR:MEGABAR", new double[][]{null});
        c.put("BAR:MEGAPA", new double[][]{null});
        c.put("BAR:MICROPA", new double[][]{null});
        c.put("BAR:MMHG", new double[][]{null});
        c.put("BAR:MPA", new double[][]{null});
        c.put("BAR:MTORR", new double[][]{null});
        c.put("BAR:NPA", new double[][]{null});
        c.put("BAR:PA", new double[][]{null});
        c.put("BAR:PETAPA", new double[][]{null});
        c.put("BAR:PPA", new double[][]{null});
        c.put("BAR:PSI", new double[][]{null});
        c.put("BAR:TERAPA", new double[][]{null});
        c.put("BAR:TORR", new double[][]{null});
        c.put("BAR:YOTTAPA", new double[][]{null});
        c.put("BAR:YPA", new double[][]{null});
        c.put("BAR:ZETTAPA", new double[][]{null});
        c.put("BAR:ZPA", new double[][]{null});
        c.put("CBAR:APA", new double[][]{null});
        c.put("CBAR:ATM", new double[][]{null});
        c.put("CBAR:BAR", new double[][]{null});
        c.put("CBAR:CPA", new double[][]{null});
        c.put("CBAR:DAPA", new double[][]{null});
        c.put("CBAR:DBAR", new double[][]{null});
        c.put("CBAR:DPA", new double[][]{null});
        c.put("CBAR:EXAPA", new double[][]{null});
        c.put("CBAR:FPA", new double[][]{null});
        c.put("CBAR:GIGAPA", new double[][]{null});
        c.put("CBAR:HPA", new double[][]{null});
        c.put("CBAR:KBAR", new double[][]{null});
        c.put("CBAR:KPA", new double[][]{null});
        c.put("CBAR:MBAR", new double[][]{null});
        c.put("CBAR:MEGABAR", new double[][]{null});
        c.put("CBAR:MEGAPA", new double[][]{null});
        c.put("CBAR:MICROPA", new double[][]{null});
        c.put("CBAR:MMHG", new double[][]{null});
        c.put("CBAR:MPA", new double[][]{null});
        c.put("CBAR:MTORR", new double[][]{null});
        c.put("CBAR:NPA", new double[][]{null});
        c.put("CBAR:PA", new double[][]{null});
        c.put("CBAR:PETAPA", new double[][]{null});
        c.put("CBAR:PPA", new double[][]{null});
        c.put("CBAR:PSI", new double[][]{null});
        c.put("CBAR:TERAPA", new double[][]{null});
        c.put("CBAR:TORR", new double[][]{null});
        c.put("CBAR:YOTTAPA", new double[][]{null});
        c.put("CBAR:YPA", new double[][]{null});
        c.put("CBAR:ZETTAPA", new double[][]{null});
        c.put("CBAR:ZPA", new double[][]{null});
        c.put("CPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("CPA:ATM", new double[][]{null});
        c.put("CPA:BAR", new double[][]{null});
        c.put("CPA:CBAR", new double[][]{null});
        c.put("CPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("CPA:DBAR", new double[][]{null});
        c.put("CPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("CPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("CPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("CPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("CPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("CPA:KBAR", new double[][]{null});
        c.put("CPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("CPA:MBAR", new double[][]{null});
        c.put("CPA:MEGABAR", new double[][]{null});
        c.put("CPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("CPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("CPA:MMHG", new double[][]{null});
        c.put("CPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("CPA:MTORR", new double[][]{null});
        c.put("CPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("CPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("CPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("CPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("CPA:PSI", new double[][]{null});
        c.put("CPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("CPA:TORR", new double[][]{null});
        c.put("CPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("CPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("CPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("CPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("DAPA:ATM", new double[][]{null});
        c.put("DAPA:BAR", new double[][]{null});
        c.put("DAPA:CBAR", new double[][]{null});
        c.put("DAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("DAPA:DBAR", new double[][]{null});
        c.put("DAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("DAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("DAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("DAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DAPA:KBAR", new double[][]{null});
        c.put("DAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DAPA:MBAR", new double[][]{null});
        c.put("DAPA:MEGABAR", new double[][]{null});
        c.put("DAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("DAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("DAPA:MMHG", new double[][]{null});
        c.put("DAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("DAPA:MTORR", new double[][]{null});
        c.put("DAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("DAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("DAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("DAPA:PSI", new double[][]{null});
        c.put("DAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("DAPA:TORR", new double[][]{null});
        c.put("DAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("DAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("DAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("DAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("DBAR:APA", new double[][]{null});
        c.put("DBAR:ATM", new double[][]{null});
        c.put("DBAR:BAR", new double[][]{null});
        c.put("DBAR:CBAR", new double[][]{null});
        c.put("DBAR:CPA", new double[][]{null});
        c.put("DBAR:DAPA", new double[][]{null});
        c.put("DBAR:DPA", new double[][]{null});
        c.put("DBAR:EXAPA", new double[][]{null});
        c.put("DBAR:FPA", new double[][]{null});
        c.put("DBAR:GIGAPA", new double[][]{null});
        c.put("DBAR:HPA", new double[][]{null});
        c.put("DBAR:KBAR", new double[][]{null});
        c.put("DBAR:KPA", new double[][]{null});
        c.put("DBAR:MBAR", new double[][]{null});
        c.put("DBAR:MEGABAR", new double[][]{null});
        c.put("DBAR:MEGAPA", new double[][]{null});
        c.put("DBAR:MICROPA", new double[][]{null});
        c.put("DBAR:MMHG", new double[][]{null});
        c.put("DBAR:MPA", new double[][]{null});
        c.put("DBAR:MTORR", new double[][]{null});
        c.put("DBAR:NPA", new double[][]{null});
        c.put("DBAR:PA", new double[][]{null});
        c.put("DBAR:PETAPA", new double[][]{null});
        c.put("DBAR:PPA", new double[][]{null});
        c.put("DBAR:PSI", new double[][]{null});
        c.put("DBAR:TERAPA", new double[][]{null});
        c.put("DBAR:TORR", new double[][]{null});
        c.put("DBAR:YOTTAPA", new double[][]{null});
        c.put("DBAR:YPA", new double[][]{null});
        c.put("DBAR:ZETTAPA", new double[][]{null});
        c.put("DBAR:ZPA", new double[][]{null});
        c.put("DPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("DPA:ATM", new double[][]{null});
        c.put("DPA:BAR", new double[][]{null});
        c.put("DPA:CBAR", new double[][]{null});
        c.put("DPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("DPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("DPA:DBAR", new double[][]{null});
        c.put("DPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("DPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("DPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("DPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("DPA:KBAR", new double[][]{null});
        c.put("DPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("DPA:MBAR", new double[][]{null});
        c.put("DPA:MEGABAR", new double[][]{null});
        c.put("DPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("DPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("DPA:MMHG", new double[][]{null});
        c.put("DPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("DPA:MTORR", new double[][]{null});
        c.put("DPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("DPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("DPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("DPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("DPA:PSI", new double[][]{null});
        c.put("DPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("DPA:TORR", new double[][]{null});
        c.put("DPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("DPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("DPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("DPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("EXAPA:ATM", new double[][]{null});
        c.put("EXAPA:BAR", new double[][]{null});
        c.put("EXAPA:CBAR", new double[][]{null});
        c.put("EXAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("EXAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("EXAPA:DBAR", new double[][]{null});
        c.put("EXAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("EXAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("EXAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("EXAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("EXAPA:KBAR", new double[][]{null});
        c.put("EXAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("EXAPA:MBAR", new double[][]{null});
        c.put("EXAPA:MEGABAR", new double[][]{null});
        c.put("EXAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("EXAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("EXAPA:MMHG", new double[][]{null});
        c.put("EXAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("EXAPA:MTORR", new double[][]{null});
        c.put("EXAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("EXAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("EXAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("EXAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("EXAPA:PSI", new double[][]{null});
        c.put("EXAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("EXAPA:TORR", new double[][]{null});
        c.put("EXAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("EXAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("EXAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("EXAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("FPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("FPA:ATM", new double[][]{null});
        c.put("FPA:BAR", new double[][]{null});
        c.put("FPA:CBAR", new double[][]{null});
        c.put("FPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("FPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("FPA:DBAR", new double[][]{null});
        c.put("FPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("FPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("FPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("FPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -17}});
        c.put("FPA:KBAR", new double[][]{null});
        c.put("FPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("FPA:MBAR", new double[][]{null});
        c.put("FPA:MEGABAR", new double[][]{null});
        c.put("FPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("FPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("FPA:MMHG", new double[][]{null});
        c.put("FPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("FPA:MTORR", new double[][]{null});
        c.put("FPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("FPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("FPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("FPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("FPA:PSI", new double[][]{null});
        c.put("FPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("FPA:TORR", new double[][]{null});
        c.put("FPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("FPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("FPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("FPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("GIGAPA:ATM", new double[][]{null});
        c.put("GIGAPA:BAR", new double[][]{null});
        c.put("GIGAPA:CBAR", new double[][]{null});
        c.put("GIGAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("GIGAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("GIGAPA:DBAR", new double[][]{null});
        c.put("GIGAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("GIGAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("GIGAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("GIGAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("GIGAPA:KBAR", new double[][]{null});
        c.put("GIGAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("GIGAPA:MBAR", new double[][]{null});
        c.put("GIGAPA:MEGABAR", new double[][]{null});
        c.put("GIGAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("GIGAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("GIGAPA:MMHG", new double[][]{null});
        c.put("GIGAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("GIGAPA:MTORR", new double[][]{null});
        c.put("GIGAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("GIGAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("GIGAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("GIGAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("GIGAPA:PSI", new double[][]{null});
        c.put("GIGAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("GIGAPA:TORR", new double[][]{null});
        c.put("GIGAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("GIGAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("GIGAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("GIGAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("HPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("HPA:ATM", new double[][]{null});
        c.put("HPA:BAR", new double[][]{null});
        c.put("HPA:CBAR", new double[][]{null});
        c.put("HPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("HPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("HPA:DBAR", new double[][]{null});
        c.put("HPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("HPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -16}});
        c.put("HPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("HPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("HPA:KBAR", new double[][]{null});
        c.put("HPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("HPA:MBAR", new double[][]{null});
        c.put("HPA:MEGABAR", new double[][]{null});
        c.put("HPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("HPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("HPA:MMHG", new double[][]{null});
        c.put("HPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("HPA:MTORR", new double[][]{null});
        c.put("HPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("HPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("HPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("HPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("HPA:PSI", new double[][]{null});
        c.put("HPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("HPA:TORR", new double[][]{null});
        c.put("HPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("HPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("HPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("HPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("KBAR:APA", new double[][]{null});
        c.put("KBAR:ATM", new double[][]{null});
        c.put("KBAR:BAR", new double[][]{null});
        c.put("KBAR:CBAR", new double[][]{null});
        c.put("KBAR:CPA", new double[][]{null});
        c.put("KBAR:DAPA", new double[][]{null});
        c.put("KBAR:DBAR", new double[][]{null});
        c.put("KBAR:DPA", new double[][]{null});
        c.put("KBAR:EXAPA", new double[][]{null});
        c.put("KBAR:FPA", new double[][]{null});
        c.put("KBAR:GIGAPA", new double[][]{null});
        c.put("KBAR:HPA", new double[][]{null});
        c.put("KBAR:KPA", new double[][]{null});
        c.put("KBAR:MBAR", new double[][]{null});
        c.put("KBAR:MEGABAR", new double[][]{null});
        c.put("KBAR:MEGAPA", new double[][]{null});
        c.put("KBAR:MICROPA", new double[][]{null});
        c.put("KBAR:MMHG", new double[][]{null});
        c.put("KBAR:MPA", new double[][]{null});
        c.put("KBAR:MTORR", new double[][]{null});
        c.put("KBAR:NPA", new double[][]{null});
        c.put("KBAR:PA", new double[][]{null});
        c.put("KBAR:PETAPA", new double[][]{null});
        c.put("KBAR:PPA", new double[][]{null});
        c.put("KBAR:PSI", new double[][]{null});
        c.put("KBAR:TERAPA", new double[][]{null});
        c.put("KBAR:TORR", new double[][]{null});
        c.put("KBAR:YOTTAPA", new double[][]{null});
        c.put("KBAR:YPA", new double[][]{null});
        c.put("KBAR:ZETTAPA", new double[][]{null});
        c.put("KBAR:ZPA", new double[][]{null});
        c.put("KPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("KPA:ATM", new double[][]{null});
        c.put("KPA:BAR", new double[][]{null});
        c.put("KPA:CBAR", new double[][]{null});
        c.put("KPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("KPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("KPA:DBAR", new double[][]{null});
        c.put("KPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("KPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("KPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("KPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("KPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("KPA:KBAR", new double[][]{null});
        c.put("KPA:MBAR", new double[][]{null});
        c.put("KPA:MEGABAR", new double[][]{null});
        c.put("KPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("KPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("KPA:MMHG", new double[][]{null});
        c.put("KPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("KPA:MTORR", new double[][]{null});
        c.put("KPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("KPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("KPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("KPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("KPA:PSI", new double[][]{null});
        c.put("KPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("KPA:TORR", new double[][]{null});
        c.put("KPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("KPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("KPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("KPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MBAR:APA", new double[][]{null});
        c.put("MBAR:ATM", new double[][]{null});
        c.put("MBAR:BAR", new double[][]{null});
        c.put("MBAR:CBAR", new double[][]{null});
        c.put("MBAR:CPA", new double[][]{null});
        c.put("MBAR:DAPA", new double[][]{null});
        c.put("MBAR:DBAR", new double[][]{null});
        c.put("MBAR:DPA", new double[][]{null});
        c.put("MBAR:EXAPA", new double[][]{null});
        c.put("MBAR:FPA", new double[][]{null});
        c.put("MBAR:GIGAPA", new double[][]{null});
        c.put("MBAR:HPA", new double[][]{null});
        c.put("MBAR:KBAR", new double[][]{null});
        c.put("MBAR:KPA", new double[][]{null});
        c.put("MBAR:MEGABAR", new double[][]{null});
        c.put("MBAR:MEGAPA", new double[][]{null});
        c.put("MBAR:MICROPA", new double[][]{null});
        c.put("MBAR:MMHG", new double[][]{null});
        c.put("MBAR:MPA", new double[][]{null});
        c.put("MBAR:MTORR", new double[][]{null});
        c.put("MBAR:NPA", new double[][]{null});
        c.put("MBAR:PA", new double[][]{null});
        c.put("MBAR:PETAPA", new double[][]{null});
        c.put("MBAR:PPA", new double[][]{null});
        c.put("MBAR:PSI", new double[][]{null});
        c.put("MBAR:TERAPA", new double[][]{null});
        c.put("MBAR:TORR", new double[][]{null});
        c.put("MBAR:YOTTAPA", new double[][]{null});
        c.put("MBAR:YPA", new double[][]{null});
        c.put("MBAR:ZETTAPA", new double[][]{null});
        c.put("MBAR:ZPA", new double[][]{null});
        c.put("MEGABAR:APA", new double[][]{null});
        c.put("MEGABAR:ATM", new double[][]{null});
        c.put("MEGABAR:BAR", new double[][]{null});
        c.put("MEGABAR:CBAR", new double[][]{null});
        c.put("MEGABAR:CPA", new double[][]{null});
        c.put("MEGABAR:DAPA", new double[][]{null});
        c.put("MEGABAR:DBAR", new double[][]{null});
        c.put("MEGABAR:DPA", new double[][]{null});
        c.put("MEGABAR:EXAPA", new double[][]{null});
        c.put("MEGABAR:FPA", new double[][]{null});
        c.put("MEGABAR:GIGAPA", new double[][]{null});
        c.put("MEGABAR:HPA", new double[][]{null});
        c.put("MEGABAR:KBAR", new double[][]{null});
        c.put("MEGABAR:KPA", new double[][]{null});
        c.put("MEGABAR:MBAR", new double[][]{null});
        c.put("MEGABAR:MEGAPA", new double[][]{null});
        c.put("MEGABAR:MICROPA", new double[][]{null});
        c.put("MEGABAR:MMHG", new double[][]{null});
        c.put("MEGABAR:MPA", new double[][]{null});
        c.put("MEGABAR:MTORR", new double[][]{null});
        c.put("MEGABAR:NPA", new double[][]{null});
        c.put("MEGABAR:PA", new double[][]{null});
        c.put("MEGABAR:PETAPA", new double[][]{null});
        c.put("MEGABAR:PPA", new double[][]{null});
        c.put("MEGABAR:PSI", new double[][]{null});
        c.put("MEGABAR:TERAPA", new double[][]{null});
        c.put("MEGABAR:TORR", new double[][]{null});
        c.put("MEGABAR:YOTTAPA", new double[][]{null});
        c.put("MEGABAR:YPA", new double[][]{null});
        c.put("MEGABAR:ZETTAPA", new double[][]{null});
        c.put("MEGABAR:ZPA", new double[][]{null});
        c.put("MEGAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("MEGAPA:ATM", new double[][]{null});
        c.put("MEGAPA:BAR", new double[][]{null});
        c.put("MEGAPA:CBAR", new double[][]{null});
        c.put("MEGAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 8}});
        c.put("MEGAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 5}});
        c.put("MEGAPA:DBAR", new double[][]{null});
        c.put("MEGAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 7}});
        c.put("MEGAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MEGAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MEGAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MEGAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 4}});
        c.put("MEGAPA:KBAR", new double[][]{null});
        c.put("MEGAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MEGAPA:MBAR", new double[][]{null});
        c.put("MEGAPA:MEGABAR", new double[][]{null});
        c.put("MEGAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MEGAPA:MMHG", new double[][]{null});
        c.put("MEGAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MEGAPA:MTORR", new double[][]{null});
        c.put("MEGAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MEGAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MEGAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MEGAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MEGAPA:PSI", new double[][]{null});
        c.put("MEGAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MEGAPA:TORR", new double[][]{null});
        c.put("MEGAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MEGAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("MEGAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MEGAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("MICROPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MICROPA:ATM", new double[][]{null});
        c.put("MICROPA:BAR", new double[][]{null});
        c.put("MICROPA:CBAR", new double[][]{null});
        c.put("MICROPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MICROPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("MICROPA:DBAR", new double[][]{null});
        c.put("MICROPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MICROPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MICROPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MICROPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MICROPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("MICROPA:KBAR", new double[][]{null});
        c.put("MICROPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MICROPA:MBAR", new double[][]{null});
        c.put("MICROPA:MEGABAR", new double[][]{null});
        c.put("MICROPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MICROPA:MMHG", new double[][]{null});
        c.put("MICROPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MICROPA:MTORR", new double[][]{null});
        c.put("MICROPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MICROPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MICROPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MICROPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MICROPA:PSI", new double[][]{null});
        c.put("MICROPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MICROPA:TORR", new double[][]{null});
        c.put("MICROPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("MICROPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MICROPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MICROPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MMHG:APA", new double[][]{null});
        c.put("MMHG:ATM", new double[][]{null});
        c.put("MMHG:BAR", new double[][]{null});
        c.put("MMHG:CBAR", new double[][]{null});
        c.put("MMHG:CPA", new double[][]{null});
        c.put("MMHG:DAPA", new double[][]{null});
        c.put("MMHG:DBAR", new double[][]{null});
        c.put("MMHG:DPA", new double[][]{null});
        c.put("MMHG:EXAPA", new double[][]{null});
        c.put("MMHG:FPA", new double[][]{null});
        c.put("MMHG:GIGAPA", new double[][]{null});
        c.put("MMHG:HPA", new double[][]{null});
        c.put("MMHG:KBAR", new double[][]{null});
        c.put("MMHG:KPA", new double[][]{null});
        c.put("MMHG:MBAR", new double[][]{null});
        c.put("MMHG:MEGABAR", new double[][]{null});
        c.put("MMHG:MEGAPA", new double[][]{null});
        c.put("MMHG:MICROPA", new double[][]{null});
        c.put("MMHG:MPA", new double[][]{null});
        c.put("MMHG:MTORR", new double[][]{null});
        c.put("MMHG:NPA", new double[][]{null});
        c.put("MMHG:PA", new double[][]{null});
        c.put("MMHG:PETAPA", new double[][]{null});
        c.put("MMHG:PPA", new double[][]{null});
        c.put("MMHG:PSI", new double[][]{null});
        c.put("MMHG:TERAPA", new double[][]{null});
        c.put("MMHG:TORR", new double[][]{null});
        c.put("MMHG:YOTTAPA", new double[][]{null});
        c.put("MMHG:YPA", new double[][]{null});
        c.put("MMHG:ZETTAPA", new double[][]{null});
        c.put("MMHG:ZPA", new double[][]{null});
        c.put("MPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("MPA:ATM", new double[][]{null});
        c.put("MPA:BAR", new double[][]{null});
        c.put("MPA:CBAR", new double[][]{null});
        c.put("MPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("MPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -4}});
        c.put("MPA:DBAR", new double[][]{null});
        c.put("MPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("MPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("MPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("MPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("MPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -5}});
        c.put("MPA:KBAR", new double[][]{null});
        c.put("MPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("MPA:MBAR", new double[][]{null});
        c.put("MPA:MEGABAR", new double[][]{null});
        c.put("MPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("MPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("MPA:MMHG", new double[][]{null});
        c.put("MPA:MTORR", new double[][]{null});
        c.put("MPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("MPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("MPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("MPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("MPA:PSI", new double[][]{null});
        c.put("MPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("MPA:TORR", new double[][]{null});
        c.put("MPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("MPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("MPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("MPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("MTORR:APA", new double[][]{null});
        c.put("MTORR:ATM", new double[][]{null});
        c.put("MTORR:BAR", new double[][]{null});
        c.put("MTORR:CBAR", new double[][]{null});
        c.put("MTORR:CPA", new double[][]{null});
        c.put("MTORR:DAPA", new double[][]{null});
        c.put("MTORR:DBAR", new double[][]{null});
        c.put("MTORR:DPA", new double[][]{null});
        c.put("MTORR:EXAPA", new double[][]{null});
        c.put("MTORR:FPA", new double[][]{null});
        c.put("MTORR:GIGAPA", new double[][]{null});
        c.put("MTORR:HPA", new double[][]{null});
        c.put("MTORR:KBAR", new double[][]{null});
        c.put("MTORR:KPA", new double[][]{null});
        c.put("MTORR:MBAR", new double[][]{null});
        c.put("MTORR:MEGABAR", new double[][]{null});
        c.put("MTORR:MEGAPA", new double[][]{null});
        c.put("MTORR:MICROPA", new double[][]{null});
        c.put("MTORR:MMHG", new double[][]{null});
        c.put("MTORR:MPA", new double[][]{null});
        c.put("MTORR:NPA", new double[][]{null});
        c.put("MTORR:PA", new double[][]{null});
        c.put("MTORR:PETAPA", new double[][]{null});
        c.put("MTORR:PPA", new double[][]{null});
        c.put("MTORR:PSI", new double[][]{null});
        c.put("MTORR:TERAPA", new double[][]{null});
        c.put("MTORR:TORR", new double[][]{null});
        c.put("MTORR:YOTTAPA", new double[][]{null});
        c.put("MTORR:YPA", new double[][]{null});
        c.put("MTORR:ZETTAPA", new double[][]{null});
        c.put("MTORR:ZPA", new double[][]{null});
        c.put("NPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("NPA:ATM", new double[][]{null});
        c.put("NPA:BAR", new double[][]{null});
        c.put("NPA:CBAR", new double[][]{null});
        c.put("NPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -7}});
        c.put("NPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("NPA:DBAR", new double[][]{null});
        c.put("NPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -8}});
        c.put("NPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("NPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("NPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("NPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("NPA:KBAR", new double[][]{null});
        c.put("NPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("NPA:MBAR", new double[][]{null});
        c.put("NPA:MEGABAR", new double[][]{null});
        c.put("NPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("NPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("NPA:MMHG", new double[][]{null});
        c.put("NPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("NPA:MTORR", new double[][]{null});
        c.put("NPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("NPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("NPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("NPA:PSI", new double[][]{null});
        c.put("NPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("NPA:TORR", new double[][]{null});
        c.put("NPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("NPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("NPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("NPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PA:ATM", new double[][]{null});
        c.put("PA:BAR", new double[][]{null});
        c.put("PA:CBAR", new double[][]{null});
        c.put("PA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 2}});
        c.put("PA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -1}});
        c.put("PA:DBAR", new double[][]{null});
        c.put("PA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 1}});
        c.put("PA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -2}});
        c.put("PA:KBAR", new double[][]{null});
        c.put("PA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PA:MBAR", new double[][]{null});
        c.put("PA:MEGABAR", new double[][]{null});
        c.put("PA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PA:MMHG", new double[][]{null});
        c.put("PA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PA:MTORR", new double[][]{null});
        c.put("PA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PA:PSI", new double[][]{null});
        c.put("PA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PA:TORR", new double[][]{null});
        c.put("PA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("PETAPA:ATM", new double[][]{null});
        c.put("PETAPA:BAR", new double[][]{null});
        c.put("PETAPA:CBAR", new double[][]{null});
        c.put("PETAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 17}});
        c.put("PETAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("PETAPA:DBAR", new double[][]{null});
        c.put("PETAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 16}});
        c.put("PETAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PETAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("PETAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PETAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("PETAPA:KBAR", new double[][]{null});
        c.put("PETAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PETAPA:MBAR", new double[][]{null});
        c.put("PETAPA:MEGABAR", new double[][]{null});
        c.put("PETAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PETAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("PETAPA:MMHG", new double[][]{null});
        c.put("PETAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("PETAPA:MTORR", new double[][]{null});
        c.put("PETAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("PETAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("PETAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("PETAPA:PSI", new double[][]{null});
        c.put("PETAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PETAPA:TORR", new double[][]{null});
        c.put("PETAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PETAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("PETAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PETAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("PPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("PPA:ATM", new double[][]{null});
        c.put("PPA:BAR", new double[][]{null});
        c.put("PPA:CBAR", new double[][]{null});
        c.put("PPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -10}});
        c.put("PPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -13}});
        c.put("PPA:DBAR", new double[][]{null});
        c.put("PPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -11}});
        c.put("PPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("PPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("PPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("PPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -14}});
        c.put("PPA:KBAR", new double[][]{null});
        c.put("PPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("PPA:MBAR", new double[][]{null});
        c.put("PPA:MEGABAR", new double[][]{null});
        c.put("PPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("PPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("PPA:MMHG", new double[][]{null});
        c.put("PPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("PPA:MTORR", new double[][]{null});
        c.put("PPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("PPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("PPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("PPA:PSI", new double[][]{null});
        c.put("PPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("PPA:TORR", new double[][]{null});
        c.put("PPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("PPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("PPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("PPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("PSI:APA", new double[][]{null});
        c.put("PSI:ATM", new double[][]{null});
        c.put("PSI:BAR", new double[][]{null});
        c.put("PSI:CBAR", new double[][]{null});
        c.put("PSI:CPA", new double[][]{null});
        c.put("PSI:DAPA", new double[][]{null});
        c.put("PSI:DBAR", new double[][]{null});
        c.put("PSI:DPA", new double[][]{null});
        c.put("PSI:EXAPA", new double[][]{null});
        c.put("PSI:FPA", new double[][]{null});
        c.put("PSI:GIGAPA", new double[][]{null});
        c.put("PSI:HPA", new double[][]{null});
        c.put("PSI:KBAR", new double[][]{null});
        c.put("PSI:KPA", new double[][]{null});
        c.put("PSI:MBAR", new double[][]{null});
        c.put("PSI:MEGABAR", new double[][]{null});
        c.put("PSI:MEGAPA", new double[][]{null});
        c.put("PSI:MICROPA", new double[][]{null});
        c.put("PSI:MMHG", new double[][]{null});
        c.put("PSI:MPA", new double[][]{null});
        c.put("PSI:MTORR", new double[][]{null});
        c.put("PSI:NPA", new double[][]{null});
        c.put("PSI:PA", new double[][]{null});
        c.put("PSI:PETAPA", new double[][]{null});
        c.put("PSI:PPA", new double[][]{null});
        c.put("PSI:TERAPA", new double[][]{null});
        c.put("PSI:TORR", new double[][]{null});
        c.put("PSI:YOTTAPA", new double[][]{null});
        c.put("PSI:YPA", new double[][]{null});
        c.put("PSI:ZETTAPA", new double[][]{null});
        c.put("PSI:ZPA", new double[][]{null});
        c.put("TERAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("TERAPA:ATM", new double[][]{null});
        c.put("TERAPA:BAR", new double[][]{null});
        c.put("TERAPA:CBAR", new double[][]{null});
        c.put("TERAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 14}});
        c.put("TERAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 11}});
        c.put("TERAPA:DBAR", new double[][]{null});
        c.put("TERAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 13}});
        c.put("TERAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("TERAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("TERAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("TERAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 10}});
        c.put("TERAPA:KBAR", new double[][]{null});
        c.put("TERAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("TERAPA:MBAR", new double[][]{null});
        c.put("TERAPA:MEGABAR", new double[][]{null});
        c.put("TERAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("TERAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("TERAPA:MMHG", new double[][]{null});
        c.put("TERAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("TERAPA:MTORR", new double[][]{null});
        c.put("TERAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("TERAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("TERAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("TERAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("TERAPA:PSI", new double[][]{null});
        c.put("TERAPA:TORR", new double[][]{null});
        c.put("TERAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("TERAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("TERAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("TERAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("TORR:APA", new double[][]{null});
        c.put("TORR:ATM", new double[][]{null});
        c.put("TORR:BAR", new double[][]{null});
        c.put("TORR:CBAR", new double[][]{null});
        c.put("TORR:CPA", new double[][]{null});
        c.put("TORR:DAPA", new double[][]{null});
        c.put("TORR:DBAR", new double[][]{null});
        c.put("TORR:DPA", new double[][]{null});
        c.put("TORR:EXAPA", new double[][]{null});
        c.put("TORR:FPA", new double[][]{null});
        c.put("TORR:GIGAPA", new double[][]{null});
        c.put("TORR:HPA", new double[][]{null});
        c.put("TORR:KBAR", new double[][]{null});
        c.put("TORR:KPA", new double[][]{null});
        c.put("TORR:MBAR", new double[][]{null});
        c.put("TORR:MEGABAR", new double[][]{null});
        c.put("TORR:MEGAPA", new double[][]{null});
        c.put("TORR:MICROPA", new double[][]{null});
        c.put("TORR:MMHG", new double[][]{null});
        c.put("TORR:MPA", new double[][]{null});
        c.put("TORR:MTORR", new double[][]{null});
        c.put("TORR:NPA", new double[][]{null});
        c.put("TORR:PA", new double[][]{null});
        c.put("TORR:PETAPA", new double[][]{null});
        c.put("TORR:PPA", new double[][]{null});
        c.put("TORR:PSI", new double[][]{null});
        c.put("TORR:TERAPA", new double[][]{null});
        c.put("TORR:YOTTAPA", new double[][]{null});
        c.put("TORR:YPA", new double[][]{null});
        c.put("TORR:ZETTAPA", new double[][]{null});
        c.put("TORR:ZPA", new double[][]{null});
        c.put("YOTTAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("YOTTAPA:ATM", new double[][]{null});
        c.put("YOTTAPA:BAR", new double[][]{null});
        c.put("YOTTAPA:CBAR", new double[][]{null});
        c.put("YOTTAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 26}});
        c.put("YOTTAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("YOTTAPA:DBAR", new double[][]{null});
        c.put("YOTTAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 25}});
        c.put("YOTTAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("YOTTAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("YOTTAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("YOTTAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("YOTTAPA:KBAR", new double[][]{null});
        c.put("YOTTAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("YOTTAPA:MBAR", new double[][]{null});
        c.put("YOTTAPA:MEGABAR", new double[][]{null});
        c.put("YOTTAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("YOTTAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("YOTTAPA:MMHG", new double[][]{null});
        c.put("YOTTAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("YOTTAPA:MTORR", new double[][]{null});
        c.put("YOTTAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("YOTTAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("YOTTAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("YOTTAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("YOTTAPA:PSI", new double[][]{null});
        c.put("YOTTAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("YOTTAPA:TORR", new double[][]{null});
        c.put("YOTTAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 48}});
        c.put("YOTTAPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("YOTTAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("YPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("YPA:ATM", new double[][]{null});
        c.put("YPA:BAR", new double[][]{null});
        c.put("YPA:CBAR", new double[][]{null});
        c.put("YPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("YPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -25}});
        c.put("YPA:DBAR", new double[][]{null});
        c.put("YPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("YPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -42}});
        c.put("YPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("YPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("YPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -26}});
        c.put("YPA:KBAR", new double[][]{null});
        c.put("YPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("YPA:MBAR", new double[][]{null});
        c.put("YPA:MEGABAR", new double[][]{null});
        c.put("YPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("YPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("YPA:MMHG", new double[][]{null});
        c.put("YPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("YPA:MTORR", new double[][]{null});
        c.put("YPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("YPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("YPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("YPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("YPA:PSI", new double[][]{null});
        c.put("YPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("YPA:TORR", new double[][]{null});
        c.put("YPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -48}});
        c.put("YPA:ZETTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -45}});
        c.put("YPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, 39}});
        c.put("ZETTAPA:ATM", new double[][]{null});
        c.put("ZETTAPA:BAR", new double[][]{null});
        c.put("ZETTAPA:CBAR", new double[][]{null});
        c.put("ZETTAPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, 23}});
        c.put("ZETTAPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, 20}});
        c.put("ZETTAPA:DBAR", new double[][]{null});
        c.put("ZETTAPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, 22}});
        c.put("ZETTAPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, 3}});
        c.put("ZETTAPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, 36}});
        c.put("ZETTAPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 12}});
        c.put("ZETTAPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, 19}});
        c.put("ZETTAPA:KBAR", new double[][]{null});
        c.put("ZETTAPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, 18}});
        c.put("ZETTAPA:MBAR", new double[][]{null});
        c.put("ZETTAPA:MEGABAR", new double[][]{null});
        c.put("ZETTAPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, 15}});
        c.put("ZETTAPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, 27}});
        c.put("ZETTAPA:MMHG", new double[][]{null});
        c.put("ZETTAPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, 24}});
        c.put("ZETTAPA:MTORR", new double[][]{null});
        c.put("ZETTAPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, 30}});
        c.put("ZETTAPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, 21}});
        c.put("ZETTAPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, 6}});
        c.put("ZETTAPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, 33}});
        c.put("ZETTAPA:PSI", new double[][]{null});
        c.put("ZETTAPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, 9}});
        c.put("ZETTAPA:TORR", new double[][]{null});
        c.put("ZETTAPA:YOTTAPA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZETTAPA:YPA", new double[][]{new double[]{0, 1}, new double[]{10, 45}});
        c.put("ZETTAPA:ZPA", new double[][]{new double[]{0, 1}, new double[]{10, 42}});
        c.put("ZPA:APA", new double[][]{new double[]{0, 1}, new double[]{10, -3}});
        c.put("ZPA:ATM", new double[][]{null});
        c.put("ZPA:BAR", new double[][]{null});
        c.put("ZPA:CBAR", new double[][]{null});
        c.put("ZPA:CPA", new double[][]{new double[]{0, 1}, new double[]{10, -19}});
        c.put("ZPA:DAPA", new double[][]{new double[]{0, 1}, new double[]{10, -22}});
        c.put("ZPA:DBAR", new double[][]{null});
        c.put("ZPA:DPA", new double[][]{new double[]{0, 1}, new double[]{10, -20}});
        c.put("ZPA:EXAPA", new double[][]{new double[]{0, 1}, new double[]{10, -39}});
        c.put("ZPA:FPA", new double[][]{new double[]{0, 1}, new double[]{10, -6}});
        c.put("ZPA:GIGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -30}});
        c.put("ZPA:HPA", new double[][]{new double[]{0, 1}, new double[]{10, -23}});
        c.put("ZPA:KBAR", new double[][]{null});
        c.put("ZPA:KPA", new double[][]{new double[]{0, 1}, new double[]{10, -24}});
        c.put("ZPA:MBAR", new double[][]{null});
        c.put("ZPA:MEGABAR", new double[][]{null});
        c.put("ZPA:MEGAPA", new double[][]{new double[]{0, 1}, new double[]{10, -27}});
        c.put("ZPA:MICROPA", new double[][]{new double[]{0, 1}, new double[]{10, -15}});
        c.put("ZPA:MMHG", new double[][]{null});
        c.put("ZPA:MPA", new double[][]{new double[]{0, 1}, new double[]{10, -18}});
        c.put("ZPA:MTORR", new double[][]{null});
        c.put("ZPA:NPA", new double[][]{new double[]{0, 1}, new double[]{10, -12}});
        c.put("ZPA:PA", new double[][]{new double[]{0, 1}, new double[]{10, -21}});
        c.put("ZPA:PETAPA", new double[][]{new double[]{0, 1}, new double[]{10, -36}});
        c.put("ZPA:PPA", new double[][]{new double[]{0, 1}, new double[]{10, -9}});
        c.put("ZPA:PSI", new double[][]{null});
        c.put("ZPA:TERAPA", new double[][]{new double[]{0, 1}, new double[]{10, -33}});
        c.put("ZPA:TORR", new double[][]{null});
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

