#!/usr/bin/env python
# -*- coding: utf-8 -*-

SCRIPT = """if True:
    import omero
    import omero.scripts as s
    s.client("name")"""

import omero, omero.scripts
import time
import omero_ext.uuid as uuid # see ticket:3774

launched = time.time()
client = omero.client()
try:
    sf = client.createSession()
    scriptService = sf.getScriptService()
    id = scriptService.uploadOfficialScript(\
        "/examples/%s.py" % uuid.uuid4(), SCRIPT)
    proc = scriptService.runScript(id, None, None)
    cb = omero.scripts.ProcessCallbackI(client, proc)
    launched = time.time()
    while cb.block(500) is None:
        if 10.0 < (time.time() - launched):
            raise StopIteration("Too long!")
finally:
    print "Finished in (s): %s" % (time.time() - launched)
    client.closeSession();
