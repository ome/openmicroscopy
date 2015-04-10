#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009-2014 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from datetime import datetime
from omero.cli import BaseControl, CLI
import platform
import sys
import os
from omero_ext.argparse import SUPPRESS

HELP = "OMERO.web configuration/deployment tools"


LONGHELP = """OMERO.web configuration/deployment tools

Configuration:

    Configuration for OMERO.web takes place via the omero config commands. The
    configuration values which are checked are as below:

%s

Example Nginx developer usage:

    omero config set omero.web.debug true
    omero web config nginx-development --http=8000 >> nginx.conf
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

"""


class WebControl(BaseControl):

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
            x.add_argument(
                "--skip-clear-sessions", action="store_true",
                help="Skip clean-up of expired sessions at startup")

        #
        # Advanced
        #

        config = parser.add(
            sub, self.config,
            "Output a config template for web server\n"
            "  nginx: Nginx system configuration for inclusion\n"
            "  nginx-development: Standalone user-run Nginx server\n"
            "  apache: Apache 2.2 with mod_fastcgi\n"
            "  apache-fcgi: Apache 2.4+ with mod_proxy_fcgi\n")
        config.add_argument("type", choices=(
            "nginx", "nginx-development", "apache", "apache-fcgi"))
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

        parser.add(
            sub, self.clearsessions,
            "Advanced use: Can be run as a cron job or directly to clean "
            "out expired sessions.\n See "
            "https://docs.djangoproject.com/en/1.6/topics/http/sessions/"
            "#clearing-the-session-store for more information.")

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

    def help(self, args):
        """Return extended help"""
        from omeroweb import settings
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

    def _get_templates_dir(self):
        return self.ctx.dir / "etc" / "templates"

    def _set_nginx_fastcgi(self, d, settings):
        script_info = (
            "fastcgi_split_path_info ^(%s)(.*)$;\n"
            "            fastcgi_param PATH_INFO $fastcgi_path_info;\n"
            "            fastcgi_param SCRIPT_INFO $fastcgi_script_name;\n")
        script_info_fallback = (
            "fastcgi_param PATH_INFO $fastcgi_script_name;\n")
        try:
            d["FASTCGI_PATH_SCRIPT_INFO"] = (
                script_info % settings.FORCE_SCRIPT_NAME)
        except:
            d["FASTCGI_PATH_SCRIPT_INFO"] = script_info_fallback

    def _set_apache_fcgi_fastcgi(self, d, settings):
        # OMERO.web requires the fastcgi PATH_INFO variable, which
        # mod_proxy_fcgi obtains by taking everything after the last
        # path component containing a dot.
        d["CGI_PREFIX"] = "%s.fcgi" % d["FORCE_SCRIPT_NAME"]

    def config(self, args):
        """Generate a configuration file from a template"""
        from omeroweb import settings
        if not args.type:
            self.ctx.die(
                "Available configuration helpers:\n"
                " - nginx, nginx-development, apache, apache-fcgi\n")

        if args.system:
            self.ctx.err(
                "WARNING: --system is no longer supported, see --help")

        server = args.type
        if args.http:
            port = args.http
        elif server == 'nginx-development':
            port = 8080
        else:
            port = 80

        if settings.APPLICATION_SERVER == settings.FASTCGITCP:
            if settings.APPLICATION_SERVER_PORT == port:
                self.ctx.die(
                    678, "Port conflict: HTTP(%s) and"" fastcgi-tcp(%s)."
                    % (port, settings.APPLICATION_SERVER_PORT))

        d = {
            "ROOT": self.ctx.dir,
            "OMEROWEBROOT": self._get_python_dir() / "omeroweb",
            "STATIC_URL": settings.STATIC_URL.rstrip("/"),
            "NOW": str(datetime.now())}

        if server in ("nginx", "nginx-development"):
            d["HTTPPORT"] = port
            d["MAX_BODY_SIZE"] = args.max_body_size

        # FORCE_SCRIPT_NAME always has a starting /, and will not have a
        # trailing / unless there is no prefix (/)
        # WEB_PREFIX will never end in / (so may be empty)

        try:
            d["FORCE_SCRIPT_NAME"] = settings.FORCE_SCRIPT_NAME.rstrip("/")
        except:
            d["FORCE_SCRIPT_NAME"] = "/"

        if server in ("apache", "apache-fcgi"):
            try:
                d["WEB_PREFIX"] = settings.FORCE_SCRIPT_NAME.rstrip("/")
            except:
                d["WEB_PREFIX"] = ""

        if settings.APPLICATION_SERVER != settings.FASTCGITCP:
            self.ctx.die(
                679, "Web template configuration requires fastcgi-tcp")

        d["FASTCGI_EXTERNAL"] = '%s:%s' % (
            settings.APPLICATION_SERVER_HOST, settings.APPLICATION_SERVER_PORT)

        if server in ("nginx", "nginx-development"):
            self._set_nginx_fastcgi(d, settings)

        if server == "apache-fcgi":
            self._set_apache_fcgi_fastcgi(d, settings)

        template_file = "%s.conf.template" % server
        c = file(self._get_templates_dir() / template_file).read()
        self.ctx.out(c % d)

    def syncmedia(self, args):
        self.collectstatic()

    def enableapp(self, args):
        location = self._get_python_dir() / "omeroweb"
        if not args.appname:
            from omeroweb import settings
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
            print cargs
            os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
            self.set_environ()
            self.ctx.call(cargs, cwd=location)
        except:
            import traceback
            print traceback.print_exc()

    def collectstatic(self):
        """Ensure that static media is copied to the correct location"""
        location = self._get_python_dir() / "omeroweb"
        args = [sys.executable, "manage.py", "collectstatic", "--noinput"]
        rv = self.ctx.call(args, cwd=location)
        if rv != 0:
            self.ctx.die(607, "Failed to collect static content.\n")

    def clearsessions(self, args, wait=True):
        """Clean out expired sessions."""
        self.ctx.out("Clearing expired sessions. This may take some time... ")
        location = self._get_python_dir() / "omeroweb"
        args = [sys.executable, "manage.py", "clearsessions"]
        if wait:
            rv = self.ctx.call(args, cwd=location)
            if rv != 0:
                self.ctx.die(607, "Failed to clear sessions.\n")
            self.ctx.out("[OK]")
        else:
            self.ctx.popen(args, cwd=location)

    def start(self, args):
        self.collectstatic()
        if not args.skip_clear_sessions:
            self.clearsessions(args, wait=False)
        import omeroweb.settings as settings
        link = ("%s:%s" % (settings.APPLICATION_SERVER_HOST,
                           settings.APPLICATION_SERVER_PORT))
        location = self._get_python_dir() / "omeroweb"
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
                      "--noreload", "--nothreading"]
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
                    return True
                os.kill(pid, signal.SIGTERM)  # kill whole group
                self.ctx.out("[OK]")
                self.ctx.out("Django FastCGI workers (PID %d) killed." % pid)
                return True
            finally:
                if pid_path.exists():
                    pid_path.remove()
        else:
            self.ctx.err(
                "DEVELOPMENT: You will have to kill processes by hand!")
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

    def iis(self, args):
        if not (self._isWindows() or self.ctx.isdebug):
            self.ctx.die(2, "'iis' command is for Windows only")

        self.collectstatic()
        if not args.skip_clear_sessions:
            self.clearsessions(args, wait=False)

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
