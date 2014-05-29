#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009-2013 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from datetime import datetime
from omero.cli import BaseControl, CLI
import platform
import sys
import os

try:
    from omeroweb import settings

    CONFIG_TABLE_FMT = "    %-35.35s  %-8s  %r\n"
    CONFIG_TABLE = CONFIG_TABLE_FMT % ("Key", "Default?", "Current value")

    for key in sorted(settings.CUSTOM_SETTINGS_MAPPINGS):
        global_name, default_value, mapping, using_default = \
            settings.CUSTOM_SETTINGS_MAPPINGS[key]
        global_value = getattr(settings, global_name, "(unset)")
        CONFIG_TABLE += CONFIG_TABLE_FMT % (key, using_default, global_value)
except:
    CONFIG_TABLE = "INVALID OR LOCKED CONFIGURATION! Cannot display default"\
        " values"

HELP = """OMERO.web configuration/deployment tools

Configuration:

    Configuration for OMERO.web takes place via the omero config commands. The
    configuration values which are checked are as below:

%s

Example Nginx usage:

    omero config set omero.web.debug true
    omero config set omero.web.application_server fastcgi
    omero web config nginx --http=8000 >> nginx.conf
    omero web start
    nginx -c `pwd`/nginx.conf
    omero web status
    omero web stop
    nginx -s stop

Example IIS usage:

    # Install server
    omero config set omero.web.debug true
    omero web iis
    iisreset

    # Uninstall server
    omero web iis --remove
    iisreset

""" % CONFIG_TABLE


class WebControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()

        parser.add(sub, self.start, "Primary start for the OMERO.web server")
        parser.add(sub, self.stop, "Stop the OMERO.web server")
        parser.add(sub, self.status, "Status for the OMERO.web server")

        iis = parser.add(sub, self.iis, "IIS (un-)install of OMERO.web ")
        iis.add_argument("--remove", action="store_true", default=False)

        #
        # Advanced
        #

        config = parser.add(
            sub, self.config,
            "Output a config template for server"
            " ('nginx' or 'apache' for the moment")
        config.add_argument("type", choices=("nginx", "apache"))
        config.add_argument(
            "--http", type=int,
            help="HTTP port for web server (not fastcgi)")
        config.add_argument(
            "--system", action="store_true",
            help="System appropriate configuration file")

        parser.add(
            sub, self.syncmedia,
            "Advanced use: Creates needed symlinks for static media"
            " files\n(Performed automatically by 'start')")

        #
        # Developer
        #

        call = parser.add(
            sub, self.call,
            """Developer use: call appname "[executable] scriptname" args""")
        call.add_argument("appname")
        call.add_argument("scriptname")
        call.add_argument("arg", nargs="*")

        enableapp = parser.add(
            sub, self.enableapp,
            "Developer use: runs enable.py and then syncdb")
        enableapp.add_argument("appname", nargs="*")

        parser.add(
            sub, self.gateway,
            "Developer use: Loads the blitz gateway into a Python"
            " interpreter")

        selenium = parser.add(
            sub, self.seleniumtest,
            "Developer use: runs selenium tests on a django app")
        selenium.add_argument(
            "--config", action="store", help="ice.config location")
        selenium.add_argument("djangoapp", help="Django-app to be tested")
        selenium.add_argument("seleniumserver", help="E.g. localhost")
        selenium.add_argument("hostname", help="E.g. http://localhost:4080")
        selenium.add_argument("browser", help="E.g. firefox")

        test = parser.add(
            sub, self.test, "Developer use: Runs omero web tests"
            " (py.test)\n--cov* options depend on pytest-cov plugin")
        test.add_argument(
            "--config", action="store", help="ice.config location")
        test.add_argument(
            "--basepath", action="store",
            help="Base omeroweb path (default lib/python/omeroweb)")
        test.add_argument(
            "--testpath", action="store",
            help="Path for test collection (relative to basepath)")
        test.add_argument(
            "--string", action="store",
            help="Only run tests including string.")
        test.add_argument(
            "--failfast", action="store_true", default=False,
            help="Exit on first error")
        test.add_argument(
            "--verbose", action="store_true", default=False,
            help="More verbose output")
        test.add_argument(
            "--quiet", action="store_true", default=False,
            help="Less verbose output")
        test.add_argument(
            "--pdb", action="store_true", default=False,
            help="Fallback to pdb on error")
        test.add_argument(
            '--cov', action='append', default=[],
            help='measure coverage for filesystem path (multi-allowed)')
        test.add_argument(
            '--cov-report', action='append', default=[],
            choices=['term', 'term-missing', 'annotate', 'html', 'xml'],
            help="type of report to generate: term, term-missing, annotate,"
            " html, xml (multi-allowed)")

    def config(self, args):
        if not args.type:
            self.ctx.out(
                "Available configuration helpers:\n - nginx, apache\n")
        else:
            server = args.type
            port = 8080
            if args.http:
                port = args.http
            if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                if settings.APPLICATION_SERVER_PORT == port:
                    self.ctx.die(
                        678, "Port conflict: HTTP(%s) and"" fastcgi-tcp(%s)."
                        % (port, settings.APPLICATION_SERVER_PORT))
            if server == "nginx":
                if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                    fastcgi_pass = "%s:%s" \
                        % (settings.APPLICATION_SERVER_HOST,
                           settings.APPLICATION_SERVER_PORT)
                else:
                    fastcgi_pass = "unix:%s/var/django_fcgi.sock" \
                        % self.ctx.dir
                if args.system:
                    c = file(self.ctx.dir / "etc" /
                             "nginx.conf.system.template").read()
                else:
                    c = file(self.ctx.dir / "etc" /
                             "nginx.conf.template").read()
                d = {
                    "ROOT": self.ctx.dir,
                    "OMEROWEBROOT": self.ctx.dir / "lib" / "python" /
                    "omeroweb",
                    "HTTPPORT": port,
                    "FASTCGI_PASS": fastcgi_pass,
                    }
                if hasattr(settings, 'FORCE_SCRIPT_NAME') \
                        and len(settings.FORCE_SCRIPT_NAME) > 0:
                    d["FASTCGI_PATH_SCRIPT_INFO"] = \
                        "fastcgi_split_path_info ^(%s)(.*)$;\n" \
                        "            " \
                        "fastcgi_param PATH_INFO $fastcgi_path_info;\n" \
                        "            " \
                        "fastcgi_param SCRIPT_INFO $fastcgi_script_name;\n" \
                        % (settings.FORCE_SCRIPT_NAME)
                else:
                    d["FASTCGI_PATH_SCRIPT_INFO"] = \
                        "fastcgi_param PATH_INFO $fastcgi_script_name;\n"
                self.ctx.out(c % d)
            if server == "apache":
                if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                    fastcgi_external = '-host %s:%s' % \
                        (settings.APPLICATION_SERVER_HOST,
                         settings.APPLICATION_SERVER_PORT)
                else:
                    fastcgi_external = '-socket "%s/var/django_fcgi.sock"' % \
                        self.ctx.dir
                stanza = """###
# apache config for omero
# this file should be loaded *after* ssl.conf
#
# -D options to control configurations
#  OmeroWebClientRedirect - redirect / to /omero/webclient
#  OmeroWebAdminRedirect  - redirect / to /omero/webadmin
#  OmeroForceSSL - redirect all http requests to https

###
### Example SSL stanza for OMERO.web created %(NOW)s
###

# Eliminate overlap warnings with the default ssl vhost
# Requires SNI (http://wiki.apache.org/httpd/NameBasedSSLVHostsWithSNI) \
support
# most later versions of mod_ssl and OSes will support it
# if you see "You should not use name-based virtual hosts in conjunction \
with  SSL!!"
# or similar start apache with -D DISABLE_SNI and modify ssl.conf
#<IfDefine !DISABLE_SNI>
#  NameVirtualHost *:443
#</IfDefine>
#
## force https/ssl
#<IfDefine OmeroForceSSL>
#  RewriteEngine on
#  RewriteCond %%{HTTPS} !on
#  RewriteRule (.*) https://%%{HTTP_HOST}%%{REQUEST_URI} [L]
#</IfDefine>
#
#<VirtualHost _default_:443>
#
#  ErrorLog logs/ssl_error_log
#  TransferLog logs/ssl_access_log
#  LogLevel warn
#
#  SSLEngine on
#  SSLProtocol all -SSLv2
#  SSLCipherSuite ALL:!ADH:!EXPORT:!SSLv2:RC4+RSA:+HIGH:+MEDIUM:+LOW
#  SSLCertificateFile /etc/pki/tls/certs/server.crt
#  SSLCertificateKeyFile /etc/pki/tls/private/server.key
#
#  # SSL Protocol Adjustments:
#  SetEnvIf User-Agent ".*MSIE.*" \
#    nokeepalive ssl-unclean-shutdown \
#    downgrade-1.0 force-response-1.0
#
#  # Per-Server Logging:
#  CustomLog logs/ssl_request_log \
#    "%%t %%h %%{SSL_PROTOCOL}x %%{SSL_CIPHER}x \"%%r\" %%b"
#
#</VirtualHost>

RewriteEngine on
RewriteRule ^/?$ /omero/ [R]

###
### Stanza for OMERO.web created %(NOW)s
###
FastCGIExternalServer "%(ROOT)s/var/omero.fcgi" %(FASTCGI_EXTERNAL)s

<Directory "%(ROOT)s/var">
    Options -Indexes FollowSymLinks
    Order allow,deny
    Allow from all
</Directory>

<Directory "%(STATIC)s">
    Options -Indexes FollowSymLinks
    Order allow,deny
    Allow from all
</Directory>

Alias /static %(STATIC)s
Alias /omero "%(ROOT)s/var/omero.fcgi/"
"""
                d = {
                    "ROOT": self.ctx.dir,
                    "STATIC": self.ctx.dir / "lib" / "python" / "omeroweb" /
                    "static",
                    "OMEROWEBROOT": self.ctx.dir / "lib" / "python" /
                    "omeroweb",
                    "FASTCGI_EXTERNAL": fastcgi_external,
                    "NOW": str(datetime.now()),
                    }
                self.ctx.out(stanza % d)

    def syncmedia(self, args):
        self.collectstatic()

    def enableapp(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        if not args.appname:
            apps = [x.name for x in filter(
                lambda x: x.isdir() and
                (x / 'scripts' / 'enable.py').exists(), location.listdir())]
            iapps = map(lambda x: x.startswith('omeroweb.') and x[9:] or
                        x, settings.INSTALLED_APPS)
            apps = filter(lambda x: x not in iapps, apps)
            self.ctx.out('[enableapp] available apps:\n - ' +
                         '\n - '.join(apps) + '\n')
        else:
            for app in args.appname:
                args = [sys.executable, location / app / "scripts" /
                        "enable.py"]
                rv = self.ctx.call(args, cwd=location)
                if rv != 0:
                    self.ctx.die(121, "Failed to enable '%s'.\n" % app)
                else:
                    self.ctx.out("App '%s' was enabled\n" % app)
            args = [sys.executable, "manage.py", "syncdb", "--noinput"]
            rv = self.ctx.call(args, cwd=location)
            self.syncmedia(None)

    def gateway(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        args = [sys.executable, "-i", location /
                "../omero/gateway/scripts/dbhelpers.py"]
        self.set_environ()
        os.environ['DJANGO_SETTINGS_MODULE'] = \
            os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        self.ctx.call(args, cwd=location)

    def test(self, args):
        try:
            pass
        except:
            self.ctx.die(121, 'test: wrong arguments, run test -h for a list')

        cargs = ['py.test']

        if args.config:
            self.set_environ(ice_config=args.config)
        else:
            self.set_environ(ice_config=self.ctx.dir / 'etc' / 'ice.config')

        if args.basepath:
            cwd = args.basepath
        else:
            cwd = self.ctx.dir / 'lib' / 'python' / 'omeroweb'

        if args.testpath:
            cargs.extend(['-s', args.testpath])
        if args.string:
            cargs.extend(['-k', args.string])
        if args.failfast:
            cargs.append('-x')
        if args.verbose:
            cargs.append('-v')
        if args.quiet:
            cargs.append('-q')
        if args.pdb:
            cargs.append('--pdb')
        for cov in args.cov:
            cargs.extend(['--cov', cov])
        for cov_rep in args.cov_report:
            cargs.extend(['--cov-report', cov_rep])

        os.environ['DJANGO_SETTINGS_MODULE'] = \
            os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        # The following is needed so the cwd is included in the python path
        # when using --testpath
        os.environ['PYTHONPATH'] += ':.'

        self.ctx.call(cargs, cwd=cwd)

    def seleniumtest(self, args):
        try:
            ice_config = args.config
            appname = args.djangoapp
            seleniumserver = args.seleniumserver
            hostname = args.hostname
            browser = args.browser
        except:
            self.ctx.die(121, "usage: seleniumtest [path.]{djangoapp}"
                         " [seleniumserver] [hostname] [browser]")

        if appname.find('.') > 0:
            appname = appname.split('.')
            appbase = appname[0]
            location = self.ctx.dir / appbase
            appname = '.'.join(appname[1:])
        else:
            appbase = "omeroweb"
            location = self.ctx.dir / "lib" / "python" / "omeroweb"

        cargs = [sys.executable, location / appname / "tests" /
                 "seleniumtests.py", seleniumserver, hostname, browser]
        # cargs += args.arg[1:]
        self.set_environ(ice_config=ice_config)
        os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
        self.ctx.call(cargs, cwd=location)

    def call(self, args):
        try:
            location = self.ctx.dir / "lib" / "python" / "omeroweb"
            cargs = []
            appname = args.appname
            scriptname = args.scriptname.split(' ')
            if len(scriptname) > 1:
                cargs.append(scriptname[0])
                scriptname = ' '.join(scriptname[1:])
            else:
                scriptname = scriptname[0]
            cargs.extend([location / appname / "scripts" / scriptname] +
                         args.arg)
            print cargs
            os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
            self.set_environ()
            self.ctx.call(cargs, cwd=location)
        except:
            import traceback
            print traceback.print_exc()

    def collectstatic(self):
        # Ensure that static media is copied to the correct location
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        args = [sys.executable, "manage.py", "collectstatic", "--noinput"]
        rv = self.ctx.call(args, cwd=location)
        if rv != 0:
            self.ctx.die(607, "Failed to collect static content.\n")

    def start(self, args):
        self.collectstatic()
        import omeroweb.settings as settings
        link = ("%s:%s" % (settings.APPLICATION_SERVER_HOST,
                           settings.APPLICATION_SERVER_PORT))
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        self.ctx.out("Starting OMERO.web... ", newline=False)
        cache_backend = getattr(settings, 'CACHE_BACKEND', None)
        if cache_backend is not None and cache_backend.startswith("file:///"):
            cache_backend = cache_backend[7:]
            if "Windows" != platform.system() \
               and not os.access(cache_backend, os.R_OK | os.W_OK):
                self.ctx.out("[FAILED]")
                self.ctx.out("CACHE_BACKEND '%s' not writable or missing." %
                             getattr(settings, 'CACHE_BACKEND'))
                return 1
        deploy = getattr(settings, 'APPLICATION_SERVER')

        # 3216
        if deploy in (settings.FASTCGI_TYPES):
            if "Windows" == platform.system():
                self.ctx.out("""
WARNING: Unless you **really** know what you are doing you should NOT be
using bin\omero web start on Windows with FastCGI.
""")
            pid_path = self.ctx.dir / "var" / "django.pid"
            pid_num = None

            if pid_path.exists():
                pid_txt = pid_path.text().strip()
                try:
                    pid_num = int(pid_txt)
                except:
                    pid_path.remove()
                    self.ctx.err("Removed invalid %s: '%s'"
                                 % (pid_path, pid_txt))

            if pid_num is not None:
                try:
                    os.kill(pid_num, 0)
                    self.ctx.die(606,
                                 "%s exists! Use 'web stop' first" % pid_path)
                except OSError:
                    pid_path.remove()
                    self.ctx.err("Removed stale %s" % pid_path)

        if deploy == settings.FASTCGI:
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork socket=%(base)s/var/django_fcgi.sock"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=true"
            cmd += " maxchildren=5 minspare=1 maxspare=5"
            cmd += " maxrequests=%(maxrequests)d"
            django = (cmd % {
                'maxrequests': settings.APPLICATION_SERVER_MAX_REQUESTS,
                'base': self.ctx.dir}).split()
            rv = self.ctx.popen(args=django, cwd=location)  # popen
        elif deploy == settings.FASTCGITCP:
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork host=%(host)s port=%(port)s"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=true"
            cmd += " maxchildren=5 minspare=1 maxspare=5"
            cmd += " maxrequests=%(maxrequests)d"
            django = (cmd % {
                'maxrequests': settings.APPLICATION_SERVER_MAX_REQUESTS,
                'base': self.ctx.dir,
                'host': settings.APPLICATION_SERVER_HOST,
                'port': settings.APPLICATION_SERVER_PORT}).split()
            rv = self.ctx.popen(args=django, cwd=location)  # popen
        else:
            django = [sys.executable, "manage.py", "runserver", link,
                      "--noreload"]
            rv = self.ctx.call(django, cwd=location)
        self.ctx.out("[OK]")
        return rv

    def status(self, args):
        self.ctx.out("OMERO.web status... ", newline=False)
        import omeroweb.settings as settings
        deploy = getattr(settings, 'APPLICATION_SERVER')
        cache_backend = getattr(settings, 'CACHE_BACKEND', None)
        if cache_backend is not None:
            cache_backend = ' (CACHE_BACKEND %s)' % cache_backend
        else:
            cache_backend = ''
        rv = 0
        if deploy in settings.FASTCGI_TYPES:
            try:
                f = open(self.ctx.dir / "var" / "django.pid", 'r')
                pid = int(f.read())
            except IOError:
                self.ctx.out("[NOT STARTED]")
                return rv

            try:
                os.kill(pid, 0)  # NULL signal
                self.ctx.out("[RUNNING] (PID %d)%s" % (pid, cache_backend))
            except:
                self.ctx.out("[NOT STARTED]")
                return rv
        else:
            self.ctx.err("DEVELOPMENT: You will have to check status by hand!")
        return rv

    def stop(self, args):
        self.ctx.out("Stopping OMERO.web... ", newline=False)
        import omeroweb.settings as settings
        deploy = getattr(settings, 'APPLICATION_SERVER')
        if deploy in settings.FASTCGI_TYPES:
            if "Windows" == platform.system():
                self.ctx.out("""
WARNING: Unless you **really** know what you are doing you should NOT be
using bin\omero web start on Windows with FastCGI.
""")
            pid = 'Unknown'
            pid_path = self.ctx.dir / "var" / "django.pid"
            pid_text = "Unknown"
            if pid_path.exists():
                pid_text = pid_path.text().strip()
            try:
                try:
                    pid = int(pid_text)
                    import signal
                    os.kill(pid, 0)  # NULL signal
                except:
                    self.ctx.out("[FAILED]")
                    self.ctx.out(
                        "Django FastCGI workers (PID %s) not started?"
                        % pid_text)
                    return
                os.kill(pid, signal.SIGTERM)  # kill whole group
                self.ctx.out("[OK]")
                self.ctx.out("Django FastCGI workers (PID %d) killed." % pid)
            finally:
                if pid_path.exists():
                    pid_path.remove()
        else:
            self.ctx.err(
                "DEVELOPMENT: You will have to kill processes by hand!")

    def set_environ(self, ice_config=None):
        os.environ['ICE_CONFIG'] = ice_config is None and \
            str(self.ctx.dir / "etc" / "ice.config") or str(ice_config)
        os.environ['PATH'] = str(os.environ.get('PATH', '.') + ':' +
                                 self.ctx.dir / 'bin')

    def iis(self, args):
        self.collectstatic()
        if not (self._isWindows() or self.ctx.isdebug):
            self.ctx.die(2, "'iis' command is for Windows only")

        web_iis = self.ctx.dir / "lib" / "python" / "omero_web_iis.py"
        cmd = [sys.executable, str(web_iis)]
        if args.remove:
            cmd.append("remove")
        self.ctx.call(cmd)

try:
    register("web", WebControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("web", WebControl, HELP)
        cli.invoke(sys.argv[1:])
