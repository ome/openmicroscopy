#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Tag plugin for command-line tag manipulation

   :author: Sam Hart <sam@glencoesoftware.com>

   Copyright (C) 2013-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt
"""

import platform
import subprocess
import sys
import json

import omero
from omero.cli import BaseControl, CLI, ExceptionHandler
from omero.rtypes import rlong, rstring, unwrap
from omero.model import TagAnnotationI, AnnotationAnnotationLinkI

HELP = """Manage OMERO user tags.

Plugin for managing and viewing OMERO user tags.

Examples:

    bin/omero tag list       # List all the tags, grouped by tagset
    bin/omero tag create     # Creates a tag

    # Create a tag set named 'data_10.28' and associate the tag number 18 wth
    # it.
    bin/omero tag createset --tag 18 --name data_10.28
"""


class Tag(object):
    def __init__(self, tag_id=None, name=None, description=None, owner=None,
                 children=None):
        self.tag_id = tag_id
        self.name = name
        self.description = description
        self.owner = owner
        self.children = children


class TagCollection(object):
    def __init__(self):
        self.tags = dict()
        self.owners = dict()
        self.mapping = dict()
        self.orphans = []


def exec_command(cmd):
    """
    given a command, will execute it in the parent environment
    Returns a list containing the output
    """
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    output = p.stdout.readlines()
    p.stdout.close()
    return output


def clip(s, width):
    """
    Given a string, s, and a width, will clip the string to that width or
    fill it with spaces up to that width.

    Returns modified string
    """
    mod_s = s
    if s and len(s) > width:
        mod_s = s[:width]
    elif s and len(s) < width:
        mod_s = s + " " * (width - len(s))
    return mod_s


class TagControl(BaseControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        parser.add_login_arguments()
        sub = parser.sub()

        listtags = parser.add(
            sub, self.list, help="List all the tags, grouped by tagset")
        self.add_standard_params(listtags)
        listtags.add_argument(
            "--tagset", nargs="+", type=long, help="One or more tagset IDs")
        self.add_tag_common_params(listtags)
        listtags.add_login_arguments()

        listsets = parser.add(sub, self.listsets, help="List tag sets")
        self.add_standard_params(listsets)
        listsets.add_argument(
            "--tag", nargs="+", type=long,
            help="List only tagsets containing the following tag ID(s)")
        self.add_tag_common_params(listsets)
        listsets.add_login_arguments()

        create = parser.add(sub, self.create, help="Create a new tag")
        self.add_newtag_params(create)
        create.add_login_arguments()

        createset = parser.add(
            sub, self.createset, help="Create a new tag set")
        createset.add_argument(
            "--tag", nargs="+", required=True, type=long,
            help="ID(s) of the tag(s) to include in this set")
        self.add_newtag_params(createset)
        createset.add_login_arguments()

        loadj = sub.add_parser(
            self.load.im_func.__name__,
            help="Import new tag(s) and tagset(s) from JSON file",
            description="Import new tag(s) and tagset(s) from JSON file",
            epilog="""
