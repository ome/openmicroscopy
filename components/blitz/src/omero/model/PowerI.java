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

import omero.model.enums.UnitsPower;

/**
 * Blitz wrapper around the {@link ome.model.units.Power} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PowerI extends Power implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Function<Double, Double>> conversions;
    static {
        Map<String, Function<Double, Double>> c = new HashMap<String, Function<Double, Double>>();

        c.put("AW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("AW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("AW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("AW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("AW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("AW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("AW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("AW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("AW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("AW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("AW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("AW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("AW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("AW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("AW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("AW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("AW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("AW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("AW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("AW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("CW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("CW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("CW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("CW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("CW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("CW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("CW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("CW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("CW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("CW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("CW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("CW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("CW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("CW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("CW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("CW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("CW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("CW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("CW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("CW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("DAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("DAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("DAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("DAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("DAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("DAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("DAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("DAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("DAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("DAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("DAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("DAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("DAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("DAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("DW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("DW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("DW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("DW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("DW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("DW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("DW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("DW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("DW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("DW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("DW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("DW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("DW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("DW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("DW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("DW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("EXAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("EXAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("EXAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("EXAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("EXAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("EXAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("EXAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("EXAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("EXAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("EXAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("EXAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("EXAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("EXAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("EXAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("EXAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("EXAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("EXAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("EXAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("FW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("FW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("FW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("FW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("FW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("FW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("FW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("FW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("FW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("FW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("FW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("FW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("FW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("FW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("FW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("FW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("FW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("FW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("FW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("FW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("GIGAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("GIGAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("GIGAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("GIGAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("GIGAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("GIGAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("GIGAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("GIGAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("GIGAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("GIGAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("GIGAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("GIGAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("GIGAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("GIGAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("GIGAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("GIGAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("GIGAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("GIGAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("HW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("HW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("HW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("HW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("HW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("HW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("HW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("HW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("HW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("HW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("HW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("HW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("HW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("HW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("HW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("HW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("KW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("KW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("KW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("KW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("KW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("KW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("KW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("KW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("KW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("KW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("KW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("KW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("KW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("KW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("KW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("KW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("KW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("KW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("KW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("MEGAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("MEGAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("MEGAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MEGAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MEGAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("MEGAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MEGAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MEGAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MEGAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MEGAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MEGAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MEGAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MEGAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MEGAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MEGAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("MEGAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MEGAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("MICROW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MICROW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MICROW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("MICROW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MICROW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MICROW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MICROW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MICROW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("MICROW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MICROW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MICROW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MICROW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MICROW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MICROW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MICROW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MICROW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MICROW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("MICROW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MICROW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("MW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("MW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("NW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("NW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("NW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("NW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("NW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("NW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("NW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("NW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("NW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("NW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("NW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("NW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("NW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("NW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("NW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("NW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("NW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("NW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("NW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("PETAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("PETAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("PETAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("PETAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PETAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("PETAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PETAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("PETAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PETAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PETAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PETAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("PETAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PETAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PETAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PETAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("PETAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PETAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("PW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("PW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("PW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("PW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("PW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("PW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("PW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("PW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("PW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("TERAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("TERAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("TERAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("TERAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("TERAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("TERAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("TERAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("TERAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("TERAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("TERAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("TERAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("TERAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("TERAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("TERAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("TERAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("TERAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("TERAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("W:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("W:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("W:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("W:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("W:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("W:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("W:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("W:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("W:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("W:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("W:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("W:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("W:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("W:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("W:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("W:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("W:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("W:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("W:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("W:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("YOTTAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("YOTTAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("YOTTAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("YOTTAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("YOTTAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("YOTTAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("YOTTAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("YOTTAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("YOTTAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("YOTTAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("YOTTAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("YOTTAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("YOTTAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("YOTTAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("YOTTAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 48) * value;
              }});

        c.put("YOTTAW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("YOTTAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("YW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("YW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("YW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("YW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("YW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("YW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("YW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("YW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("YW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("YW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("YW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("YW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("YW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("YW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("YW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("YW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("YW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("YW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -48) * value;
              }});

        c.put("YW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("YW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("ZETTAW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("ZETTAW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("ZETTAW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("ZETTAW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZETTAW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("ZETTAW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("ZETTAW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("ZETTAW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("ZETTAW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("ZETTAW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("ZETTAW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("ZETTAW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("ZETTAW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("ZETTAW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("ZETTAW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("ZETTAW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("ZETTAW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAW:ZW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("ZW:AW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZW:CW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("ZW:DAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("ZW:DW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("ZW:EXAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("ZW:FW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("ZW:GIGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("ZW:HW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("ZW:KW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("ZW:MEGAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("ZW:MICROW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("ZW:MW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("ZW:NW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("ZW:PETAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("ZW:PW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("ZW:TERAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("ZW:W", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("ZW:YOTTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("ZW:YW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZW:ZETTAW", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});
        conversions = Collections.unmodifiableMap(c);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PowerI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsPower makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsPower
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Power unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Power makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Power> units =
                ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Power(d, units);
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
   public static ome.units.quantity.Power convert(Power t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsPower.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsPower units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Power> units2 =
               ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Power(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public PowerI() {
        super();
    }

    public PowerI(double d, UnitsPower unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public PowerI(double d,
            Unit<ome.units.quantity.Power> unit) {
        this(d, ome.model.enums.UnitsPower.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given ome-xml enum
    */
   public PowerI(Power value, Unit<ome.units.quantity.Power> ul) {
       this(value,
            ome.model.enums.UnitsPower.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given ome.model enum
    */
   public PowerI(double d, ome.model.enums.UnitsPower ul) {
        this(d, UnitsPower.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Power}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public PowerI(Power value, String target) {
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
            setUnit(UnitsPower.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public PowerI(Power value, UnitsPower target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public PowerI(ome.units.quantity.Power value) {
        ome.model.enums.UnitsPower internal =
            ome.model.enums.UnitsPower.bySymbol(value.unit().getSymbol());
        UnitsPower ul = UnitsPower.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsPower getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPower unit, Ice.Current current) {
        this.unit = unit;
    }

    public Power copy(Ice.Current ignore) {
        PowerI copy = new PowerI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Power) {
            ome.model.units.Power t = (ome.model.units.Power) model;
            this.value = t.getValue();
            this.unit = UnitsPower.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Power cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsPower ut = ome.model.enums.UnitsPower.valueOf(getUnit().toString());
        ome.model.units.Power t = new ome.model.units.Power(getValue(), ut);
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
        return "Power(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Power other = (Power) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

