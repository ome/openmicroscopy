#!/usr/bin/env python
"""
   Plugin for our managing the OMERO database.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import Arguments, BaseControl, VERSION

class DatabaseControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("Load intial schema")

    def _lookup(self, data, key, map):
        map[key] = data.properties.getProperty("omero.db."+key)
        if not map[key] or map[key] == "":
            self.ctx.die(150, """ "omero.db.%s" is not defined""" % key)

    def _create(self, db_name, root_pass):
        print db_name
        print root_pass
        print self.ctx.dir

    def script(self, args):
        args = Arguments(args)

        data = self.ctx.initData({})
        map = {}
        self._lookup(data, "name", map)
        self._lookup(data, "host", map)
        self._lookup(data, "pass", map)
        self._lookup(data, "version", map)
        self._lookup(data, "patch", map)
        self._create(db_name, map["omero.db.pass"])

try:
    register("db", DatabaseControl)
except NameError:
    DatabaseControl()._main()
