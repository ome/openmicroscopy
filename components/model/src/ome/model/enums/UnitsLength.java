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


package ome.model.enums;

import ome.model.units.UnitEnum;

import java.util.HashMap;
import java.util.Map;

public enum UnitsLength implements UnitEnum {

    YOTTAMETER("Ym"),
    ZETTAMETER("Zm"),
    EXAMETER("Em"),
    PETAMETER("Pm"),
    TERAMETER("Tm"),
    GIGAMETER("Gm"),
    MEGAMETER("Mm"),
    KILOMETER("km"),
    HECTOMETER("hm"),
    DECAMETER("dam"),
    METER("m"),
    DECIMETER("dm"),
    CENTIMETER("cm"),
    MILLIMETER("mm"),
    MICROMETER("µm"),
    NANOMETER("nm"),
    PICOMETER("pm"),
    FEMTOMETER("fm"),
    ATTOMETER("am"),
    ZEPTOMETER("zm"),
    YOCTOMETER("ym"),
    ANGSTROM("Å"),
    ASTRONOMICALUNIT("ua"),
    LIGHTYEAR("ly"),
    PARSEC("pc"),
    THOU("thou"),
    LINE("li"),
    INCH("in"),
    FOOT("ft"),
    YARD("yd"),
    MILE("mi"),
    POINT("pt"),
    PIXEL("pixel"),
    REFERENCEFRAME("reference frame");

    private static final Map<String, UnitsLength> bySymbol
        = new HashMap<String, UnitsLength>();

    static {
        for (UnitsLength t : UnitsLength.values()) {
            bySymbol.put(t.symbol, t);
        }
    }

    protected String symbol;

    private UnitsLength(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static UnitsLength bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

