/*
 *   $Id$
 *   
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jamonapi.MonitorFactory;

/**
 * Used as a simple print out of {@link MonitorFactory} results.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class Report {

    static int LABEL = 0;
    static int HITS = 1;
    static int AVG = 2;
    static int TOTAL = 3;
    static int STDDEV = 4;
    static int LASTVALUE = 5;
    static int MIN = 6;
    static int MAX = 7;
    static int ACTIVE = 8;
    static int AVGACTIVE = 9;
    static int MAXACTIVE = 10;
    static int FIRSTACCESS = 11;
    static int LASTACCESS = 12;

    String[] header;
    Object[][] data;

    /**
     * Saves the current data from {@link MonitorFactory} and then resets all
     * values.
     */
    public Report() {
        header = MonitorFactory.getHeader();
        data = MonitorFactory.getData();
        MonitorFactory.reset();
    }

    @Override
    public String toString() {
        if (header == null) {
            return "====== No report =======";
        }
        int[] labels = new int[] { LABEL, AVG, MIN, MAX, TOTAL, HITS };
        StringBuilder sb = new StringBuilder();
        for (int l : labels) {
            sb.append(header[l]);
            for (int i = 0; i < 8 - header[l].length(); i++) {
                sb.append(" ");
            }
            sb.append("\t");
        }
        sb.append("\n");

        Map<String, String> ordering = new HashMap<String, String>();
        for (int i = 0; i < data.length; i++) {
            StringBuilder line = new StringBuilder();
            for (int l : labels) {
                Object d = data[i][l];
                if (d instanceof Double) {
                    line.append(String.format("%3.2e\t", (Double) d));
                } else {
                    line.append(d + "\t");
                }
            }
            line.append("\n");
            ordering.put((String) data[i][LABEL], line.toString());
        }
        List<String> keys = new ArrayList<String>(ordering.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            sb.append(ordering.get(key));
        }

        return sb.toString();
    }

}
