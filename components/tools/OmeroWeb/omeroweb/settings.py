#!/usr/bin/env python
# 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
# #                Django settings for OMERO.web project.               # # 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# 
# 
# Copyright (c) 2008 University of Dundee. 
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
import sys
import datetime
import logging
import omero
import omero.config
import omero.clients
import tempfile
import exceptions

from django.utils import simplejson as json
from portalocker import LockException

# LOGS
# NEVER DEPLOY a site into production with DEBUG turned on.
# Debuging mode.
# A boolean that turns on/off debug mode.
# handler404 and handler500 works only when False
if os.environ.has_key('OMERO_HOME'):
    OMERO_HOME =os.environ.get('OMERO_HOME') 
else:
    OMERO_HOME = os.path.join(os.path.dirname(__file__), '..', '..', '..')
    OMERO_HOME = os.path.normpath(OMERO_HOME)

LOGFILE = ('OMEROweb.log')
LOGLEVEL = logging.INFO
LOGDIR = os.path.join(OMERO_HOME, 'var', 'log').replace('\\','/')

if not os.path.isdir(LOGDIR):
    try:
        os.makedirs(LOGDIR)
    except Exception, x:
        exctype, value = sys.exc_info()[:2]
        raise exctype, value

import logconfig
logger = logconfig.get_logger(os.path.join(LOGDIR, LOGFILE), LOGLEVEL)

# Load custom settings from etc/grid/config.xml
# Tue  2 Nov 2010 11:03:18 GMT -- ticket:3228
from omero.util.concurrency import get_event
CONFIG_XML = os.path.join(OMERO_HOME, 'etc', 'grid', 'config.xml')
count = 10
event = get_event("websettings")

while True:
    try:
        CONFIG_XML = omero.config.ConfigXml(CONFIG_XML)
        CUSTOM_SETTINGS = CONFIG_XML.as_map()
        CONFIG_XML.close()
        break
    except LockException:
        logger.error("Exception while loading configuration retrying...", exc_info=True)
        count -= 1
        if not count:
            raise
        else:
            event.wait(1) # Wait a total of 10 seconds
    except:
        logger.error("Exception while loading configuration...", exc_info=True)
        raise

del event
del count
del get_event

FASTCGI = "fastcgi"
FASTCGITCP = "fastcgi-tcp"
FASTCGI_TYPES = (FASTCGI, FASTCGITCP)
DEVELOPMENT = "development"
DEFAULT_SERVER_TYPE = FASTCGITCP
ALL_SERVER_TYPES = (FASTCGITCP, FASTCGI, DEVELOPMENT)

DEFAULT_SESSION_ENGINE = 'django.contrib.sessions.backends.file'
SESSION_ENGINE_VALUES = ('django.contrib.sessions.backends.db',
                         'django.contrib.sessions.backends.file',
                         'django.contrib.sessions.backends.cache',
                         'django.contrib.sessions.backends.cached_db')

def parse_boolean(s):
    s = s.strip().lower()
    if s in ('true', '1', 't'):
        return True
    return False

def parse_paths(s):
    return [os.path.normpath(path) for path in json.loads(s)]

def check_server_type(s):
    if s not in ALL_SERVER_TYPES:
        raise ValueError("Unknown server type: %s. Valid values are: %s" % (s, ALL_SERVER_TYPES))
    return s

def check_session_engine(s):
    if s not in SESSION_ENGINE_VALUES:
        raise ValueError("Unknown session engine: %s. Valid values are: %s" % (s, SESSION_ENGINE_VALUES))
    return s

def identity(x):
    return x

def remove_slash(s):
    if s is not None and len(s) > 0:
        if s.endswith("/"):
            s = s[:-1]
    return s

class LeaveUnset(exceptions.Exception):
    pass

def leave_none_unset(s):
    if s is None:
        raise LeaveUnset()
    return s

