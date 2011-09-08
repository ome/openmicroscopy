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

from webadmin.custom_models import ServerObjects
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
DEFAULT_CACHE_DIR = os.path.join(OMERO_HOME, 'var', 'cache', 'webgateway').replace('\\','/')

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
    "omero.web.application_host": ["APPLICATION_HOST", "http://localhost:4080", remove_slash],
    "omero.web.application_server": ["APPLICATION_SERVER", DEFAULT_SERVER_TYPE, check_server_type],
    "omero.web.application_server.host": ["APPLICATION_SERVER_HOST", "0.0.0.0", str],
    "omero.web.application_server.port": ["APPLICATION_SERVER_PORT", "4080", str],
    "omero.web.cache_backend": ["CACHE_BACKEND", None, leave_none_unset],
    "omero.web.webgateway_cache": ["WEBGATEWAY_CACHE", DEFAULT_CACHE_DIR, leave_none_unset],
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
    "omero.web.scripts_to_ignore": ["SCRIPTS_TO_IGNORE", '["/omero/figure_scripts/Movie_Figure.py", "/omero/figure_scripts/Split_View_Figure.py", "/omero/figure_scripts/Thumbnail_Figure.py", "/omero/figure_scripts/ROI_Split_Figure.py", "/omero/export_scripts/Make_Movie.py"]', json.loads],
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

TEMPLATE_DEBUG = DEBUG

# Configure logging and set place to store logs.
INTERNAL_IPS = ()
LOGGING_LOG_SQL = False

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

# Local time zone for this installation. Choices can be found here:
# http://www.postgresql.org/docs/8.1/appmedia/omeroweb/datetime-keywords.html#DATETIME-TIMEZONE-SET-TABLE
# although not all variations may be possible on all operating systems.
# If running in a Windows environment this must be set to the same as your
# system time zone.
TIME_ZONE = 'Europe/London'
FIRST_DAY_OF_WEEK = 0     # 0-Monday, ... 6-Sunday

# Language code for this installation. All choices can be found here:
# http://www.w3.org/TR/REC-html40/struct/dirlang.html#langcodes
# http://blogs.law.harvard.edu/tech/stories/storyReader$15
LANGUAGE_CODE = 'en-gb'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
MEDIA_ROOT = os.path.join(os.path.dirname(__file__), 'media')

# URL that handles the media served from MEDIA_ROOT.
# Example: "http://media.lawrence.com"
MEDIA_URL = '/appmedia/'

# URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
# trailing slash.
# Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PREFIX = '/admin_appmedia/omeroweb/'

# Make this unique, and don't share it with anybody.
SECRET_KEY = '@@k%g#7=%4b6ib7yr1tloma&g0s2nni6ljf!m0h&x9c712c7yj'

# List of callables that know how to import templates from various sources.
TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.load_template_source',
    'django.template.loaders.app_directories.load_template_source',
#     'django.template.loaders.eggs.load_template_source',
)

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.doc.XViewMiddleware',
)

ROOT_URLCONF = 'omeroweb.urls'

# Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
# Always use forward slashes, even on Windows.
# Don't forget to use absolute paths, not relative paths.
TEMPLATE_DIRS = (
    os.path.join(os.path.join(os.path.dirname(__file__), 'feedback'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webadmin'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'templates').replace('\\','/'),
    #os.path.join(os.path.join(os.path.dirname(__file__), 'webemdb'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webmobile'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webpublic'), 'templates').replace('\\','/'),
)

INSTALLED_APPS = (
    'django.contrib.admin',
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
)

FEEDBACK_URL = "qa.openmicroscopy.org.uk:80"

IGNORABLE_404_ENDS = ('*.ico')

# SESSION_ENGINE is now set by the bin/omero config infrastructure
SESSION_FILE_PATH = tempfile.gettempdir()

# Cookies config
SESSION_EXPIRE_AT_BROWSER_CLOSE = True # False
SESSION_COOKIE_AGE = 86400 # 1 day in sec (86400)

# file upload settings
FILE_UPLOAD_TEMP_DIR = tempfile.gettempdir()
FILE_UPLOAD_MAX_MEMORY_SIZE = 2621440 #default 2621440

DEFAULT_IMG = os.path.join(os.path.dirname(__file__), 'media', 'omeroweb', "images", 'image128.png').replace('\\','/')
DEFAULT_USER = os.path.join(os.path.dirname(__file__), 'media', 'omeroweb', "images", 'personal32.png').replace('\\','/')

# Pagination
try:
    PAGE
except:
    PAGE = 200

SERVER_LIST = ServerObjects(SERVER_LIST)

MANAGERS = ADMINS

EMAIL_TEMPLATES = {
    'create_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%s/webclient/public/?server=%i">%s/webclient/public/?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %s/webclient/public/?server=%i. /n %s'
    },
    'add_member_to_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%s/webclient/public/?server=%i">%s/webclient/public/?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %s/webclient/public/?server=%i. /n %s'
    },
    'remove_member_from_share': {
        'html_content':'<p>You were removed from the share <a href="%s/webclient/public/?server=%i">%s/webclient/public/?server=%i</a>. This share is no longer available for you.</p>',
        'text_content':'You were removed from the share %s/webclient/public/?server=%i. This share is no longer available for you.'
    },
    'add_comment_to_share': {
        'html_content':'<p>New comment is available on share <a href="%s/webclient/public/?server=%i">%s/webclient/public/?server=%i</a>.</p>',
        'text_content':'New comment is available on share %s/webclient/public/?server=%i.'
    }
}