JSON File Format:
  The format of the JSON file should be as follows:

  [{
    "name" : "Name of the tagset",
    "desc" : "Description of the tagset",
    "set" : [{
        "name" : "Name of tag",
        "desc" : "Description of tag"
    },{
        "name" : "Name of tag",
        "desc" : "Description of tag"
    },{
        ....
  },{
    ....
  }]
            """)
        loadj.set_defaults(func=self.load)
        loadj.add_argument(
            "filename", nargs="?", help="The filename containing tag JSON")
        loadj.add_login_arguments()

        links = parser.add(
            sub, self.link, help="Link annotation to an object")
        links.add_argument(
            "object",
            help="The object to link to. Should be of form"
            " <object_type>:<object_id>")
        links.add_argument(
            'tag_id', type=long,
            help="The tag annotation ID")
        self.add_standard_params(links)
        links.add_login_arguments()

    # Recurring parameter methods
    def add_newtag_params(self, parser):
        parser.add_argument(
            "--name", help="The name of the new tag or tagset")
        parser.add_argument(
            "--desc", "--description",
            help="The description of the new tag or tagset")

    def add_tag_common_params(self, parser):
        parser.add_argument(
            "--uid",
            help="List only tags/tagsets belonging to the following user ID")
        parser.add_argument(
            "--desc", "--description", "--descriptions",
            action="store_true", default=False,
            help="Display descriptions of tags")

    def add_standard_params(self, parser):
        parser.add_argument(
            "--admin", action="store_true", default=False,
            help="Perform action as an administrator")
        parser.add_argument(
            "--nopage", action="store_true", default=False,
            help="Disable pagination")

    # Output methods
    def print_line(self, line, index):
        if self.console_length is None:
                self.ctx.out(line)
        elif index % self.console_length == 0 and index:
            input = raw_input("[Enter], [f]orward forever, or [q]uit: ")
            if input.lower() == 'q':
                sys.exit(0)
            elif input.lower() == 'f':
                self.console_length = None
        else:
            self.ctx.out(line)

    def pagetext(self, lines):
        for index, line in enumerate(lines):
            self.print_line(line, index)

    def pagetext_format(self, format, elements):
        for index, line in enumerate(elements):
            self.print_line(format.format(*line), index)

    def determine_console_size(self):
        """
        Will attempt to determine console size based upon the current
        platform.

        Returns tuple of width and length.
        """
        # The defaults if we can't figure it out
        lines = 25
        width = 80

        this_system = platform.system().lower()

        try:
            if this_system in ['linux', 'darwin', 'macosx', 'cygwin']:
                output = exec_command(['tput', 'lines'])
                if len(output) > 0:
                    lines = int(output[0].rstrip())
                output = exec_command(['tput', 'cols'])
                if len(output) > 0:
                    width = int(output[0].rstrip())
            elif this_system in ['windows', 'win32']:
                # http://stackoverflow.com/questions/566746/\
                # how-to-get-console-window-width-in-python
                from ctypes import windll, create_string_buffer
                from struct import unpack
                # stdin handle is -10
                # stdout handle is -11
                # stderr handle is -12
                h = windll.kernel32.GetStdHandle(-12)
                csbi = create_string_buffer(22)
                res = windll.kernel32.GetConsoleScreenBufferInfo(h, csbi)
                if res:
                    (bufx, bufy, curx, cury, wattr,
                     left, top, right, bottom,
                     maxx, maxy) = unpack("hhhhHhhhhhh", csbi.raw)
                    lines = bottom - top + 1
                    width = bottom - top + 1
        except:
            # Possible evil to ignore what the error was, but, truthfully,
            # the reason we do so is because it means it's a platform we
            # don't have support for, or a platform we should have support
            # for but which has some non-standard witch-craftery going on
            self.ctx.out("Could not determine the console length.")

        return width, lines

    # Data gathering methods
    def list_tags_recursive(self, args, tagset=None):
        """
        Returns a TagCollection object
        """
        params = omero.sys.ParametersI()
        params.addString('ns', omero.constants.metadata.NSINSIGHTTAGSET)
        ice_map = dict()
        if args.admin:
            ice_map["omero.group"] = "-1"

        client = self.ctx.conn(args)
        session = client.getSession()
        q = session.getQueryService()

        parent_tags = []

        sql = """
            select aal.parent.id, aal.child.id, aal.parent.description,
            aal.parent.textValue from AnnotationAnnotationLink aal
            inner join aal.parent ann
            where ann.ns=:ns
            """
        if args.uid:
            params.map["eid"] = rlong(long(args.uid))
            sql += " and ann.details.owner.id = :eid"
        if tagset:
            sql += " and ann.id = :tid"
            params.map['tid'] = rlong(long(tagset))

        tc = TagCollection()

        for element in q.projection(sql, params, ice_map):
            parent = unwrap(element[0])
            child = unwrap(element[1])
            tc.mapping.setdefault(parent, []).append(child)
            parent_tags.append(Tag(
                tag_id=parent,
                name=unwrap(element[3]),
                description=unwrap(element[2])
            ))

        if tagset:
            sql = """
                select distinct child.id, child.description, child.textValue,
                owner.id, owner.firstName, owner.lastName
                from AnnotationAnnotationLink as ann
                join ann.child as child
                join child.details.owner as owner
                join ann.parent as parent
                where parent.id = :tid or child.id = :tid
                """
        else:
            # Get orphans first
            sql = """
                select ann.id, ann.textValue, ann.description
                from TagAnnotation ann where ann.id not in
                (select distinct l.child.id from AnnotationAnnotationLink l
                    join l.parent as ts
                    where ts.ns = 'openmicroscopy.org/omero/insight/tagset')
                and ann.ns is null
                """
            if args.uid:
                sql += " and ann.details.owner.id = :eid"

            for element in q.projection(sql, params, ice_map):
                tag_id, text, description = map(unwrap, element)
                tc.orphans.append(Tag(
                    tag_id=tag_id,
                    name=text,
                    description=description))

            # Now set up search for rest of tags
            sql = """
                select ann.id, ann.description, ann.textValue,
                ann.details.owner.id,
                ann.details.owner.firstName, ann.details.owner.lastName
                from TagAnnotation ann
                """

        if args.uid:
            if tagset:
                sql += " and ann.details.owner.id = :eid"
            else:
                sql += " where ann.details.owner.id = :eid"

        for element in q.projection(sql, params, ice_map):
            tag_id, description, text, owner, first, last = map(unwrap,
                                                                element)
            tc.tags[tag_id] = Tag(
                tag_id=tag_id,
                name=text,
                description=description,
                owner=owner,
                children=tc.mapping.get(tag_id) or 0
                )
            tc.owners[owner] = "%s %s" % (first, last)

        for parent in parent_tags:
            if parent.tag_id not in tc.tags:
                tc.tags[parent.tag_id] = parent

        return tc

    def list_tagsets(self, args, tag):
        """
        Returns a TagCollection of just the tagsets.

        If tag is provided, will return the tagsets with those tags.
        """
        params = omero.sys.ParametersI()
        params.addString('ns', omero.constants.metadata.NSINSIGHTTAGSET)
        ice_map = dict()
        if args.admin:
            ice_map["omero.group"] = "-1"

        client = self.ctx.conn(args)
        session = client.getSession()
        q = session.getQueryService()

        tc = TagCollection()

        sql = """
            select a.id, a.description, a.textValue,
            a.details.owner.id, a.details.owner.firstName,
            a.details.owner.lastName
            from AnnotationAnnotationLink b
            inner join b.parent a
            where a.ns=:ns
            """
        if args.uid:
            params.map["eid"] = rlong(long(args.uid))
            sql += " and a.owner.id = :eid"
        if tag:
            sql += " and b.child.id = :tid"
            params.map['tid'] = rlong(long(tag))

        for element in q.projection(sql, params, ice_map):
            tag_id, description, text, owner, first, last = map(unwrap,
                                                                element)
            tc.tags[tag_id] = Tag(
                tag_id=tag_id,
                name=text,
                description=description,
                owner=owner
            )
            tc.owners[owner] = "%s %s" % (first, last)

        return tc

    def generate_tagset(self, tags, mapping, args):
        """
        Given a dict of tags and mappings for parent/child relationships
        return a list of lines representing the tagset output.
        """
        lines = []
        for key in mapping.keys():
            lines.append("+- %s:'%s'" % (str(key), tags[key].name))
            if args.desc:
                lines.append("|   '%s'" % tags[key].description)
                lines.append("|")
            lines.append('|\\')
            for tag_key in mapping[key]:
                lines.append("| +- %s:'%s'" % (str(tag_key),
                             tags[tag_key].name))
                if args.desc:
                    lines.append("|   '%s'" % tags[tag_key].description)
                    lines.append("|")
            lines.append('')

        return lines

    def generate_orphans(self, tags, orphans, args):
        """
        Given a dict of tags and a list of orphaned tags, return a list of
        lines representing the orphan output.
        """
        lines = []
        lines.append('Orphaned tags:')
        for orphan in orphans:
            lines.append("> %s:'%s'" % (str(orphan.tag_id), orphan.name))
            if args.desc:
                lines.append("   '%s'" % orphan.description)
                lines.append('')
        return lines

    def create_tag(self, name, description, text="tag"):
        """
        Creates a new tag object. Returns the new tag object.

        If name parameter is None, the user will be prompted to input it.

        The "text" parameter should be the text description to use upon user
        input. For example, if we were creating a tag, this would be "tag"
        (the default). If we were creating a tagset, this could be "tag set".
        """
        if name is None:
            name = raw_input("Please enter a name for this %s: " % text)

        if name is not None and name != '':
            tag = TagAnnotationI()
            tag.textValue = rstring(name)
            if description is not None and len(description) > 0:
                tag.description = rstring(description)

            return tag
        else:
            self.ctx.err("Tag/tagset name cannot be 'None' or empty.")
            sys.exit(1)

    # Actual command methods
    def create(self, args):
        """
        create a tag command.
        """
        tag = self.create_tag(args.name, args.desc)

        client = self.ctx.conn(args)
        session = client.getSession()
        update_service = session.getUpdateService()
        tag = update_service.saveAndReturnObject(tag)
        self.ctx.out("TagAnnotation:%s" % tag.id.val)

    def createset(self, args):
        """
        Create a tag set command.
        """
        tags = []
        if args.tag:
            if type(args.tag) is list:
                tags = args.tag
            else:
                tags = [args.tag]
        else:
            # Should not happen
            self.ctx.err("Missing tag parameter")
            sys.exit(1)

        tag = self.create_tag(args.name, args.desc, text="tag set")
        tag.ns = rstring(omero.constants.metadata.NSINSIGHTTAGSET)
        links = []
        for t in tags:
            link = AnnotationAnnotationLinkI()
            link.parent = tag
            link.child = TagAnnotationI(rlong(long(t)), False)
            links.append(link)
        client = self.ctx.conn(args)
        session = client.getSession()
        update_service = session.getUpdateService()
        try:
            links = update_service.saveAndReturnArray(links)
            self.ctx.out("TagAnnotation:%s" % links[0].parent.id.val)
        except omero.ValidationException as e:
            self.ctx.err(e.message)
            self.ctx.err("Check that tag '%s' exists." % t)
            sys.exit(1)

    def load(self, args):
        """
        Import new tag(s) from json.
        """
        if args.filename:
            fobj = open(args.filename)
        else:
            fobj = sys.stdin

        p = json.load(fobj)

        if fobj is not sys.stdin:
            fobj.close()

        to_add = []

        for element in p:
            if 'set' in element:
                tag = self.create_tag(str(element['name']),
                                      str(element['desc']))
                tag.ns = rstring(omero.constants.metadata.NSINSIGHTTAGSET)
                links = []
                for e in element['set']:
                    t = self.create_tag(str(e['name']), str(e['desc']))
                    link = AnnotationAnnotationLinkI()
                    link.parent = tag
                    link.child = t
                    links.append(link)
                to_add.extend(links)
            else:
                to_add.append(self.create_tag(str(element['name']),
                              str(element['desc'])))

        client = self.ctx.conn(args)
        session = client.getSession()
        update_service = session.getUpdateService()
        to_add = update_service.saveAndReturnArray(to_add)
        ids = []
        for element in to_add:
            if isinstance(element, TagAnnotationI):
                self.ctx.out("TagAnnotation:%s" % element.id.val)
                ids.append(element.id.val)
            else:
                tag_id = element.parent.id.val
                if tag_id not in ids:
                    self.ctx.out("TagAnnotation:%s" % tag_id)
                    ids.append(tag_id)

    def link(self, args):
        """
        Links an object to a tag annotation.
        """

        try:
            obj_type, obj_id = args.object.split(':')
            obj_id = long(obj_id)
        except ValueError:
            obj_type = None
            obj_id = None

        if obj_type is None or obj_id is None:
            self.ctx.err("Missing object or object not of form"
                         " <object_type>:<object_id>")
            sys.exit(1)

        if not args.tag_id:
            self.ctx.err("Missing tag_id")
            sys.exit(1)

        tag_id = args.tag_id

        parameters = omero.sys.ParametersI()
        parameters.addId(obj_id)
        ice_map = dict()
        if args.admin:
            ice_map["omero.group"] = "-1"

        # Retrieve annotation
        client = self.ctx.conn(args)
        session = client.getSession()
        query_service = session.getQueryService()
        update_service = session.getUpdateService()
        try:
            annotation = query_service.find("TagAnnotation", tag_id)
        except omero.SecurityViolation, sv:
            self.ctx.die(510, "SecurityViolation: %s" % sv.message)

        if not annotation:
            self.ctx.die(400, "Could not find annotation")

        obj = query_service.findByQuery(
            "select o from %s as o "
            "left outer join fetch o.annotationLinks "
            "where o.id = :id" % obj_type, parameters, ice_map)
        if obj is None:
            self.ctx.err(
                "Object query returned nothing. Check your object type.")
            sys.exit(1)

        obj.linkAnnotation(annotation)
        try:
            obj = update_service.saveAndReturnObject(obj)
        except omero.SecurityViolation, sv:
            self.ctx.die(510, "SecurityViolation: %s" % sv.message)

        self.ctx.out("%sAnnotationLink:%s" % (obj_type, obj.id.val))

    def list(self, args):
        """
        List tags command.
        """
        if args.nopage:
            self.console_length = None
            self.width = 80
        else:
            self.width, self.console_length = self.determine_console_size()

        tagsets = [None]
        lines = []
        if args.tagset:
            if type(args.tagset) is list:
                tagsets = args.tagset
            else:
                tagsets = [args.tagset]

        for tagset in tagsets:
            tc = self.list_tags_recursive(args, tagset)
            lines.extend(self.generate_tagset(tc.tags, tc.mapping, args))
            if len(tc.orphans) > 0:
                lines.extend(self.generate_orphans(tc.tags, tc.orphans, args))
        self.pagetext(lines)

    def listsets(self, args):
        """
        List tag sets command.
        """
        # The max width of the ID field. We need something here unless
        # we want to pre-search to determine the max size. Our assumption
        # here is that we wont have more than 10,000,000 tags.
        # FIXME - We can figure this out easily enough- Sam
        max_id_width = 8

        if args.nopage:
            self.console_length = None
            self.width = 80
        else:
            self.width, self.console_length = self.determine_console_size()

        if args.desc:
            max_field_width = int(((self.width - max_id_width) / 2.0) - 2)
        else:
            max_field_width = self.width - max_id_width - 2

        tags = [None]
        lines = []
        if args.tag:
            if type(args.tag) is list:
                tags = args.tag
            else:
                tags = [args.tag]

        separator = (
            "-" * max_id_width,
            "-" * max_field_width,
            "-" * max_field_width
        )

        lines.append(separator)

        lines.append((
            clip("ID", max_id_width),
            clip("Name", max_field_width),
            clip("Description", max_field_width)
        ))

        lines.append(separator)

        for tag_id in tags:
            tc = self.list_tagsets(args, tag_id)
            for key, tag in tc.tags.items():
                if args.nopage:
                    lines.append((
                        tag.tag_id,
                        tag.name,
                        tag.description

                    ))
                else:
                    lines.append((
                        clip(str(tag.tag_id), max_id_width),
                        clip(tag.name, max_field_width),
                        clip(tag.description, max_field_width)
                    ))

        lines.append(separator)

        if args.desc:
            self.pagetext_format("{0}|{1}|{2}", lines)
        else:
            self.pagetext_format("{0}|{1}", lines)


try:
    register("tag", TagControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("user", TagControl, HELP)
        cli.invoke(sys.argv[1:])
