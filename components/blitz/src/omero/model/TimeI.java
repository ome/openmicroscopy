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

import omero.model.enums.UnitsTime;

/**
 * Blitz wrapper around the {@link ome.model.units.Time} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class TimeI extends Time implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Function<Double, Double>> conversions;
    static {
        Map<String, Function<Double, Double>> c = new HashMap<String, Function<Double, Double>>();

        c.put("AS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("AS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AS:D"));
              }});

        c.put("AS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("AS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("AS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("AS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("AS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("AS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AS:H"));
              }});

        c.put("AS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("AS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("AS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("AS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("AS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AS:MIN"));
              }});

        c.put("AS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("AS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("AS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("AS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("AS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("AS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("AS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("AS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("AS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("AS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("CS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("CS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CS:D"));
              }});

        c.put("CS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("CS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("CS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("CS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("CS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("CS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CS:H"));
              }});

        c.put("CS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("CS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("CS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("CS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("CS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CS:MIN"));
              }});

        c.put("CS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("CS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("CS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("CS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("CS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("CS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("CS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("CS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("CS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("CS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("D:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:AS"));
              }});

        c.put("D:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:CS"));
              }});

        c.put("D:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:DAS"));
              }});

        c.put("D:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:DS"));
              }});

        c.put("D:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:EXAS"));
              }});

        c.put("D:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:FS"));
              }});

        c.put("D:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:GIGAS"));
              }});

        c.put("D:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:H"));
              }});

        c.put("D:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:HS"));
              }});

        c.put("D:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:KS"));
              }});

        c.put("D:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:MEGAS"));
              }});

        c.put("D:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:MICROS"));
              }});

        c.put("D:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:MIN"));
              }});

        c.put("D:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:MS"));
              }});

        c.put("D:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:NS"));
              }});

        c.put("D:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:PETAS"));
              }});

        c.put("D:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:PS"));
              }});

        c.put("D:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:S"));
              }});

        c.put("D:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:TERAS"));
              }});

        c.put("D:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:YOTTAS"));
              }});

        c.put("D:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:YS"));
              }});

        c.put("D:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:ZETTAS"));
              }});

        c.put("D:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "D:ZS"));
              }});

        c.put("DAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("DAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAS:D"));
              }});

        c.put("DAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("DAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("DAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("DAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAS:H"));
              }});

        c.put("DAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("DAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("DAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAS:MIN"));
              }});

        c.put("DAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("DAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("DAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("DAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("DAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("DAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("DAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("DAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("DAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("DS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("DS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DS:D"));
              }});

        c.put("DS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("DS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("DS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("DS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DS:H"));
              }});

        c.put("DS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("DS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("DS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("DS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("DS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DS:MIN"));
              }});

        c.put("DS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("DS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("DS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("DS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("DS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("DS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("DS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("DS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("EXAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAS:D"));
              }});

        c.put("EXAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("EXAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("EXAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("EXAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("EXAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAS:H"));
              }});

        c.put("EXAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("EXAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("EXAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("EXAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("EXAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAS:MIN"));
              }});

        c.put("EXAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("EXAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("EXAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("EXAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("EXAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("EXAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("EXAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("EXAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("EXAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("EXAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("FS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("FS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("FS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FS:D"));
              }});

        c.put("FS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("FS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("FS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("FS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("FS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FS:H"));
              }});

        c.put("FS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("FS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("FS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("FS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("FS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FS:MIN"));
              }});

        c.put("FS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("FS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("FS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("FS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("FS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("FS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("FS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("FS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("FS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("FS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("GIGAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("GIGAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAS:D"));
              }});

        c.put("GIGAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("GIGAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("GIGAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("GIGAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("GIGAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAS:H"));
              }});

        c.put("GIGAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("GIGAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("GIGAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("GIGAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAS:MIN"));
              }});

        c.put("GIGAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("GIGAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("GIGAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("GIGAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("GIGAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("GIGAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("GIGAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("GIGAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("GIGAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("GIGAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("H:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:AS"));
              }});

        c.put("H:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:CS"));
              }});

        c.put("H:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:D"));
              }});

        c.put("H:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:DAS"));
              }});

        c.put("H:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:DS"));
              }});

        c.put("H:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:EXAS"));
              }});

        c.put("H:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:FS"));
              }});

        c.put("H:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:GIGAS"));
              }});

        c.put("H:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:HS"));
              }});

        c.put("H:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:KS"));
              }});

        c.put("H:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:MEGAS"));
              }});

        c.put("H:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:MICROS"));
              }});

        c.put("H:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:MIN"));
              }});

        c.put("H:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:MS"));
              }});

        c.put("H:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:NS"));
              }});

        c.put("H:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:PETAS"));
              }});

        c.put("H:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:PS"));
              }});

        c.put("H:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:S"));
              }});

        c.put("H:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:TERAS"));
              }});

        c.put("H:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:YOTTAS"));
              }});

        c.put("H:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:YS"));
              }});

        c.put("H:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:ZETTAS"));
              }});

        c.put("H:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "H:ZS"));
              }});

        c.put("HS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("HS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("HS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HS:D"));
              }});

        c.put("HS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("HS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("HS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("HS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HS:H"));
              }});

        c.put("HS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("HS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("HS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HS:MIN"));
              }});

        c.put("HS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("HS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("HS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("HS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("HS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("HS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("HS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("HS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("HS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("KS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("KS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KS:D"));
              }});

        c.put("KS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("KS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("KS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("KS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("KS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("KS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KS:H"));
              }});

        c.put("KS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("KS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("KS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("KS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KS:MIN"));
              }});

        c.put("KS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("KS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("KS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("KS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("KS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("KS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("KS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("KS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("KS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("KS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("MEGAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAS:D"));
              }});

        c.put("MEGAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("MEGAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("MEGAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MEGAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MEGAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAS:H"));
              }});

        c.put("MEGAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("MEGAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MEGAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MEGAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAS:MIN"));
              }});

        c.put("MEGAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MEGAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MEGAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MEGAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MEGAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MEGAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MEGAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MEGAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("MEGAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MEGAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("MICROS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MICROS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MICROS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROS:D"));
              }});

        c.put("MICROS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("MICROS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MICROS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MICROS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MICROS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MICROS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROS:H"));
              }});

        c.put("MICROS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("MICROS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MICROS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MICROS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROS:MIN"));
              }});

        c.put("MICROS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MICROS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MICROS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MICROS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MICROS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MICROS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MICROS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("MICROS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MICROS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MIN:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:AS"));
              }});

        c.put("MIN:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:CS"));
              }});

        c.put("MIN:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:D"));
              }});

        c.put("MIN:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:DAS"));
              }});

        c.put("MIN:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:DS"));
              }});

        c.put("MIN:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:EXAS"));
              }});

        c.put("MIN:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:FS"));
              }});

        c.put("MIN:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:GIGAS"));
              }});

        c.put("MIN:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:H"));
              }});

        c.put("MIN:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:HS"));
              }});

        c.put("MIN:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:KS"));
              }});

        c.put("MIN:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:MEGAS"));
              }});

        c.put("MIN:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:MICROS"));
              }});

        c.put("MIN:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:MS"));
              }});

        c.put("MIN:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:NS"));
              }});

        c.put("MIN:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:PETAS"));
              }});

        c.put("MIN:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:PS"));
              }});

        c.put("MIN:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:S"));
              }});

        c.put("MIN:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:TERAS"));
              }});

        c.put("MIN:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:YOTTAS"));
              }});

        c.put("MIN:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:YS"));
              }});

        c.put("MIN:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:ZETTAS"));
              }});

        c.put("MIN:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MIN:ZS"));
              }});

        c.put("MS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("MS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MS:D"));
              }});

        c.put("MS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("MS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MS:H"));
              }});

        c.put("MS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MS:MIN"));
              }});

        c.put("MS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("NS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("NS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("NS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NS:D"));
              }});

        c.put("NS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("NS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("NS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("NS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("NS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("NS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NS:H"));
              }});

        c.put("NS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("NS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("NS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("NS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("NS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NS:MIN"));
              }});

        c.put("NS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("NS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("NS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("NS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("NS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("NS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("NS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("NS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("PETAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("PETAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAS:D"));
              }});

        c.put("PETAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("PETAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("PETAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PETAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("PETAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PETAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAS:H"));
              }});

        c.put("PETAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("PETAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PETAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAS:MIN"));
              }});

        c.put("PETAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PETAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PETAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("PETAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PETAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PETAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PETAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("PETAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PETAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("PS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("PS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PS:D"));
              }});

        c.put("PS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("PS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("PS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("PS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PS:H"));
              }});

        c.put("PS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("PS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PS:MIN"));
              }});

        c.put("PS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("PS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("PS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("PS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("S:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("S:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("S:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "S:D"));
              }});

        c.put("S:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("S:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("S:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("S:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("S:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("S:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "S:H"));
              }});

        c.put("S:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("S:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("S:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("S:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("S:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "S:MIN"));
              }});

        c.put("S:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("S:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("S:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("S:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("S:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("S:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("S:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("S:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("S:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("TERAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("TERAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAS:D"));
              }});

        c.put("TERAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("TERAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("TERAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("TERAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("TERAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("TERAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAS:H"));
              }});

        c.put("TERAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("TERAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("TERAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("TERAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAS:MIN"));
              }});

        c.put("TERAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("TERAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("TERAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("TERAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("TERAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("TERAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("TERAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("TERAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("YOTTAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("YOTTAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAS:D"));
              }});

        c.put("YOTTAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("YOTTAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("YOTTAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("YOTTAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("YOTTAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("YOTTAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAS:H"));
              }});

        c.put("YOTTAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("YOTTAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("YOTTAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("YOTTAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAS:MIN"));
              }});

        c.put("YOTTAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("YOTTAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("YOTTAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("YOTTAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("YOTTAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("YOTTAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 48) * value;
              }});

        c.put("YOTTAS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("YOTTAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("YS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("YS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("YS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YS:D"));
              }});

        c.put("YS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("YS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("YS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("YS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("YS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("YS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YS:H"));
              }});

        c.put("YS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("YS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("YS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("YS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("YS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YS:MIN"));
              }});

        c.put("YS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("YS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("YS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("YS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("YS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("YS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("YS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -48) * value;
              }});

        c.put("YS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("YS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("ZETTAS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("ZETTAS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAS:D"));
              }});

        c.put("ZETTAS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("ZETTAS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("ZETTAS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZETTAS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("ZETTAS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("ZETTAS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAS:H"));
              }});

        c.put("ZETTAS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("ZETTAS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("ZETTAS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("ZETTAS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("ZETTAS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAS:MIN"));
              }});

        c.put("ZETTAS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("ZETTAS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("ZETTAS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("ZETTAS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("ZETTAS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("ZETTAS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("ZETTAS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAS:ZS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("ZS:AS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZS:CS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("ZS:D", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZS:D"));
              }});

        c.put("ZS:DAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("ZS:DS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("ZS:EXAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("ZS:FS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("ZS:GIGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("ZS:H", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZS:H"));
              }});

        c.put("ZS:HS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("ZS:KS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("ZS:MEGAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("ZS:MICROS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("ZS:MIN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZS:MIN"));
              }});

        c.put("ZS:MS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("ZS:NS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("ZS:PETAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("ZS:PS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("ZS:S", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("ZS:TERAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("ZS:YOTTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("ZS:YS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZS:ZETTAS", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTime, String> SYMBOLS;
    static {
        Map<UnitsTime, String> s = new HashMap<UnitsTime, String>();
        s.put(UnitsTime.AS, "as");
        s.put(UnitsTime.CS, "cs");
        s.put(UnitsTime.D, "d");
        s.put(UnitsTime.DAS, "das");
        s.put(UnitsTime.DS, "ds");
        s.put(UnitsTime.EXAS, "Es");
        s.put(UnitsTime.FS, "fs");
        s.put(UnitsTime.GIGAS, "Gs");
        s.put(UnitsTime.H, "h");
        s.put(UnitsTime.HS, "hs");
        s.put(UnitsTime.KS, "ks");
        s.put(UnitsTime.MEGAS, "Ms");
        s.put(UnitsTime.MICROS, "µs");
        s.put(UnitsTime.MIN, "min");
        s.put(UnitsTime.MS, "ms");
        s.put(UnitsTime.NS, "ns");
        s.put(UnitsTime.PETAS, "Ps");
        s.put(UnitsTime.PS, "ps");
        s.put(UnitsTime.S, "s");
        s.put(UnitsTime.TERAS, "Ts");
        s.put(UnitsTime.YOTTAS, "Ys");
        s.put(UnitsTime.YS, "ys");
        s.put(UnitsTime.ZETTAS, "Zs");
        s.put(UnitsTime.ZS, "zs");
        SYMBOLS = s;
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new TimeI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsTime makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsTime
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Time unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Time makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Time> units =
                ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Time(d, units);
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
   public static ome.units.quantity.Time convert(Time t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsTime.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsTime units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Time> units2 =
               ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Time(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public TimeI() {
        super();
    }

    public TimeI(double d, UnitsTime unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public TimeI(double d,
            Unit<ome.units.quantity.Time> unit) {
        this(d, ome.model.enums.UnitsTime.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given ome-xml enum
    */
   public TimeI(Time value, Unit<ome.units.quantity.Time> ul) {
       this(value,
            ome.model.enums.UnitsTime.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given ome.model enum
    */
   public TimeI(double d, ome.model.enums.UnitsTime ul) {
        this(d, UnitsTime.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public TimeI(Time value, String target) {
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
            setUnit(UnitsTime.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public TimeI(Time value, UnitsTime target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public TimeI(ome.units.quantity.Time value) {
        ome.model.enums.UnitsTime internal =
            ome.model.enums.UnitsTime.bySymbol(value.unit().getSymbol());
        UnitsTime ul = UnitsTime.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsTime getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsTime unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Time copy(Ice.Current ignore) {
        TimeI copy = new TimeI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Time) {
            ome.model.units.Time t = (ome.model.units.Time) model;
            this.value = t.getValue();
            this.unit = UnitsTime.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Time cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsTime ut = ome.model.enums.UnitsTime.valueOf(getUnit().toString());
        ome.model.units.Time t = new ome.model.units.Time(getValue(), ut);
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
        return "Time(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

