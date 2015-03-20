from omero_version import omero_version


def url_suffix(request):
    suffix = u"?_%s" % omero_version
    return {'url_suffix': suffix}