CUSTOM_SETTINGS_MAPPINGS = {
    "omero.web.public.user": ["PUBLIC_USER", None, leave_none_unset],
    "omero.web.public.password": ["PUBLIC_PASSWORD", None, leave_none_unset],
    "omero.web.database_engine": ["DATABASE_ENGINE", None, leave_none_unset],
    "omero.web.database_host": ["DATABASE_HOST", None, leave_none_unset],
    "omero.web.database_name": ["DATABASE_NAME", None, leave_none_unset],
    "omero.web.database_password": ["DATABASE_PASSWORD", None, leave_none_unset],
    "omero.web.database_port": ["DATABASE_PORT", None, leave_none_unset],
    "omero.web.database_user": ["DATABASE_USER", None, leave_none_unset],
    "omero.web.admins": ["ADMINS", '[]', json.loads],
    "omero.web.application_server": ["APPLICATION_SERVER", DEFAULT_SERVER_TYPE, check_server_type],
    "omero.web.application_server.host": ["APPLICATION_SERVER_HOST", "0.0.0.0", str],
    "omero.web.application_server.port": ["APPLICATION_SERVER_PORT", "4080", str],
    "omero.web.static_url": ["STATIC_URL", "/static/", str],
    "omero.web.cache_backend": ["CACHE_BACKEND", None, leave_none_unset],
    "omero.web.webgateway_cache": ["WEBGATEWAY_CACHE", None, leave_none_unset],
    "omero.web.session_engine": ["SESSION_ENGINE", DEFAULT_SESSION_ENGINE, check_session_engine],
    "omero.web.debug": ["DEBUG", "false", parse_boolean],
    "omero.web.email_host": ["EMAIL_HOST", None, identity],
    "omero.web.email_host_password": ["EMAIL_HOST_PASSWORD", None, identity],
    "omero.web.email_host_user": ["EMAIL_HOST_USER", None, identity],
    "omero.web.email_port": ["EMAIL_PORT", None, identity],
    "omero.web.email_subject_prefix": ["EMAIL_SUBJECT_PREFIX", "[OMERO.web] ", str],
    "omero.web.email_use_tls": ["EMAIL_USE_TLS", "false", parse_boolean],
    "omero.web.logdir": ["LOGDIR", LOGDIR, str],
    "omero.web.send_broken_link_emails": ["SEND_BROKEN_LINK_EMAILS", "true", parse_boolean],
    "omero.web.server_email": ["SERVER_EMAIL", None, identity],
    "omero.web.server_list": ["SERVER_LIST", '[["localhost", 4064, "omero"]]', json.loads],
    "omero.web.use_eman2": ["USE_EMAN2", "false", parse_boolean],
    # the following parameters configure when to show/hide the 'Volume viewer' icon in the Image metadata panel
    "omero.web.open_astex_max_side": ["OPEN_ASTEX_MAX_SIDE", 400, int],
    "omero.web.open_astex_min_side": ["OPEN_ASTEX_MIN_SIDE", 20, int],
    "omero.web.open_astex_max_voxels": ["OPEN_ASTEX_MAX_VOXELS", 27000000, int],  # 300 x 300 x 300
    "omero.web.scripts_to_ignore": ["SCRIPTS_TO_IGNORE", '["/omero/figure_scripts/Movie_Figure.py", "/omero/figure_scripts/Split_View_Figure.py", "/omero/figure_scripts/Thumbnail_Figure.py", "/omero/figure_scripts/ROI_Split_Figure.py", "/omero/export_scripts/Make_Movie.py"]', parse_paths],
    
    # sharing no longer use this variable. replaced by request.build_absolute_uri
    # after testing this line should be removed.
    # "omero.web.application_host": ["APPLICATION_HOST", None, remove_slash], 
    
}

for key, values in CUSTOM_SETTINGS_MAPPINGS.items():

    global_name, default_value, mapping = values

    try:
        global_value = CUSTOM_SETTINGS[key]
        values.append(False)
    except KeyError:
        global_value = default_value
        values.append(True)

    try:
        globals()[global_name] = mapping(global_value)
    except ValueError:
        raise ValueError("Invalid %s JSON: %r" % (global_name, global_value))
    except LeaveUnset:
        pass

# TEMPLATE_DEBUG: A boolean that turns on/off template debug mode. If this is True, the fancy 
# error page will display a detailed report for any TemplateSyntaxError. This report contains 
# the relevant snippet of the template, with the appropriate line highlighted.
# Note that Django only displays fancy error pages if DEBUG is True, alternatively error 
# is handled by:
#    handler404 = "omeroweb.feedback.views.handler404"
#    handler500 = "omeroweb.feedback.views.handler500"
TEMPLATE_DEBUG = DEBUG

# DEBUG: Never deploy a site into production with DEBUG turned on.
# Logging levels: logging.DEBUG, logging.INFO, logging.WARNING, logging.ERROR logging.CRITICAL
if DEBUG:
    LOGLEVEL = logging.DEBUG
    logger.setLevel(LOGLEVEL)

for key in sorted(CUSTOM_SETTINGS_MAPPINGS):
    values = CUSTOM_SETTINGS_MAPPINGS[key]
    global_name, default_value, mapping, using_default = values
    source = using_default and "default" or key
    global_value = globals().get(global_name, "(unset)")
    if global_name.lower().find("password") < 0:
        logger.debug("%s = %r (source:%s)", global_name, global_value, source)
    else:
        logger.debug("%s = '***' (source:%s)", global_name, source)

