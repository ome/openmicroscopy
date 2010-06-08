#!/usr/bin/env python
"""
   HQL plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl, CLI
import cmd, sys, exceptions
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
        c = self.ctx.conn(args)
        q = c.sf.getQueryService()
        p = ParametersI()
        p.page(args.offset, args.limit)
        rv = q.findAllByQuery(args.query, p)
        self.display(rv)
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
                rv = p.page(p.getOffset().val + p.getLimit().val, p.getLimit())
                rv = q.findAllByQuery(args.query, p)
                self.display(rv)
            elif id.startswith("r"):
                self.display(rv)
            else:
                try:
                    id = long(id)
                    obj = rv[id]
                except:
                    self.ctx.out("Invalid choice: %s" % id)
                    continue
                keys = sorted(obj.__dict__)
                keys.remove("_id")
                keys.remove("_details")
                self.ctx.out("id=%s" % obj.id.val)
                for key in keys:
                    self.ctx.out("%s=%s" % (key, self.unwrap(obj.__dict__[key])))
            continue

    def display(self, rv):
        import omero_model_Details_ice
        import omero_model_IObject_ice
        from omero.model import IObject
        from omero.model import Details
        from omero.util.text import TableBuilder
        tb = TableBuilder("#", "Class", "Id")
        for i, o in enumerate(rv):
            klass = "Null"
            id = ""
            values = {}
            if o:
                klass = o.__class__.__name__
                id = o.id.val
                for k, v in o.__dict__.items():
                    values[k] = self.unwrap(v)
                values = self.filter(values)
                tb.cols(values.keys())
            tb.row(i, klass, id, **values)
        self.ctx.out(str(tb.build()))

    def unwrap(self, object):
        from omero.rtypes import unwrap
        import omero_model_Details_ice
        import omero_model_IObject_ice
        from omero.model import IObject
        from omero.model import Details
        if isinstance(object, IObject):
            return "%s:%s" % (object.__class__.__name__, object.id.val)
        elif isinstance(object, Details):
            owner = None
            group = None
            if object.owner is not None:
                owner = object.owner.id.val
            if object.group is not None:
                group = object.group.id.val
            return "owner=%s;group=%s" % (owner, group)
        else:
            return unwrap(object)

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

try:
    register("hql", HqlControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("hql", HqlControl, HELP)
        cli.invoke(sys.argv[1:])
