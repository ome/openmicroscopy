/*
 * ome.util.Validation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * collector for Model-validation status.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class Validation {

    protected static Logger log = LoggerFactory.getLogger(Validation.class);

    boolean valid = true;

    List messages = new ArrayList();

    public Validation() {

    }

    public boolean isValid() {
        return valid;
    }

    public static Validation VALID() {
        return new Validation();
    }

    public void invalidate(String message) {
        valid = false;
        messages.add(message);
    }

}
