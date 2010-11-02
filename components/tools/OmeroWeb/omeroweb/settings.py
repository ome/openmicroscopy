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

from django.utils import simplejson as json
from portalocker import LockException

def parse_boolean(s):
    s = s.strip().lower()
    if s in ('true', '1', 't'):
        return True
    return False

OMERO_HOME = os.path.join(os.path.dirname(__file__), '..', '..', '..')
OMERO_HOME = os.path.normpath(OMERO_HOME)

# Load custom settings from etc/grid/config.xml
# Tue  2 Nov 2010 11:03:18 GMT -- ticket:3228
CONFIG_XML = os.path.join(OMERO_HOME, 'etc', 'grid', 'config.xml')
while True:
    try:
        CONFIG_XML = omero.config.ConfigXml(CONFIG_XML)
        CUSTOM_SETTINGS = CONFIG_XML.as_map()
        CONFIG_XML.close()
        break
    except LockException:
        pass

# LOGS
# NEVER DEPLOY a site into production with DEBUG turned on.

# Debuging mode. 
# A boolean that turns on/off debug mode.
# handler404 and handler500 works only when False

try:
    DEBUG=parse_boolean(CUSTOM_SETTINGS['omero.web.debug'])
except:
    DEBUG=False

TEMPLATE_DEBUG = DEBUG

# Configure logging and set place to store logs.
INTERNAL_IPS = ()
LOGGING_LOG_SQL = False

# LOG path
# Logging levels: logging.DEBUG, logging.INFO, logging.WARNING, logging.ERROR logging.CRITICAL
try:
    LOGDIR = CUSTOM_SETTINGS['omero.web.logdir']
except:
    LOGDIR = os.path.join(OMERO_HOME, 'var', 'log').replace('\\','/')

if DEBUG:  
    LOGFILE = ('OMEROweb-DEBUG.log')
    LOGLEVEL = logging.DEBUG
else:
    LOGFILE = ('OMEROweb.log')
    LOGLEVEL = logging.INFO
    
if not os.path.isdir(LOGDIR):
    try:
        os.makedirs(LOGDIR)
    except Exception, x:
        exctype, value = sys.exc_info()[:2]
        raise exctype, value

import logconfig
logger = logconfig.get_logger(os.path.join(LOGDIR, LOGFILE), LOGLEVEL)

logger.debug("OMERO config properties: " + repr(CUSTOM_SETTINGS))

try:
    ADMINS = json.loads(CUSTOM_SETTINGS['omero.web.admins'])
except:
    ADMINS = ()

logger.debug('ADMINS: ' + repr(ADMINS))

MANAGERS = ADMINS

###
### BEGIN EMDB settings
###
try:
    CACHE_BACKEND = CUSTOM_SETTINGS['omero.web.cache_backend']
    logger.debug("CACHE_BACKEND: " + CACHE_BACKEND)
except:
    pass

try:
    if parse_boolean(CUSTOM_SETTINGS['omero.web.use_eman2']):
        logger.info("Using EMAN2...")
        from eman2 import *
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
    'omeroweb.webemdb',
    'omeroweb.webmobile',
)

FEEDBACK_URL = "qa.openmicroscopy.org.uk:80"

IGNORABLE_404_ENDS = ('*.ico')

# Other option: "django.contrib.sessions.backends.cache_db"; "django.contrib.sessions.backends.cache"; "django.contrib.sessions.backends.file"
SESSION_ENGINE = "django.contrib.sessions.backends.file" 
SESSION_FILE_PATH = tempfile.gettempdir()

# Cookies config
SESSION_EXPIRE_AT_BROWSER_CLOSE = True # False
SESSION_COOKIE_AGE = 86400 # 1 day in sec (86400)

# file upload settings
FILE_UPLOAD_TEMP_DIR = '/tmp'
FILE_UPLOAD_MAX_MEMORY_SIZE = 2621440 #default 2621440

DEFAULT_IMG = os.path.join(os.path.dirname(__file__), 'media', 'omeroweb', "images", 'image128.png').replace('\\','/')
DEFAULT_USER = os.path.join(os.path.dirname(__file__), 'media', 'omeroweb', "images", 'personal32.png').replace('\\','/')

# Pagination
try:
    PAGE
except:
    PAGE = 24

# CUSTOM CONFIG
from webadmin.custom_models import ServerObjects
try:
    SERVER_LIST = json.loads(CUSTOM_SETTINGS['omero.web.server_list'])
except:
    SERVER_LIST = (('localhost', 4064, 'omero'),)
logger.debug('SERVER_LIST: ' + repr(SERVER_LIST))
SERVER_LIST = ServerObjects(SERVER_LIST)

