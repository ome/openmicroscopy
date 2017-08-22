from omero_version import omero_version
from django.conf import settings


def url_suffix(request):
    suffix = u"?_%s" % omero_version
    return {'url_suffix': suffix}


def base_include_template(request):
	return {'base_include_template': settings.BASE_INCLUDE_TEMPLATE}
