#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# #                Django settings for OMERO.web project.               # #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
#
# Copyright (c) 2008-2016 University of Dundee.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
#
# Version: 1.0
#


import os.path
import warnings
import sys
import logging
import omero
import omero.config
import omero.clients
import tempfile
import re
import inspect
import json
import random
import string

from omero_ext import portalocker
from omero.install.python_warning import py27_only, PYTHON_WARNING
from omero.util.concurrency import get_event
from utils import sort_properties_to_tuple
from connector import Server

logger = logging.getLogger(__name__)

if not py27_only():
    warnings.warn("WARNING: %s" % PYTHON_WARNING, RuntimeWarning)

# LOGS
# NEVER DEPLOY a site into production with DEBUG turned on.
# Debuging mode.
# A boolean that turns on/off debug mode.
# handler404 and handler500 works only when False
if 'OMERO_HOME' in os.environ:
    OMERO_HOME = os.environ.get('OMERO_HOME')
else:
    OMERO_HOME = os.path.join(os.path.dirname(__file__), '..', '..', '..')
    OMERO_HOME = os.path.normpath(OMERO_HOME)

# Logging
LOGDIR = os.path.join(OMERO_HOME, 'var', 'log').replace('\\', '/')

if not os.path.isdir(LOGDIR):
    try:
        os.makedirs(LOGDIR)
    except Exception, x:
        exctype, value = sys.exc_info()[:2]
        raise exctype(value)

# DEBUG: Never deploy a site into production with DEBUG turned on.
# Logging levels: logging.DEBUG, logging.INFO, logging.WARNING, logging.ERROR
# logging.CRITICAL
# FORMAT: 2010-01-01 00:00:00,000 INFO  [omeroweb.webadmin.webadmin_utils]
# (proc.1308 ) getGuestConnection:20 Open connection is not available

STANDARD_LOGFORMAT = (
    '%(asctime)s %(levelname)5.5s [%(name)40.40s]'
    ' (proc.%(process)5.5d) %(funcName)s():%(lineno)d %(message)s')

FULL_REQUEST_LOGFORMAT = (
    '%(asctime)s %(levelname)5.5s [%(name)40.40s]'
    ' (proc.%(process)5.5d) %(funcName)s():%(lineno)d'
    ' HTTP %(status_code)d %(request)s')

LOGGING_CLASS = 'omero_ext.cloghandler.ConcurrentRotatingFileHandler'
LOGSIZE = 500000000


LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'standard': {
            'format': STANDARD_LOGFORMAT
        },
        'full_request': {
            'format': FULL_REQUEST_LOGFORMAT
        },
    },
    'filters': {
        'require_debug_false': {
            '()': 'django.utils.log.RequireDebugFalse',
        },
        'require_debug_true': {
            '()': 'django.utils.log.RequireDebugTrue',
        },
    },
    'handlers': {
        'default': {
            'level': 'DEBUG',
            'class': LOGGING_CLASS,
            'filename': os.path.join(
                LOGDIR, 'OMEROweb.log').replace('\\', '/'),
            'maxBytes': LOGSIZE,
            'backupCount': 10,
            'formatter': 'standard',
        },
        'request_handler': {
            'level': 'DEBUG',
            'class': LOGGING_CLASS,
            'filename': os.path.join(
                LOGDIR, 'OMEROweb.log').replace('\\', '/'),
            'maxBytes': LOGSIZE,
            'backupCount': 10,
            'filters': ['require_debug_false'],
            'formatter': 'full_request',
        },
        'null': {
            'level': 'DEBUG',
            'class': 'django.utils.log.NullHandler',
        },
        'console': {
            'level': 'DEBUG',
            'filters': ['require_debug_true'],
            'class': 'logging.StreamHandler',
            'formatter': 'standard'
        },
        'mail_admins': {
            'level': 'ERROR',
            'filters': ['require_debug_false'],
            'class': 'django.utils.log.AdminEmailHandler'
        }
    },
    'loggers': {
        'django.request': {  # Stop SQL debug from logging to main logger
            'handlers': ['default', 'request_handler', 'mail_admins'],
            'level': 'DEBUG',
            'propagate': False
        },
        'django': {
            'handlers': ['null'],
            'level': 'DEBUG',
            'propagate': True
        },
        '': {
            'handlers': ['default'],
            'level': 'DEBUG',
            'propagate': True
        }
    }
}


JSON_CONFIG_DIR = os.getenv('OMERO_WEB_CONFIG_DIR')
CONFIG_XML = os.path.join(OMERO_HOME, 'etc', 'grid', 'config.xml')
count = 10
event = get_event("websettings")

while True:
    try:
        CUSTOM_SETTINGS = dict()
        if os.path.exists(CONFIG_XML):
            CONFIG_XML = omero.config.ConfigXml(CONFIG_XML, read_only=True)
            CUSTOM_SETTINGS = CONFIG_XML.as_map()
            CONFIG_XML.close()
        break
    except portalocker.LockException:
        # logger.error("Exception while loading configuration retrying...",
        # exc_info=True)
        exctype, value = sys.exc_info()[:2]
        count -= 1
        if not count:
            raise exctype(value)
        else:
            event.wait(1)  # Wait a total of 10 seconds
    except:
        # logger.error("Exception while loading configuration...",
        # exc_info=True)
        exctype, value = sys.exc_info()[:2]
        raise exctype(value)

CUSTOM_SETTINGS_JSON_SET = dict()
CUSTOM_SETTINGS_JSON_APPEND = dict()
if JSON_CONFIG_DIR:
    CUSTOM_SETTINGS_JSON_SET, CUSTOM_SETTINGS_JSON_APPEND = \
        omero.config.load_json_config_dir(JSON_CONFIG_DIR)

del event
del count
del get_event

WSGI = "wsgi"
WSGITCP = "wsgi-tcp"
WSGI_TYPES = (WSGI, WSGITCP)
DEVELOPMENT = "development"
DEFAULT_SERVER_TYPE = WSGITCP
ALL_SERVER_TYPES = (WSGI, WSGITCP, DEVELOPMENT)

DEFAULT_SESSION_ENGINE = 'omeroweb.filesessionstore'
SESSION_ENGINE_VALUES = ('omeroweb.filesessionstore',
                         'django.contrib.sessions.backends.db',
                         'django.contrib.sessions.backends.file',
                         'django.contrib.sessions.backends.cache',
                         'django.contrib.sessions.backends.cached_db')


def parse_boolean(s, src=None):
    if src == 'json':
        return s
    s = s.strip().lower()
    if s in ('true', '1', 't'):
        return True
    return False


def parse_paths(s):
    return [os.path.normpath(path) for path in parse_json(s)]


def check_server_type(s, src=None):
    if s not in ALL_SERVER_TYPES:
        raise ValueError(
            "Unknown server type: %s. Valid values are: %s"
            % (s, ALL_SERVER_TYPES))
    return s


