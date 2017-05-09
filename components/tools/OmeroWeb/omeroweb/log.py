import logging

logger = logging.getLogger(__name__)

try:
    from django_slack.log import SlackExceptionHandler
except:
    logger.error("Cannot resolve 'django_slack.log.SlackExceptionHandler':"
                 " No module named django_slack")

from django.conf import settings
from django.utils.encoding import force_text
from django.views.debug import get_exception_reporter_filter


class OmeroSlackExceptionHandler(SlackExceptionHandler):

    def __init__(self, **kwargs):
        SlackExceptionHandler.__init__(self)

    def emit(self, record):
        try:
            request = record.request
            subject = '%s (%s IP): %s' % (
                record.levelname,
                ('internal' if request.META.get(
                    'REMOTE_ADDR') in settings.INTERNAL_IPS
                 else 'EXTERNAL'),
                record.getMessage()
            )
            filter = get_exception_reporter_filter(request)
            request_repr = '\n{}'.format(force_text(
                filter.get_request_repr(request)))
        except Exception:
            subject = '%s: %s' % (
                record.levelname,
                record.getMessage()
            )
            request = None
            request_repr = "unavailable"
        subject = self.format_subject(subject)

        message = "%s\n\nRequest repr(): %s" % (
            self.format(record), request_repr
        )
        colors = {
            'ERROR': 'danger',
            'WARNING': 'warning',
            'INFO': 'good',
        }

        attachments = {
            'title': subject,
            'text': message,
            'color': colors.get(record.levelname, '#AAAAAA'),
        }

        attachments.update(self.kwargs)
        self.send_message(
            self.template,
            {'text': subject},
            self.generate_attachments(**attachments),
        )
