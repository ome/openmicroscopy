#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009-2014 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import traceback
from datetime import datetime
from omero.cli import BaseControl, CLI
import platform
import sys
import os
import re
from functools import wraps
from omero_ext.argparse import SUPPRESS

HELP = "OMERO.web configuration/deployment tools"


LONGHELP = """OMERO.web configuration/deployment tools

Configuration:

    Configuration for OMERO.web takes place via the omero config commands. The
    configuration values which are checked are as below:

%s

Example Nginx developer usage:

    omero config set omero.web.debug true
    omero config set omero.web.application_server.max_requests 1
    omero web config nginx-development --http=8000 >> nginx.conf
    nginx -c `pwd`/nginx.conf
    omero web start --wsgi-args ' --reload'
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

"""


def config_required(func):
    """Decorator validating Django dependencies and omeroweb/settings.py"""
    def import_django_settings(func):
        def wrapper(self, *args, **kwargs):
            try:
                import django  # NOQA
            except:
                self.ctx.die(681, "ERROR: Django not installed!")
            if django.VERSION < (1, 6) or django.VERSION >= (1, 9):
                self.ctx.err("ERROR: Django version %s is not "
                             "supported!" % django.get_version())
            try:
                import omeroweb.settings as settings
                kwargs['settings'] = settings
            except Exception, e:
                self.ctx.die(682, e)
            return func(self, *args, **kwargs)
        return wrapper
    return wraps(func)(import_django_settings(func))


def assert_config_argtype(func):
    """Decorator validating OMERO.web deployment dependencies"""
    def config_argtype(func):
        def wrapper(self, *args, **kwargs):
            argtype = args[0].type
            settings = kwargs['settings']
            mismatch = False
            if args[0].system:
                self.ctx.die(683,
                             "ERROR: --system is no longer supported, "
                             "see --help")
            if settings.APPLICATION_SERVER in ("development",):
                mismatch = True
            if settings.APPLICATION_SERVER in (settings.WSGITCP,):
                if argtype not in ("nginx", "nginx-development",):
                    mismatch = True
            if (settings.APPLICATION_SERVER in (settings.WSGI,) and
                    argtype not in ("apache",)):
                mismatch = True
            if mismatch:
                self.ctx.die(680,
                             ("ERROR: configuration mismatch. "
                              "omero.web.application_server=%s cannot be "
                              "used with 'omero web config %s'.") %
                             (settings.APPLICATION_SERVER, argtype))
            return func(self, *args, **kwargs)
        return wrapper
    return wraps(func)(config_argtype(func))