def check_session_engine(s, src=None):
    if s not in SESSION_ENGINE_VALUES:
        raise ValueError(
            "Unknown session engine: %s. Valid values are: %s"
            % (s, SESSION_ENGINE_VALUES))
    return s


def identity(x, src=None):
    return x


def parse_json(j, src=None):
    if src == 'json':
        return j
    return json.loads(j)


def str_slash(s, src=None):
    if s is not None:
        s = str(s)
        if s and not s.endswith("/"):
            s += "/"
    return s


def str_plain(s, src=None):
    return str(s)


def int_plain(i, src=None):
    return int(i)


def normpath_plain(p, src=None):
    return os.path.normpath(p)


def str_compile(s, src=None):
    return re.compile(s)


class LeaveUnset(Exception):
    pass


def leave_none_unset(s, src=None):
    if s is None:
        raise LeaveUnset()
    return s


def leave_none_unset_int(s, src=None):
    s = leave_none_unset(s)
    if s is not None:
        return int(s)


CUSTOM_HOST = CUSTOM_SETTINGS.get("Ice.Default.Host", "localhost")
CUSTOM_HOST = CUSTOM_SETTINGS.get("omero.master.host", CUSTOM_HOST)
# DO NOT EDIT!
INTERNAL_SETTINGS_MAPPING = {
    "omero.qa.feedback":
        ["FEEDBACK_URL", "http://qa.openmicroscopy.org.uk", str_plain, None],
    "omero.web.upgrades.url":
        ["UPGRADES_URL", None, leave_none_unset, None],
    "omero.web.check_version":
        ["CHECK_VERSION", "true", parse_boolean, None],

    # Allowed hosts:
    # https://docs.djangoproject.com/en/1.8/ref/settings/#allowed-hosts
    "omero.web.allowed_hosts":
        ["ALLOWED_HOSTS", '["*"]', parse_json, None],

    # Do not show WARNING (1_8.W001): The standalone TEMPLATE_* settings
    # were deprecated in Django 1.8 and the TEMPLATES dictionary takes
    # precedence. You must put the values of the following settings
    # into your default TEMPLATES dict:
    # TEMPLATE_DIRS, TEMPLATE_CONTEXT_PROCESSORS.
    "omero.web.system_checks":
        ["SILENCED_SYSTEM_CHECKS", '["1_8.W001"]', parse_json, None],

    # Internal email notification for omero.web.admins,
    # loaded from config.xml directly
    "omero.mail.from":
        ["SERVER_EMAIL",
         None,
         identity,
         ("The email address that error messages come from, such as those"
          " sent to :property:`omero.web.admins`.  Requires EMAIL properties"
          " below.")],
    "omero.mail.host":
        ["EMAIL_HOST",
         None,
         identity,
         "The SMTP server host to use for sending email."],
    "omero.mail.password":
        ["EMAIL_HOST_PASSWORD",
         None,
         identity,
         "Password to use for the SMTP server."],
    "omero.mail.username":
        ["EMAIL_HOST_USER",
         None,
         identity,
         "Username to use for the SMTP server."],
    "omero.mail.port":
        ["EMAIL_PORT",
         25,
         identity,
         "Port to use for the SMTP server."],
    "omero.web.admins.email_subject_prefix":
        ["EMAIL_SUBJECT_PREFIX",
         "[OMERO.web - admin notification]",
         str_plain,
         "Subject-line prefix for email messages"],
    "omero.mail.smtp.starttls.enable":
        ["EMAIL_USE_TLS",
         "false",
         parse_boolean,
         ("Whether to use a TLS (secure) connection when talking to the SMTP"
          " server.")],
}

