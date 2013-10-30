
# This settings.py file will be imported by omero.settings file AFTER it has initialised custom settings.
from django.conf import settings

# We can directly manipulate the settings
# E.g. add links to TOP_LINKS list
settings.TOP_LINKS.append(["OMERO.Figure", "webfigure_index"])

# Don't want this script to show up in the webclient scripts menu
settings.SCRIPTS_TO_IGNORE.append("/webfigure_scripts/Figure_To_Pdf.py")
