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
import time

from Ice import OperationNotExistException
from omero.cli import CLI
from omero.plugins.hql import HqlControl
from omero.rtypes import robject


HELP = """Search for object ids by string.

Examples:

  bin/omero search Image "my-text"
  bin/omero search Image "with wildcard*"
  bin/omero search Project "with wildcard*"

Examples (admin-only):

  bin/omero search --index Image:1
  bin/omero search --index Well:5

See also:

  http://lucene.apache.org/core/4_9_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html

"""


class SearchControl(HqlControl):

    def _configure(self, parser):
        parser.add_argument(
            "--index", action="store_true", default=False,
            help="Index an object as a administrator")
        parser.add_argument(
            "--no-parse",
            action="store_true",
            help="Pass the search string directly to Lucene with no parsing")
        parser.add_argument(
            "--field", nargs="*",
            default=(),
            help=("Fields which should be searched "
                  "(e.g. name, description, annotation)"))
        parser.add_argument(
            "--from",
            dest="_from",
            metavar="YYYY-MM-DD",
            type=self.date,
            help="Start date for limiting searches (YYYY-MM-DD)")
        parser.add_argument(
            "--to",
            dest="_to",
            metavar="YYYY-MM-DD",
            type=self.date,
            help="End date for limiting searches (YYYY-MM-DD")
        parser.add_argument(
            "--date-type",
            default="import",
            choices=("acquisitionDate", "import"),
            help=("Which field to use for --from/--to "
                  "(default: acquisitionDate)"))
        parser.add_argument(
            "type",
            help="Object type to search for, e.g. 'Image' or 'Well'")
        HqlControl._configure(self, parser)
        parser.set_defaults(func=self.search)

    def date(self, user_string):
        try:
            t = time.strptime(user_string, "%Y-%m-%d")
            return time.strftime("%Y%m%d", t)
        except Exception, e:
            self.ctx.dbg(str(e))
            raise

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
            group = None
            if args.admin:
                group = "-1"
            ctx = c.getContext(group)
            search = c.sf.createSearchService()
            try:
                try:
                    # Matching OMEROGateway.search()
                    search.setAllowLeadingWildcard(True)
                    search.setCaseSentivice(False)
                    search.onlyType(args.type)

                    if args.no_parse:
                        if args._from or args._to or args.field:
                            self.ctx.err("Ignoring from/to/fields")
                        search.byFullText(args.query)
                    else:
                        try:
                            if args.date_type == "import":
                                args.date_type = "details.creationEvent.time"
                            search.byLuceneQueryBuilder(
                                ",".join(args.field),
                                args._from, args._to, args.date_type,
                                args.query, ctx)
                        except OperationNotExistException:
                            self.ctx.err(
                                "Server does not support byLuceneQueryBuilder")
                            search.byFullText(args.query)

                    if not search.hasNext(ctx):
                        self.ctx.die(433, "No results found.")

                    self.ctx.set("search.results", [])
                    while search.hasNext(ctx):
                        results = search.results(ctx)
                        self.ctx.get("search.results").extend(results)
                        results = [[x] for x in results]
                        if not args.ids_only:
                            results = [[robject(x[0])] for x in results]
                        self.display(results,
                                     style=args.style,
                                     idsonly=args.ids_only)
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