CUSTOM_SETTINGS_MAPPINGS = {
    # Deployment configuration
    "omero.web.debug":
        ["DEBUG",
         "false",
         parse_boolean,
         ("A boolean that turns on/off debug mode. "
          "Use debug mode only in development, not in production, as it logs "
          "sensitive and confidential information in plaintext.")],
    "omero.web.secret_key":
        ["SECRET_KEY",
         None,
         leave_none_unset,
         ("A boolean that sets SECRET_KEY for a particular Django "
          "installation.")],
    "omero.web.admins":
        ["ADMINS",
         '[]',
         parse_json,
         ("A list of people who get code error notifications whenever the "
          "application identifies a broken link or raises an unhandled "
          "exception that results in an internal server error. This gives "
          "the administrators immediate notification of any errors, "
          "see :doc:`/sysadmins/mail`. "
          "Example:``'[[\"Full Name\", \"email address\"]]'``.")],
    "omero.web.application_server":
        ["APPLICATION_SERVER",
         DEFAULT_SERVER_TYPE,
         check_server_type,
         ("OMERO.web is configured to run in Gunicorn as a generic WSGI (TCP)"
          "application by default. Available options: ``wsgi-tcp`` "
          "(Gunicorn, default), ``wsgi`` (Advanced users only, e.g. manual "
          "Apache configuration with ``mod_wsgi``).")],
    "omero.web.application_server.host":
        ["APPLICATION_SERVER_HOST",
         "127.0.0.1",
         str_plain,
         "Upstream application host"],
    "omero.web.application_server.port":
        ["APPLICATION_SERVER_PORT",
         4080,
         int_plain,
         "Upstream application port"],
    "omero.web.application_server.max_requests":
        ["APPLICATION_SERVER_MAX_REQUESTS",
         0,
         int_plain,
         ("The maximum number of requests a worker will process before "
          "restarting.")],
    "omero.web.middleware":
        ["MIDDLEWARE_CLASSES_LIST",
         ('['
          '{"index": 1, '
          '"class": "django.middleware.common.BrokenLinkEmailsMiddleware"},'
          '{"index": 2, '
          '"class": "django.middleware.common.CommonMiddleware"},'
          '{"index": 3, '
          '"class": "django.contrib.sessions.middleware.SessionMiddleware"},'
          '{"index": 4, '
          '"class": "django.middleware.csrf.CsrfViewMiddleware"},'
          '{"index": 5, '
          '"class": "django.contrib.messages.middleware.MessageMiddleware"},'
          '{"index": 6, '
          '"class": "django.middleware.clickjacking.XFrameOptionsMiddleware"}'
          ']'),
         parse_json,
         ('Warning: Only system administrators should use this feature. '
          'List of Django middleware classes in the form '
          '[{"class": "class.name", "index": FLOAT}]. '
          'See https://docs.djangoproject.com/en/1.8/topics/http/middleware/. '
          'Classes will be ordered by increasing index')],
    "omero.web.prefix":
        ["FORCE_SCRIPT_NAME",
         None,
         leave_none_unset,
         ("Used as the value of the SCRIPT_NAME environment variable in any"
          " HTTP request.")],
    "omero.web.use_x_forwarded_host":
        ["USE_X_FORWARDED_HOST",
         "false",
         parse_boolean,
         ("Specifies whether to use the X-Forwarded-Host header in preference "
          "to the Host header. This should only be enabled if a proxy which "
          "sets this header is in use.")],
    "omero.web.static_url":
        ["STATIC_URL",
         "/static/",
         str_slash,
         ("URL to use when referring to static files. Example: ``'/static/'``"
          " or ``'http://static.example.com/'``. Used as the base path for"
          " asset  definitions (the Media class) and the staticfiles app. It"
          " must end in a slash if set to a non-empty value.")],
    "omero.web.static_root":
        ["STATIC_ROOT",
         os.path.join(os.path.dirname(__file__), 'static').replace('\\', '/'),
         normpath_plain,
         ("The absolute path to the directory where collectstatic will"
          " collect static files for deployment. If the staticfiles contrib"
          " app is enabled (default) the collectstatic management command"
          " will collect static files into this directory.")],
    "omero.web.session_engine":
        ["SESSION_ENGINE",
         DEFAULT_SESSION_ENGINE,
         check_session_engine,
         ("Controls where Django stores session data. See :djangodoc:"
          "`Configuring the session engine for more details <ref/settings"
          "/#session-engine>`.")],
    "omero.web.session_expire_at_browser_close":
        ["SESSION_EXPIRE_AT_BROWSER_CLOSE",
         "true",
         parse_boolean,
         ("A boolean that determines whether to expire the session when the "
          "user closes their browser. See :djangodoc:`Django Browser-length "
          "sessions vs. persistent sessions documentation <topics/http/"
          "sessions/#browser-length-vs-persistent-sessions>` for more "
          "details.")],
    "omero.web.caches":
        ["CACHES",
         ('{"default": {"BACKEND":'
          ' "django.core.cache.backends.dummy.DummyCache"}}'),
         parse_json,
         ("OMERO.web offers alternative session backends to automatically"
          " delete stale data using the cache session store backend, see "
          ":djangodoc:`Django cached session documentation <topics/http/"
          "sessions/#using-cached-sessions>` for more details.")],
    "omero.web.secure":
        ["SECURE",
         "false",
         parse_boolean,
         ("Force all backend OMERO.server connections to use SSL.")],
    "omero.web.session_cookie_age":
        ["SESSION_COOKIE_AGE",
         86400,
         int_plain,
         "The age of session cookies, in seconds."],
    "omero.web.session_cookie_domain":
        ["SESSION_COOKIE_DOMAIN",
         None,
         leave_none_unset,
         "The domain to use for session cookies"],
    "omero.web.session_cookie_name":
        ["SESSION_COOKIE_NAME",
         None,
         leave_none_unset,
         "The name to use for session cookies"],
    "omero.web.session_cookie_secure":
        ["SESSION_COOKIE_SECURE",
         "false",
         parse_boolean,
         ("Restrict session cookies to HTTPS only, you are strongly "
          "recommended to set this to ``true`` in production.")],
    "omero.web.csrf_cookie_secure":
        ["CSRF_COOKIE_SECURE",
         "false",
         parse_boolean,
         ("Restrict CSRF cookies to HTTPS only, you are strongly "
          "recommended to set this to ``true`` in production.")],
    "omero.web.csrf_cookie_httponly":
        ["CSRF_COOKIE_HTTPONLY",
         "false",
         parse_boolean,
         ("Prevent CSRF cookie from being accessed in JavaScript. "
          "Currently disabled as it breaks background JavaScript POSTs in "
          "OMERO.web.")],

    "omero.web.logdir":
        ["LOGDIR", LOGDIR, str_plain, "A path to the custom log directory."],
    "omero.web.secure_proxy_ssl_header":
        ["SECURE_PROXY_SSL_HEADER",
         '[]',
         parse_json,
         ("A tuple representing a HTTP header/value combination that "
          "signifies a request is secure. Example "
          "``'[\"HTTP_X_FORWARDED_PROTO_OMERO_WEB\", \"https\"]'``. "
          "For more details see :djangodoc:`secure proxy ssl header <ref/"
          "settings/#secure-proxy-ssl-header>`.")],
    "omero.web.wsgi_args":
        ["WSGI_ARGS",
         None,
         leave_none_unset,
         ("A string representing Gunicorn additional arguments. "
          "Check Gunicorn Documentation "
          "https://docs.gunicorn.org/en/latest/settings.html")],
    "omero.web.wsgi_workers":
        ["WSGI_WORKERS",
         5,
         int_plain,
         ("The number of worker processes for handling requests. "
          "Check Gunicorn Documentation "
          "https://docs.gunicorn.org/en/stable/settings.html#workers")],
    "omero.web.wsgi_timeout":
        ["WSGI_TIMEOUT",
         60,
         int_plain,
         ("Workers silent for more than this many seconds are killed "
          "and restarted. Check Gunicorn Documentation "
          "https://docs.gunicorn.org/en/stable/settings.html#timeout")],

    # Public user
    "omero.web.public.enabled":
        ["PUBLIC_ENABLED",
         "false",
         parse_boolean,
         "Enable and disable the OMERO.web public user functionality."],
    "omero.web.public.url_filter":
        ["PUBLIC_URL_FILTER",
         r'(?#This regular expression matches nothing)a^',
         str_compile,
         ("Set a regular expression that matches URLs the public user is "
          "allowed to access. If this is not set, no URLs will be "
          "publicly available.")],
    "omero.web.public.get_only":
        ["PUBLIC_GET_ONLY",
         "true",
         parse_boolean,
         "Restrict public users to GET requests only"],
    "omero.web.public.server_id":
        ["PUBLIC_SERVER_ID",
         1,
         int_plain,
         "Server to authenticate against."],
    "omero.web.public.user":
        ["PUBLIC_USER",
         None,
         leave_none_unset,
         "Username to use during authentication."],
    "omero.web.public.password":
        ["PUBLIC_PASSWORD",
         None,
         leave_none_unset,
         "Password to use during authentication."],
    "omero.web.public.cache.enabled":
        ["PUBLIC_CACHE_ENABLED", "false", parse_boolean, None],
    "omero.web.public.cache.key":
        ["PUBLIC_CACHE_KEY", "omero.web.public.cache.key", str_plain, None],
    "omero.web.public.cache.timeout":
        ["PUBLIC_CACHE_TIMEOUT", 60 * 60 * 24, int_plain, None],

    # Social media integration
    "omero.web.sharing.twitter":
        ["SHARING_TWITTER",
         '{}',
         parse_json,
         ("Dictionary of `server-name: @twitter-site-username`, where "
          "server-name matches a name from `omero.web.server_list`. "
          "For example: ``'{\"omero\": \"@openmicroscopy\"}'``")],
    "omero.web.sharing.opengraph":
        ["SHARING_OPENGRAPH",
         '{}',
         parse_json,
         ("Dictionary of `server-name: site-name`, where "
          "server-name matches a name from `omero.web.server_list`. "
          "For example: ``'{\"omero\": \"Open Microscopy\"}'``")],

    # Application configuration
    "omero.web.server_list":
        ["SERVER_LIST",
         '[["%s", 4064, "omero"]]' % CUSTOM_HOST,
         parse_json,
         "A list of servers the Web client can connect to."],
    "omero.web.ping_interval":
        ["PING_INTERVAL",
         60000,
         int_plain,
         "Timeout interval between ping invocations in seconds"],
    "omero.web.chunk_size":
        ["CHUNK_SIZE",
         1048576,
         int_plain,
         "Size, in bytes, of the “chunk”"],
    "omero.web.webgateway_cache":
        ["WEBGATEWAY_CACHE", None, leave_none_unset, None],

    # VIEWER
    "omero.web.viewer.view":
        ["VIEWER_VIEW",
         'omeroweb.webclient.views.image_viewer',
         str_plain,
         ("Django view which handles display of, or redirection to, the "
          "desired full image viewer.")],

    # OPEN WITH
    "omero.web.open_with":
        ["OPEN_WITH",
         ('[["Image viewer", "webgateway", {"supported_objects": ["image"],'
          '"script_url": "webclient/javascript/ome.openwith_viewer.js"}]]'),
         parse_json,
         ("A list of viewers that can be used to display selected Images "
          "or other objects. Each viewer is defined as "
          "``[\"Name\", \"url\", options]``. Url is reverse(url). "
          "Selected objects are added to the url as ?image=:1&image=2"
          "Objects supported must be specified in options with "
          "e.g. ``{\"supported_objects\":[\"images\"]}`` "
          "to enable viewer for one or more images.")],

    # PIPELINE 1.3.20

    # Pipeline is an asset packaging library for Django, providing both CSS
    # and JavaScript concatenation and compression, built-in JavaScript
    # template support, and optional data-URI image and font embedding.
    "omero.web.pipeline_js_compressor":
        ["PIPELINE_JS_COMPRESSOR",
         None,
         identity,
         ("Compressor class to be applied to JavaScript files. If empty or "
          "None, JavaScript files won't be compressed.")],
    "omero.web.pipeline_css_compressor":
        ["PIPELINE_CSS_COMPRESSOR",
         None,
         identity,
         ("Compressor class to be applied to CSS files. If empty or None,"
          " CSS files won't be compressed.")],
    "omero.web.pipeline_staticfile_storage":
        ["STATICFILES_STORAGE",
         "pipeline.storage.PipelineStorage",
         str_plain,
         ("The file storage engine to use when collecting static files with"
          " the collectstatic management command. See `the documentation "
          "<https://django-pipeline.readthedocs.org/en/latest/storages.html>`_"
          " for more details.")],

    # Customisation
    "omero.web.login_logo":
        ["LOGIN_LOGO",
         None,
         leave_none_unset,
         ("Customize webclient login page with your own logo. Logo images "
          "should ideally be 150 pixels high or less and will appear above "
          "the OMERO logo. You will need to host the image somewhere else "
          "and link to it with the OMERO logo.")],
    "omero.web.login_view":
        ["LOGIN_VIEW",
         "weblogin",
         str_plain,
         ("The Django view name used for login. Use this to provide an "
          "alternative login workflow.")],
    "omero.web.login_incorrect_credentials_text":
        ["LOGIN_INCORRECT_CREDENTIALS_TEXT",
         "Connection not available, please check your user name and password.",
         str_plain,
         ("The error message shown to users who enter an incorrect username "
          "or password.")],

    "omero.web.top_logo":
        ["TOP_LOGO",
         "",
         str_plain,
         ("Customize the webclient top bar logo. The recommended image height "
          "is 23 pixels and it must be hosted outside of OMERO.web.")],
    "omero.web.top_logo_link":
        ["TOP_LOGO_LINK",
         "",
         str_plain,
         ("The target location of the webclient top logo, default unlinked.")],

    "omero.web.user_dropdown":
        ["USER_DROPDOWN",
         "true",
         parse_boolean,
         ("Whether or not to include a user dropdown in the base template."
          " Particularly useful when used in combination with the OMERO.web"
          " public user where logging in may not make sense.")],
    "omero.web.feedback.comment.enabled":
        ["FEEDBACK_COMMENT_ENABLED",
         "true",
         parse_boolean,
         ("Enable the feedback form for comments. "
          "These comments are sent to the URL in ``omero.qa.feedback`` "
          "(OME team by default).")],
    "omero.web.feedback.error.enabled":
        ["FEEDBACK_ERROR_ENABLED",
         "true",
         parse_boolean,
         ("Enable the feedback form for errors. "
          "These errors are sent to the URL in ``omero.qa.feedback`` "
          "(OME team by default).")],
    "omero.web.staticfile_dirs":
        ["STATICFILES_DIRS",
         '[]',
         parse_json,
         ("Defines the additional locations the staticfiles app will traverse"
          " if the FileSystemFinder finder is enabled, e.g. if you use the"
          " collectstatic or findstatic management command or use the static"
          " file serving view.")],
    "omero.web.template_dirs":
        ["TEMPLATE_DIRS",
         '[]',
         parse_json,
         ("List of locations of the template source files, in search order. "
          "Note that these paths should use Unix-style forward slashes.")],
    "omero.web.index_template":
        ["INDEX_TEMPLATE",
         None,
         identity,
         ("Define template used as an index page ``http://your_host/omero/``."
          "If None user is automatically redirected to the login page."
          "For example use 'webclient/index.html'. ")],
    "omero.web.base_include_template":
        ["BASE_INCLUDE_TEMPLATE",
         None,
         identity,
         ("Template to be included in every page, at the end of the <body>")],
    "omero.web.login_redirect":
        ["LOGIN_REDIRECT",
         '{}',
         parse_json,
         ("Redirect to the given location after logging in. It only supports "
          "arguments for :djangodoc:`Django reverse function"
          " <ref/urlresolvers/#django.core.urlresolvers.reverse>`. "
          "For example: ``'{\"redirect\": [\"webindex\"], \"viewname\":"
          " \"load_template\", \"args\":[\"userdata\"], \"query_string\":"
          " {\"experimenter\": -1}}'``")],

    "omero.web.login.show_client_downloads":
        ["SHOW_CLIENT_DOWNLOADS",
         "true",
         parse_boolean,
         ("Whether to link to official client downloads on the login page")],
    "omero.web.login.client_downloads_base":
        ["CLIENT_DOWNLOAD_GITHUB_REPO",
         'ome/omero-insight',
         str_plain,
         ("GitHub repository containing the Desktop client downloads")],

    "omero.web.apps":
        ["ADDITIONAL_APPS",
         '[]',
         parse_json,
         ("Add additional Django applications. For example, see"
          " :doc:`/developers/Web/CreateApp`")],
    "omero.web.databases":
        ["DATABASES", '{}', parse_json, None],
    "omero.web.page_size":
        ["PAGE",
         200,
         int_plain,
         ("Number of images displayed within a dataset or 'orphaned'"
          " container to prevent from loading them all at once.")],
    "omero.web.thumbnails_batch":
        ["THUMBNAILS_BATCH",
         50,
         int_plain,
         ("Number of thumbnails retrieved to prevent from loading them"
          " all at once. Make sure the size is not too big, otherwise"
          " you may exceed limit request line, see"
          " https://docs.gunicorn.org/en/latest/settings.html"
          "?highlight=limit_request_line")],
    "omero.web.ui.top_links":
        ["TOP_LINKS",
         ('['
          '["Data", "webindex", {"title": "Browse Data via Projects, Tags'
          ' etc"}],'
          '["History", "history", {"title": "History"}],'
          '["Help", "https://help.openmicroscopy.org/",'
          '{"title":"Open OMERO user guide in a new tab", "target":"new"}]'
          ']'),
         parse_json,
         ("Add links to the top header: links are ``['Link Text', "
          "'link|lookup_view', options]``, where the url is reverse('link'), "
          "simply 'link' (for external urls) or lookup_view is a detailed "
          "dictionary {\"viewname\": \"str\", \"args\": [], \"query_string\": "
          "{\"param\": \"value\" }], "
          "E.g. ``'[\"Webtest\", \"webtest_index\"] or [\"Homepage\","
          " \"http://...\", {\"title\": \"Homepage\", \"target\": \"new\"}"
          " ] or [\"Repository\", {\"viewname\": \"webindex\", "
          "\"query_string\": {\"experimenter\": -1}}, "
          "{\"title\": \"Repo\"}]'``")],
    "omero.web.ui.metadata_panes":
        ["METADATA_PANES",
         ('['
          '{"name": "tag", "label": "Tags", "index": 1},'
          '{"name": "map", "label": "Key-Value Pairs", "index": 2},'
          '{"name": "table", "label": "Tables", "index": 3},'
          '{"name": "file", "label": "Attachments", "index": 4},'
          '{"name": "comment", "label": "Comments", "index": 5},'
          '{"name": "rating", "label": "Ratings", "index": 6},'
          '{"name": "other", "label": "Others", "index": 7}'
          ']'),
         parse_json,
         ("Manage Metadata pane accordion. This functionality is limited to"
          " the existing sections.")],
    "omero.web.ui.right_plugins":
        ["RIGHT_PLUGINS",
         ('[["Acquisition",'
          ' "webclient/data/includes/right_plugin.acquisition.js.html",'
          ' "metadata_tab"],'
          # '["ROIs", "webtest/webclient_plugins/right_plugin.rois.js.html",
          # "image_roi_tab"],'
          '["Preview", "webclient/data/includes/right_plugin.preview.js.html"'
          ', "preview_tab"]]'),
         parse_json,
         ("Add plugins to the right-hand panel. "
          "Plugins are ``['Label', 'include.js', 'div_id']``. "
          "The javascript loads data into ``$('#div_id')``.")],
    "omero.web.ui.center_plugins":
        ["CENTER_PLUGINS",
         ('['
          # '["Split View",
          # "webtest/webclient_plugins/center_plugin.splitview.js.html",
          # "split_view_panel"],'
          ']'),
         parse_json,
         ("Add plugins to the center panels. Plugins are "
          "``['Channel overlay',"
          " 'webtest/webclient_plugins/center_plugin.overlay.js.html',"
          " 'channel_overlay_panel']``. "
          "The javascript loads data into ``$('#div_id')``.")],

    # CORS
    "omero.web.cors_origin_whitelist":
        ["CORS_ORIGIN_WHITELIST",
         '[]',
         parse_json,
         ("A list of origin hostnames that are authorized to make cross-site "
          "HTTP requests. "
          "Used by the django-cors-headers app as described at "
          "https://github.com/ottoyiu/django-cors-headers")],
    "omero.web.cors_origin_allow_all":
        ["CORS_ORIGIN_ALLOW_ALL",
         "false",
         parse_boolean,
         ("If True, cors_origin_whitelist will not be used and all origins "
          "will be authorized to make cross-site HTTP requests.")],

    "omero.web.x_frame_options":
        ["X_FRAME_OPTIONS",
         "SAMEORIGIN",
         str_plain,
         "Whether to allow OMERO.web to be loaded in a frame."
         ],

    "omero.web.django_additional_settings":
        ["DJANGO_ADDITIONAL_SETTINGS",
         "[]",
         parse_json,
         ("Additional Django settings as list of key-value tuples. "
          "Use this to set or override Django settings that aren't managed by "
          "OMERO.web. E.g. ``[\"CUSTOM_KEY\", \"CUSTOM_VALUE\"]``")],
}

