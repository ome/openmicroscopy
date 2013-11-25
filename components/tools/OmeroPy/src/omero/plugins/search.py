#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Simple command-line searching. Similar to the hql plugin.
"""


import sys

from omero.cli import CLI
from omero.plugins.hql import HqlControl


HELP = """Search for object ids by string.

Examples:

  bin/omero search Image "my-text"
  bin/omero search Image "with wildcard*"
  bin/omero search Project "with wildcard*"

Examples (admin-only):

  bin/omero search --index Image:1
  bin/omero search --index Well:5

See also:

  https://lucene.apache.org/java/2_9_1/queryparsersyntax.html

"""


class SearchControl(HqlControl):

    def _configure(self, parser):
        parser.add_argument(
            "--index", action="store_true", default=False,
            help="Index an object as a administrator")
        parser.add_argument(
            "type",
            help="Object type to search for, e.g. 'Image' or 'Well'")
        parser.add_argument(
            "search_string", nargs="?",
            help="Lucene search string")
        parser.set_defaults(func=self.search)
        parser.add_login_arguments()

    def search(self, args):
        c = self.ctx.conn(args)

        import omero
        import omero.all

        if args.index:
            try:
                parts = args.type.split(":")
                kls = parts[0].strip()
                kls = getattr(omero.model, kls)
                kls = kls.ice_staticId()
                of = c.getCommunicator().findObjectFactory(kls)
                obj = of.create(kls)
                id = long(parts[1].strip())
                obj.setId(omero.rtypes.rlong(id))
            except Exception, e:
                self.ctx.dbg(e)
                self.ctx.die(432, "Bad object: %s" % args.type)

            c.sf.getUpdateService().indexObject(obj)

        else:
            search = c.sf.createSearchService()
            try:
                try:
                    search.onlyType(args.type)
                    search.byFullText(args.search_string)
                    if not search.hasNext():
                        self.ctx.die(433, "No results found.")
                    while search.hasNext():
                        results = search.results()
                        results = [[x] for x in results]
                        self.display(results)
                except omero.ApiUsageException, aue:
                    self.ctx.die(434, aue.message)

            finally:
                search.close()

try:
    register("search", SearchControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("search", SearchControl, HELP)
        cli.invoke(sys.argv[1:])
