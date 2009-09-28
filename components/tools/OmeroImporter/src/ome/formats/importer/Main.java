/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import ome.formats.importer.cli.CommandLineImporter;
import ome.formats.importer.gui.GuiImporter;

/**
 * Wrapper around the various importer applications.
 * 
 * @since Beta4.1
 */
public class Main {

    public static void main(String[] args) {
        if (args.length >= 1) {
            String first = args[0];
            String[] arg = other(args);
            if (first == "--cli") {
                CommandLineImporter.main(arg);
                return;
            } else if (first == "--gui") {
                GuiImporter.main(arg);
                return;
            }
        } else {
            try
            {
            GuiImporter.main(args);
            return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static String[] other(String[] args) {
        if (args != null && args.length > 1) {
            String[] rv = new String[args.length - 1];
            System.arraycopy(args, 1, rv, 0, rv.length);
            return rv;
        }
        return new String[0];
    }

}
