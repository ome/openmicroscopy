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

import java.util.HashMap;
import java.util.Map;

public enum UnitsFrequency {

    YOTTAHZ("YHz"),
    ZETTAHZ("ZHz"),
    EXAHZ("EHz"),
    PETAHZ("PHz"),
    TERAHZ("THz"),
    GIGAHZ("GHz"),
    MEGAHZ("MHz"),
    KHZ("kHz"),
    HHZ("hHz"),
    DAHZ("daHz"),
    HZ("Hz"),
    DHZ("dHz"),
    CHZ("cHz"),
    MHZ("mHz"),
    MICROHZ("µHz"),
    NHZ("nHz"),
    PHZ("pHz"),
    FHZ("fHz"),
    AHZ("aHz"),
    ZHZ("zHz"),
    YHZ("yHz");

    private static final Map<String, UnitsFrequency> bySymbol
        = new HashMap<String, UnitsFrequency>();

    static {
        for (UnitsFrequency t : UnitsFrequency.values()) {
            bySymbol.put(t.value, t);
        }
    }

    protected String value;

    private UnitsFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UnitsFrequency bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

