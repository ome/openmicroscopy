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
import re
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
%s keystore.jks alias server-zip|server-dir [-v]
  [-kp keystore-password] [-cp certificate-password] [-kf keystore-passfile]
  [-cf certificate-passfile] [-ts yes|no|timestamp-server] [-oz output.zip]
  [-skipverify]

If a zip is given and no -oz option is given a new zip will be created called
<server>.zip, if -oz is passed an empty string then no zip will be created. If
a directory is given then no zip will be created unless an output zip is
specified.

Passwords can be specified on the command line (-kp, -cp), in a file (-kf, -cf)
or by entering at the command line when prompted (default).

Use -v for verbose output (note this will print out passwords).

If no timestamping option is given timestamping will be enabled using
%s.

If the http_proxy/https_proxy environment variables are set they will be
automatically used.

A verification step will be automatically run unless -skipverify is passed.

If jarsigner fails, for example due to a timestamping error, it will
automatically retry %d times before aborting the whole signing process""" % (
        os.path.basename(__file__), DEFAULT_TIMESTAMP_SERVER, FAILURE_RETRIES))


def getLogFormatter():
    return logging.Formatter(fmt='%(asctime)s %(message)s',
                             datefmt='%Y-%m-%d %H:%M:%S')


class Args:
    """
    Parse command line arguments
    Check environment variables
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

        self.verbose = False

        self.keystore = args[1]
        self.alias = args[2]
        self.server = args[3]
        self.keypass = None
        self.certpass = None
        self.timestamper = DEFAULT_TIMESTAMP_SERVER
        self.zipout = None
        self.skipverify = False

        self.httpproxy = self.parse_proxy_envvar('http_proxy')
        self.httpsproxy = self.parse_proxy_envvar('https_proxy')

        n = 4
        while n < len(args):
            arg = args[n]
            if arg == '-v':
                self.verbose = True
                n += 1
                continue
            elif arg == '-kp':
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
            elif arg == '-skipverify':
                self.skipverify = True
                n += 1
                continue

            else:
                raise Stop(2, 'Unknown argument: %s\n%s' % (arg, usage()))
            n += 2

    def parse_proxy_envvar(self, var):
        """
        Parse a system proxy environment variable
        E.g. http://username:password@example.org:8080
        """
        proxy = os.getenv(var)
        if not proxy:
            return None
        m = re.match('(?P<protocol>.*://)((?P<user>.+):(?P<pass>.+)@)?'
                     '(?P<host>[^:]+)(:(?P<port>\d+))?$', proxy)
        if not m:
            raise Stop(2, 'Invalid proxy variable %s=%s' % (var, proxy))
        return m.groupdict()


def check_jarsigner():
    """
    Checks whether jarsigner can be run
    """
    cmd = ['jarsigner']
    logging.debug('Running: %s', cmd)
    with open(os.devnull, 'w') as devnull:
        try:
            r = subprocess.call(cmd, stdout=devnull)
        except Exception as e:
            raise Stop(2, 'Unable to execute jarsigner: %s' % e)
        if r != 0 and r != 1:
            # jarsigner with no args returns 0 on Java 1.7, 1 on Java 1.6
            raise Stop(r, 'jarsigner unexpected exit code: %d' % r)
    logging.debug('jarsigner is runnable')


###########################################################################
# Signing
###########################################################################

def jarsign(jar, alias, keystore, keypass, certpass, timestamper, proxy=None):
    # Additional jarsigner args must come before the jar and alias
    cmd1 = ['jarsigner', '-keystore', keystore, '-storepass', keypass,
            '-keypass', certpass]
    cmd2 = [jar, alias]

    if timestamper:
        tsargs = ['-tsa', timestamper]
        if proxy:
            tsargs += proxy

        failures = 0
        while failures < FAILURE_RETRIES:
            cmd = cmd1 + tsargs + cmd2
            logging.info('Signing %s', jar)
            logging.debug('Running: %s', cmd)
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
        cmd = cmd1 + cmd2
        logging.info('Signing %s', jar)
        logging.debug('Running: %s', cmd)
        r = subprocess.call(cmd)
        if r != 0:
            raise Stop(r, 'Failed to sign %s' % jar)
        logging.info('Signed %s', jar)


def get_proxy_args(http, https):
    args = []
    if http:
        args.append(
            '-J-Dhttp.proxyHost=%s' % http['host'])
        if http['port']:
            args.append('-J-Dhttp.proxyPort=%s' % http['port'])
        if http['user']:
            args.append('-J-Dhttp.proxyUser=%s' % http['user'])
        if http['pass']:
            args.append('-J-Dhttp.proxyPassword=%s' % http['pass'])
    if https:
        args.append(
            '-J-Dhttps.proxyHost=%s' % https['host'])
        if https['port']:
            args.append('-J-Dhttps.proxyPort=%s' % https['port'])
        if https['user']:
            args.append('-J-Dhttps.proxyUser=%s' % https['user'])
        if https['pass']:
            args.append('-J-Dhttps.proxyPassword=%s' % https['pass'])

    if args:
        logging.info('Using proxy args: %s', args)

    return args


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
    logging.info('Zipping %s %s', zipname, d)
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


