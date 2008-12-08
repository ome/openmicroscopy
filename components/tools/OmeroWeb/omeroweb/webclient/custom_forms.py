from itertools import chain
from django import forms
from django.forms.widgets import SelectMultiple, CheckboxInput
from django.utils.encoding import force_unicode
from django.utils.html import escape, conditional_escape
from django.utils.safestring import mark_safe

import re

class PermissionCheckboxSelectMultiple(SelectMultiple):
    def render(self, name, value, attrs=None, choices=()):
        if value is None: value = []
        has_id = attrs and 'id' in attrs
        final_attrs = self.build_attrs(attrs, name=name)
        output = [u'']
        # Normalize to strings
        str_values = set([force_unicode(v) for v in value])
        for i, (option_value, option_label) in enumerate(chain(self.choices, choices)):
            # If an ID attribute was given, add a numeric index as a suffix,
            # so that the checkboxes don't all have the same ID attribute.
            if has_id:
                final_attrs = dict(final_attrs, id='%s_%s' % (attrs['id'], i))
            cb = CheckboxInput(final_attrs, check_test=lambda value: value in str_values)
            option_value = force_unicode(option_value)
            rendered_cb = cb.render(name, option_value)
            output.append(u'<label>%s %s</label>' % (rendered_cb,
                    conditional_escape(force_unicode(option_label))))
        return mark_safe(u'\n'.join(output))

    def id_for_label(self, id_):
        # See the comment for RadioSelect.id_for_label()
        if id_:
            id_ += '_0'
        return id_
    id_for_label = classmethod(id_for_label)


##################################################################
# Fields

class MultiEmailField(forms.Field):
    def clean(self, value):
        if not value:
            raise forms.ValidationError('No email.')
        if value.count(' ') > 0:
            raise forms.ValidationError('Use only separator ";". Remove every spaces.')
        emails = value.split(';')
        for email in emails:
            if not self.is_valid_email(email):
                raise forms.ValidationError('%s is not a valid e-mail address. Use separator ";"' % email)
        return emails

    def is_valid_email(self, email):
        email_pat = re.compile(r"(?:^|\s)[-a-z0-9_.]+@(?:[-a-z0-9]+\.)+[a-z]{2,6}(?:\s|$)",re.IGNORECASE)
        return email_pat.match(email) is not None

class UrlField(forms.Field):
    def clean(self, value):
        if not value:
            raise forms.ValidationError('No url.')
        if not self.is_valid_url(value):
            raise forms.ValidationError('%s is not a valid url' % value)
        return value
    
    def is_valid_url(self, url):
        url_pat = re_http = re.compile(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+',re.IGNORECASE)
        return url_pat.match(url) is not None