DEPRECATED_SETTINGS_MAPPINGS = {
    # Deprecated settings, description should indicate the replacement.
    "omero.web.force_script_name":
        ["FORCE_SCRIPT_NAME",
         None,
         leave_none_unset,
         ("Use omero.web.prefix instead.")],
    "omero.web.server_email":
        ["SERVER_EMAIL",
         None,
         identity,
         ("Use omero.mail.from instead.")],
    "omero.web.email_host":
        ["EMAIL_HOST",
         None,
         identity,
         ("Use omero.mail.host instead.")],
    "omero.web.email_host_password":
        ["EMAIL_HOST_PASSWORD",
         None,
         identity,
         ("Use omero.mail.password instead.")],
    "omero.web.email_host_user":
        ["EMAIL_HOST_USER",
         None,
         identity,
         ("Use omero.mail.username instead.")],
    "omero.web.email_port":
        ["EMAIL_PORT",
         None,
         identity,
         ("Use omero.mail.port instead.")],
    "omero.web.email_subject_prefix":
        ["EMAIL_SUBJECT_PREFIX",
         "[OMERO.web]",
         str_plain,
         ("Default email subject is no longer configurable.")],
    "omero.web.email_use_tls":
        ["EMAIL_USE_TLS",
         "false",
         parse_boolean,
         ("Use omero.mail.smtp.* instead to set up"
          " javax.mail.Session properties.")],
    "omero.web.plate_download.enabled":
        ["PLATE_DOWNLOAD_ENABLED",
         "false",
         parse_boolean,
         ("Use omero.policy.binary_access instead to restrict download.")],
    "omero.web.viewer.initial_zoom_level":
        ["VIEWER_INITIAL_ZOOM_LEVEL",
         None,
         leave_none_unset_int,
         ("Use omero.client.viewer.initial_zoom_level instead.")],
    "omero.web.send_broken_link_emails":
        ["SEND_BROKEN_LINK_EMAILS",
         "false",
         parse_boolean,
         ("Replaced by django.middleware.common.BrokenLinkEmailsMiddleware."
          "To get notification set :property:`omero.web.admins` property.")
         ],
}