try:
    EMAIL_HOST = CUSTOM_SETTINGS['omero.web.email_host']
    logger.debug('EMAIL_HOST: ' + repr(EMAIL_HOST))
except:
    pass
try:
    EMAIL_HOST_PASSWORD = CUSTOM_SETTINGS['omero.web.email_host_password']
    logger.debug('EMAIL_HOST_PASSWORD: ' + repr(EMAIL_HOST_PASSWORD))
except:
    pass
try:
    EMAIL_HOST_USER = CUSTOM_SETTINGS['omero.web.email_host_user']
    logger.debug('EMAIL_HOST_USER: ' + repr(EMAIL_HOST_USER))
except:
    pass
try:
    EMAIL_PORT = CUSTOM_SETTINGS['omero.web.email_port']
    logger.debug('EMAIL_PORT: ' + repr(EMAIL_PORT))
except:
    pass
try:
    EMAIL_SUBJECT_PREFIX = CUSTOM_SETTINGS['omero.web.email_subject_prefix']
    logger.debug('EMAIL_SUBJECT_PREFIX: ' + repr(EMAIL_SUBJECT_PREFIX))
except:
    pass
EMAIL_USE_TLS = False
try:
    EMAIL_USE_TLS = parse_boolean(CUSTOM_SETTINGS['omero.web.email_use_tls'])
except:
    pass
logger.debug('EMAIL_USE_TLS: ' + repr(EMAIL_USE_TLS))
try:
    SERVER_EMAIL = CUSTOM_SETTINGS['omero.web.server_email']
    logger.debug('SERVER_EMAIL: ' + repr(SERVER_EMAIL))
except:
    pass

SEND_BROKEN_LINK_EMAILS = True
EMAIL_SUBJECT_PREFIX = '[OMERO.web] '

###
### BEGIN Application host and server configuration
###

FASTCGI = "fastcgi"
FASTCGITCP = "fastcgi-tcp"
FASTCGI_TYPES = (FASTCGI, FASTCGITCP)
DEVELOPMENT = "development"
DEFAULT_SERVER_TYPE = FASTCGITCP
ALL_SERVER_TYPES = (FASTCGITCP, FASTCGI, DEVELOPMENT)

APPLICATION_HOST = 'http://localhost:80/'
try:
    APPLICATION_HOST = CUSTOM_SETTINGS['omero.web.application_host']
except:
    pass
if not APPLICATION_HOST.endswith("/"):
    APPLICATION_HOST=APPLICATION_HOST+"/"
logger.debug('APPLICATION_HOST: ' + repr(APPLICATION_HOST))

APPLICATION_SERVER = 'fastcgi-tcp'
try:
    APPLICATION_SERVER = CUSTOM_SETTINGS['omero.web.application_server']
except:
    pass
logger.debug('APPLICATION_SERVER: ' + repr(APPLICATION_SERVER))

APPLICATION_SERVER_HOST = '0.0.0.0'
try:
    APPLICATION_SERVER = CUSTOM_SETTINGS['omero.web.application_server.host']
except:
    pass
logger.debug('APPLICATION_SERVER_HOST: ' + repr(APPLICATION_SERVER_HOST))

APPLICATION_SERVER_PORT = '4080'
try:
    APPLICATION_SERVER = CUSTOM_SETTINGS['omero.web.application_server.port']
except:
    pass
logger.debug('APPLICATION_SERVER_PORT: ' + repr(APPLICATION_SERVER_PORT))
###
### END Application host and server configuration
###

EMAIL_TEMPLATES = {
    'create_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%swebclient/public/?server=%i">%swebclient/public/?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %swebclient/public/?server=%i. /n %s'
    },
    'add_member_to_share': {
        'html_content':'<p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href="%swebclient/public/?server=%i">%swebclient/public/?server=%i</a>.</p><p>%s</p>', 
        'text_content':'Hi, I would like to share some of my data with you. Please find it on the %swebclient/public/?server=%i. /n %s'
    },
    'remove_member_from_share': {
        'html_content':'<p>You were removed from the share <a href="%swebclient/public/?server=%i">%swebclient/public/?server=%i</a>. This share is no longer available for you.</p>',
        'text_content':'You were removed from the share %swebclient/public/?server=%i. This share is no longer available for you.'
    },
    'add_comment_to_share': {
        'html_content':'<p>New comment is available on share <a href="%swebclient/public/?server=%i">%swebclient/public/?server=%i</a>.</p>',
        'text_content':'New comment is available on share %swebclient/public/?server=%i.'
    }
}