#! /usr/bin/env python

"""Runner Script for OMERO.web tests

Tests are run by giving a path to the tests to be executed as an argument to
this script. Possible Robot Framework options are given before the path.

Examples:
  run.py testcases/web                          # Run all tests in a directory
  rundemo.py testcases/web/webadmin_login.txt   # Run tests in a specific file
  rundemo.py --variable BROWSER:IE testcases/web # Override variable
  rundemo.py -v BROWSER:IE -v DELAY:0.25 login_tests

By default tests are executed with Firefox browser, but this can be changed
by overriding the `BROWSER` variable as illustrated above. Similarly it is
possible to slow down the test execution by overriding the `DELAY` variable
with a non-zero value.

Running the demo requires that Robot Framework, Selenium2Library, Python, and
Java to be installed.
"""

import os
import sys
from subprocess import call

try:
    import Selenium2Library
except ImportError, e:
    print 'Importing Selenium2Library module failed (%s).' % e
    print 'Please make sure you have Selenium2Library properly installed.'
    sys.exit(1)


ROOT = os.path.dirname(os.path.abspath(__file__))
DEMOAPP = os.path.join(ROOT, 'demoapp', 'server.py')


def run_tests(args):
    call(['pybot'] + args, shell=(os.sep == '\\'))

def print_help():
    print __doc__

def print_usage():
    print 'Usage: run.py [options] datasource'
    print '   or: run.py help'


if __name__ == '__main__':
    action = {'help': print_help,
              '': print_usage}.get('-'.join(sys.argv[1:]))
    if action:
        action()
    else:
        run_tests(sys.argv[1:])