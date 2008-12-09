import os.path
import datetime
import logging

# Django settings for webadmin project.
DEBUG = False # if True handler404 and handler500 works only when False
TEMPLATE_DEBUG = DEBUG

ADMINS = (
    # ('Your Name', 'your_email@domain.com'),
)

MANAGERS = ADMINS

# Database settings
DATABASE_ENGINE = 'sqlite3'    # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = 'db.sqlite3'   # Or path to database file if using sqlite3.
DATABASE_USER = ''             # Not used with sqlite3.
DATABASE_PASSWORD = ''         # Not used with sqlite3.
DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.

# Test database name
TEST_DATABASE_NAME = 'test-db.sqlite3'

# Admin error notification
# when is turn below parameters should be set, this option require DEBUG = False
EMAIL_NOTIFICATION = False
EMAIL_SENDER_ADDRESS = 'sender@domain' # email address
EMAIL_SMTP_SERVER = 'smtp.domain'

# Local time zone for this installation. Choices can be found here:
# http://www.postgresql.org/docs/8.1/static/datetime-keywords.html#DATETIME-TIMEZONE-SET-TABLE
# although not all variations may be possible on all operating systems.
# If running in a Windows environment this must be set to the same as your
# system time zone.
TIME_ZONE = 'Europe/London GB GB-Eire'
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
MEDIA_ROOT = ''

# URL that handles the media served from MEDIA_ROOT.
# Example: "http://media.lawrence.com"
MEDIA_URL = ''

# URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
# trailing slash.
# Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PREFIX = '/media/'

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
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.middleware.doc.XViewMiddleware',
)

ROOT_URLCONF = 'omeroweb.urls'

# Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
# Always use forward slashes, even on Windows.
# Don't forget to use absolute paths, not relative paths.
TEMPLATE_DIRS = (
    os.path.join(os.path.join(os.path.dirname(__file__), 'webadmin'), 'templates').replace('\\','/'),
    os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'templates').replace('\\','/'),
)

INSTALLED_APPS = (
    'django.contrib.admin',
    'django.contrib.markup',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'omeroweb.webadmin',
    'omeroweb.webclient',
)

# cookies config
SESSION_EXPIRE_AT_BROWSER_CLOSE = True # False
SESSION_COOKIE_AGE = 86400 # 1 day in sec

# file upload settings
FILE_UPLOAD_TEMP_DIR = '/tmp'
FILE_UPLOAD_MAX_MEMORY_SIZE = 100000 #default 2621440

# APPLICATIONS CONFIG

# BASE config
WEBADMIN_ROOT_BASE = 'webadmin'
WEBCLIENT_ROOT_BASE = 'webclient'

WEBCLIENT_STATIC_LOGO = os.path.join(os.path.join(os.path.dirname(__file__), 'media'), "images", 'logo.jpg').replace('\\','/')
STATIC_ROOT = os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'media').replace('\\','/')
STATIC_LOGO = os.path.join(os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'media'), "images", 'logo.jpg').replace('\\','/')
DEFAULT_IMG = os.path.join(os.path.join(os.path.join(os.path.dirname(__file__), 'webclient'), 'media'), "images", 'image128.png').replace('\\','/')

# LOGS
# to change the log place, please specify new path
LOGDIR = os.path.join(os.path.dirname(__file__), 'logs')
if not os.path.isdir(LOGDIR):
    try:
        os.mkdir(LOGDIR)
    except Exception, x:
        raise IOError("Error: Cannot create LOGDIR = %s" % (LOGDIR))

if DEBUG:
    DEBUGLOGFILE = ('debug-%s.log' % str(datetime.date.today()))
    # define a Handler which writes INFO messages or higher to the sys.stderr
    fileLog = logging.FileHandler(os.path.join(LOGDIR, DEBUGLOGFILE), 'w')
    fileLog.setLevel(logging.DEBUG)
    # set a format which is simpler for console use
    formatter = logging.Formatter('%(asctime)s %(name)-12s: %(levelname)-8s %(message)s')
    # tell the handler to use this format
    fileLog.setFormatter(formatter)
    # add the handler to the root logger
    logging.getLogger().addHandler(fileLog)
    logging.getLogger().setLevel(logging.DEBUG)
    
else:
    LOGFILE = ('info-%s.log' % str(datetime.datetime.now())) #datetime.date.today()
    logging.basicConfig(level=logging.INFO,
                        format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                        datefmt='%a, %d %b %Y %H:%M:%S',
                        filename=os.path.join(LOGDIR, LOGFILE),
                        filemode='w')

logging.info("Application Started...")

