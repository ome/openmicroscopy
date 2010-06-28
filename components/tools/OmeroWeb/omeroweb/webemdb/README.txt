For the purpose of this text, the following assumptions are made:
- DIST is the full path to the directory where you installed omero
- Only tested on Mac, so paths and path separators may be different
- You have omero and omeroweb fully installed and running

Using webtest as part of omeroweb:

- Create the app, we will be using the minimal webtest app for this.
- edit DIST/lib/python/omeroweb/settings.py, adding "omeroweb.webtest" to INSTALLED_APPS
- edit DIST/lib/python/omeroweb/urls.py, adding "(r'(?i)^webtest/', include('omeroweb.webtest.urls'))" to urlpatterns
- restart omeroweb
  $ cd DIST
  $ bin/omero admin ice server stop Web
  $ bin/omero admin ice server start Web