del CUSTOM_HOST


def check_worker_class(c, src=None):
    if c == "gevent":
        try:
            import gevent  # NOQA
        except ImportError:
            raise ImportError("You are using async workers based "
                              "on Greenlets via Gevent. Install gevent")
    return str(c)


def check_threading(t, src=None):
    if t > 1:
        try:
            import concurrent.futures  # NOQA
        except ImportError:
            raise ImportError("You are using sync workers with "
                              "multiple threads. Install futures")
    return int(t)


# DEVELOPMENT_SETTINGS_MAPPINGS - WARNING: For each setting developer MUST open
# a ticket that needs to be resolved before a release either by moving the
# setting to CUSTOM_SETTINGS_MAPPINGS or by removing the setting at all.
DEVELOPMENT_SETTINGS_MAPPINGS = {
    "omero.web.wsgi_worker_class":
        ["WSGI_WORKER_CLASS",
         "sync",
         check_worker_class,
         ("The default OMERO.web uses sync workers to handle most “normal” "
          "types of workloads. Check Gunicorn Design Documentation "
          "https://docs.gunicorn.org/en/stable/design.html")],
    "omero.web.wsgi_worker_connections":
        ["WSGI_WORKER_CONNECTIONS",
         1000,
         int_plain,
         ("(ASYNC WORKERS only) The maximum number of simultaneous clients. "
          "Check Gunicorn Documentation https://docs.gunicorn.org"
          "/en/stable/settings.html#worker-connections")],
    "omero.web.wsgi_threads":
        ["WSGI_THREADS",
         1,
         check_threading,
         ("(SYNC WORKERS only) The number of worker threads for handling "
          "requests. Check Gunicorn Documentation "
          "https://docs.gunicorn.org/en/stable/settings.html#threads")],
}


