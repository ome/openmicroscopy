import fnmatch
import os
import re
import sys
from lxml.html import parse

tags = {'script': 'src', 'link': 'href'}


def check_url_suffix(attr):
    # test for url_suffix
    regexp = re.compile(r"{% static '(.*?)'\|add:url_suffix %}")
    match = regexp.search(attr)
    if match:
        return True


def check_version(attr):
    # test for version
    regexp = re.compile(r"{% static '.*(\d\.\d).*' %}")
    match = regexp.search(attr)
    if match:
        return True


def check_variable(attr):
    # skip variables
    regexp = re.compile(r"{{(.*?)}}")
    match = regexp.search(attr)
    if match:
        return True


def check_web_templates():
    for root, dirs, files in os.walk('.'):
        # fidn template dirs only
        for dirname in fnmatch.filter(dirs, "templates"):
            template_dir = os.path.join(root, dirname)
            # any file
            for r, d, f in os.walk(template_dir):
                templates = list(fnmatch.filter(f, "*"))
                for _f in templates:
                    fname = os.path.join(r, _f)
                    with open(fname) as infile:
                        root = parse(infile).getroot()
                        # parse to find sensitive tags
                        for t in tags:
                            for e in root.iter(t):
                                # find atribute
                                attr = e.get(tags[t])
                                if attr:
                                    if not check_variable(attr):
                                        if not check_url_suffix(attr):
                                            if not check_version(attr):
                                                sline = e.sourceline
                                                yield '{0}:{1} {2}'.format(
                                                    fname, sline, attr)

fail = False
for r in check_web_templates():
    if not fail:
        print "Template error."
    print r
    fail = True

if fail:
    sys.exit(1)
