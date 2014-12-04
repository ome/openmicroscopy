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

import com.google.common.base.Function;

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

    private static final Map<String, Function<Double, Double>> conversions;
    static {
        Map<String, Function<Double, Double>> c = new HashMap<String, Function<Double, Double>>();

        c.put("APA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:ATM"));
              }});

        c.put("APA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:BAR"));
              }});

        c.put("APA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:CBAR"));
              }});

        c.put("APA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("APA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("APA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:DBAR"));
              }});

        c.put("APA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("APA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("APA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("APA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("APA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("APA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:KBAR"));
              }});

        c.put("APA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("APA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:MBAR"));
              }});

        c.put("APA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:MEGABAR"));
              }});

        c.put("APA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("APA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("APA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:MMHG"));
              }});

        c.put("APA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("APA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:MTORR"));
              }});

        c.put("APA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("APA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("APA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("APA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("APA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:PSI"));
              }});

        c.put("APA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("APA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "APA:TORR"));
              }});

        c.put("APA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("APA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("APA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("APA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ATM:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:APA"));
              }});

        c.put("ATM:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:BAR"));
              }});

        c.put("ATM:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:CBAR"));
              }});

        c.put("ATM:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:CPA"));
              }});

        c.put("ATM:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:DAPA"));
              }});

        c.put("ATM:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:DBAR"));
              }});

        c.put("ATM:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:DPA"));
              }});

        c.put("ATM:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:EXAPA"));
              }});

        c.put("ATM:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:FPA"));
              }});

        c.put("ATM:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:GIGAPA"));
              }});

        c.put("ATM:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:HPA"));
              }});

        c.put("ATM:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:KBAR"));
              }});

        c.put("ATM:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:KPA"));
              }});

        c.put("ATM:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MBAR"));
              }});

        c.put("ATM:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MEGABAR"));
              }});

        c.put("ATM:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MEGAPA"));
              }});

        c.put("ATM:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MICROPA"));
              }});

        c.put("ATM:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MMHG"));
              }});

        c.put("ATM:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MPA"));
              }});

        c.put("ATM:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:MTORR"));
              }});

        c.put("ATM:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:NPA"));
              }});

        c.put("ATM:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:PA"));
              }});

        c.put("ATM:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:PETAPA"));
              }});

        c.put("ATM:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:PPA"));
              }});

        c.put("ATM:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:PSI"));
              }});

        c.put("ATM:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:TERAPA"));
              }});

        c.put("ATM:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:TORR"));
              }});

        c.put("ATM:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:YOTTAPA"));
              }});

        c.put("ATM:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:YPA"));
              }});

        c.put("ATM:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:ZETTAPA"));
              }});

        c.put("ATM:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ATM:ZPA"));
              }});

        c.put("BAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:APA"));
              }});

        c.put("BAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:ATM"));
              }});

        c.put("BAR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:CBAR"));
              }});

        c.put("BAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:CPA"));
              }});

        c.put("BAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:DAPA"));
              }});

        c.put("BAR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:DBAR"));
              }});

        c.put("BAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:DPA"));
              }});

        c.put("BAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:EXAPA"));
              }});

        c.put("BAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:FPA"));
              }});

        c.put("BAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:GIGAPA"));
              }});

        c.put("BAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:HPA"));
              }});

        c.put("BAR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:KBAR"));
              }});

        c.put("BAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:KPA"));
              }});

        c.put("BAR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MBAR"));
              }});

        c.put("BAR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MEGABAR"));
              }});

        c.put("BAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MEGAPA"));
              }});

        c.put("BAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MICROPA"));
              }});

        c.put("BAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MMHG"));
              }});

        c.put("BAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MPA"));
              }});

        c.put("BAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:MTORR"));
              }});

        c.put("BAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:NPA"));
              }});

        c.put("BAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:PA"));
              }});

        c.put("BAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:PETAPA"));
              }});

        c.put("BAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:PPA"));
              }});

        c.put("BAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:PSI"));
              }});

        c.put("BAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:TERAPA"));
              }});

        c.put("BAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:TORR"));
              }});

        c.put("BAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:YOTTAPA"));
              }});

        c.put("BAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:YPA"));
              }});

        c.put("BAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:ZETTAPA"));
              }});

        c.put("BAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "BAR:ZPA"));
              }});

        c.put("CBAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:APA"));
              }});

        c.put("CBAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:ATM"));
              }});

        c.put("CBAR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:BAR"));
              }});

        c.put("CBAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:CPA"));
              }});

        c.put("CBAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:DAPA"));
              }});

        c.put("CBAR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:DBAR"));
              }});

        c.put("CBAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:DPA"));
              }});

        c.put("CBAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:EXAPA"));
              }});

        c.put("CBAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:FPA"));
              }});

        c.put("CBAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:GIGAPA"));
              }});

        c.put("CBAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:HPA"));
              }});

        c.put("CBAR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:KBAR"));
              }});

        c.put("CBAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:KPA"));
              }});

        c.put("CBAR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MBAR"));
              }});

        c.put("CBAR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MEGABAR"));
              }});

        c.put("CBAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MEGAPA"));
              }});

        c.put("CBAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MICROPA"));
              }});

        c.put("CBAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MMHG"));
              }});

        c.put("CBAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MPA"));
              }});

        c.put("CBAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:MTORR"));
              }});

        c.put("CBAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:NPA"));
              }});

        c.put("CBAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:PA"));
              }});

        c.put("CBAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:PETAPA"));
              }});

        c.put("CBAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:PPA"));
              }});

        c.put("CBAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:PSI"));
              }});

        c.put("CBAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:TERAPA"));
              }});

        c.put("CBAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:TORR"));
              }});

        c.put("CBAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:YOTTAPA"));
              }});

        c.put("CBAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:YPA"));
              }});

        c.put("CBAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:ZETTAPA"));
              }});

        c.put("CBAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CBAR:ZPA"));
              }});

        c.put("CPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("CPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:ATM"));
              }});

        c.put("CPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:BAR"));
              }});

        c.put("CPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:CBAR"));
              }});

        c.put("CPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("CPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:DBAR"));
              }});

        c.put("CPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("CPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("CPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("CPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("CPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("CPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:KBAR"));
              }});

        c.put("CPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("CPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:MBAR"));
              }});

        c.put("CPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:MEGABAR"));
              }});

        c.put("CPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("CPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("CPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:MMHG"));
              }});

        c.put("CPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("CPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:MTORR"));
              }});

        c.put("CPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("CPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("CPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("CPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("CPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:PSI"));
              }});

        c.put("CPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("CPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CPA:TORR"));
              }});

        c.put("CPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("CPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("CPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("CPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:ATM"));
              }});

        c.put("DAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:BAR"));
              }});

        c.put("DAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:CBAR"));
              }});

        c.put("DAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("DAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:DBAR"));
              }});

        c.put("DAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("DAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("DAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("DAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:KBAR"));
              }});

        c.put("DAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:MBAR"));
              }});

        c.put("DAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:MEGABAR"));
              }});

        c.put("DAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("DAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("DAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:MMHG"));
              }});

        c.put("DAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("DAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:MTORR"));
              }});

        c.put("DAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("DAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("DAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("DAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:PSI"));
              }});

        c.put("DAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("DAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAPA:TORR"));
              }});

        c.put("DAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("DAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("DAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("DAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("DBAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:APA"));
              }});

        c.put("DBAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:ATM"));
              }});

        c.put("DBAR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:BAR"));
              }});

        c.put("DBAR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:CBAR"));
              }});

        c.put("DBAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:CPA"));
              }});

        c.put("DBAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:DAPA"));
              }});

        c.put("DBAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:DPA"));
              }});

        c.put("DBAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:EXAPA"));
              }});

        c.put("DBAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:FPA"));
              }});

        c.put("DBAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:GIGAPA"));
              }});

        c.put("DBAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:HPA"));
              }});

        c.put("DBAR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:KBAR"));
              }});

        c.put("DBAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:KPA"));
              }});

        c.put("DBAR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MBAR"));
              }});

        c.put("DBAR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MEGABAR"));
              }});

        c.put("DBAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MEGAPA"));
              }});

        c.put("DBAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MICROPA"));
              }});

        c.put("DBAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MMHG"));
              }});

        c.put("DBAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MPA"));
              }});

        c.put("DBAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:MTORR"));
              }});

        c.put("DBAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:NPA"));
              }});

        c.put("DBAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:PA"));
              }});

        c.put("DBAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:PETAPA"));
              }});

        c.put("DBAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:PPA"));
              }});

        c.put("DBAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:PSI"));
              }});

        c.put("DBAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:TERAPA"));
              }});

        c.put("DBAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:TORR"));
              }});

        c.put("DBAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:YOTTAPA"));
              }});

        c.put("DBAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:YPA"));
              }});

        c.put("DBAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:ZETTAPA"));
              }});

        c.put("DBAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DBAR:ZPA"));
              }});

        c.put("DPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("DPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:ATM"));
              }});

        c.put("DPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:BAR"));
              }});

        c.put("DPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:CBAR"));
              }});

        c.put("DPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:DBAR"));
              }});

        c.put("DPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("DPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("DPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("DPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("DPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:KBAR"));
              }});

        c.put("DPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("DPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:MBAR"));
              }});

        c.put("DPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:MEGABAR"));
              }});

        c.put("DPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("DPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("DPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:MMHG"));
              }});

        c.put("DPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:MTORR"));
              }});

        c.put("DPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("DPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("DPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("DPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:PSI"));
              }});

        c.put("DPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("DPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DPA:TORR"));
              }});

        c.put("DPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("DPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("DPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("DPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("EXAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:ATM"));
              }});

        c.put("EXAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:BAR"));
              }});

        c.put("EXAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:CBAR"));
              }});

        c.put("EXAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("EXAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:DBAR"));
              }});

        c.put("EXAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("EXAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("EXAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("EXAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("EXAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:KBAR"));
              }});

        c.put("EXAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("EXAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:MBAR"));
              }});

        c.put("EXAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:MEGABAR"));
              }});

        c.put("EXAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("EXAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("EXAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:MMHG"));
              }});

        c.put("EXAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("EXAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:MTORR"));
              }});

        c.put("EXAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("EXAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("EXAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("EXAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("EXAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:PSI"));
              }});

        c.put("EXAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("EXAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAPA:TORR"));
              }});

        c.put("EXAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("EXAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("EXAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("EXAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("FPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("FPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:ATM"));
              }});

        c.put("FPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:BAR"));
              }});

        c.put("FPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:CBAR"));
              }});

        c.put("FPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("FPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("FPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:DBAR"));
              }});

        c.put("FPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("FPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("FPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("FPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("FPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:KBAR"));
              }});

        c.put("FPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("FPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:MBAR"));
              }});

        c.put("FPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:MEGABAR"));
              }});

        c.put("FPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("FPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("FPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:MMHG"));
              }});

        c.put("FPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("FPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:MTORR"));
              }});

        c.put("FPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("FPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("FPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("FPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("FPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:PSI"));
              }});

        c.put("FPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("FPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FPA:TORR"));
              }});

        c.put("FPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("FPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("FPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("FPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("GIGAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:ATM"));
              }});

        c.put("GIGAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:BAR"));
              }});

        c.put("GIGAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:CBAR"));
              }});

        c.put("GIGAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("GIGAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("GIGAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:DBAR"));
              }});

        c.put("GIGAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("GIGAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("GIGAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("GIGAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("GIGAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:KBAR"));
              }});

        c.put("GIGAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:MBAR"));
              }});

        c.put("GIGAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:MEGABAR"));
              }});

        c.put("GIGAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("GIGAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("GIGAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:MMHG"));
              }});

        c.put("GIGAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("GIGAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:MTORR"));
              }});

        c.put("GIGAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("GIGAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("GIGAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("GIGAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("GIGAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:PSI"));
              }});

        c.put("GIGAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("GIGAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAPA:TORR"));
              }});

        c.put("GIGAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("GIGAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("GIGAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("GIGAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("HPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("HPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:ATM"));
              }});

        c.put("HPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:BAR"));
              }});

        c.put("HPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:CBAR"));
              }});

        c.put("HPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("HPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:DBAR"));
              }});

        c.put("HPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("HPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("HPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("HPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:KBAR"));
              }});

        c.put("HPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:MBAR"));
              }});

        c.put("HPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:MEGABAR"));
              }});

        c.put("HPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("HPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("HPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:MMHG"));
              }});

        c.put("HPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("HPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:MTORR"));
              }});

        c.put("HPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("HPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("HPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("HPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:PSI"));
              }});

        c.put("HPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("HPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HPA:TORR"));
              }});

        c.put("HPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("HPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("HPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("HPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("KBAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:APA"));
              }});

        c.put("KBAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:ATM"));
              }});

        c.put("KBAR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:BAR"));
              }});

        c.put("KBAR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:CBAR"));
              }});

        c.put("KBAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:CPA"));
              }});

        c.put("KBAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:DAPA"));
              }});

        c.put("KBAR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:DBAR"));
              }});

        c.put("KBAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:DPA"));
              }});

        c.put("KBAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:EXAPA"));
              }});

        c.put("KBAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:FPA"));
              }});

        c.put("KBAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:GIGAPA"));
              }});

        c.put("KBAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:HPA"));
              }});

        c.put("KBAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:KPA"));
              }});

        c.put("KBAR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MBAR"));
              }});

        c.put("KBAR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MEGABAR"));
              }});

        c.put("KBAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MEGAPA"));
              }});

        c.put("KBAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MICROPA"));
              }});

        c.put("KBAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MMHG"));
              }});

        c.put("KBAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MPA"));
              }});

        c.put("KBAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:MTORR"));
              }});

        c.put("KBAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:NPA"));
              }});

        c.put("KBAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:PA"));
              }});

        c.put("KBAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:PETAPA"));
              }});

        c.put("KBAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:PPA"));
              }});

        c.put("KBAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:PSI"));
              }});

        c.put("KBAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:TERAPA"));
              }});

        c.put("KBAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:TORR"));
              }});

        c.put("KBAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:YOTTAPA"));
              }});

        c.put("KBAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:YPA"));
              }});

        c.put("KBAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:ZETTAPA"));
              }});

        c.put("KBAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KBAR:ZPA"));
              }});

        c.put("KPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:ATM"));
              }});

        c.put("KPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:BAR"));
              }});

        c.put("KPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:CBAR"));
              }});

        c.put("KPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("KPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("KPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:DBAR"));
              }});

        c.put("KPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("KPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("KPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("KPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("KPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("KPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:KBAR"));
              }});

        c.put("KPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:MBAR"));
              }});

        c.put("KPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:MEGABAR"));
              }});

        c.put("KPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("KPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("KPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:MMHG"));
              }});

        c.put("KPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("KPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:MTORR"));
              }});

        c.put("KPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("KPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("KPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("KPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("KPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:PSI"));
              }});

        c.put("KPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("KPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KPA:TORR"));
              }});

        c.put("KPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("KPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("KPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("KPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MBAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:APA"));
              }});

        c.put("MBAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:ATM"));
              }});

        c.put("MBAR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:BAR"));
              }});

        c.put("MBAR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:CBAR"));
              }});

        c.put("MBAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:CPA"));
              }});

        c.put("MBAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:DAPA"));
              }});

        c.put("MBAR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:DBAR"));
              }});

        c.put("MBAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:DPA"));
              }});

        c.put("MBAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:EXAPA"));
              }});

        c.put("MBAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:FPA"));
              }});

        c.put("MBAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:GIGAPA"));
              }});

        c.put("MBAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:HPA"));
              }});

        c.put("MBAR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:KBAR"));
              }});

        c.put("MBAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:KPA"));
              }});

        c.put("MBAR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MEGABAR"));
              }});

        c.put("MBAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MEGAPA"));
              }});

        c.put("MBAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MICROPA"));
              }});

        c.put("MBAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MMHG"));
              }});

        c.put("MBAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MPA"));
              }});

        c.put("MBAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:MTORR"));
              }});

        c.put("MBAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:NPA"));
              }});

        c.put("MBAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:PA"));
              }});

        c.put("MBAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:PETAPA"));
              }});

        c.put("MBAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:PPA"));
              }});

        c.put("MBAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:PSI"));
              }});

        c.put("MBAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:TERAPA"));
              }});

        c.put("MBAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:TORR"));
              }});

        c.put("MBAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:YOTTAPA"));
              }});

        c.put("MBAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:YPA"));
              }});

        c.put("MBAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:ZETTAPA"));
              }});

        c.put("MBAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MBAR:ZPA"));
              }});

        c.put("MEGABAR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:APA"));
              }});

        c.put("MEGABAR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:ATM"));
              }});

        c.put("MEGABAR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:BAR"));
              }});

        c.put("MEGABAR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:CBAR"));
              }});

        c.put("MEGABAR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:CPA"));
              }});

        c.put("MEGABAR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:DAPA"));
              }});

        c.put("MEGABAR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:DBAR"));
              }});

        c.put("MEGABAR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:DPA"));
              }});

        c.put("MEGABAR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:EXAPA"));
              }});

        c.put("MEGABAR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:FPA"));
              }});

        c.put("MEGABAR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:GIGAPA"));
              }});

        c.put("MEGABAR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:HPA"));
              }});

        c.put("MEGABAR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:KBAR"));
              }});

        c.put("MEGABAR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:KPA"));
              }});

        c.put("MEGABAR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MBAR"));
              }});

        c.put("MEGABAR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MEGAPA"));
              }});

        c.put("MEGABAR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MICROPA"));
              }});

        c.put("MEGABAR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MMHG"));
              }});

        c.put("MEGABAR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MPA"));
              }});

        c.put("MEGABAR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:MTORR"));
              }});

        c.put("MEGABAR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:NPA"));
              }});

        c.put("MEGABAR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:PA"));
              }});

        c.put("MEGABAR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:PETAPA"));
              }});

        c.put("MEGABAR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:PPA"));
              }});

        c.put("MEGABAR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:PSI"));
              }});

        c.put("MEGABAR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:TERAPA"));
              }});

        c.put("MEGABAR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:TORR"));
              }});

        c.put("MEGABAR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:YOTTAPA"));
              }});

        c.put("MEGABAR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:YPA"));
              }});

        c.put("MEGABAR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:ZETTAPA"));
              }});

        c.put("MEGABAR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGABAR:ZPA"));
              }});

        c.put("MEGAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:ATM"));
              }});

        c.put("MEGAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:BAR"));
              }});

        c.put("MEGAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:CBAR"));
              }});

        c.put("MEGAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("MEGAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("MEGAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:DBAR"));
              }});

        c.put("MEGAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("MEGAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MEGAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MEGAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("MEGAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:KBAR"));
              }});

        c.put("MEGAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MEGAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:MBAR"));
              }});

        c.put("MEGAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:MEGABAR"));
              }});

        c.put("MEGAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MEGAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:MMHG"));
              }});

        c.put("MEGAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MEGAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:MTORR"));
              }});

        c.put("MEGAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MEGAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MEGAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MEGAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MEGAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:PSI"));
              }});

        c.put("MEGAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MEGAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAPA:TORR"));
              }});

        c.put("MEGAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MEGAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("MEGAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MEGAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("MICROPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MICROPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:ATM"));
              }});

        c.put("MICROPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:BAR"));
              }});

        c.put("MICROPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:CBAR"));
              }});

        c.put("MICROPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MICROPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("MICROPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:DBAR"));
              }});

        c.put("MICROPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MICROPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MICROPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MICROPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MICROPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("MICROPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:KBAR"));
              }});

        c.put("MICROPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MICROPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:MBAR"));
              }});

        c.put("MICROPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:MEGABAR"));
              }});

        c.put("MICROPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MICROPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:MMHG"));
              }});

        c.put("MICROPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MICROPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:MTORR"));
              }});

        c.put("MICROPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MICROPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MICROPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MICROPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MICROPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:PSI"));
              }});

        c.put("MICROPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MICROPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROPA:TORR"));
              }});

        c.put("MICROPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("MICROPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MICROPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MMHG:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:APA"));
              }});

        c.put("MMHG:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:ATM"));
              }});

        c.put("MMHG:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:BAR"));
              }});

        c.put("MMHG:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:CBAR"));
              }});

        c.put("MMHG:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:CPA"));
              }});

        c.put("MMHG:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:DAPA"));
              }});

        c.put("MMHG:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:DBAR"));
              }});

        c.put("MMHG:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:DPA"));
              }});

        c.put("MMHG:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:EXAPA"));
              }});

        c.put("MMHG:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:FPA"));
              }});

        c.put("MMHG:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:GIGAPA"));
              }});

        c.put("MMHG:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:HPA"));
              }});

        c.put("MMHG:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:KBAR"));
              }});

        c.put("MMHG:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:KPA"));
              }});

        c.put("MMHG:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MBAR"));
              }});

        c.put("MMHG:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MEGABAR"));
              }});

        c.put("MMHG:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MEGAPA"));
              }});

        c.put("MMHG:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MICROPA"));
              }});

        c.put("MMHG:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MPA"));
              }});

        c.put("MMHG:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:MTORR"));
              }});

        c.put("MMHG:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:NPA"));
              }});

        c.put("MMHG:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:PA"));
              }});

        c.put("MMHG:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:PETAPA"));
              }});

        c.put("MMHG:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:PPA"));
              }});

        c.put("MMHG:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:PSI"));
              }});

        c.put("MMHG:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:TERAPA"));
              }});

        c.put("MMHG:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:TORR"));
              }});

        c.put("MMHG:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:YOTTAPA"));
              }});

        c.put("MMHG:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:YPA"));
              }});

        c.put("MMHG:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:ZETTAPA"));
              }});

        c.put("MMHG:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MMHG:ZPA"));
              }});

        c.put("MPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:ATM"));
              }});

        c.put("MPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:BAR"));
              }});

        c.put("MPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:CBAR"));
              }});

        c.put("MPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("MPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:DBAR"));
              }});

        c.put("MPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("MPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:KBAR"));
              }});

        c.put("MPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:MBAR"));
              }});

        c.put("MPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:MEGABAR"));
              }});

        c.put("MPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:MMHG"));
              }});

        c.put("MPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:MTORR"));
              }});

        c.put("MPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:PSI"));
              }});

        c.put("MPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MPA:TORR"));
              }});

        c.put("MPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MTORR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:APA"));
              }});

        c.put("MTORR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:ATM"));
              }});

        c.put("MTORR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:BAR"));
              }});

        c.put("MTORR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:CBAR"));
              }});

        c.put("MTORR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:CPA"));
              }});

        c.put("MTORR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:DAPA"));
              }});

        c.put("MTORR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:DBAR"));
              }});

        c.put("MTORR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:DPA"));
              }});

        c.put("MTORR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:EXAPA"));
              }});

        c.put("MTORR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:FPA"));
              }});

        c.put("MTORR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:GIGAPA"));
              }});

        c.put("MTORR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:HPA"));
              }});

        c.put("MTORR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:KBAR"));
              }});

        c.put("MTORR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:KPA"));
              }});

        c.put("MTORR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MBAR"));
              }});

        c.put("MTORR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MEGABAR"));
              }});

        c.put("MTORR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MEGAPA"));
              }});

        c.put("MTORR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MICROPA"));
              }});

        c.put("MTORR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MMHG"));
              }});

        c.put("MTORR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:MPA"));
              }});

        c.put("MTORR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:NPA"));
              }});

        c.put("MTORR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:PA"));
              }});

        c.put("MTORR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:PETAPA"));
              }});

        c.put("MTORR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:PPA"));
              }});

        c.put("MTORR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:PSI"));
              }});

        c.put("MTORR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:TERAPA"));
              }});

        c.put("MTORR:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:TORR"));
              }});

        c.put("MTORR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:YOTTAPA"));
              }});

        c.put("MTORR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:YPA"));
              }});

        c.put("MTORR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:ZETTAPA"));
              }});

        c.put("MTORR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MTORR:ZPA"));
              }});

        c.put("NPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("NPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:ATM"));
              }});

        c.put("NPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:BAR"));
              }});

        c.put("NPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:CBAR"));
              }});

        c.put("NPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("NPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("NPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:DBAR"));
              }});

        c.put("NPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("NPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("NPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("NPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("NPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("NPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:KBAR"));
              }});

        c.put("NPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("NPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:MBAR"));
              }});

        c.put("NPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:MEGABAR"));
              }});

        c.put("NPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("NPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("NPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:MMHG"));
              }});

        c.put("NPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("NPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:MTORR"));
              }});

        c.put("NPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("NPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("NPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("NPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:PSI"));
              }});

        c.put("NPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("NPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NPA:TORR"));
              }});

        c.put("NPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("NPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("NPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:ATM"));
              }});

        c.put("PA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:BAR"));
              }});

        c.put("PA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:CBAR"));
              }});

        c.put("PA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("PA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("PA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:DBAR"));
              }});

        c.put("PA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("PA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("PA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:KBAR"));
              }});

        c.put("PA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:MBAR"));
              }});

        c.put("PA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:MEGABAR"));
              }});

        c.put("PA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:MMHG"));
              }});

        c.put("PA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:MTORR"));
              }});

        c.put("PA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:PSI"));
              }});

        c.put("PA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PA:TORR"));
              }});

        c.put("PA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("PETAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:ATM"));
              }});

        c.put("PETAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:BAR"));
              }});

        c.put("PETAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:CBAR"));
              }});

        c.put("PETAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("PETAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("PETAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:DBAR"));
              }});

        c.put("PETAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("PETAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PETAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("PETAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PETAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("PETAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:KBAR"));
              }});

        c.put("PETAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:MBAR"));
              }});

        c.put("PETAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:MEGABAR"));
              }});

        c.put("PETAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PETAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:MMHG"));
              }});

        c.put("PETAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PETAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:MTORR"));
              }});

        c.put("PETAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PETAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PETAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("PETAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:PSI"));
              }});

        c.put("PETAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PETAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAPA:TORR"));
              }});

        c.put("PETAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PETAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("PETAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PETAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("PPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:ATM"));
              }});

        c.put("PPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:BAR"));
              }});

        c.put("PPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:CBAR"));
              }});

        c.put("PPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("PPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("PPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:DBAR"));
              }});

        c.put("PPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("PPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("PPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("PPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:KBAR"));
              }});

        c.put("PPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:MBAR"));
              }});

        c.put("PPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:MEGABAR"));
              }});

        c.put("PPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:MMHG"));
              }});

        c.put("PPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:MTORR"));
              }});

        c.put("PPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("PPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:PSI"));
              }});

        c.put("PPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PPA:TORR"));
              }});

        c.put("PPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("PPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("PPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PSI:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:APA"));
              }});

        c.put("PSI:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:ATM"));
              }});

        c.put("PSI:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:BAR"));
              }});

        c.put("PSI:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:CBAR"));
              }});

        c.put("PSI:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:CPA"));
              }});

        c.put("PSI:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:DAPA"));
              }});

        c.put("PSI:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:DBAR"));
              }});

        c.put("PSI:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:DPA"));
              }});

        c.put("PSI:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:EXAPA"));
              }});

        c.put("PSI:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:FPA"));
              }});

        c.put("PSI:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:GIGAPA"));
              }});

        c.put("PSI:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:HPA"));
              }});

        c.put("PSI:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:KBAR"));
              }});

        c.put("PSI:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:KPA"));
              }});

        c.put("PSI:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MBAR"));
              }});

        c.put("PSI:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MEGABAR"));
              }});

        c.put("PSI:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MEGAPA"));
              }});

        c.put("PSI:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MICROPA"));
              }});

        c.put("PSI:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MMHG"));
              }});

        c.put("PSI:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MPA"));
              }});

        c.put("PSI:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:MTORR"));
              }});

        c.put("PSI:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:NPA"));
              }});

        c.put("PSI:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:PA"));
              }});

        c.put("PSI:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:PETAPA"));
              }});

        c.put("PSI:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:PPA"));
              }});

        c.put("PSI:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:TERAPA"));
              }});

        c.put("PSI:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:TORR"));
              }});

        c.put("PSI:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:YOTTAPA"));
              }});

        c.put("PSI:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:YPA"));
              }});

        c.put("PSI:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:ZETTAPA"));
              }});

        c.put("PSI:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PSI:ZPA"));
              }});

        c.put("TERAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("TERAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:ATM"));
              }});

        c.put("TERAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:BAR"));
              }});

        c.put("TERAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:CBAR"));
              }});

        c.put("TERAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("TERAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("TERAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:DBAR"));
              }});

        c.put("TERAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("TERAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("TERAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("TERAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("TERAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("TERAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:KBAR"));
              }});

        c.put("TERAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:MBAR"));
              }});

        c.put("TERAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:MEGABAR"));
              }});

        c.put("TERAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("TERAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("TERAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:MMHG"));
              }});

        c.put("TERAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("TERAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:MTORR"));
              }});

        c.put("TERAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("TERAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("TERAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("TERAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:PSI"));
              }});

        c.put("TERAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAPA:TORR"));
              }});

        c.put("TERAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("TERAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("TERAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("TERAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("TORR:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:APA"));
              }});

        c.put("TORR:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:ATM"));
              }});

        c.put("TORR:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:BAR"));
              }});

        c.put("TORR:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:CBAR"));
              }});

        c.put("TORR:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:CPA"));
              }});

        c.put("TORR:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:DAPA"));
              }});

        c.put("TORR:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:DBAR"));
              }});

        c.put("TORR:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:DPA"));
              }});

        c.put("TORR:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:EXAPA"));
              }});

        c.put("TORR:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:FPA"));
              }});

        c.put("TORR:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:GIGAPA"));
              }});

        c.put("TORR:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:HPA"));
              }});

        c.put("TORR:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:KBAR"));
              }});

        c.put("TORR:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:KPA"));
              }});

        c.put("TORR:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MBAR"));
              }});

        c.put("TORR:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MEGABAR"));
              }});

        c.put("TORR:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MEGAPA"));
              }});

        c.put("TORR:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MICROPA"));
              }});

        c.put("TORR:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MMHG"));
              }});

        c.put("TORR:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MPA"));
              }});

        c.put("TORR:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:MTORR"));
              }});

        c.put("TORR:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:NPA"));
              }});

        c.put("TORR:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:PA"));
              }});

        c.put("TORR:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:PETAPA"));
              }});

        c.put("TORR:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:PPA"));
              }});

        c.put("TORR:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:PSI"));
              }});

        c.put("TORR:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:TERAPA"));
              }});

        c.put("TORR:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:YOTTAPA"));
              }});

        c.put("TORR:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:YPA"));
              }});

        c.put("TORR:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:ZETTAPA"));
              }});

        c.put("TORR:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TORR:ZPA"));
              }});

        c.put("YOTTAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("YOTTAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:ATM"));
              }});

        c.put("YOTTAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:BAR"));
              }});

        c.put("YOTTAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:CBAR"));
              }});

        c.put("YOTTAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("YOTTAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("YOTTAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:DBAR"));
              }});

        c.put("YOTTAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("YOTTAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("YOTTAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("YOTTAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("YOTTAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("YOTTAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:KBAR"));
              }});

        c.put("YOTTAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:MBAR"));
              }});

        c.put("YOTTAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:MEGABAR"));
              }});

        c.put("YOTTAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("YOTTAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("YOTTAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:MMHG"));
              }});

        c.put("YOTTAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("YOTTAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:MTORR"));
              }});

        c.put("YOTTAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("YOTTAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("YOTTAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("YOTTAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:PSI"));
              }});

        c.put("YOTTAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("YOTTAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAPA:TORR"));
              }});

        c.put("YOTTAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 48) * value;
              }});

        c.put("YOTTAPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("YOTTAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("YPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("YPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:ATM"));
              }});

        c.put("YPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:BAR"));
              }});

        c.put("YPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:CBAR"));
              }});

        c.put("YPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("YPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("YPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:DBAR"));
              }});

        c.put("YPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("YPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("YPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("YPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("YPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("YPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:KBAR"));
              }});

        c.put("YPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("YPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:MBAR"));
              }});

        c.put("YPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:MEGABAR"));
              }});

        c.put("YPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("YPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("YPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:MMHG"));
              }});

        c.put("YPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("YPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:MTORR"));
              }});

        c.put("YPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("YPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("YPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("YPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("YPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:PSI"));
              }});

        c.put("YPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("YPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YPA:TORR"));
              }});

        c.put("YPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -48) * value;
              }});

        c.put("YPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("YPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("ZETTAPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:ATM"));
              }});

        c.put("ZETTAPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:BAR"));
              }});

        c.put("ZETTAPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:CBAR"));
              }});

        c.put("ZETTAPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("ZETTAPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("ZETTAPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:DBAR"));
              }});

        c.put("ZETTAPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("ZETTAPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZETTAPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("ZETTAPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("ZETTAPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("ZETTAPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:KBAR"));
              }});

        c.put("ZETTAPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("ZETTAPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:MBAR"));
              }});

        c.put("ZETTAPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:MEGABAR"));
              }});

        c.put("ZETTAPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("ZETTAPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("ZETTAPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:MMHG"));
              }});

        c.put("ZETTAPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("ZETTAPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:MTORR"));
              }});

        c.put("ZETTAPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("ZETTAPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("ZETTAPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("ZETTAPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("ZETTAPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:PSI"));
              }});

        c.put("ZETTAPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("ZETTAPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAPA:TORR"));
              }});

        c.put("ZETTAPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAPA:ZPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("ZPA:APA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZPA:ATM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:ATM"));
              }});

        c.put("ZPA:BAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:BAR"));
              }});

        c.put("ZPA:CBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:CBAR"));
              }});

        c.put("ZPA:CPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("ZPA:DAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("ZPA:DBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:DBAR"));
              }});

        c.put("ZPA:DPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("ZPA:EXAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("ZPA:FPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("ZPA:GIGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("ZPA:HPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("ZPA:KBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:KBAR"));
              }});

        c.put("ZPA:KPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("ZPA:MBAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:MBAR"));
              }});

        c.put("ZPA:MEGABAR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:MEGABAR"));
              }});

        c.put("ZPA:MEGAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("ZPA:MICROPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("ZPA:MMHG", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:MMHG"));
              }});

        c.put("ZPA:MPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("ZPA:MTORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:MTORR"));
              }});

        c.put("ZPA:NPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("ZPA:PA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("ZPA:PETAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("ZPA:PPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("ZPA:PSI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:PSI"));
              }});

        c.put("ZPA:TERAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("ZPA:TORR", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZPA:TORR"));
              }});

        c.put("ZPA:YOTTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("ZPA:YPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZPA:ZETTAPA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});
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
            Function<Double, Double> c = conversions.get(source + ":" + target);
            if (c == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            setValue(c.apply(value.getValue()));
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