def map_deprecated_settings(settings):
    m = {}
    for key, values in settings.items():
        try:
            global_name = values[0]
            m[global_name] = (CUSTOM_SETTINGS[key], key)
            if len(values) < 5:
                # Not using default (see process_custom_settings)
                values.append(False)
        except KeyError:
            if len(values) < 5:
                values.append(True)
    return m


def _apply_mapping(value, src, mapping, key):
    """
    Applies the mapping function for an OMERO.web setting.
    This attempts to handle both new mapping functions (value, src) and old
    (value).
    Old mapping functions only work with config.xml settings, they do not
    support new JSON settings.

    :param value: The raw config value
    :param src str: The source of the value
    :param mapping function: The mapping function
    :param key str: The key name, used to log a deprecation message if an old
           mapping function is found
    :return (value, unset):
            value: The mapped value
            unset: If True the property should be left unset
    """
    try:
        argspec = inspect.getargspec(mapping)
        isold = len(argspec.args) < 2
    except TypeError:
        # E.g. int(), str()
        isold = True
    if isold:
        logger.warn(
            'Setting %s uses a deprecated mapping function %s',
            key, mapping.__name__)
        if src not in ('default', 'xml'):
            raise ValueError(
                'Deprecated mapping function cannot be used with JSON '
                'configuration for key %s' % key)
        try:
            return mapping(value), False
        except LeaveUnset:
            return None, True
    try:
        return mapping(value, src), False
    except LeaveUnset:
        return None, True


def lookup_web_config(key, default_value=None, mapping=identity):
    """
    Lookup an omero config property as seen by OMERO.web, taking into account
    the config.xml and and config JSON files.

    :param key str: The omero config property name
    :param default_value: The default value of the property
    :param mapping func: The mapping function(value, src) to convert the
           property to the OMERO.web value

    :return (value, src, unset):
            value: the  value of the property taking into account the
            default_value and mapping parameters
            src: whether the property is the 'default', from the 'xml' config,
            or from the 'json', config
            unset: If True the property should be left unset, e.g. it should
            inherit the default Django value
    """
    if (key in CUSTOM_SETTINGS_JSON_SET or
            key in CUSTOM_SETTINGS_JSON_APPEND):
        try:
            global_value = CUSTOM_SETTINGS_JSON_SET[key]
            src = 'json'
        except KeyError:
            global_value = default_value
            src = 'default'
        global_value, unset = _apply_mapping(global_value, src, mapping, key)
        if key in CUSTOM_SETTINGS_JSON_APPEND:
            src = 'json'
            try:
                global_value.extend(CUSTOM_SETTINGS_JSON_APPEND[key])
            except AttributeError:
                global_value.update(CUSTOM_SETTINGS_JSON_APPEND[key])
            global_value, unset = _apply_mapping(
                global_value, src, mapping, key)
    else:
        try:
            src = 'xml'
            global_value = CUSTOM_SETTINGS[key]
        except KeyError:
            src = 'default'
            global_value = default_value
        global_value, unset = _apply_mapping(
            global_value, src, mapping, key)
    return global_value, src, unset


def process_custom_settings(
        module, settings='CUSTOM_SETTINGS_MAPPINGS', deprecated=None):
    logging.info('Processing custom settings for module %s' % module.__name__)

    if deprecated:
        deprecated_map = map_deprecated_settings(
            getattr(module, deprecated, {}))
    else:
        deprecated_map = {}

    for key, values in getattr(module, settings, {}).items():
        # Django may import settings.py more than once, see:
        # http://blog.dscpl.com.au/2010/03/improved-wsgi-script-for-use-with.html
        # In that case, the custom settings have already been processed.
        if len(values) == 5:
            continue

        global_name, default_value, mapping, description = values
        try:
            global_value, src, unset = lookup_web_config(
                key, default_value, mapping)
            values.append(src)

            if global_name in deprecated_map:
                dep_value, dep_key = deprecated_map[global_name]
                if src == 'default':
                    logging.warning(
                        'Setting %s is deprecated, use %s', dep_key, key)
                    global_value, unset = _apply_mapping(
                        global_value, src, mapping, dep_key)
                else:
                    logging.error(
                        '%s and its deprecated key %s are both set, using %s',
                        key, dep_key, key)
            if not unset:
                setattr(module, global_name, global_value)
        except ValueError, e:
            raise ValueError(
                "Invalid %s (%s = %r). %s. %s" %
                (global_name, key, global_value, e.message, description))
        except ImportError, e:
            raise ImportError(
                "ImportError: %s. %s (%s = %r).\n%s" %
                (e.message, global_name, key, global_value, description))


process_custom_settings(sys.modules[__name__], 'INTERNAL_SETTINGS_MAPPING')
process_custom_settings(sys.modules[__name__], 'CUSTOM_SETTINGS_MAPPINGS',
                        'DEPRECATED_SETTINGS_MAPPINGS')
process_custom_settings(sys.modules[__name__], 'DEVELOPMENT_SETTINGS_MAPPINGS')

if not DEBUG:  # from CUSTOM_SETTINGS_MAPPINGS  # noqa
    LOGGING['loggers']['django.request']['level'] = 'INFO'
    LOGGING['loggers']['django']['level'] = 'INFO'
    LOGGING['loggers']['']['level'] = 'INFO'


