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

import omero.model.enums.UnitsLength;

/**
 * Blitz wrapper around the {@link ome.model.units.Length} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class LengthI extends Length implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Function<Double, Double>> conversions;
    static {
        Map<String, Function<Double, Double>> c = new HashMap<String, Function<Double, Double>>();

        c.put("AM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:ANGSTROM"));
              }});

        c.put("AM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("AM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("AM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("AM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("AM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("AM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:FT"));
              }});

        c.put("AM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("AM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("AM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:IN"));
              }});

        c.put("AM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("AM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:LI"));
              }});

        c.put("AM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:LY"));
              }});

        c.put("AM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("AM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("AM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:MI"));
              }});

        c.put("AM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("AM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("AM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("AM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:PC"));
              }});

        c.put("AM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("AM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:PIXEL"));
              }});

        c.put("AM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("AM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:PT"));
              }});

        c.put("AM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:REFERENCEFRAME"));
              }});

        c.put("AM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("AM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:THOU"));
              }});

        c.put("AM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:UA"));
              }});

        c.put("AM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "AM:YD"));
              }});

        c.put("AM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("AM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("AM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("AM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ANGSTROM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:AM"));
              }});

        c.put("ANGSTROM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:CM"));
              }});

        c.put("ANGSTROM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:DAM"));
              }});

        c.put("ANGSTROM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:DM"));
              }});

        c.put("ANGSTROM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:EXAM"));
              }});

        c.put("ANGSTROM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:FM"));
              }});

        c.put("ANGSTROM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:FT"));
              }});

        c.put("ANGSTROM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:GIGAM"));
              }});

        c.put("ANGSTROM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:HM"));
              }});

        c.put("ANGSTROM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:IN"));
              }});

        c.put("ANGSTROM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:KM"));
              }});

        c.put("ANGSTROM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:LI"));
              }});

        c.put("ANGSTROM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:LY"));
              }});

        c.put("ANGSTROM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:M"));
              }});

        c.put("ANGSTROM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:MEGAM"));
              }});

        c.put("ANGSTROM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:MI"));
              }});

        c.put("ANGSTROM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:MICROM"));
              }});

        c.put("ANGSTROM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:MM"));
              }});

        c.put("ANGSTROM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:NM"));
              }});

        c.put("ANGSTROM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:PC"));
              }});

        c.put("ANGSTROM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:PETAM"));
              }});

        c.put("ANGSTROM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:PIXEL"));
              }});

        c.put("ANGSTROM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:PM"));
              }});

        c.put("ANGSTROM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:PT"));
              }});

        c.put("ANGSTROM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:REFERENCEFRAME"));
              }});

        c.put("ANGSTROM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:TERAM"));
              }});

        c.put("ANGSTROM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:THOU"));
              }});

        c.put("ANGSTROM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:UA"));
              }});

        c.put("ANGSTROM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:YD"));
              }});

        c.put("ANGSTROM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:YM"));
              }});

        c.put("ANGSTROM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:YOTTAM"));
              }});

        c.put("ANGSTROM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:ZETTAM"));
              }});

        c.put("ANGSTROM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ANGSTROM:ZM"));
              }});

        c.put("CM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("CM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:ANGSTROM"));
              }});

        c.put("CM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("CM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("CM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("CM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("CM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:FT"));
              }});

        c.put("CM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("CM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("CM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:IN"));
              }});

        c.put("CM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("CM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:LI"));
              }});

        c.put("CM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:LY"));
              }});

        c.put("CM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("CM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("CM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:MI"));
              }});

        c.put("CM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("CM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("CM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("CM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:PC"));
              }});

        c.put("CM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("CM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:PIXEL"));
              }});

        c.put("CM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("CM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:PT"));
              }});

        c.put("CM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:REFERENCEFRAME"));
              }});

        c.put("CM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("CM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:THOU"));
              }});

        c.put("CM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:UA"));
              }});

        c.put("CM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "CM:YD"));
              }});

        c.put("CM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("CM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("CM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("CM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:ANGSTROM"));
              }});

        c.put("DAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("DAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("DAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("DAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:FT"));
              }});

        c.put("DAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("DAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:IN"));
              }});

        c.put("DAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:LI"));
              }});

        c.put("DAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:LY"));
              }});

        c.put("DAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("DAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:MI"));
              }});

        c.put("DAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("DAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("DAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("DAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:PC"));
              }});

        c.put("DAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("DAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:PIXEL"));
              }});

        c.put("DAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("DAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:PT"));
              }});

        c.put("DAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:REFERENCEFRAME"));
              }});

        c.put("DAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("DAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:THOU"));
              }});

        c.put("DAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:UA"));
              }});

        c.put("DAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DAM:YD"));
              }});

        c.put("DAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("DAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("DAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("DAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("DM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("DM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:ANGSTROM"));
              }});

        c.put("DM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("DM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("DM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:FT"));
              }});

        c.put("DM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("DM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("DM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:IN"));
              }});

        c.put("DM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("DM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:LI"));
              }});

        c.put("DM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:LY"));
              }});

        c.put("DM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("DM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:MI"));
              }});

        c.put("DM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("DM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("DM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:PC"));
              }});

        c.put("DM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("DM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:PIXEL"));
              }});

        c.put("DM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("DM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:PT"));
              }});

        c.put("DM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:REFERENCEFRAME"));
              }});

        c.put("DM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("DM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:THOU"));
              }});

        c.put("DM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:UA"));
              }});

        c.put("DM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "DM:YD"));
              }});

        c.put("DM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("DM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("DM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("DM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("EXAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:ANGSTROM"));
              }});

        c.put("EXAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("EXAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("EXAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("EXAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:FT"));
              }});

        c.put("EXAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("EXAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("EXAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:IN"));
              }});

        c.put("EXAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("EXAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:LI"));
              }});

        c.put("EXAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:LY"));
              }});

        c.put("EXAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("EXAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("EXAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:MI"));
              }});

        c.put("EXAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("EXAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("EXAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("EXAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:PC"));
              }});

        c.put("EXAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("EXAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:PIXEL"));
              }});

        c.put("EXAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("EXAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:PT"));
              }});

        c.put("EXAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:REFERENCEFRAME"));
              }});

        c.put("EXAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("EXAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:THOU"));
              }});

        c.put("EXAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:UA"));
              }});

        c.put("EXAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "EXAM:YD"));
              }});

        c.put("EXAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("EXAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("EXAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("EXAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("FM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("FM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:ANGSTROM"));
              }});

        c.put("FM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("FM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("FM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("FM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("FM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:FT"));
              }});

        c.put("FM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("FM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("FM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:IN"));
              }});

        c.put("FM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("FM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:LI"));
              }});

        c.put("FM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:LY"));
              }});

        c.put("FM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("FM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("FM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:MI"));
              }});

        c.put("FM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("FM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("FM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("FM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:PC"));
              }});

        c.put("FM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("FM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:PIXEL"));
              }});

        c.put("FM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("FM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:PT"));
              }});

        c.put("FM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:REFERENCEFRAME"));
              }});

        c.put("FM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("FM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:THOU"));
              }});

        c.put("FM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:UA"));
              }});

        c.put("FM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FM:YD"));
              }});

        c.put("FM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("FM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("FM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("FM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("FT:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:AM"));
              }});

        c.put("FT:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:ANGSTROM"));
              }});

        c.put("FT:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:CM"));
              }});

        c.put("FT:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:DAM"));
              }});

        c.put("FT:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:DM"));
              }});

        c.put("FT:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:EXAM"));
              }});

        c.put("FT:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:FM"));
              }});

        c.put("FT:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:GIGAM"));
              }});

        c.put("FT:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:HM"));
              }});

        c.put("FT:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:IN"));
              }});

        c.put("FT:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:KM"));
              }});

        c.put("FT:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:LI"));
              }});

        c.put("FT:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:LY"));
              }});

        c.put("FT:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:M"));
              }});

        c.put("FT:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:MEGAM"));
              }});

        c.put("FT:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:MI"));
              }});

        c.put("FT:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:MICROM"));
              }});

        c.put("FT:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:MM"));
              }});

        c.put("FT:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:NM"));
              }});

        c.put("FT:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:PC"));
              }});

        c.put("FT:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:PETAM"));
              }});

        c.put("FT:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:PIXEL"));
              }});

        c.put("FT:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:PM"));
              }});

        c.put("FT:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:PT"));
              }});

        c.put("FT:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:REFERENCEFRAME"));
              }});

        c.put("FT:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:TERAM"));
              }});

        c.put("FT:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:THOU"));
              }});

        c.put("FT:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:UA"));
              }});

        c.put("FT:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:YD"));
              }});

        c.put("FT:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:YM"));
              }});

        c.put("FT:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:YOTTAM"));
              }});

        c.put("FT:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:ZETTAM"));
              }});

        c.put("FT:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "FT:ZM"));
              }});

        c.put("GIGAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("GIGAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:ANGSTROM"));
              }});

        c.put("GIGAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("GIGAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("GIGAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("GIGAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("GIGAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("GIGAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:FT"));
              }});

        c.put("GIGAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("GIGAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:IN"));
              }});

        c.put("GIGAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:LI"));
              }});

        c.put("GIGAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:LY"));
              }});

        c.put("GIGAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("GIGAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("GIGAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:MI"));
              }});

        c.put("GIGAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("GIGAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("GIGAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("GIGAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:PC"));
              }});

        c.put("GIGAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("GIGAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:PIXEL"));
              }});

        c.put("GIGAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("GIGAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:PT"));
              }});

        c.put("GIGAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:REFERENCEFRAME"));
              }});

        c.put("GIGAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("GIGAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:THOU"));
              }});

        c.put("GIGAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:UA"));
              }});

        c.put("GIGAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "GIGAM:YD"));
              }});

        c.put("GIGAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("GIGAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("GIGAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("GIGAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("HM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("HM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:ANGSTROM"));
              }});

        c.put("HM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("HM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("HM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("HM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:FT"));
              }});

        c.put("HM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("HM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:IN"));
              }});

        c.put("HM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:LI"));
              }});

        c.put("HM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:LY"));
              }});

        c.put("HM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("HM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:MI"));
              }});

        c.put("HM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("HM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("HM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("HM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:PC"));
              }});

        c.put("HM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("HM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:PIXEL"));
              }});

        c.put("HM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("HM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:PT"));
              }});

        c.put("HM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:REFERENCEFRAME"));
              }});

        c.put("HM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("HM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:THOU"));
              }});

        c.put("HM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:UA"));
              }});

        c.put("HM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "HM:YD"));
              }});

        c.put("HM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("HM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("HM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("HM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("IN:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:AM"));
              }});

        c.put("IN:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:ANGSTROM"));
              }});

        c.put("IN:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:CM"));
              }});

        c.put("IN:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:DAM"));
              }});

        c.put("IN:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:DM"));
              }});

        c.put("IN:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:EXAM"));
              }});

        c.put("IN:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:FM"));
              }});

        c.put("IN:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:FT"));
              }});

        c.put("IN:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:GIGAM"));
              }});

        c.put("IN:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:HM"));
              }});

        c.put("IN:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:KM"));
              }});

        c.put("IN:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:LI"));
              }});

        c.put("IN:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:LY"));
              }});

        c.put("IN:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:M"));
              }});

        c.put("IN:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:MEGAM"));
              }});

        c.put("IN:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:MI"));
              }});

        c.put("IN:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:MICROM"));
              }});

        c.put("IN:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:MM"));
              }});

        c.put("IN:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:NM"));
              }});

        c.put("IN:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:PC"));
              }});

        c.put("IN:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:PETAM"));
              }});

        c.put("IN:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:PIXEL"));
              }});

        c.put("IN:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:PM"));
              }});

        c.put("IN:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:PT"));
              }});

        c.put("IN:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:REFERENCEFRAME"));
              }});

        c.put("IN:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:TERAM"));
              }});

        c.put("IN:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:THOU"));
              }});

        c.put("IN:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:UA"));
              }});

        c.put("IN:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:YD"));
              }});

        c.put("IN:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:YM"));
              }});

        c.put("IN:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:YOTTAM"));
              }});

        c.put("IN:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:ZETTAM"));
              }});

        c.put("IN:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "IN:ZM"));
              }});

        c.put("KM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:ANGSTROM"));
              }});

        c.put("KM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("KM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("KM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("KM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("KM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("KM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:FT"));
              }});

        c.put("KM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("KM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("KM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:IN"));
              }});

        c.put("KM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:LI"));
              }});

        c.put("KM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:LY"));
              }});

        c.put("KM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("KM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("KM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:MI"));
              }});

        c.put("KM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("KM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("KM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("KM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:PC"));
              }});

        c.put("KM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("KM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:PIXEL"));
              }});

        c.put("KM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("KM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:PT"));
              }});

        c.put("KM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:REFERENCEFRAME"));
              }});

        c.put("KM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("KM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:THOU"));
              }});

        c.put("KM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:UA"));
              }});

        c.put("KM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "KM:YD"));
              }});

        c.put("KM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("KM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("KM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("KM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("LI:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:AM"));
              }});

        c.put("LI:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:ANGSTROM"));
              }});

        c.put("LI:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:CM"));
              }});

        c.put("LI:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:DAM"));
              }});

        c.put("LI:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:DM"));
              }});

        c.put("LI:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:EXAM"));
              }});

        c.put("LI:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:FM"));
              }});

        c.put("LI:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:FT"));
              }});

        c.put("LI:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:GIGAM"));
              }});

        c.put("LI:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:HM"));
              }});

        c.put("LI:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:IN"));
              }});

        c.put("LI:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:KM"));
              }});

        c.put("LI:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:LY"));
              }});

        c.put("LI:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:M"));
              }});

        c.put("LI:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:MEGAM"));
              }});

        c.put("LI:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:MI"));
              }});

        c.put("LI:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:MICROM"));
              }});

        c.put("LI:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:MM"));
              }});

        c.put("LI:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:NM"));
              }});

        c.put("LI:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:PC"));
              }});

        c.put("LI:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:PETAM"));
              }});

        c.put("LI:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:PIXEL"));
              }});

        c.put("LI:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:PM"));
              }});

        c.put("LI:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:PT"));
              }});

        c.put("LI:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:REFERENCEFRAME"));
              }});

        c.put("LI:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:TERAM"));
              }});

        c.put("LI:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:THOU"));
              }});

        c.put("LI:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:UA"));
              }});

        c.put("LI:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:YD"));
              }});

        c.put("LI:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:YM"));
              }});

        c.put("LI:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:YOTTAM"));
              }});

        c.put("LI:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:ZETTAM"));
              }});

        c.put("LI:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LI:ZM"));
              }});

        c.put("LY:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:AM"));
              }});

        c.put("LY:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:ANGSTROM"));
              }});

        c.put("LY:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:CM"));
              }});

        c.put("LY:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:DAM"));
              }});

        c.put("LY:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:DM"));
              }});

        c.put("LY:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:EXAM"));
              }});

        c.put("LY:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:FM"));
              }});

        c.put("LY:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:FT"));
              }});

        c.put("LY:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:GIGAM"));
              }});

        c.put("LY:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:HM"));
              }});

        c.put("LY:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:IN"));
              }});

        c.put("LY:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:KM"));
              }});

        c.put("LY:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:LI"));
              }});

        c.put("LY:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:M"));
              }});

        c.put("LY:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:MEGAM"));
              }});

        c.put("LY:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:MI"));
              }});

        c.put("LY:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:MICROM"));
              }});

        c.put("LY:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:MM"));
              }});

        c.put("LY:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:NM"));
              }});

        c.put("LY:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:PC"));
              }});

        c.put("LY:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:PETAM"));
              }});

        c.put("LY:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:PIXEL"));
              }});

        c.put("LY:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:PM"));
              }});

        c.put("LY:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:PT"));
              }});

        c.put("LY:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:REFERENCEFRAME"));
              }});

        c.put("LY:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:TERAM"));
              }});

        c.put("LY:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:THOU"));
              }});

        c.put("LY:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:UA"));
              }});

        c.put("LY:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:YD"));
              }});

        c.put("LY:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:YM"));
              }});

        c.put("LY:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:YOTTAM"));
              }});

        c.put("LY:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:ZETTAM"));
              }});

        c.put("LY:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "LY:ZM"));
              }});

        c.put("M:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("M:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:ANGSTROM"));
              }});

        c.put("M:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("M:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("M:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("M:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("M:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("M:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:FT"));
              }});

        c.put("M:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("M:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("M:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:IN"));
              }});

        c.put("M:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("M:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:LI"));
              }});

        c.put("M:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:LY"));
              }});

        c.put("M:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("M:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:MI"));
              }});

        c.put("M:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("M:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("M:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("M:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:PC"));
              }});

        c.put("M:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("M:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:PIXEL"));
              }});

        c.put("M:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("M:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:PT"));
              }});

        c.put("M:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:REFERENCEFRAME"));
              }});

        c.put("M:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("M:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:THOU"));
              }});

        c.put("M:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:UA"));
              }});

        c.put("M:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "M:YD"));
              }});

        c.put("M:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("M:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("M:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("M:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:ANGSTROM"));
              }});

        c.put("MEGAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("MEGAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("MEGAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("MEGAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MEGAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:FT"));
              }});

        c.put("MEGAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MEGAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("MEGAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:IN"));
              }});

        c.put("MEGAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MEGAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:LI"));
              }});

        c.put("MEGAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:LY"));
              }});

        c.put("MEGAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MEGAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:MI"));
              }});

        c.put("MEGAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MEGAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MEGAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MEGAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:PC"));
              }});

        c.put("MEGAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MEGAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:PIXEL"));
              }});

        c.put("MEGAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MEGAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:PT"));
              }});

        c.put("MEGAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:REFERENCEFRAME"));
              }});

        c.put("MEGAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MEGAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:THOU"));
              }});

        c.put("MEGAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:UA"));
              }});

        c.put("MEGAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MEGAM:YD"));
              }});

        c.put("MEGAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("MEGAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MEGAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MEGAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("MI:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:AM"));
              }});

        c.put("MI:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:ANGSTROM"));
              }});

        c.put("MI:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:CM"));
              }});

        c.put("MI:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:DAM"));
              }});

        c.put("MI:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:DM"));
              }});

        c.put("MI:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:EXAM"));
              }});

        c.put("MI:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:FM"));
              }});

        c.put("MI:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:FT"));
              }});

        c.put("MI:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:GIGAM"));
              }});

        c.put("MI:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:HM"));
              }});

        c.put("MI:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:IN"));
              }});

        c.put("MI:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:KM"));
              }});

        c.put("MI:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:LI"));
              }});

        c.put("MI:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:LY"));
              }});

        c.put("MI:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:M"));
              }});

        c.put("MI:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:MEGAM"));
              }});

        c.put("MI:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:MICROM"));
              }});

        c.put("MI:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:MM"));
              }});

        c.put("MI:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:NM"));
              }});

        c.put("MI:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:PC"));
              }});

        c.put("MI:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:PETAM"));
              }});

        c.put("MI:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:PIXEL"));
              }});

        c.put("MI:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:PM"));
              }});

        c.put("MI:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:PT"));
              }});

        c.put("MI:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:REFERENCEFRAME"));
              }});

        c.put("MI:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:TERAM"));
              }});

        c.put("MI:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:THOU"));
              }});

        c.put("MI:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:UA"));
              }});

        c.put("MI:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:YD"));
              }});

        c.put("MI:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:YM"));
              }});

        c.put("MI:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:YOTTAM"));
              }});

        c.put("MI:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:ZETTAM"));
              }});

        c.put("MI:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MI:ZM"));
              }});

        c.put("MICROM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MICROM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:ANGSTROM"));
              }});

        c.put("MICROM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MICROM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("MICROM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MICROM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MICROM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MICROM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:FT"));
              }});

        c.put("MICROM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MICROM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("MICROM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:IN"));
              }});

        c.put("MICROM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MICROM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:LI"));
              }});

        c.put("MICROM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:LY"));
              }});

        c.put("MICROM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MICROM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MICROM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:MI"));
              }});

        c.put("MICROM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MICROM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MICROM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:PC"));
              }});

        c.put("MICROM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MICROM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:PIXEL"));
              }});

        c.put("MICROM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MICROM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:PT"));
              }});

        c.put("MICROM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:REFERENCEFRAME"));
              }});

        c.put("MICROM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MICROM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:THOU"));
              }});

        c.put("MICROM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:UA"));
              }});

        c.put("MICROM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MICROM:YD"));
              }});

        c.put("MICROM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("MICROM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MICROM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:ANGSTROM"));
              }});

        c.put("MM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("MM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("MM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:FT"));
              }});

        c.put("MM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:IN"));
              }});

        c.put("MM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:LI"));
              }});

        c.put("MM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:LY"));
              }});

        c.put("MM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:MI"));
              }});

        c.put("MM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:PC"));
              }});

        c.put("MM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:PIXEL"));
              }});

        c.put("MM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:PT"));
              }});

        c.put("MM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:REFERENCEFRAME"));
              }});

        c.put("MM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:THOU"));
              }});

        c.put("MM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:UA"));
              }});

        c.put("MM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "MM:YD"));
              }});

        c.put("MM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("NM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("NM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:ANGSTROM"));
              }});

        c.put("NM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("NM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("NM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("NM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("NM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("NM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:FT"));
              }});

        c.put("NM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("NM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("NM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:IN"));
              }});

        c.put("NM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("NM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:LI"));
              }});

        c.put("NM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:LY"));
              }});

        c.put("NM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("NM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("NM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:MI"));
              }});

        c.put("NM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("NM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("NM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:PC"));
              }});

        c.put("NM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("NM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:PIXEL"));
              }});

        c.put("NM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("NM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:PT"));
              }});

        c.put("NM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:REFERENCEFRAME"));
              }});

        c.put("NM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("NM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:THOU"));
              }});

        c.put("NM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:UA"));
              }});

        c.put("NM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "NM:YD"));
              }});

        c.put("NM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("NM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("NM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PC:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:AM"));
              }});

        c.put("PC:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:ANGSTROM"));
              }});

        c.put("PC:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:CM"));
              }});

        c.put("PC:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:DAM"));
              }});

        c.put("PC:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:DM"));
              }});

        c.put("PC:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:EXAM"));
              }});

        c.put("PC:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:FM"));
              }});

        c.put("PC:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:FT"));
              }});

        c.put("PC:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:GIGAM"));
              }});

        c.put("PC:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:HM"));
              }});

        c.put("PC:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:IN"));
              }});

        c.put("PC:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:KM"));
              }});

        c.put("PC:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:LI"));
              }});

        c.put("PC:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:LY"));
              }});

        c.put("PC:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:M"));
              }});

        c.put("PC:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:MEGAM"));
              }});

        c.put("PC:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:MI"));
              }});

        c.put("PC:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:MICROM"));
              }});

        c.put("PC:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:MM"));
              }});

        c.put("PC:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:NM"));
              }});

        c.put("PC:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:PETAM"));
              }});

        c.put("PC:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:PIXEL"));
              }});

        c.put("PC:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:PM"));
              }});

        c.put("PC:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:PT"));
              }});

        c.put("PC:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:REFERENCEFRAME"));
              }});

        c.put("PC:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:TERAM"));
              }});

        c.put("PC:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:THOU"));
              }});

        c.put("PC:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:UA"));
              }});

        c.put("PC:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:YD"));
              }});

        c.put("PC:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:YM"));
              }});

        c.put("PC:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:YOTTAM"));
              }});

        c.put("PC:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:ZETTAM"));
              }});

        c.put("PC:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PC:ZM"));
              }});

        c.put("PETAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("PETAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:ANGSTROM"));
              }});

        c.put("PETAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("PETAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("PETAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("PETAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PETAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("PETAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:FT"));
              }});

        c.put("PETAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PETAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("PETAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:IN"));
              }});

        c.put("PETAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:LI"));
              }});

        c.put("PETAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:LY"));
              }});

        c.put("PETAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PETAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PETAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:MI"));
              }});

        c.put("PETAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PETAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PETAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:PC"));
              }});

        c.put("PETAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:PIXEL"));
              }});

        c.put("PETAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("PETAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:PT"));
              }});

        c.put("PETAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:REFERENCEFRAME"));
              }});

        c.put("PETAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PETAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:THOU"));
              }});

        c.put("PETAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:UA"));
              }});

        c.put("PETAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PETAM:YD"));
              }});

        c.put("PETAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("PETAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PETAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PETAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("PIXEL:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:AM"));
              }});

        c.put("PIXEL:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:ANGSTROM"));
              }});

        c.put("PIXEL:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:CM"));
              }});

        c.put("PIXEL:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:DAM"));
              }});

        c.put("PIXEL:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:DM"));
              }});

        c.put("PIXEL:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:EXAM"));
              }});

        c.put("PIXEL:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:FM"));
              }});

        c.put("PIXEL:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:FT"));
              }});

        c.put("PIXEL:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:GIGAM"));
              }});

        c.put("PIXEL:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:HM"));
              }});

        c.put("PIXEL:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:IN"));
              }});

        c.put("PIXEL:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:KM"));
              }});

        c.put("PIXEL:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:LI"));
              }});

        c.put("PIXEL:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:LY"));
              }});

        c.put("PIXEL:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:M"));
              }});

        c.put("PIXEL:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:MEGAM"));
              }});

        c.put("PIXEL:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:MI"));
              }});

        c.put("PIXEL:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:MICROM"));
              }});

        c.put("PIXEL:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:MM"));
              }});

        c.put("PIXEL:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:NM"));
              }});

        c.put("PIXEL:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:PC"));
              }});

        c.put("PIXEL:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:PETAM"));
              }});

        c.put("PIXEL:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:PM"));
              }});

        c.put("PIXEL:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:PT"));
              }});

        c.put("PIXEL:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:REFERENCEFRAME"));
              }});

        c.put("PIXEL:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:TERAM"));
              }});

        c.put("PIXEL:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:THOU"));
              }});

        c.put("PIXEL:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:UA"));
              }});

        c.put("PIXEL:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:YD"));
              }});

        c.put("PIXEL:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:YM"));
              }});

        c.put("PIXEL:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:YOTTAM"));
              }});

        c.put("PIXEL:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:ZETTAM"));
              }});

        c.put("PIXEL:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PIXEL:ZM"));
              }});

        c.put("PM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:ANGSTROM"));
              }});

        c.put("PM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("PM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("PM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("PM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("PM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:FT"));
              }});

        c.put("PM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("PM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:IN"));
              }});

        c.put("PM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:LI"));
              }});

        c.put("PM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:LY"));
              }});

        c.put("PM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:MI"));
              }});

        c.put("PM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:PC"));
              }});

        c.put("PM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("PM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:PIXEL"));
              }});

        c.put("PM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:PT"));
              }});

        c.put("PM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:REFERENCEFRAME"));
              }});

        c.put("PM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:THOU"));
              }});

        c.put("PM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:UA"));
              }});

        c.put("PM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PM:YD"));
              }});

        c.put("PM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("PM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("PM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PT:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:AM"));
              }});

        c.put("PT:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:ANGSTROM"));
              }});

        c.put("PT:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:CM"));
              }});

        c.put("PT:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:DAM"));
              }});

        c.put("PT:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:DM"));
              }});

        c.put("PT:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:EXAM"));
              }});

        c.put("PT:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:FM"));
              }});

        c.put("PT:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:FT"));
              }});

        c.put("PT:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:GIGAM"));
              }});

        c.put("PT:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:HM"));
              }});

        c.put("PT:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:IN"));
              }});

        c.put("PT:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:KM"));
              }});

        c.put("PT:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:LI"));
              }});

        c.put("PT:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:LY"));
              }});

        c.put("PT:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:M"));
              }});

        c.put("PT:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:MEGAM"));
              }});

        c.put("PT:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:MI"));
              }});

        c.put("PT:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:MICROM"));
              }});

        c.put("PT:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:MM"));
              }});

        c.put("PT:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:NM"));
              }});

        c.put("PT:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:PC"));
              }});

        c.put("PT:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:PETAM"));
              }});

        c.put("PT:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:PIXEL"));
              }});

        c.put("PT:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:PM"));
              }});

        c.put("PT:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:REFERENCEFRAME"));
              }});

        c.put("PT:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:TERAM"));
              }});

        c.put("PT:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:THOU"));
              }});

        c.put("PT:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:UA"));
              }});

        c.put("PT:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:YD"));
              }});

        c.put("PT:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:YM"));
              }});

        c.put("PT:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:YOTTAM"));
              }});

        c.put("PT:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:ZETTAM"));
              }});

        c.put("PT:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "PT:ZM"));
              }});

        c.put("REFERENCEFRAME:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:AM"));
              }});

        c.put("REFERENCEFRAME:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:ANGSTROM"));
              }});

        c.put("REFERENCEFRAME:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:CM"));
              }});

        c.put("REFERENCEFRAME:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:DAM"));
              }});

        c.put("REFERENCEFRAME:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:DM"));
              }});

        c.put("REFERENCEFRAME:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:EXAM"));
              }});

        c.put("REFERENCEFRAME:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:FM"));
              }});

        c.put("REFERENCEFRAME:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:FT"));
              }});

        c.put("REFERENCEFRAME:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:GIGAM"));
              }});

        c.put("REFERENCEFRAME:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:HM"));
              }});

        c.put("REFERENCEFRAME:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:IN"));
              }});

        c.put("REFERENCEFRAME:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:KM"));
              }});

        c.put("REFERENCEFRAME:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:LI"));
              }});

        c.put("REFERENCEFRAME:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:LY"));
              }});

        c.put("REFERENCEFRAME:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:M"));
              }});

        c.put("REFERENCEFRAME:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:MEGAM"));
              }});

        c.put("REFERENCEFRAME:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:MI"));
              }});

        c.put("REFERENCEFRAME:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:MICROM"));
              }});

        c.put("REFERENCEFRAME:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:MM"));
              }});

        c.put("REFERENCEFRAME:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:NM"));
              }});

        c.put("REFERENCEFRAME:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:PC"));
              }});

        c.put("REFERENCEFRAME:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:PETAM"));
              }});

        c.put("REFERENCEFRAME:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:PIXEL"));
              }});

        c.put("REFERENCEFRAME:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:PM"));
              }});

        c.put("REFERENCEFRAME:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:PT"));
              }});

        c.put("REFERENCEFRAME:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:TERAM"));
              }});

        c.put("REFERENCEFRAME:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:THOU"));
              }});

        c.put("REFERENCEFRAME:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:UA"));
              }});

        c.put("REFERENCEFRAME:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:YD"));
              }});

        c.put("REFERENCEFRAME:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:YM"));
              }});

        c.put("REFERENCEFRAME:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:YOTTAM"));
              }});

        c.put("REFERENCEFRAME:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:ZETTAM"));
              }});

        c.put("REFERENCEFRAME:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "REFERENCEFRAME:ZM"));
              }});

        c.put("TERAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("TERAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:ANGSTROM"));
              }});

        c.put("TERAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("TERAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("TERAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("TERAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("TERAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("TERAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:FT"));
              }});

        c.put("TERAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("TERAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("TERAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:IN"));
              }});

        c.put("TERAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:LI"));
              }});

        c.put("TERAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:LY"));
              }});

        c.put("TERAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("TERAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("TERAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:MI"));
              }});

        c.put("TERAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("TERAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("TERAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:PC"));
              }});

        c.put("TERAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("TERAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:PIXEL"));
              }});

        c.put("TERAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("TERAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:PT"));
              }});

        c.put("TERAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:REFERENCEFRAME"));
              }});

        c.put("TERAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:THOU"));
              }});

        c.put("TERAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:UA"));
              }});

        c.put("TERAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "TERAM:YD"));
              }});

        c.put("TERAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("TERAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("TERAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("TERAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("THOU:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:AM"));
              }});

        c.put("THOU:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:ANGSTROM"));
              }});

        c.put("THOU:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:CM"));
              }});

        c.put("THOU:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:DAM"));
              }});

        c.put("THOU:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:DM"));
              }});

        c.put("THOU:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:EXAM"));
              }});

        c.put("THOU:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:FM"));
              }});

        c.put("THOU:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:FT"));
              }});

        c.put("THOU:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:GIGAM"));
              }});

        c.put("THOU:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:HM"));
              }});

        c.put("THOU:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:IN"));
              }});

        c.put("THOU:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:KM"));
              }});

        c.put("THOU:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:LI"));
              }});

        c.put("THOU:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:LY"));
              }});

        c.put("THOU:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:M"));
              }});

        c.put("THOU:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:MEGAM"));
              }});

        c.put("THOU:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:MI"));
              }});

        c.put("THOU:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:MICROM"));
              }});

        c.put("THOU:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:MM"));
              }});

        c.put("THOU:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:NM"));
              }});

        c.put("THOU:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:PC"));
              }});

        c.put("THOU:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:PETAM"));
              }});

        c.put("THOU:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:PIXEL"));
              }});

        c.put("THOU:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:PM"));
              }});

        c.put("THOU:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:PT"));
              }});

        c.put("THOU:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:REFERENCEFRAME"));
              }});

        c.put("THOU:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:TERAM"));
              }});

        c.put("THOU:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:UA"));
              }});

        c.put("THOU:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:YD"));
              }});

        c.put("THOU:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:YM"));
              }});

        c.put("THOU:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:YOTTAM"));
              }});

        c.put("THOU:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:ZETTAM"));
              }});

        c.put("THOU:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "THOU:ZM"));
              }});

        c.put("UA:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:AM"));
              }});

        c.put("UA:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:ANGSTROM"));
              }});

        c.put("UA:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:CM"));
              }});

        c.put("UA:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:DAM"));
              }});

        c.put("UA:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:DM"));
              }});

        c.put("UA:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:EXAM"));
              }});

        c.put("UA:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:FM"));
              }});

        c.put("UA:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:FT"));
              }});

        c.put("UA:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:GIGAM"));
              }});

        c.put("UA:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:HM"));
              }});

        c.put("UA:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:IN"));
              }});

        c.put("UA:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:KM"));
              }});

        c.put("UA:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:LI"));
              }});

        c.put("UA:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:LY"));
              }});

        c.put("UA:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:M"));
              }});

        c.put("UA:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:MEGAM"));
              }});

        c.put("UA:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:MI"));
              }});

        c.put("UA:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:MICROM"));
              }});

        c.put("UA:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:MM"));
              }});

        c.put("UA:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:NM"));
              }});

        c.put("UA:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:PC"));
              }});

        c.put("UA:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:PETAM"));
              }});

        c.put("UA:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:PIXEL"));
              }});

        c.put("UA:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:PM"));
              }});

        c.put("UA:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:PT"));
              }});

        c.put("UA:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:REFERENCEFRAME"));
              }});

        c.put("UA:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:TERAM"));
              }});

        c.put("UA:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:THOU"));
              }});

        c.put("UA:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:YD"));
              }});

        c.put("UA:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:YM"));
              }});

        c.put("UA:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:YOTTAM"));
              }});

        c.put("UA:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:ZETTAM"));
              }});

        c.put("UA:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "UA:ZM"));
              }});

        c.put("YD:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:AM"));
              }});

        c.put("YD:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:ANGSTROM"));
              }});

        c.put("YD:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:CM"));
              }});

        c.put("YD:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:DAM"));
              }});

        c.put("YD:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:DM"));
              }});

        c.put("YD:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:EXAM"));
              }});

        c.put("YD:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:FM"));
              }});

        c.put("YD:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:FT"));
              }});

        c.put("YD:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:GIGAM"));
              }});

        c.put("YD:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:HM"));
              }});

        c.put("YD:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:IN"));
              }});

        c.put("YD:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:KM"));
              }});

        c.put("YD:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:LI"));
              }});

        c.put("YD:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:LY"));
              }});

        c.put("YD:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:M"));
              }});

        c.put("YD:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:MEGAM"));
              }});

        c.put("YD:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:MI"));
              }});

        c.put("YD:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:MICROM"));
              }});

        c.put("YD:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:MM"));
              }});

        c.put("YD:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:NM"));
              }});

        c.put("YD:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:PC"));
              }});

        c.put("YD:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:PETAM"));
              }});

        c.put("YD:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:PIXEL"));
              }});

        c.put("YD:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:PM"));
              }});

        c.put("YD:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:PT"));
              }});

        c.put("YD:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:REFERENCEFRAME"));
              }});

        c.put("YD:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:TERAM"));
              }});

        c.put("YD:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:THOU"));
              }});

        c.put("YD:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:UA"));
              }});

        c.put("YD:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:YM"));
              }});

        c.put("YD:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:YOTTAM"));
              }});

        c.put("YD:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:ZETTAM"));
              }});

        c.put("YD:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YD:ZM"));
              }});

        c.put("YM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("YM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:ANGSTROM"));
              }});

        c.put("YM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("YM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("YM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("YM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("YM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("YM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:FT"));
              }});

        c.put("YM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("YM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("YM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:IN"));
              }});

        c.put("YM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("YM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:LI"));
              }});

        c.put("YM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:LY"));
              }});

        c.put("YM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("YM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("YM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:MI"));
              }});

        c.put("YM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("YM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("YM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("YM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:PC"));
              }});

        c.put("YM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("YM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:PIXEL"));
              }});

        c.put("YM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("YM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:PT"));
              }});

        c.put("YM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:REFERENCEFRAME"));
              }});

        c.put("YM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("YM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:THOU"));
              }});

        c.put("YM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:UA"));
              }});

        c.put("YM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YM:YD"));
              }});

        c.put("YM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -48) * value;
              }});

        c.put("YM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("YM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("YOTTAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("YOTTAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:ANGSTROM"));
              }});

        c.put("YOTTAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("YOTTAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("YOTTAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("YOTTAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("YOTTAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("YOTTAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:FT"));
              }});

        c.put("YOTTAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("YOTTAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("YOTTAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:IN"));
              }});

        c.put("YOTTAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:LI"));
              }});

        c.put("YOTTAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:LY"));
              }});

        c.put("YOTTAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("YOTTAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("YOTTAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:MI"));
              }});

        c.put("YOTTAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("YOTTAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("YOTTAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:PC"));
              }});

        c.put("YOTTAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("YOTTAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:PIXEL"));
              }});

        c.put("YOTTAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("YOTTAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:PT"));
              }});

        c.put("YOTTAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:REFERENCEFRAME"));
              }});

        c.put("YOTTAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("YOTTAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:THOU"));
              }});

        c.put("YOTTAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:UA"));
              }});

        c.put("YOTTAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "YOTTAM:YD"));
              }});

        c.put("YOTTAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 48) * value;
              }});

        c.put("YOTTAM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("YOTTAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("ZETTAM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:ANGSTROM"));
              }});

        c.put("ZETTAM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("ZETTAM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("ZETTAM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("ZETTAM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZETTAM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("ZETTAM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:FT"));
              }});

        c.put("ZETTAM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("ZETTAM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("ZETTAM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:IN"));
              }});

        c.put("ZETTAM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("ZETTAM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:LI"));
              }});

        c.put("ZETTAM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:LY"));
              }});

        c.put("ZETTAM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("ZETTAM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("ZETTAM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:MI"));
              }});

        c.put("ZETTAM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("ZETTAM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("ZETTAM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("ZETTAM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:PC"));
              }});

        c.put("ZETTAM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("ZETTAM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:PIXEL"));
              }});

        c.put("ZETTAM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("ZETTAM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:PT"));
              }});

        c.put("ZETTAM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:REFERENCEFRAME"));
              }});

        c.put("ZETTAM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("ZETTAM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:THOU"));
              }});

        c.put("ZETTAM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:UA"));
              }});

        c.put("ZETTAM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZETTAM:YD"));
              }});

        c.put("ZETTAM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAM:ZM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("ZM:AM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZM:ANGSTROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:ANGSTROM"));
              }});

        c.put("ZM:CM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("ZM:DAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("ZM:DM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("ZM:EXAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("ZM:FM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("ZM:FT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:FT"));
              }});

        c.put("ZM:GIGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("ZM:HM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("ZM:IN", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:IN"));
              }});

        c.put("ZM:KM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("ZM:LI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:LI"));
              }});

        c.put("ZM:LY", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:LY"));
              }});

        c.put("ZM:M", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("ZM:MEGAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("ZM:MI", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:MI"));
              }});

        c.put("ZM:MICROM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("ZM:MM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("ZM:NM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("ZM:PC", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:PC"));
              }});

        c.put("ZM:PETAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("ZM:PIXEL", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:PIXEL"));
              }});

        c.put("ZM:PM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("ZM:PT", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:PT"));
              }});

        c.put("ZM:REFERENCEFRAME", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:REFERENCEFRAME"));
              }});

        c.put("ZM:TERAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("ZM:THOU", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:THOU"));
              }});

        c.put("ZM:UA", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:UA"));
              }});

        c.put("ZM:YD", new Function<Double, Double>() {
              public Double apply(Double value) {
                  throw new RuntimeException(String.format("Unsupported conversion: %s", "ZM:YD"));
              }});

        c.put("ZM:YM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZM:YOTTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("ZM:ZETTAM", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});
        conversions = Collections.unmodifiableMap(c);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new LengthI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsLength makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsLength
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Length unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Length makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Length> units =
                ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Length(d, units);
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
   public static ome.units.quantity.Length convert(Length t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsLength.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsLength units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Length> units2 =
               ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Length(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public LengthI() {
        super();
    }

    public LengthI(double d, UnitsLength unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public LengthI(double d,
            Unit<ome.units.quantity.Length> unit) {
        this(d, ome.model.enums.UnitsLength.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome-xml enum
    */
   public LengthI(Length value, Unit<ome.units.quantity.Length> ul) {
       this(value,
            ome.model.enums.UnitsLength.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given ome.model enum
    */
   public LengthI(double d, ome.model.enums.UnitsLength ul) {
        this(d, UnitsLength.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Length}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public LengthI(Length value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
        } else {
            Function<Double, Double> c = conversions.get(source + ":" + target);
            if (c == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            setValue(c.apply(value.getValue()));
       }
       setUnit(value.getUnit());
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public LengthI(Length value, UnitsLength target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public LengthI(ome.units.quantity.Length value) {
        ome.model.enums.UnitsLength internal =
            ome.model.enums.UnitsLength.bySymbol(value.unit().getSymbol());
        UnitsLength ul = UnitsLength.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsLength getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsLength unit, Ice.Current current) {
        this.unit = unit;
    }

    public Length copy(Ice.Current ignore) {
        LengthI copy = new LengthI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Length) {
            ome.model.units.Length t = (ome.model.units.Length) model;
            this.value = t.getValue();
            this.unit = UnitsLength.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Length cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsLength ut = ome.model.enums.UnitsLength.valueOf(getUnit().toString());
        ome.model.units.Length t = new ome.model.units.Length(getValue(), ut);
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
        return "Length(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Length other = (Length) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

