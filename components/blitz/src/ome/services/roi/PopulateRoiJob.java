/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi;

import java.io.File;

import ome.services.scripts.ScriptFinder;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.Roles;

/**
 * Start-up task which guarantees that lib/python/populateroi.py is added as a
 * script to the server. Then, users like MetadataStoreI who would like to run
 * populateroi.py scripts, can use {@link #createJob(ServiceFactory)}

 * @since Beta4.1
 */
public class PopulateRoiJob extends ScriptFinder {

    private static File production() {
        File cwd = new File(".");
        File lib = new File(cwd, "lib");
        File scripts = new File(lib, "scripts");
        File omero = new File(scripts, "omero");
        File import_scripts = new File(omero, "import_scripts");
        File Populate_ROI = new File(import_scripts, "Populate_ROI.py");
        return Populate_ROI;
    }

    public PopulateRoiJob(Roles roles, String uuid, Executor executor) {
        super(roles, uuid, executor, production());
    }

    public PopulateRoiJob(Roles roles, String uuid, Executor executor, File source) {
        super(roles, new Principal(uuid, "system", "Internal"), executor, source);
    }

    public PopulateRoiJob(Roles roles, Principal principal, Executor executor, File source) {
        super(roles, principal, executor, source);
    }

    @Override
    public String getName() {
        return "Populate_ROI.py";
    }
}
