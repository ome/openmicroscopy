/*
 * ome.formats.EnumerationException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats;

/**
 * @author "Brian W. Loranger"
 */
public class EnumerationException extends RuntimeException {

    /** The enumeration class that was used in a failed enumeration lookup. */
    private Class failureClass;

    /** The enumeration value that was used in a failed enumeration lookup. */
    private String value;

    public EnumerationException(String message, Class klass, String value) {
        super(message);
        this.failureClass = klass;
        this.value = value;
    }

    public Class getFailureClass() {
        return failureClass;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return getMessage() + "'" + value + "' in '" + failureClass + "'.";
    }
}
