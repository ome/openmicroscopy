import logging

logger = logging.getLogger(__name__)

try:
    from django_slack import slack_message
except:
    logger.error("Cannot resolve 'django_slack.log.SlackExceptionHandler':"
                 " No module named django_slack")

from django.conf import settings
from django.middleware.common import BrokenLinkEmailsMiddleware

from django.utils.encoding import force_text


class BrokenLinkSlackMiddleware(BrokenLinkEmailsMiddleware):

    def process_response(self, request, response):
        """
        Send broken link emails for relevant 404 NOT FOUND responses.
        """
        if response.status_code == 404 and not settings.DEBUG:
            domain = request.get_host()
            path = request.get_full_path()
            referer = force_text(request.META.get('HTTP_REFERER', ''),
                                 errors='replace')

            if not self.is_ignorable_request(request, path, domain, referer):
                ua = request.META.get('HTTP_USER_AGENT', '<none>')
                ip = request.META.get('REMOTE_ADDR', '<none>')

                subject = "Broken %slink on %s" % (
                    ('INTERNAL ' if self.is_internal_request(
                        domain, referer) else ''),
                    domain
                )
                message = ("Referrer: %s\nRequested URL: %s\nUser agent: %s\n"
                           "IP address: %s\n" % (referer, path, ua, ip))
                attachments = {
                    'subject': subject,
                    'text': message,
                    'color': 'warning',
                }
                template = 'django_slack/exception.slack'

                slack_message(template, {'text': subject}, [attachments])
        return response
