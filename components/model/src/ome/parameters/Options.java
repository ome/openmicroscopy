/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;


/**
 * Simple options container.
 * Replaces the old PojoOptions as of 4.0, in order to support the new
 * {@link Parameters} passing defined in ticket:67.

 * @since 4.0
 */
public class Options {
    
    public boolean acquisitionData;
    public boolean leaves;
    public boolean orphan;
    public boolean cacheable;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("O[");
        if (acquisitionData) {
            sb.append('A');
        }
        if (leaves) {
            sb.append('L');
        }
        if (orphan) {
            sb.append('O');
        }
        if (cacheable) {
            sb.append('C');
        }
        sb.append(']');
        return sb.toString();
    }
}