# Local time zone for this installation. Choices can be found here:
# http://www.postgresql.org/docs/8.1/static/datetime-keywords.html#DATETIME-TIMEZONE-SET-TABLE
# although not all variations may be possible on all operating systems.
# If running in a Windows environment this must be set to the same as your
# system time zone.
TIME_ZONE = 'Europe/London'
FIRST_DAY_OF_WEEK = 0     # 0-Monday, ... 6-Sunday

# LANGUAGE_CODE: A string representing the language code for this installation. This should be
# in standard language format. For example, U.S. English is "en-us".
LANGUAGE_CODE = 'en-gb'

# FORCE_SCRIPT_NAME: This will be used as the value of the SCRIPT_NAME environment variable in any HTTP request. 
# This setting can be used to override the server-provided value of SCRIPT_NAME, which may be a rewritten 
# version of the preferred value or not supplied at all.
FORCE_SCRIPT_NAME = None

# SECRET_KEY: A secret key for this particular Django installation. Used to provide a seed 
# in secret-key hashing algorithms. Set this to a random string -- the longer, the better. 
# django-admin.py startproject creates one automatically. 
# Make this unique, and don't share it with anybody.
SECRET_KEY = '@@k%g#7=%4b6ib7yr1tloma&g0s2nni6ljf!m0h&x9c712c7yj'

# USE_I18N: A boolean that specifies whether Django's internationalization system should be enabled. 
# This provides an easy way to turn it off, for performance. If this is set to False, Django will
# make some optimizations so as not to load the internationalization machinery.
USE_I18N = True

# MIDDLEWARE_CLASSES: A tuple of middleware classes to use. 
# See https://docs.djangoproject.com/en/1.3/topics/http/middleware/.
MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    #'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
)


# ROOT_URLCONF: A string representing the full Python import path to your root URLconf. 
# For example: "mydjangoapps.urls". Can be overridden on a per-request basis by setting
# the attribute urlconf on the incoming HttpRequest object.
ROOT_URLCONF = 'omeroweb.urls'

# STATICFILES_FINDERS: The list of finder backends that know how to find static files 
# in various locations. The default will find files stored in the STATICFILES_DIRS setting 
# (using django.contrib.staticfiles.finders.FileSystemFinder) and in a static subdirectory 
# of each app (using django.contrib.staticfiles.finders.AppDirectoriesFinder)
STATICFILES_FINDERS = (
    "django.contrib.staticfiles.finders.FileSystemFinder",
    "django.contrib.staticfiles.finders.AppDirectoriesFinder"
)

# STATIC_URL: URL to use when referring to static files located in STATIC_ROOT. 
# Example: "/site_media/static/" or "http://static.example.com/".
# If not None, this will be used as the base path for media definitions and the staticfiles 
# app. It must end in a slash if set to a non-empty value.
# This var is configurable by omero.web.static_url STATIC_URL = '/static/'

# STATIC_ROOT: The absolute path to the directory where collectstatic will collect static 
# files for deployment. If the staticfiles contrib app is enabled (default) the collectstatic 
# management command will collect static files into this directory.
STATIC_ROOT = os.path.join(os.path.dirname(__file__), 'static').replace('\\','/')


# STATICFILES_DIRS: This setting defines the additional locations the staticfiles app will 
# traverse if the FileSystemFinder finder is enabled, e.g. if you use the collectstatic or 
# findstatic management command or use the static file serving view.
# STATICFILES_DIRS = ()


# TEMPLATE_CONTEXT_PROCESSORS: A tuple of callables that are used to populate the context 
# in RequestContext. These callables take a request object as their argument and return 
# a dictionary of items to be merged into the context.
TEMPLATE_CONTEXT_PROCESSORS = (
    "django.core.context_processors.debug",
    "django.core.context_processors.i18n",
    "django.core.context_processors.media",
    "django.core.context_processors.static",
    "django.contrib.messages.context_processors.messages"
)

# TEMPLATE_LOADERS: A tuple of template loader classes, specified as strings. Each Loader class 
# knows how to import templates from a particular source. Optionally, a tuple can be used 
# instead of a string. The first item in the tuple should be the Loader's module, subsequent items 
# are passed to the Loader during initialization.
TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.Loader',
    'django.template.loaders.app_directories.Loader',
)

