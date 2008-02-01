from django.conf.urls.defaults import *

urlpatterns = patterns('',
    # Example:
    # (r'^OMERO_weblitz/', include('OMERO_weblitz.foo.urls')),

    # Uncomment this for admin:
#     (r'^admin/', include('django.contrib.admin.urls')),

     (r'^admin/', include('django.contrib.admin.urls')),

     (r'', include('blitzcon.urls')),
)
