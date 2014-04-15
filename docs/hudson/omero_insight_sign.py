#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Sign all jars required by OMERO.server webstart
# Note jarsigner is called with passwords passed on the command-line to
# remain backwards compatible with Java 1.6. Later versions of Java jarsigner
# can read passwords from a file.

import getpass
import glob
import hashlib
import logging
import os
import shutil
import subprocess
import sys
import time


FAILURE_RETRIES = 3
TIMESTAMP_SERVER_DELAY = 5
DEFAULT_TIMESTAMP_SERVER = 'http://tsa.starfieldtech.com'


class Stop(Exception):
    def __init__(self, rc, *args, **kwargs):
        self.rc = rc
        super(Stop, self).__init__(*args, **kwargs)


def usage():
    return ("""\
%s keystore.jks alias server-zip|server-dir
  [-kp keystore-password] [-cp certificate-password] [-kf keystore-passfile]
  [-cf certificate-passfile] [-ts yes|no|timestamp-server] [-oz output.zip]
If a zip is given and no -oz option is given a new zip will be created called
<server>-signed.zip, if -oz is passed an empty string then no zip will be
created. If a directory is given then no zip will be created unless an output
zip is specified.
Passwords can be specified on the command line (-kp, -cp), in a file (-kf, -cf)
or by entering at the command line when prompted (default).
If no timestamping option is given timestamping will be enabled using
%s""" % (os.path.basename(__file__), DEFAULT_TIMESTAMP_SERVER))


def getLogFormatter():
    return logging.Formatter(fmt='%(asctime)s %(message)s',
                             datefmt='%Y-%m-%d %H:%M:%S')


class Args:
    """
    Parse command line arguments
    Minimise dependencies, argparse isn't distributed with Python 2.6
    """
    def __init__(self, args):
        def getarg(args, n, optname):
            try:
                return args[n]
            except IndexError:
                raise Stop(2, 'Expected argument for %s\n%s' % (
                    optname, usage()))

        def check_password_unset(arg, argname):
            if getattr(self, argname) is not None:
                raise Stop(
                    2, 'Password for %s can only be specified once\n%s' % (
                       arg, usage()))

        if len(args) < 4:
            raise Stop(2, usage())

        self.keystore = args[1]
        self.alias = args[2]
        self.server = args[3]
        self.keypass = None
        self.certpass = None
        self.timestamper = DEFAULT_TIMESTAMP_SERVER
        self.zipout = None

        n = 4
        while n < len(args):
            arg = args[n]
            if arg == '-kp':
                check_password_unset(arg, 'keypass')
                val = getarg(args, n + 1, arg)
                self.keypass = ('pass', val)
            elif arg == '-kf':
                check_password_unset(arg, 'keypass')
                val = getarg(args, n + 1, arg)
                self.keypass = ('file', val)
            elif arg == '-cp':
                check_password_unset(arg, 'certpass')
                val = getarg(args, n + 1, arg)
                self.certpass = ('pass', val)
            elif arg == '-cf':
                check_password_unset(arg, 'certpass')
                val = getarg(args, n + 1, arg)
                self.certpass = ('file', val)
            elif arg == '-ts':
                val = getarg(args, n + 1, arg)
                if val.lower() == 'yes':
                    self.timestamper = DEFAULT_TIMESTAMP_SERVER
                elif val.lower() == 'no':
                    self.timestamper = None
                else:
                    self.timestamper = val
            elif arg == '-oz':
                self.zipout = getarg(args, n + 1, arg)

            else:
                raise Stop(2, 'Unknown argument: %s\n%s' % (arg, usage()))
            n += 2


