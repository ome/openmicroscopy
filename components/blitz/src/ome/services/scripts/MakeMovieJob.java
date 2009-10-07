/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts;

import java.io.File;

import ome.services.util.Executor;
import ome.system.Principal;

/**
 * Start-up task which guarantees that lib/python/makemovie.py is added as a
 * script to the server.

 * @since Beta4.1
 */
public class MakeMovieJob extends ScriptUploader {

    private static File production() {
        File cwd = new File(".");
        File lib = new File(cwd, "lib");
        File py = new File(lib, "python");
        File populate = new File(py, "makemovie.py");
        return populate;
    }

    public MakeMovieJob(String uuid, Executor executor) {
        super(uuid, executor, production());
    }

    public MakeMovieJob(String uuid, Executor executor, File source) {
        super(new Principal(uuid, "system", "Internal"), executor, source);
    }

    public MakeMovieJob(Principal principal, Executor executor, File source) {
        super(principal, executor, source);
    }

    @Override
    public String getName() {
        return "makemovie.py";
    }
}