# TEMPLATE_DIRS: List of locations of the template source files, in search order. Note that these 
# paths should use Unix-style forward slashes, even on Windows.
# Put strings here, like "/home/html/django_templates" or "C:/www/django/templates". Always use 
# forward slashes, even on Windows. Don't forget to use absolute paths, not relative paths.
TEMPLATE_DIRS = (
    os.path.join(os.path.join(os.path.dirname(__file__), 'feedback'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webadmin'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'templates').replace('\\','/'),
    #os.path.join(os.path.join(os.path.dirname(__file__), 'webemdb'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webmobile'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webpublic'), 'templates').replace('\\','/'),
)

# INSTALLED_APPS: A tuple of strings designating all applications that are enabled in this Django 
# installation. Each string should be a full Python path to a Python package that contains 
# a Django application, as created by django-admin.py startapp.
INSTALLED_APPS = (
    'django.contrib.staticfiles',
    'django.contrib.markup',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'omeroweb.feedback',
    'omeroweb.webadmin',
    'omeroweb.webclient',
    'omeroweb.webgateway',
    'omeroweb.webtest',
    #'omeroweb.webemdb',
    'omeroweb.webmobile',
    'omeroweb.webpublic',
    'omeroweb.webredirect',
    'omeroweb.common',
    
)

# FEEDBACK_URL: Used in feedback.sendfeedback.SendFeedback class in order to submit 
# error or comment messages to http://qa.openmicroscopy.org.uk.
FEEDBACK_URL = "qa.openmicroscopy.org.uk:80"

# IGNORABLE_404_STARTS: 
# Default: ('/cgi-bin/', '/_vti_bin', '/_vti_inf')
# IGNORABLE_404_ENDS: 
# Default: ('mail.pl', 'mailform.pl', 'mail.cgi', 'mailform.cgi', 'favicon.ico', '.php')

# SESSION_FILE_PATH: If you're using file-based session storage, this sets the directory in which Django
# will store session data. When the default value (None) is used, Django will use the standard temporary 
# directory for the system.
SESSION_FILE_PATH = tempfile.gettempdir()

# SESSION_EXPIRE_AT_BROWSER_CLOSE: Whether to expire the session when the user closes his or her browser.
SESSION_EXPIRE_AT_BROWSER_CLOSE = True # False

# SESSION_COOKIE_AGE: The age of session cookies, in seconds. See How to use sessions.
SESSION_COOKIE_AGE = 86400 # 1 day in sec (86400)

# FILE_UPLOAD_TEMP_DIR: The directory to store data temporarily while uploading files.
FILE_UPLOAD_TEMP_DIR = tempfile.gettempdir()

# # FILE_UPLOAD_MAX_MEMORY_SIZE: The maximum size (in bytes) that an upload will be before it gets streamed 
# to the file system.
FILE_UPLOAD_MAX_MEMORY_SIZE = 2621440 #default 2621440 (i.e. 2.5 MB).

# DEFAULT_IMG: Used in webclient.webclient_gateway.OmeroWebGateway.defaultThumbnail in order to load default
# image while thumbnail can't be retrieved from the server.
DEFAULT_IMG = os.path.join(os.path.dirname(__file__), 'common', 'static', 'common', 'image', 'image128.png').replace('\\','/')

# # DEFAULT_USER: Used in webclient.webclient_gateway.OmeroWebGateway.getExperimenterDefaultPhoto in order to load default
# avatar while experimenter photo can't be retrieved from the server.
DEFAULT_USER = os.path.join(os.path.dirname(__file__), 'common', 'static', 'common', 'image', 'personal32.png').replace('\\','/')

# MANAGERS: A tuple in the same format as ADMINS that specifies who should get broken-link notifications when 
# SEND_BROKEN_LINK_EMAILS=True.
MANAGERS = ADMINS

# PAGE: Used in varous locations where large number of data is retrieved from the server.
try:
    PAGE
except:
    PAGE = 200

EMAIL_TEMPLATES = {
    'create_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%s?server=%i">%s?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %s?server=%i. /n %s'
    },
    'add_member_to_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%s?server=%i">%s?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %s?server=%i. /n %s'
    },
    'remove_member_from_share': {
        'html_content':'<p>You were removed from the share <a href="%s?server=%i">%s?server=%i</a>. This share is no longer available for you.</p>',
        'text_content':'You were removed from the share %s?server=%i. This share is no longer available for you.'
    },
    'add_comment_to_share': {
        'html_content':'<p>New comment is available on share <a href="%s?server=%i">%s?server=%i</a>.</p>',
        'text_content':'New comment is available on share %s?server=%i.'
    }
}

# Load server list and freeze 
from webadmin.custom_models import Server
def load_server_list():
    for s in SERVER_LIST:
        server = (len(s) > 2) and unicode(s[2]) or None
        Server(host=unicode(s[0]), port=int(s[1]), server=server)
    Server.freeze()
load_server_list()

###
### OTHER APPLICATIONS:
###


###
### BEGIN EMDB settings
###
try:
    if USE_EMAN2:
        logger.info("Using EMAN2...")
        from EMAN2 import *
except:
    logger.info("Not using EMAN2...")
    pass
###
### END EMDB settings
###