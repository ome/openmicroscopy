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

import omero.model.enums.UnitsFrequency;

/**
 * Blitz wrapper around the {@link ome.model.units.Frequency} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class FrequencyI extends Frequency implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Function<Double, Double>> conversions;
    static {
        Map<String, Function<Double, Double>> c = new HashMap<String, Function<Double, Double>>();

        c.put("AHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("AHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("AHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("AHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("AHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("AHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("AHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("AHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("AHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("AHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("AHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("AHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("AHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("AHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("AHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("AHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("AHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("AHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("AHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("AHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("CHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("CHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("CHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("CHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("CHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("CHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("CHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("CHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("CHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("CHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("CHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("CHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("CHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("CHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("CHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("CHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("CHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("CHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("CHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("CHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("DAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("DAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("DAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("DAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("DAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("DAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("DAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("DAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("DAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("DAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("DAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("DAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("DAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("DAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("DAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("DHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("DHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("DHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("DHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("DHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("DHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("DHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("DHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("DHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("DHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("DHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("DHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("DHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("DHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("DHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("DHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("DHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("DHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("DHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("DHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("EXAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("EXAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("EXAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("EXAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("EXAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("EXAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("EXAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("EXAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("EXAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("EXAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("EXAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("EXAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("EXAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("EXAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("EXAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("EXAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("EXAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("EXAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("EXAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("FHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("FHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("FHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("FHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("FHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("FHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("FHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -17) * value;
              }});

        c.put("FHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("FHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("FHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("FHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("FHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("FHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("FHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("FHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("FHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("FHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("FHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("FHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("FHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("GIGAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("GIGAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("GIGAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("GIGAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("GIGAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("GIGAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("GIGAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("GIGAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("GIGAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("GIGAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("GIGAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("GIGAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("GIGAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("GIGAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("GIGAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("GIGAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("GIGAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("GIGAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("GIGAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("HHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("HHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("HHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -16) * value;
              }});

        c.put("HHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("HHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("HHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("HHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("HHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("HHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("HHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("HHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("HHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("HHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("HHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("HHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("HHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("HZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("HZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("HZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("HZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("HZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("HZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("HZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("HZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("HZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("HZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("HZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("HZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("HZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("HZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("HZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("HZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("HZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("HZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("HZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("HZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("KHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("KHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 2) * value;
              }});

        c.put("KHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("KHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("KHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("KHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("KHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return 10 * value;
              }});

        c.put("KHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("KHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("KHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("KHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("KHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("KHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("KHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("KHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("KHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("KHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("KHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("KHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("MEGAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 8) * value;
              }});

        c.put("MEGAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 5) * value;
              }});

        c.put("MEGAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 7) * value;
              }});

        c.put("MEGAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MEGAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MEGAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MEGAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 4) * value;
              }});

        c.put("MEGAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MEGAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MEGAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MEGAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MEGAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MEGAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MEGAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MEGAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MEGAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("MEGAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MEGAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MEGAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("MHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("MHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -1) * value;
              }});

        c.put("MHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -2) * value;
              }});

        c.put("MHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("MHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("MICROHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -4) * value;
              }});

        c.put("MICROHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("MICROHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -5) * value;
              }});

        c.put("MICROHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("MICROHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("MICROHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("MICROHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("MICROHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("MICROHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("MICROHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("MICROHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("MICROHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("MICROHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("MICROHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("MICROHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("MICROHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("MICROHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("MICROHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("MICROHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("NHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -7) * value;
              }});

        c.put("NHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("NHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -8) * value;
              }});

        c.put("NHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("NHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("NHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("NHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("NHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("NHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("NHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("NHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("NHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("NHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("NHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("NHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("NHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("NHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("NHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("NHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("PETAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 17) * value;
              }});

        c.put("PETAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("PETAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 16) * value;
              }});

        c.put("PETAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PETAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("PETAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PETAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("PETAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("PETAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PETAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("PETAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("PETAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("PETAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("PETAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("PETAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PETAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("PETAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PETAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PETAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("PHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("PHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -10) * value;
              }});

        c.put("PHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -13) * value;
              }});

        c.put("PHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -11) * value;
              }});

        c.put("PHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("PHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("PHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("PHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -14) * value;
              }});

        c.put("PHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("PHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("PHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("PHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("PHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("PHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("PHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("PHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("PHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("PHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("PHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("PHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("TERAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 14) * value;
              }});

        c.put("TERAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 11) * value;
              }});

        c.put("TERAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 13) * value;
              }});

        c.put("TERAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("TERAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("TERAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("TERAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 10) * value;
              }});

        c.put("TERAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("TERAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("TERAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("TERAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("TERAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("TERAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("TERAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("TERAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("TERAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("TERAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("TERAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("TERAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("YHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("YHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -25) * value;
              }});

        c.put("YHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("YHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});

        c.put("YHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("YHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("YHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -26) * value;
              }});

        c.put("YHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("YHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("YHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("YHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("YHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("YHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("YHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("YHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("YHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("YHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -48) * value;
              }});

        c.put("YHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("YHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("YOTTAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("YOTTAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 26) * value;
              }});

        c.put("YOTTAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("YOTTAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 25) * value;
              }});

        c.put("YOTTAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("YOTTAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("YOTTAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("YOTTAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("YOTTAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("YOTTAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("YOTTAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("YOTTAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("YOTTAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("YOTTAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("YOTTAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("YOTTAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("YOTTAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("YOTTAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 48) * value;
              }});

        c.put("YOTTAHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("YOTTAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 39) * value;
              }});

        c.put("ZETTAHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 23) * value;
              }});

        c.put("ZETTAHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 20) * value;
              }});

        c.put("ZETTAHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 22) * value;
              }});

        c.put("ZETTAHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZETTAHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 36) * value;
              }});

        c.put("ZETTAHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 12) * value;
              }});

        c.put("ZETTAHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 19) * value;
              }});

        c.put("ZETTAHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 21) * value;
              }});

        c.put("ZETTAHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 18) * value;
              }});

        c.put("ZETTAHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 15) * value;
              }});

        c.put("ZETTAHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 24) * value;
              }});

        c.put("ZETTAHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 27) * value;
              }});

        c.put("ZETTAHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 30) * value;
              }});

        c.put("ZETTAHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 6) * value;
              }});

        c.put("ZETTAHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 33) * value;
              }});

        c.put("ZETTAHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 9) * value;
              }});

        c.put("ZETTAHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 45) * value;
              }});

        c.put("ZETTAHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZETTAHZ:ZHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 42) * value;
              }});

        c.put("ZHZ:AHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -3) * value;
              }});

        c.put("ZHZ:CHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -19) * value;
              }});

        c.put("ZHZ:DAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -22) * value;
              }});

        c.put("ZHZ:DHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -20) * value;
              }});

        c.put("ZHZ:EXAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -39) * value;
              }});

        c.put("ZHZ:FHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -6) * value;
              }});

        c.put("ZHZ:GIGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -30) * value;
              }});

        c.put("ZHZ:HHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -23) * value;
              }});

        c.put("ZHZ:HZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -21) * value;
              }});

        c.put("ZHZ:KHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -24) * value;
              }});

        c.put("ZHZ:MEGAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -27) * value;
              }});

        c.put("ZHZ:MHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -18) * value;
              }});

        c.put("ZHZ:MICROHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -15) * value;
              }});

        c.put("ZHZ:NHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -12) * value;
              }});

        c.put("ZHZ:PETAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -36) * value;
              }});

        c.put("ZHZ:PHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -9) * value;
              }});

        c.put("ZHZ:TERAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -33) * value;
              }});

        c.put("ZHZ:YHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, 3) * value;
              }});

        c.put("ZHZ:YOTTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -45) * value;
              }});

        c.put("ZHZ:ZETTAHZ", new Function<Double, Double>() {
              public Double apply(Double value) {
                  return Math.pow(10, -42) * value;
              }});
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsFrequency, String> SYMBOLS;
    static {
        Map<UnitsFrequency, String> s = new HashMap<UnitsFrequency, String>();
        s.put(UnitsFrequency.AHZ, "aHz");
        s.put(UnitsFrequency.CHZ, "cHz");
        s.put(UnitsFrequency.DAHZ, "daHz");
        s.put(UnitsFrequency.DHZ, "dHz");
        s.put(UnitsFrequency.EXAHZ, "EHz");
        s.put(UnitsFrequency.FHZ, "fHz");
        s.put(UnitsFrequency.GIGAHZ, "GHz");
        s.put(UnitsFrequency.HHZ, "hHz");
        s.put(UnitsFrequency.HZ, "Hz");
        s.put(UnitsFrequency.KHZ, "kHz");
        s.put(UnitsFrequency.MEGAHZ, "MHz");
        s.put(UnitsFrequency.MHZ, "mHz");
        s.put(UnitsFrequency.MICROHZ, "µHz");
        s.put(UnitsFrequency.NHZ, "nHz");
        s.put(UnitsFrequency.PETAHZ, "PHz");
        s.put(UnitsFrequency.PHZ, "pHz");
        s.put(UnitsFrequency.TERAHZ, "THz");
        s.put(UnitsFrequency.YHZ, "yHz");
        s.put(UnitsFrequency.YOTTAHZ, "YHz");
        s.put(UnitsFrequency.ZETTAHZ, "ZHz");
        s.put(UnitsFrequency.ZHZ, "zHz");
        SYMBOLS = s;
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new FrequencyI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsFrequency makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsFrequency
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Frequency unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Frequency makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Frequency> units =
                ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Frequency(d, units);
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
   public static ome.units.quantity.Frequency convert(Frequency t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsFrequency.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsFrequency units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Frequency> units2 =
               ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Frequency(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public FrequencyI() {
        super();
    }

    public FrequencyI(double d, UnitsFrequency unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public FrequencyI(double d,
            Unit<ome.units.quantity.Frequency> unit) {
        this(d, ome.model.enums.UnitsFrequency.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome-xml enum
    */
   public FrequencyI(Frequency value, Unit<ome.units.quantity.Frequency> ul) {
       this(value,
            ome.model.enums.UnitsFrequency.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome.model enum
    */
   public FrequencyI(double d, ome.model.enums.UnitsFrequency ul) {
        this(d, UnitsFrequency.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public FrequencyI(Frequency value, String target) {
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
            setUnit(UnitsFrequency.valueOf(target));
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public FrequencyI(Frequency value, UnitsFrequency target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public FrequencyI(ome.units.quantity.Frequency value) {
        ome.model.enums.UnitsFrequency internal =
            ome.model.enums.UnitsFrequency.bySymbol(value.unit().getSymbol());
        UnitsFrequency ul = UnitsFrequency.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsFrequency getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsFrequency unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Frequency copy(Ice.Current ignore) {
        FrequencyI copy = new FrequencyI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Frequency) {
            ome.model.units.Frequency t = (ome.model.units.Frequency) model;
            this.value = t.getValue();
            this.unit = UnitsFrequency.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Frequency cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsFrequency ut = ome.model.enums.UnitsFrequency.valueOf(getUnit().toString());
        ome.model.units.Frequency t = new ome.model.units.Frequency(getValue(), ut);
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
        return "Frequency(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Frequency other = (Frequency) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