def report_settings(module):
    from django.views.debug import cleanse_setting
    custom_settings_mappings = getattr(module, 'CUSTOM_SETTINGS_MAPPINGS', {})
    for key in sorted(custom_settings_mappings):
        values = custom_settings_mappings[key]
        global_name, default_value, mapping, description, source = values
        if source != 'default':
            source = '%s:%s' % (source, key)
        global_value = getattr(module, global_name, None)
        if global_name.isupper():
            logger.debug(
                "%s = %r (source:%s)", global_name,
                cleanse_setting(global_name, global_value), source)

    deprecated_settings = getattr(module, 'DEPRECATED_SETTINGS_MAPPINGS', {})
    for key in sorted(deprecated_settings):
        values = deprecated_settings[key]
        global_name, default_value, mapping, description, source = values
        global_value = getattr(module, global_name, None)
        if global_name.isupper() and source != 'default':
            logger.debug(
                "%s = %r (deprecated:%s, %s)", global_name,
                cleanse_setting(global_name, global_value), key, description)


report_settings(sys.modules[__name__])

SITE_ID = 1

# Local time zone for this installation. Choices can be found here:
# http://www.postgresql.org/docs/8.1/static/datetime-keywords.html#DATETIME-TIMEZONE-SET-TABLE
# although not all variations may be possible on all operating systems.
TIME_ZONE = 'Europe/London'
FIRST_DAY_OF_WEEK = 0     # 0-Monday, ... 6-Sunday

# LANGUAGE_CODE: A string representing the language code for this
# installation. This should be in standard language format. For example, U.S.
# English is "en-us".
LANGUAGE_CODE = 'en-gb'

# SECRET_KEY: A secret key for this particular Django installation. Used to
# provide a seed in secret-key hashing algorithms. Set this to a random string,
# the longer, the better. Make this unique, and don't share it with anybody.
try:
    SECRET_KEY
except NameError:
    secret_path = os.path.join(OMERO_HOME, 'var',
                               'django_secret_key').replace('\\', '/')
    if not os.path.isfile(secret_path):
        try:
            secret_key = ''.join(
                [random.SystemRandom()
                 .choice("{0}{1}{2}"
                 .format(string.ascii_letters,
                         string.digits,
                         string.punctuation)) for i in range(50)]
            )
            with os.fdopen(os.open(secret_path,
                                   os.O_WRONLY | os.O_CREAT,
                                   0600), 'w') as secret_file:
                secret_file.write(secret_key)
        except IOError, e:
            raise IOError("Please create a %s file with random characters"
                          " to generate your secret key!" % secret_path)
    try:
        with open(secret_path, 'r') as secret_file:
            SECRET_KEY = secret_file.read().strip()
    except IOError, e:
        raise IOError("Could not find secret key in %s!" % secret_path)

# USE_I18N: A boolean that specifies whether Django's internationalization
# system should be enabled.
# This provides an easy way to turn it off, for performance. If this is set to
# False, Django will make some optimizations so as not to load the
# internationalization machinery.
USE_I18N = True

# ROOT_URLCONF: A string representing the full Python import path to your root
# URLconf.
# For example: "mydjangoapps.urls". Can be overridden on a per-request basis
# by setting the attribute urlconf on the incoming HttpRequest object.
ROOT_URLCONF = 'omeroweb.urls'

# STATICFILES_FINDERS: The list of finder backends that know how to find
# static files in various locations. The default will find files stored in the
# STATICFILES_DIRS setting (using
# django.contrib.staticfiles.finders.FileSystemFinder) and in a static
# subdirectory of each app (using
# django.contrib.staticfiles.finders.AppDirectoriesFinder)
STATICFILES_FINDERS = (
    "django.contrib.staticfiles.finders.FileSystemFinder",
    "django.contrib.staticfiles.finders.AppDirectoriesFinder",
)

# STATICFILES_DIRS: This setting defines the additional locations the
# staticfiles app will traverse if the FileSystemFinder finder is enabled,
# e.g. if you use the collectstatic or findstatic management command or use
# the static file serving view.
# from CUSTOM_SETTINGS_MAPPINGS
# STATICFILES_DIRS += (("webapp/custom_static", path/to/statics),)  # noqa

# TEMPLATES: A list containing the settings for all template engines
# to be used with Django. Each item of the list is a dictionary containing
# the options for an individual engine.
TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': TEMPLATE_DIRS,  # noqa
        'APP_DIRS': True,
        'OPTIONS': {
            'debug': DEBUG,  # noqa
            'context_processors': [
                # Insert your TEMPLATE_CONTEXT_PROCESSORS here or use this
                # list if you haven't customized them:
                'django.contrib.auth.context_processors.auth',
                'django.template.context_processors.debug',
                'django.template.context_processors.i18n',
                'django.template.context_processors.media',
                'django.template.context_processors.static',
                'django.template.context_processors.tz',
                'django.contrib.messages.context_processors.messages',
                'omeroweb.custom_context_processor.url_suffix',
                'omeroweb.custom_context_processor.base_include_template',
            ],
        },
    },
]

# INSTALLED_APPS: A tuple of strings designating all applications that are
# enabled in this Django installation. Each string should be a full Python
# path to a Python package that contains a Django application, as created by
# django-admin.py startapp.
INSTALLED_APPS = (
    'django.contrib.staticfiles',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
)

# ADDITONAL_APPS: We import any settings.py from apps. This allows them to
# modify settings.
# We're also processing any CUSTOM_SETTINGS_MAPPINGS defined there.
for app in ADDITIONAL_APPS:  # from CUSTOM_SETTINGS_MAPPINGS  # noqa
    # Previously the app was added to INSTALLED_APPS as 'omeroweb.app', which
    # then required the app to reside within or be symlinked from within
    # omeroweb, instead of just having to be somewhere on the python path.
    # To allow apps to just be on the path, but keep it backwards compatible,
    # try to import as omeroweb.app, if it works, keep that in INSTALLED_APPS,
    # otherwise add it to INSTALLED_APPS just with its own name.
    try:
        __import__('omeroweb.%s' % app)
        INSTALLED_APPS += ('omeroweb.%s' % app,)
    except ImportError:
        INSTALLED_APPS += (app,)
    try:
        logger.debug(
            'Attempting to import additional app settings for app: %s' % app)
        module = __import__('%s.settings' % app)
        process_custom_settings(module.settings)
        report_settings(module.settings)
    except ImportError:
        logger.debug("Couldn't import settings from app: %s" % app)

INSTALLED_APPS += (
    'omeroweb.feedback',
    'omeroweb.webadmin',
    'omeroweb.webclient',
    'omeroweb.webgateway',
    'omeroweb.webredirect',
    'omeroweb.api',
    'pipeline',
)

logger.debug('INSTALLED_APPS=%s' % [INSTALLED_APPS])