def jarsign(jar, alias, keystore, keypass, certpass, timestamper):
    if timestamper:
        failures = 0
        while failures < FAILURE_RETRIES:
            cmd = ['jarsigner', '-keystore', keystore, '-storepass', keypass,
                   '-keypass', certpass, '-tsa', timestamper, jar, alias]
            logging.info('Signing %s', jar)
            r = subprocess.call(cmd)
            if r == 0:
                logging.info('Signed %s', jar)
                return

            failures += 1
            if failures < FAILURE_RETRIES:
                logging.warn('Failed to sign %s, retrying', jar)
                time.sleep(TIMESTAMP_SERVER_DELAY)

        raise Stop(2, 'Failed to sign %s after %d attempts' % (
            jar, failures))
    else:
        cmd = ['jarsigner', '-keystore', keystore, '-storepass', keypass,
               '-keypass', certpass, jar, alias]
        logging.info('Signing %s', jar)
        r = subprocess.call(cmd)
        if r != 0:
            raise Stop(r, 'Failed to sign %s' % jar)
        logging.info('Signed %s', jar)


def getpassword(arg, what):
    if arg and arg[0] == 'pass':
        p = arg[1]
    elif arg and arg[0] == 'file':
        with open(arg[1], 'rU') as f:
            p = f.read().rstrip('\n')
    else:
        p = getpass.getpass('%s password: ' % what)
    return p


def unzip(zipname):
    cmd = ['unzip', '-q', zipname]
    logging.info('Unzipping %s', zipname)
    r = subprocess.call(cmd)
    if r != 0:
        raise Stop(r, 'Failed to unzip: %s' % zipname)


def zip(zipname, d):
    cmd = ['zip', '-q', '-r', zipname, d]
    logging.info('Zipping %s', zipname)
    r = subprocess.call(cmd)
    if r != 0:
        raise Stop(r, 'Failed to zip: %s %s' % (zipname, d))


def rename_backup(filename):
    newname = '%s.bak' % filename
    n = 0
    while os.path.exists(newname):
        n += 1
        newname = '%s.bak.%d' % (filename, n)
    logging.info('Renaming %s to %s', filename, newname)
    os.rename(filename, newname)


def md5sum(filename):
    md5file = filename + '.md5'
    md5 = hashlib.md5()
    logging.info('Creating %s', md5file)
    with open(filename, 'rb') as f:
        for chunk in iter(lambda: f.read(2**20), b''):
            md5.update(chunk)
    with open(md5file, 'w') as f:
        f.write('%s  %s\n' % (md5.hexdigest(), os.path.basename(filename)))


def sign_server(args):
    if not os.path.exists(args.server):
        raise Stop('Server path %s does not exist' % args.server)
    iszip = not os.path.isdir(args.server)

    if iszip:
        if not args.server.endswith('.zip'):
            raise Stop('Expected zip-filename to end with .zip')
        serverdir = os.path.basename(args.server[:-4])
        if args.zipout is None:
            args.zipout = serverdir + '-signed.zip'
    else:
        serverdir = args.server

    logfile = serverdir + '-signing.log'
    fileHandler = logging.FileHandler(logfile)
    fileHandler.setFormatter(getLogFormatter())
    logging.getLogger().addHandler(fileHandler)

    if iszip:
        unzip(args.server)
        if not os.path.isdir(serverdir):
            raise Stop('Expected directory %s does not exist' % serverdir)

    keypass = getpassword(args.keypass, 'Keystore')
    certpass = getpassword(args.certpass, 'Certificate')

    jardir = os.path.join(serverdir, 'lib', 'insight')
    if not os.path.isdir(jardir):
        raise Stop(3, 'Directory %s not found' % jardir)
    jars = glob.glob(os.path.join(serverdir, 'lib', 'insight', '*.jar'))
    if not jars:
        raise Stop(3, 'No jars found in %s' % jardir)

    for jar in jars:
        jarsign(jar, args.alias, args.keystore, keypass, certpass,
                args.timestamper)

    if args.zipout:
        if os.path.exists(args.zipout):
            rename_backup(args.zipout)
        zip(args.zipout, serverdir)
        md5sum(args.zipout)
        if iszip:
            shutil.rmtree(serverdir)


if __name__ == '__main__':
    logging.getLogger().setLevel(logging.INFO)
    consoleHandler = logging.StreamHandler()
    consoleHandler.setFormatter(getLogFormatter())
    logging.getLogger().addHandler(consoleHandler)

    try:
        args = Args(sys.argv)
        sign_server(args)
    except Stop as e:
        sys.stderr.write('ERROR: %s\n' % e)
        sys.exit(e.rc)