class WebControl(BaseControl):

    config_choices = ("nginx", "nginx-development", "apache")

    def _configure(self, parser):
        sub = parser.sub()

        parser.add(sub, self.help, "Extended help")
        start = parser.add(
            sub, self.start, "Primary start for the OMERO.web server")
        parser.add(sub, self.stop, "Stop the OMERO.web server")
        restart = parser.add(
            sub, self.restart, "Restart the OMERO.web server")
        parser.add(sub, self.status, "Status for the OMERO.web server")

        iis = parser.add(sub, self.iis, "IIS (un-)install of OMERO.web ")
        iis.add_argument("--remove", action="store_true", default=False)

        for x in (start, restart, iis):
            group = x.add_mutually_exclusive_group()
            group.add_argument(
                "--keep-sessions", action="store_true",
                help="Skip clean-up of expired sessions at startup")
            group.add_argument(
                "--no-wait", action="store_true",
                help="Do not wait on expired sessions clean-up")

        for x in (start, restart):
            x.add_argument(
                "--workers", type=int, default=5,
                help="NGINX only: the number of worker processes for handling "
                     "requests.")
            x.add_argument(
                "--worker-connections", type=int, default=1000,
                help="NGINX only: the maximum number of simultaneous clients.")
            x.add_argument(
                "--wsgi-args", type=str, default="",
                help=("NGINX only: additional arguments. "
                      "Check Gunicorn Documentation"
                      "http://docs.gunicorn.org/en/latest/settings.html"))

        #
        # Advanced
        #

        config = parser.add(
            sub, self.config,
            "Output a config template for web server\n"
            "  nginx: Nginx system configuration for inclusion\n"
            "  nginx-development: Standalone user-run Nginx server\n"
            "  apache: Apache 2.2+ with mod_wsgi\n")
        config.add_argument("type", choices=self.config_choices)
        nginx_group = config.add_argument_group(
            'Nginx arguments', 'Optional arguments for nginx templates.')
        nginx_group.add_argument(
            "--http", type=int,
            help="HTTP port for web server")
        nginx_group.add_argument(
            "--max-body-size", type=str, default='0',
            help="Maximum allowed size of the client request body."
            "Default: 0 (disabled)")
        nginx_group.add_argument(
            "--system", action="store_true", help=SUPPRESS)

        parser.add(
            sub, self.syncmedia,
            "Advanced use: Creates needed symlinks for static"
            " media files (Performed automatically by 'start')")

        clearsessions = parser.add(
            sub, self.clearsessions,
            "Advanced use: Can be run as a cron job or directly to clean "
            "out expired sessions.\n See "
            "https://docs.djangoproject.com/en/1.6/topics/http/sessions/"
            "#clearing-the-session-store for more information.")
        clearsessions.add_argument(
            "--no-wait", action="store_true",
            help="Do not wait on expired sessions clean-up")

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

    @config_required
    def help(self, args, settings):
        """Return extended help"""
        try:
            CONFIG_TABLE_FMT = "    %-35.35s  %-8s  %r\n"
            CONFIG_TABLE = CONFIG_TABLE_FMT % (
                "Key", "Default?", "Current value")

            for key in sorted(settings.CUSTOM_SETTINGS_MAPPINGS):
                global_name, default_value, mapping, desc, using_default = \
                    settings.CUSTOM_SETTINGS_MAPPINGS[key]
                global_value = getattr(settings, global_name, "(unset)")
                CONFIG_TABLE += CONFIG_TABLE_FMT % (
                    key, using_default, global_value)
        except:
            CONFIG_TABLE = (
                "INVALID OR LOCKED CONFIGURATION!"
                " Cannot display default values")

        self.ctx.err(LONGHELP % CONFIG_TABLE)

    def _get_python_dir(self):
        return self.ctx.dir / "lib" / "python"

    def _get_fallback_dir(self):
        return self.ctx.dir / "lib" / "fallback"

    def _get_web_templates_dir(self):
        return self.ctx.dir / "etc" / "templates" / "web"

    def _set_apache_wsgi(self, d, settings):
        # WSGIDaemonProcess requires python-path and user
        try:
            import pwd
            d["OMEROUSER"] = pwd.getpwuid(os.getuid()).pw_name
        except ImportError:
            import getpass
            d["OMEROUSER"] = getpass.getuser()
        try:
            import Ice
            d["ICEPYTHONROOT"] = os.path.dirname(Ice.__file__)
        except:
            print traceback.print_exc()
            self.ctx.err(
                "Cannot import Ice.")
        d["OMEROPYTHONROOT"] = self._get_python_dir()
        d["OMEROFALLBACKROOT"] = self._get_fallback_dir()

    @config_required
    @assert_config_argtype
    def config(self, args, settings):
        """Generate a configuration file from a template"""
        server = args.type
        if args.http:
            port = args.http
        elif server in ('nginx-development',):
            port = 8080
        else:
            port = 80

        if settings.APPLICATION_SERVER in settings.WSGITCP:
            if settings.APPLICATION_SERVER_PORT == port:
                self.ctx.die(
                    678, "Port conflict: HTTP(%s) and"" wsgi(%s)."
                    % (port, settings.APPLICATION_SERVER_PORT))

        d = {
            "ROOT": self.ctx.dir,
            "OMEROWEBROOT": self._get_python_dir() / "omeroweb",
            "STATIC_ROOT": settings.STATIC_ROOT,
            "STATIC_URL": settings.STATIC_URL.rstrip("/"),
            "NOW": str(datetime.now())}

        if server in ("nginx", "nginx-development",
                      "apache",):
            d["HTTPPORT"] = port

        if server in ("nginx", "nginx-development",):
            d["MAX_BODY_SIZE"] = args.max_body_size

        # FORCE_SCRIPT_NAME always has a starting /, and will not have a
        # trailing / unless there is no prefix (/)
        # WEB_PREFIX will never end in / (so may be empty)

        try:
            d["FORCE_SCRIPT_NAME"] = settings.FORCE_SCRIPT_NAME.rstrip("/")
            prefix = re.sub(r'\W+', '', d["FORCE_SCRIPT_NAME"])
            d["PREFIX_NAME"] = "_%s" % prefix
        except:
            d["FORCE_SCRIPT_NAME"] = "/"
            d["PREFIX_NAME"] = ""

        if server in ("apache",):
            try:
                d["WEB_PREFIX"] = settings.FORCE_SCRIPT_NAME.rstrip("/")
            except:
                d["WEB_PREFIX"] = "/"

        d["FASTCGI_EXTERNAL"] = '%s:%s' % (
            settings.APPLICATION_SERVER_HOST, settings.APPLICATION_SERVER_PORT)

        if settings.APPLICATION_SERVER not in settings.WSGI_TYPES:
            self.ctx.die(679,
                         "Web template configuration requires"
                         "wsgi or wsgi-tcp.")

        if server == "apache":
            self._set_apache_wsgi(d, settings)

        template_file = "%s.conf.template" % server
        c = file(self._get_web_templates_dir() / template_file).read()
        self.ctx.out(c % d)

    def syncmedia(self, args):
        self.collectstatic()

    @config_required
    def enableapp(self, args, settings):
        location = self._get_python_dir() / "omeroweb"
        if not args.appname:
            apps = [x.name for x in filter(
                lambda x: x.isdir() and
                (x / 'scripts' / 'enable.py').exists(),
                location.listdir(unreadable_as_empty=True))]
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
        location = self._get_python_dir() / "omeroweb"
        args = [sys.executable, "-i", location /
                "../omero/gateway/scripts/dbhelpers.py"]
        self.set_environ()
        os.environ['DJANGO_SETTINGS_MODULE'] = \
            os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        self.ctx.call(args, cwd=location)

    def call(self, args):
        try:
            location = self._get_python_dir() / "omeroweb"
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
            os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
            self.set_environ()
            self.ctx.call(cargs, cwd=location)
        except:
            print traceback.print_exc()

    @config_required
    def collectstatic(self, settings):
        """Ensure that static media is copied to the correct location"""
        location = self._get_python_dir() / "omeroweb"
        args = [sys.executable, "manage.py", "collectstatic", "--noinput"]
        rv = self.ctx.call(args, cwd=location)
        if rv != 0:
            self.ctx.die(607, "Failed to collect static content.\n")

    @config_required
    def clearsessions(self, args, settings):
        """Clean out expired sessions."""
        self.ctx.out("Clearing expired sessions. This may take some time... ")
        location = self._get_python_dir() / "omeroweb"
        cmd = [sys.executable, "manage.py", "clearsessions"]
        if not args.no_wait:
            rv = self.ctx.call(cmd, cwd=location)
            if rv != 0:
                self.ctx.die(607, "Failed to clear sessions.\n")
            self.ctx.out("[OK]")
        else:
            self.ctx.popen(cmd, cwd=location)

    @config_required
    def start(self, args, settings):
        self.collectstatic()
        if not args.keep_sessions:
            self.clearsessions(args)

        link = ("%s:%d" % (settings.APPLICATION_SERVER_HOST,
                           settings.APPLICATION_SERVER_PORT))
        location = self._get_python_dir() / "omeroweb"
        deploy = getattr(settings, 'APPLICATION_SERVER')

        if deploy in (settings.WSGI,):
            self.ctx.out("You are deploying OMERO.web using apache and"
                         " mod_wsgi. Generate apache config using"
                         " 'omero web config apache' and reload"
                         " web server.")
            return 1
        else:
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

        # 3216
        if deploy in (settings.WSGI,):
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

        if deploy == settings.WSGITCP:
            try:
                import gunicorn  # NOQA
            except ImportError:
                self.ctx.err("[FAILED]")
                self.ctx.die(690,
                             "ERROR: FastCGI support was removed in "
                             "OMERO 5.2. Install Gunicorn and update "
                             "config.")
            try:
                os.environ['SCRIPT_NAME'] = settings.FORCE_SCRIPT_NAME
            except:
                pass
            cmd = "gunicorn -D -p %(base)s/var/django.pid"
            cmd += " --bind %(host)s:%(port)d"
            cmd += " --workers %(workers)d "
            cmd += " --worker-connections %(worker_conn)d"
            cmd += " --max-requests %(maxrequests)d"
            cmd += " %(wsgi_args)s"
            cmd += " omeroweb.wsgi:application"
            django = (cmd % {
                'base': self.ctx.dir,
                'host': settings.APPLICATION_SERVER_HOST,
                'port': settings.APPLICATION_SERVER_PORT,
                'maxrequests': settings.APPLICATION_SERVER_MAX_REQUESTS,
                'workers': args.workers,
                'worker_conn': args.worker_connections,
                'wsgi_args': args.wsgi_args}).split()
            rv = self.ctx.popen(args=django, cwd=location)  # popen
        else:
            django = [sys.executable, "manage.py", "runserver", link,
                      "--noreload", "--nothreading"]
            rv = self.ctx.call(django, cwd=location)
        self.ctx.out("[OK]")
        return rv

    @config_required
    def status(self, args, settings):
        self.ctx.out("OMERO.web status... ", newline=False)

        deploy = getattr(settings, 'APPLICATION_SERVER')
        cache_backend = getattr(settings, 'CACHE_BACKEND', None)
        if cache_backend is not None:
            cache_backend = ' (CACHE_BACKEND %s)' % cache_backend
        else:
            cache_backend = ''
        rv = 0
        if deploy in settings.WSGI_TYPES:
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
        elif deploy in settings.DEVELOPMENT:
            self.ctx.err(
                "DEVELOPMENT: You will have to kill processes by hand!")
        else:
            self.ctx.err(
                "Invalid APPLICATION_SERVER "
                "(omero.web.application_server = '%s')!" % deploy)
        return rv

    @config_required
    def stop(self, args, settings):
        self.ctx.out("Stopping OMERO.web... ", newline=False)
        deploy = getattr(settings, 'APPLICATION_SERVER')
        if deploy in settings.WSGI_TYPES:
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
                        "OMERO.web %s workers (PID %s) not started?"
                        % (deploy.replace("-tcp", "").upper(), pid_text))
                    return True
                os.kill(pid, signal.SIGTERM)  # kill whole group
                self.ctx.out("[OK]")
                self.ctx.out("OMERO.web %s workers (PID %d) killed." %
                             (deploy.replace("-tcp", "").upper(), pid))
                return True
            finally:
                if pid_path.exists():
                    pid_path.remove()
        elif deploy in settings.DEVELOPMENT:
            self.ctx.err(
                "DEVELOPMENT: You will have to kill processes by hand!")
            return False
        else:
            self.ctx.err(
                "Invalid APPLICATION_SERVER "
                "(omero.web.application_server = '%s')!" % deploy)
            return False

    def restart(self, args):
        if self.stop(args):
            return self.start(args)
        else:
            return False

    def set_environ(self, ice_config=None):
        os.environ['ICE_CONFIG'] = ice_config is None and \
            str(self.ctx.dir / "etc" / "ice.config") or str(ice_config)
        os.environ['PATH'] = str(os.environ.get('PATH', '.') + ':' +
                                 self.ctx.dir / 'bin')

    @config_required
    def iis(self, args, settings):
        if not (self._isWindows() or self.ctx.isdebug):
            self.ctx.die(2, "'iis' command is for Windows only")

        self.collectstatic()
        if not args.keep_sessions:
            self.clearsessions(args)

        web_iis = self._get_python_dir() / "omero_web_iis.py"
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