PIPELINE_CSS = {
    'webgateway_viewer': {
        'source_filenames': (
            'webgateway/css/reset.css',
            'webgateway/css/ome.body.css',
            'webclient/css/dusty.css',
            'webgateway/css/ome.viewport.css',
            'webgateway/css/ome.toolbar.css',
            'webgateway/css/ome.gs_slider.css',
            'webgateway/css/base.css',
            'webgateway/css/ome.snippet_header_logo.css',
            'webgateway/css/ome.postit.css',
            '3rdparty/farbtastic-1.2/farbtastic.css',
            'webgateway/css/ome.colorbtn.css',
            '3rdparty/JQuerySpinBtn-1.3a/JQuerySpinBtn.css',
            '3rdparty/jquery-ui-1.10.4/themes/base/jquery-ui.all.css',
            'webgateway/css/omero_image.css',
            '3rdparty/panojs-2.0.0/panojs.css',
        ),
        'output_filename': 'omeroweb.viewer.min.css',
    },
}

PIPELINE_JS = {
    'webgateway_viewer': {
        'source_filenames': (
            '3rdparty/jquery-1.11.1.js',
            '3rdparty/jquery-migrate-1.2.1.js',
            '3rdparty/jquery-ui-1.10.4/js/jquery-ui.1.10.4.js',
            'webgateway/js/ome.popup.js',
            '3rdparty/aop-1.3.js',
            '3rdparty/raphael-2.1.0/raphael.js',
            '3rdparty/raphael-2.1.0/scale.raphael.js',
            '3rdparty/panojs-2.0.0/utils.js',
            '3rdparty/panojs-2.0.0/PanoJS.js',
            '3rdparty/panojs-2.0.0/controls.js',
            '3rdparty/panojs-2.0.0/pyramid_Bisque.js',
            '3rdparty/panojs-2.0.0/pyramid_imgcnv.js',
            '3rdparty/panojs-2.0.0/pyramid_Zoomify.js',
            '3rdparty/panojs-2.0.0/control_thumbnail.js',
            '3rdparty/panojs-2.0.0/control_info.js',
            '3rdparty/panojs-2.0.0/control_svg.js',
            '3rdparty/panojs-2.0.0/control_roi.js',
            '3rdparty/panojs-2.0.0/control_scalebar.js',
            '3rdparty/hammer-2.0.2/hammer.min.js',
            'webgateway/js/ome.gs_utils.js',
            'webgateway/js/ome.viewportImage.js',
            'webgateway/js/ome.gs_slider.js',
            'webgateway/js/ome.viewport.js',
            'webgateway/js/omero_image.js',
            'webgateway/js/ome.roidisplay.js',
            'webgateway/js/ome.scalebardisplay.js',
            'webgateway/js/ome.smartdialog.js',
            'webgateway/js/ome.roiutils.js',
            '3rdparty/JQuerySpinBtn-1.3a/JQuerySpinBtn.js',
            'webgateway/js/ome.colorbtn.js',
            'webgateway/js/ome.postit.js',
            '3rdparty/jquery.selectboxes-2.2.6.js',
            '3rdparty/farbtastic-1.2/farbtastic.js',
            '3rdparty/jquery.mousewheel-3.0.6.js',
        ),
        'output_filename': 'omeroweb.viewer.min.js',
    }
}

# Prevent scripting attacks from obtaining session cookie
SESSION_COOKIE_HTTPONLY = True

CSRF_FAILURE_VIEW = "omeroweb.feedback.views.csrf_failure"

# Configuration for django-cors-headers app
# See https://github.com/ottoyiu/django-cors-headers
# Configration of allowed origins is handled by custom settings above
CORS_ALLOW_CREDENTIALS = True
# Needed for Django <1.9 since CSRF_TRUSTED_ORIGINS not supported
CORS_REPLACE_HTTPS_REFERER = True

# FEEDBACK - DO NOT MODIFY!
# FEEDBACK_URL: Is now configurable for testing purpuse only. Used in
# feedback.sendfeedback.SendFeedback class in order to submit errors or
# comment messages to http://qa.openmicroscopy.org.uk.
# FEEDBACK_APP: 6 = OMERO.web
FEEDBACK_APP = 6

# IGNORABLE_404_STARTS:
# Default: ('/cgi-bin/', '/_vti_bin', '/_vti_inf')
# IGNORABLE_404_ENDS:
# Default: ('mail.pl', 'mailform.pl', 'mail.cgi', 'mailform.cgi',
# 'favicon.ico', '.php')

# SESSION_FILE_PATH: If you're using file-based session storage, this sets the
# directory in which Django will store session data. When the default value
# (None) is used, Django will use the standard temporary directory for the
# system.
SESSION_FILE_PATH = tempfile.gettempdir()

# FILE_UPLOAD_TEMP_DIR: The directory to store data temporarily while
# uploading files.
FILE_UPLOAD_TEMP_DIR = tempfile.gettempdir()

# # FILE_UPLOAD_MAX_MEMORY_SIZE: The maximum size (in bytes) that an upload
# will be before it gets streamed to the file system.
FILE_UPLOAD_MAX_MEMORY_SIZE = 2621440  # default 2621440 (i.e. 2.5 MB).

# DEFAULT_IMG: Used in
# webclient.webclient_gateway.OmeroWebGateway.defaultThumbnail in order to
# load default image while thumbnail can't be retrieved from the server.
DEFAULT_IMG = os.path.join(
    os.path.dirname(__file__), 'webgateway', 'static', 'webgateway', 'img',
    'image128.png').replace('\\', '/')

# # DEFAULT_USER: Used in
# webclient.webclient_gateway.OmeroWebGateway.getExperimenterDefaultPhoto in
# order to load default avatar while experimenter photo can't be retrieved
# from the server.
DEFAULT_USER = os.path.join(
    os.path.dirname(__file__), 'webgateway', 'static', 'webgateway', 'img',
    'personal32.png').replace('\\', '/')

# MANAGERS: A tuple in the same format as ADMINS that specifies who should get
# broken-link notifications when
# SEND_BROKEN_LINK_EMAILS=True.
MANAGERS = ADMINS  # from CUSTOM_SETTINGS_MAPPINGS  # noqa

# https://docs.djangoproject.com/en/1.6/releases/1.6/#default-session-serialization-switched-to-json
# JSON serializer, which is now the default, cannot handle
# omeroweb.connector.Connector object
SESSION_SERIALIZER = 'django.contrib.sessions.serializers.PickleSerializer'

# Load custom settings from etc/grid/config.xml
# Tue  2 Nov 2010 11:03:18 GMT -- ticket:3228
# MIDDLEWARE_CLASSES: A tuple of middleware classes to use.
MIDDLEWARE_CLASSES = sort_properties_to_tuple(MIDDLEWARE_CLASSES_LIST)  # noqa

for k, v in DJANGO_ADDITIONAL_SETTINGS:  # noqa
    setattr(sys.modules[__name__], k, v)


# Load server list and freeze
def load_server_list():
    for s in SERVER_LIST:  # from CUSTOM_SETTINGS_MAPPINGS  # noqa
        server = (len(s) > 2) and unicode(s[2]) or None
        Server(host=unicode(s[0]), port=int(s[1]), server=server)
    Server.freeze()


load_server_list()
