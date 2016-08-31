from django.conf import settings

# This settings.py file will be imported AFTER settings
# have been initialised in omeroweb/settings.py

# We can directly manipulate the settings
# E.g. link to the 'top links' in webclient page header
# 'webgallery_index' is name in urls.py
settings.TOP_LINKS.append(["Gallery", "webgallery_index"])
