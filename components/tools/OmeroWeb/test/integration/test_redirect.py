import omero
from weblibrary import IWebTest
from weblibrary import _response

from django.core.urlresolvers import reverse
from omeroweb.webredirect.baseconv import base62


class TestShortUrl(IWebTest):
    """
    Tests short url.
    """

    def test_short_url(self):
        origin = "/webclient/?show=image-1"
        alias = omero.model.AliasI()
        alias.setNs(omero.rtypes.rstring("host/port"))
        alias.origin = omero.rtypes.rstring(origin)
        alias.details.permissions = omero.model.PermissionsI("rwr-r-")
        alias = self.root.sf.getUpdateService().saveAndReturnObject(alias)
        short = base62.from_decimal(alias.id.val)
        request_url = reverse('webshorturl', args=[short])
        rsp = _response(self.django_root_client, request_url, 'get', {}, 200,
                        follow=True)

        # Work arround for Django 1.6
        try:
            location = "%s?%s" % (rsp.wsgi_request.META['PATH_INFO'],
                                  rsp.wsgi_request.META['QUERY_STRING'])
        except:
            # TODO: remove when 1.6 is dropped
            location = "%s?%s" % (rsp.request['PATH_INFO'],
                                  rsp.request['QUERY_STRING'])
        assert origin == location
