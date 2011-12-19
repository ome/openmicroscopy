#!/usr/bin/env python
"""
   HQL plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl, CLI
import exceptions
import cmd
import time
import cmd
import sys

HELP = """Executes an HQL statement with the given parameters.
If no query is given, then a shell is opened which
will run any entered query with the current parameters."""

class HqlControl(BaseControl):

    def _configure(self, parser):
        parser.set_defaults(func=self.__call__)
        parser.add_argument("query", nargs="?", help="Single query to run")
        parser.add_argument("-q", "--quiet", action="store_true", help="No user input")
        parser.add_argument("--limit", help="Maximum number of return values", type=int, default=25)
        parser.add_argument("--offset", help="Number of entries to skip", type=int, default=0)
        parser.add_argument("--admin", help="Run an admin query", default=False, action="store_true")

    def __call__(self, args):
        if args.query:
            self.hql(args)
        else:
            if args.quiet:
                self.ctx.die(67, "Can't ask for query with --quiet option")
            while True:
                args.query = self.ctx.input("Enter query:")
                if not args.query:
                    break
                if not self.hql(args, loop = True):
                    break

    def hql(self, args, loop = False):
        from omero_sys_ParametersI import ParametersI

        ice_map = dict()
        if args.admin:
            ice_map["omero.group"]="-1"

        c = self.ctx.conn(args)
        q = c.sf.getQueryService()
        p = ParametersI()
        p.page(args.offset, args.limit)
        rv = self.project(q, args.query, p, ice_map)
        has_details = self.display(rv)
        if args.quiet:
            return

        input = """
To see details for object, enter line number.
To move ahead one page, enter 'p'
To re-display list, enter 'r'.
To quit, enter 'q' or just enter.
"""
        if loop:
            input = input + """To run another query, press enter\n"""

        while True:
            id = self.ctx.input(input)
            id = id.lower()

            # Exit loop
            if not id:
                return True
            if id.startswith("q"):
                return False

            # Stay in loop
            if id.startswith("p"):
                p.page(p.getOffset().val + p.getLimit().val, p.getLimit())
                self.ctx.dbg("\nCurrent page: offset=%s, limit=%s\n" % (p.theFilter.offset.val, p.theFilter.limit.val))
                rv = self.project(q, args.query, p, ice_map)
                self.display(rv)
            elif id.startswith("r"):
                self.display(rv)
            else:
                try:
                    id = long(id)
                    obj = rv[id]
                    if id not in has_details:
                        self.ctx.out("No details available: %s" % id)
                        continue
                    else:
                        obj = obj[0].val # Unwrap the object_list from IQuery.projection
                except:
                    self.ctx.out("Invalid choice: %s" % id)
                    continue
                keys = sorted(obj.__dict__)
                keys.remove("_id")
                keys.remove("_details")
                self.ctx.out("id = %s" % obj.id.val)
                for key in keys:
                    value = self.unwrap(obj.__dict__[key])
                    if isinstance(value, (str, unicode)):
                        value = "'%s'" % value
                    if key.startswith("_"):
                        key = key[1:]
                    self.ctx.out("%s = %s" % (key, value))
            continue

    def display(self, rv, cols = None):
        import omero.all
        import omero.rtypes
        from omero.model import IObject
        from omero.model import Details
        from omero.util.text import TableBuilder

        has_details = []
        tb = TableBuilder("#")
        for idx, object_list in enumerate(rv):
            klass = "Null"
            id = ""
            values = {}
            # Handling for simple lookup
            if len(object_list) == 1 and isinstance(object_list[0], omero.rtypes.RObjectI):
                has_details.append(idx)
                o = object_list[0].val
                if o:
                    tb.cols(["Class", "Id"])
                    klass = o.__class__.__name__
                    id = o.id.val
                    for k, v in o.__dict__.items():
                        values[k] = self.unwrap(v)
                    values = self.filter(values)
                    tb.cols(values.keys())
                tb.row(idx, klass, id, **values)
            # Handling for true projections
            else:
                indices = range(1, len(object_list) + 1)
                if cols is not None:
                    tb.cols(cols)
                else:
                    tb.cols(["Col%s" % x for x in indices])
                values = tuple([self.unwrap(x) for x in object_list])
                tb.row(idx, *values)
        self.ctx.out(str(tb.build()))
        return has_details

    def unwrap(self, object, cache = None):

        if cache == None:
            cache = {}
        elif object in cache:
            return cache[id(object)]

        from omero.rtypes import unwrap
        import omero_model_Details_ice
        import omero_model_IObject_ice
        from omero.model import IObject
        from omero.model import Details
        from omero.rtypes import RObjectI
        from omero.rtypes import RTimeI
        #if isinstance(object, list):
        #    return [self.unwrap(x, cache) for x in object]
        #elif isinstance(object, RObject):
        #    return self.unwrap(object.val, cache)
        unwrapped = unwrap(object, cache)
        if isinstance(unwrapped, IObject):
            rv = "%s:%s" % (unwrapped.__class__.__name__, unwrapped.id.val)
        elif isinstance(object, RTimeI):
            rv = time.ctime(unwrapped/1000.0)
        elif isinstance(object, Details):
            owner = None
            group = None
            if unwrapped.owner is not None:
                owner = unwrapped.owner.id.val
            if unwrapped.group is not None:
                group = unwrapped.group.id.val
            rv = "owner=%s;group=%s" % (owner, group)
        else:
            rv = unwrapped

        cache[id(object)] = rv
        return rv;

    def filter(self, values):
        values = dict(values)
        for x in ("_id", "_loaded"):
            if x in values:
                values.pop(x)
        if "owner=None;group=None" == values.get("_details"):
            values.pop("_details")
        multi_valued = sorted([k for k in values if isinstance(values[k], list)])
        false_valued = sorted([k for k in values if not values[k]])
        for x in multi_valued + false_valued:
            if x in values:
                values.pop(x)

        rv = dict()
        for k, v in values.items():
            if k.startswith("_"):
                rv[k[1:]] = v
            else:
                rv[k] = v
        return rv

    def project(self, querySvc, queryStr, params, ice_map):
        import omero
        try:
            rv = querySvc.projection(queryStr, params, ice_map)
            self.ctx.set("last.hql.rv", rv)
            return rv
        except omero.SecurityViolation, sv:
            if "omero.group" in ice_map:
                self.ctx.die(53, "SecurityViolation: Current user is not an admin and cannot use '--admin'")
            else:
                self.ctx.die(54, "SecurityViolation: %s" % sv)
        except omero.QueryException, qe:
            self.ctx.set("last.hql.rv", [])
            self.ctx.die(52, "Bad query: %s" % qe.message)

try:
    register("hql", HqlControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("hql", HqlControl, HELP)
        cli.invoke(sys.argv[1:])