###########################################################################
# Verification
###########################################################################

class Status(object):
    def __init__(self, jarname):
        self.jarname = jarname
        self.verified = None
        self.warning = None
        self.unknowncert = None
        self.notimestamp = None
        self.nomanifest = None
        self.expiresoon = None

    def __str__(self):
        s = '%s %s' % (self.jarname, 'Signed' if self.verified else 'Unsigned')
        if self.warning:
            s += ' warning'
        if self.unknowncert:
            s += ' unknown-cert'
        if self.notimestamp:
            s += ' no-timestamp'
        if self.nomanifest:
            s += ' no-manifest'
        if self.expiresoon:
            s += ' expire-soon'
        return s


def parse_jarsigner_verify(jarname, out):
    s = Status(jarname)

    lines = out.split('\n')
    for line in lines:
        line = line.strip()
        if not line:
            continue
        elif line == 'jar verified.':
            assert s.verified is None
            s.verified = True
        elif line.startswith('jar is unsigned.'):
            assert s.verified is None
            s.verified = False
        elif line == 'Warning:':
            assert s.warning is None
            s.warning = True
        elif line == 'no manifest.':
            assert s.nomanifest is None
            s.nomanifest = True
        elif line.startswith('This jar contains entries whose certificate '
                             'chain is not validated.'):
            assert s.unknowncert is None
            s.unknowncert = True
        elif line.startswith('This jar contains signatures that does not '
                             'include a timestamp.'):
            assert s.notimestamp is None
            s.notimestamp = True
        elif line.startswith('This jar contains entries whose signer '
                             'certificate will expire within six months.'):
            assert s.expiresoon is None
            s.expiresoon = True
        elif line.startswith('Re-run with the -verbose and -certs options for'
                             ' more details.'):
            continue
        else:
            raise Stop(2, 'Unexpected output: for %s %s' % (jarname, lines))

    return s


def jarverify(jar):
    cmd = ['jarsigner', '-verify', jar]
    logging.debug('Running: %s', cmd)

    proc = subprocess.Popen(
        cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = proc.communicate()
    if proc.returncode != 0:
        # jarsigner returns 0 irrespective of whether the jar was verified or
        # not
        raise Stop(proc.returncode, 'Failed to run %s' % cmd)

    if err:
        raise Stop(2, 'Unexpected error output from %s\n%s' % (cmd, err))
    if not out:
        raise Stop(2, 'No output received from %s' % cmd)

    status = parse_jarsigner_verify(jar, out)
    logging.info('%s', status)
    return status


def verify_jar_directory(d):
    if not os.path.isdir(d):
        raise Stop(3, 'Directory %s not found' % d)
    jars = glob.glob(os.path.join(d, '*.jar'))
    statuses = []
    for jar in jars:
        status = jarverify(jar)
        statuses.append(status)
        logging.debug('%s', status)
    return statuses


def summarise_statuses(statuses):
    signed = 0
    warning = 0
    unknowncert = 0
    notimestamp = 0
    nomanifest = 0
    expiresoon = 0
    total = len(statuses)

    for s in statuses:
        if s.verified:
            signed += 1
        if s.warning:
            warning += 1
        if s.unknowncert:
            unknowncert += 1
        if s.notimestamp:
            notimestamp += 1
        if s.nomanifest:
            nomanifest += 1
        if s.expiresoon:
            expiresoon += 1

    return ('%d/%d signed %d warn %d unknown-cert %d not-timestamped %d '
            'no-manifest %d expire-soon' % (
                signed, total, warning, unknowncert, notimestamp, nomanifest,
                expiresoon))


def verify_jars(jardir):
    logging.info('Verifying jars')
    statuses = verify_jar_directory(jardir)
    s = summarise_statuses(statuses)
    logging.info('%s', s)


###########################################################################

def sign_server(args):
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)

    check_jarsigner()

    additional_args = get_proxy_args(args.httpproxy, args.httpsproxy)

    if not os.path.exists(args.server):
        raise Stop('Server path %s does not exist' % args.server)
    iszip = not os.path.isdir(args.server)

    if iszip:
        if not args.server.endswith('.zip'):
            raise Stop('Expected zip-filename to end with .zip')
        serverdir = os.path.basename(args.server[:-4])
        if args.zipout is None:
            args.zipout = serverdir + '.zip'
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
                args.timestamper, additional_args)

    if not args.skipverify:
        verify_jars(jardir)

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
