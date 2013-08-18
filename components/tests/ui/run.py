#! /usr/bin/env python

"""Runner Script for OMERO.web tests

Tests are run by giving a path to the tests to be executed as an argument to
this script. Possible Robot Framework options are given before the path.

Examples:
  run.py -t web testcases/web                          # Run all tests in a directory
  run.py -t web testcases/web/webadmin_login.txt   # Run tests in a specific file
  run.py -t web -v BROWSER:IE testcases/web # Override variable
  run.py -t web -v BROWSER:IE -v DELAY:0.25 login_tests

By default tests are executed with Firefox browser, but this can be changed
by overriding the `BROWSER` variable as illustrated above. Similarly it is
possible to slow down the test execution by overriding the `DELAY` variable
with a non-zero value.

Running the tests requires that Robot Framework, Selenium2Library, Python, and
Java to be installed.
"""

import os
import sys, getopt
from subprocess import call

try:
    import Selenium2Library
except ImportError, e:
    print 'Importing Selenium2Library module failed (%s).' % e
    print 'Please make sure you have Selenium2Library properly installed.'
    sys.exit(1)


ROOT = os.path.dirname(os.path.abspath(__file__))

def run_tests(args):
    """ parse the arguments"""

    client = ''

    try:
      opts, v = getopt.getopt(args[0:2],'t:', ["help", "target="])
    except getopt.GetoptError as err:
      print str(err) # will print something like "option -a not recognized"
      print_usage()
      sys.exit(2)

    print args
    
    for opt, a in opts:
       print opt
       if opt in ("-t", "--target"):
         client = a

    print client
    if client == '':
        print 'No target specified'
        sys.exit(2)

    if client == 'web':
       run_web_tests(sys.argv[3:])
    elif client == 'cli':
       run_cli_tests(sys.argv[3:])
    elif client == 'insight':
       run_insight_tests(sys.argv[3:])

def run_cli_tests(args):
    call(['pybot'] + args, shell=(os.sep == '\\'))

def run_web_tests(args):
    call(['pybot'] + args, shell=(os.sep == '\\'))

def run_insight_tests(args):
    call(['jybot'] + args, shell=(os.sep == '\\'))


def print_help():
    print __doc__

def print_usage():
    print 'Usage: run.py -t web [options] datasource'
    print '   or: run.py help'


if __name__ == '__main__':
    action = {'help': print_help,
              '': print_usage}.get('-'.join(sys.argv[1:]))
    if action:
        action()
    else:
        run_tests(sys.argv[1:])