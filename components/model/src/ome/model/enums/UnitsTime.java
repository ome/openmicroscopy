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

public enum UnitsTime {

    YOTTASECOND("Ys"),
    ZETTASECOND("Zs"),
    EXASECOND("Es"),
    PETASECOND("Ps"),
    TERASECOND("Ts"),
    GIGASECOND("Gs"),
    MEGASECOND("Ms"),
    KILOSECOND("ks"),
    HECTOSECOND("hs"),
    DECASECOND("das"),
    SECOND("s"),
    DECISECOND("ds"),
    CENTISECOND("cs"),
    MILLISECOND("ms"),
    MICROSECOND("µs"),
    NANOSECOND("ns"),
    PICOSECOND("ps"),
    FEMTOSECOND("fs"),
    ATTOSECOND("as"),
    ZEPTOSECOND("zs"),
    YOCTOSECOND("ys"),
    MINUTE("min"),
    HOUR("h"),
    DAY("d");

    private static final Map<String, UnitsTime> bySymbol
        = new HashMap<String, UnitsTime>();

    static {
        for (UnitsTime t : UnitsTime.values()) {
            bySymbol.put(t.symbol, t);
        }
    }

    protected String symbol;

    private UnitsTime(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static UnitsTime bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

