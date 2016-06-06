#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
This script parses a list of text files and generates Wiki output.
E.g.
$ python markup.py      # to stdout
$ python markup.py > wikiText.txt    # to file

"""

import sys
import re
import os


def lines(file):
    if file.name.endswith((".py", ".m", ".java")):
        all = [line for line in file]
        skip = check_header(all, quiet=True)
        for line in all[skip:]:
            yield line
    else:
        for line in file:
            yield line
    yield '\n'


def blocks(file):
    block = []
    for line in lines(file):
        if END_MARKER in line:
            break
        elif (line.strip() and not (re.match('# =', line) or
                                    re.match('% =', line) or
                                    re.match('// =', line))):
            block.append(line.rstrip())
        elif block:
            # yield ''.join(block).strip()
            yield block
            block = []


class Rule:
    """
    Base class for all rules.
    """
    def __init__(self, comment_char=None):
        self.comment_char = comment_char

    def action(self, block, handler):
        handler.start(self.type)
        handler.feed(block)
        handler.end(self.type)
        return True


class SubtitleRule(Rule):
    """
    A single line that is a comment and follows a Code block
    """
    type = 'subtitle'
    afterCode = True

    def condition(self, block):
        if (len(block) == 1 and block[0].startswith(self.comment_char)
                and self.afterCode):
            block[0] = block[0].lstrip('%s ' % self.comment_char)
            self.afterCode = False
            return True
        # see if this is a code block - if so, reset flag
        for line in block:
            if not line.startswith(self.comment_char):
                self.afterCode = True
                break
        return False


class SphinxSubtitleRule(SubtitleRule):
    """ Need this action to only start (all on one line - no feed) """
    def action(self, block, handler):
        handler.start(self.type, block)
        return True


class CommentRule(Rule):
    """
    A comment block is a block where every line starts with a comment character
    """
    type = 'comment'

    def condition(self, block):
        for line in block:
            if not line.startswith(self.comment_char):
                return False
        # since we have a comment (not code), lets remove all the '#'
        for i in range(len(block)):
            block[i] = block[i].lstrip('%s ' % self.comment_char)
        return True


class SphinxCommentRule(CommentRule):
    """ Need this action to feed without indent) """
    def action(self, block, handler):
        handler.start(self.type)
        handler.feed(block, indent="")
        handler.end(self.type)
        return True


class CodeRule(Rule):
    """
    A code block is simply a block that isn't covered by any of the
    other rules. NB: Other rules will be tested before this one
    """
    type = 'code'

    def condition(self, block):
        return True


class Handler:
    """
    An object that handles method calls from the Parser.

    The Parser will call the start() and end() methods at the
    beginning of each block, with the proper block name as
    parameter. The sub() method will be used in regular expression
    substitution. When called with a name such as 'emphasis', it will
    return a proper substitution function.
    """
    def callback(self, prefix, name, *args):
        method = getattr(self, prefix+name, None)
        if callable(method):
            return method(*args)

    def start(self, name, *args):
        self.callback('start_', name, *args)

    def end(self, name):
        self.callback('end_', name)

    def sub(self, name):
        return lambda match: (self.callback('sub_', name, match)
                              or match.group(0))


class SphinxRenderer(Handler):
    """
    A specific handler used for rendering reStrunctured Text (sphinx Docs).
    """
    def start_document(self, title):
        print title
        print "^" * len(title)

    def end_document(self):
        print ''

    def start_code(self):
        print '\n::\n'

    def end_code(self):
        print ''

    def start_subtitle(self, block):
        print "\n-  **%s**" % block[0]

    def end_subtitle(self):
        print ""

    def start_comment(self):
        print '\n'

    def end_comment(self):
        print '\n'

    def start_list(self):
        print '\n'

    def end_list(self):
        print '\n'

    def start_listitem(self):
        print ' * '

    def end_listitem(self):
        print ''

    def start_title(self):
        print '='

    def end_title(self):
        print '='

    def sub_emphasis(self, match):
        return '**%s**' % match.group(1)

    def sub_url(self, match):
        return '[%s]' % (match.group(1))

    def sub_mail(self, match):
        return '<a href="mailto:%s">%s</a>' % (match.group(1), match.group(1))

    def feed(self, block, indent="    "):
        for i in range(len(block)-1):
            print indent + block[i]
        print indent + block[-1],


class WikiRenderer(Handler):
    """
    A specific handler used for rendering Wiki.
    """
    def start_document(self, title):
        print '== %s ==' % title

    def end_document(self):
        print ''

    def start_code(self):
        print '\n{{{'

    def end_code(self):
        print '\n}}}'

    def start_subtitle(self):
        print " * ''' ",

    def end_subtitle(self):
        print " ''' "

    def start_comment(self):
        print '\n'

    def end_comment(self):
        print '\n'

    def start_list(self):
        print '\n'

    def end_list(self):
        print '\n'

    def start_listitem(self):
        print ' * '

    def end_listitem(self):
        print ''

    def start_title(self):
        print '='

    def end_title(self):
        print '='

    def sub_emphasis(self, match):
        return '**%s**' % match.group(1)

    def sub_url(self, match):
        return '[%s]' % (match.group(1))

    def sub_mail(self, match):
        return '<a href="mailto:%s">%s</a>' % (match.group(1), match.group(1))

    def feed(self, block):
        for i in range(len(block)-1):
            print block[i]
        print block[-1],


class Parser:
    """
    A Parser reads a text file, applying rules and controlling a
    handler.
    """
    def __init__(self, handler):
        self.handler = handler
        self.rules = []
        self.filters = []

    def addRule(self, rule):
        self.rules.append(rule)

    def addFilter(self, pattern, name):
        def filter(block, handler):
            return re.sub(pattern, handler.sub(name), block)
        self.filters.append(filter)

    def parse(self, file, title):
        self.handler.start('document', title)
        for c, block in enumerate(blocks(file)):
            if c == 0:
                # don't output the first block (connection, imports etc)
                continue
            for i in range(len(block)):
                for filter in self.filters:
                    block[i] = filter(block[i], self.handler)
            for rule in self.rules:
                if rule.condition(block):
                    last = rule.action(block, self.handler)
                    if last:
                        break
        self.handler.end('document')


class PythonParser(Parser):
    """
    A specific Parser that adds rules for Python
    """
    def __init__(self, handler):
        Parser.__init__(self, handler)
        self.addRule(SphinxSubtitleRule('#'))
        self.addRule(SphinxCommentRule('#'))
        self.addRule(CodeRule())

        # self.addFilter(r'\*(.+?)\*', 'emphasis')
        self.addFilter(r'(http://[\.a-zA-Z0-9_/]+)', 'url')


class MatlabParser(Parser):
    """
    A specific Parser that adds rules for Matlab
    """
    def __init__(self, handler):
        Parser.__init__(self, handler)
        self.addRule(SphinxSubtitleRule('%'))
        self.addRule(SphinxCommentRule('%'))
        self.addRule(CodeRule())

        # self.addFilter(r'\*(.+?)\*', 'emphasis')
        self.addFilter(r'(http://[\.a-zA-Z_/]+)', 'url')


class JavaParser(Parser):
    """
    A specific Parser that adds rules for Java
    """
    def __init__(self, handler):
        Parser.__init__(self, handler)
        self.addRule(SphinxSubtitleRule('//'))
        self.addRule(SphinxCommentRule('*'))
        self.addRule(CodeRule())

        # self.addFilter(r'\*(.+?)\*', 'emphasis')
        self.addFilter(r'(http://[\.a-zA-Z_/]+)', 'url')

# Marker to find in the file the blocks to parse
START_MARKER = "start-code"
END_MARKER = "end-code"


def check_header(file_lines, quiet=False):
    """
    Checks the first N lines of the file that they match
    START_MARKER. Returns the number of lines which should be skipped.
    """
    lines = []
    for line in file_lines:
        lines.append(line)
        if START_MARKER in line:
            return len(lines)
    return len(lines)


def parseMatlab(Parser):
    """
    Generates a doc page for the Matlab files.
    """
    exclude = [
        'exampleSuite.m',
        'LoadMetadataAdvanced.m',
        'ReadDataAdvanced.m',
        'parseOmeroProperties.m',
        ]
    files = []
    os.chdir("matlab")
    for file in os.listdir("."):
        if os.path.isfile(file) and file not in exclude:
            files.append(file)

    parser = MatlabParser(handler)
    parsefiles(parser, files)


def parsePython(Parser):
    """
    Generates a doc page for the Python files.
    """
    exclude = [
        '__main__.py',
        'Metadata.py',
        'Parse_OMERO_Properties.py',
        'Scripting_Service_Example.py'
        ]
    files = []
    os.chdir("python")
    for file in os.listdir("."):
        if os.path.isfile(file) and file not in exclude:
            files.append(file)

    parser = PythonParser(handler)
    parsefiles(parser, files)


def parseJava(Parser):
    """
    Generates a doc page for the Java files.
    """
    exclude = [
        'Setup.java',
        'LoadMetadataAdvanced.java',
        'ReadDataAdvanced.java'
        ]
    files = []
    os.chdir("java/src/training")
    for file in os.listdir("."):
        if os.path.isfile(file) and file not in exclude:
            files.append(file)

    parser = JavaParser(handler)
    parsefiles(parser, files)


def parsefiles(parser=Parser, files=[]):
    '''
    Parse the files
    '''
    for f in files:
        # get title from file name
        t = os.path.splitext(os.path.basename(f))[0]
        t = t.replace("_", "")
        l = re.findall('[A-Z][a-z]*', t)
        t = " ".join(l)
        # specific case to handle.
        t = t.replace("R O I", "ROI")
        t = t.replace("O M E R O", "OMERO")
        read = open(f, 'r')
        parser.parse(read, t)


if __name__ == "__main__":

    handler = SphinxRenderer()
    if "--matlab" in sys.argv:
        parseMatlab(handler)
    elif "--java" in sys.argv:
        parseJava(handler)
    else:
        parsePython(handler)
