from itertools import chain
from django import forms
from django.forms.widgets import SelectMultiple, CheckboxInput, MultipleHiddenInput
from django.utils.encoding import force_unicode
from django.utils.html import escape, conditional_escape
from django.utils.safestring import mark_safe

from django.forms.fields import Field, EMPTY_VALUES
from django.forms.widgets import Select
from django.forms import ModelChoiceField, ValidationError
from django.utils.translation import ugettext_lazy as _
from django.utils.encoding import smart_unicode


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

##################################################################
# Metadata queryset iterator for group form

class MetadataQuerySetIterator(object):
    def __init__(self, queryset, empty_label, cache_choices):
        self.queryset = queryset
        self.empty_label = empty_label
        self.cache_choices = cache_choices

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            if hasattr(obj.id, 'val'):
                yield (obj.value.val, smart_unicode(obj.value.val))
            else:
                yield (obj.value, smart_unicode(obj.value))
        # Clear the QuerySet cache if required.
        #if not self.cache_choices:
            #self.queryset._result_cache = None

class MetadataModelChoiceField(ModelChoiceField):

    def _get_choices(self):
        # If self._choices is set, then somebody must have manually set
        # the property self.choices. In this case, just return self._choices.
        if hasattr(self, '_choices'):
            return self._choices
        # Otherwise, execute the QuerySet in self.queryset to determine the
        # choices dynamically. Return a fresh QuerySetIterator that has not
        # been consumed. Note that we're instantiating a new QuerySetIterator
        # *each* time _get_choices() is called (and, thus, each time
        # self.choices is accessed) so that we can ensure the QuerySet has not
        # been consumed.
        return MetadataQuerySetIterator(self.queryset, self.empty_label,
                                self.cache_choices)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def clean(self, value):
        Field.clean(self, value)
        if value in EMPTY_VALUES:
            return None
        #try:
            #value = self.queryset.get(pk=value)
        #except self.queryset.model.DoesNotExist:
            #raise ValidationError(self.error_messages['invalid_choice'])
        res = False
        for q in self.queryset:
            if hasattr(q.id, 'val'):
                if long(value) == q.id.val:
                    res = True
            else:
                if long(value) == q.id:
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value

class AnnotationQuerySetIterator(object):
    def __init__(self, queryset, empty_label, cache_choices):
        self.queryset = queryset
        self.empty_label = empty_label
        self.cache_choices = cache_choices

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            textValue = None
            from omero_model_FileAnnotationI import FileAnnotationI
            if isinstance(obj._obj, FileAnnotationI):
                textValue = obj.file.name.val
            else:
                if hasattr(obj.textValue, 'val'):
                    textValue = obj.textValue.val
                else:
                    if obj.textValue is not None:
                        textValue = obj.textValue

            l = len(textValue)
            if l > 55:
                textValue = "%s..." % textValue[:55]
            if hasattr(obj.id, 'val'):
                oid = obj.id.val
            else:
                oid = obj.id

            yield (oid, smart_unicode(textValue))
        # Clear the QuerySet cache if required.
        #if not self.cache_choices:
            #self.queryset._result_cache = None

class AnnotationModelChoiceField(ModelChoiceField):
    
    def _get_choices(self):
        # If self._choices is set, then somebody must have manually set
        # the property self.choices. In this case, just return self._choices.
        if hasattr(self, '_choices'):
            return self._choices
        # Otherwise, execute the QuerySet in self.queryset to determine the
        # choices dynamically. Return a fresh QuerySetIterator that has not
        # been consumed. Note that we're instantiating a new QuerySetIterator
        # *each* time _get_choices() is called (and, thus, each time
        # self.choices is accessed) so that we can ensure the QuerySet has not
        # been consumed.
        return AnnotationQuerySetIterator(self.queryset, self.empty_label,
                                self.cache_choices)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def clean(self, value):
        Field.clean(self, value)
        if value in EMPTY_VALUES:
            return None
        #try:
            #value = self.queryset.get(pk=value)
        #except self.queryset.model.DoesNotExist:
            #raise ValidationError(self.error_messages['invalid_choice'])
        res = False
        for q in self.queryset:
            if hasattr(q.id, 'val'):
                if long(value) == q.id.val:
                    res = True
            else:
                if long(value) == q.id:
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value

class AnnotationModelMultipleChoiceField(AnnotationModelChoiceField):
    """A MultipleChoiceField whose choices are a model QuerySet."""
    hidden_widget = MultipleHiddenInput
    default_error_messages = {
        'list': _(u'Enter a list of values.'),
        'invalid_choice': _(u'Select a valid choice. That choice is not one of the'
                            u' available choices.'),
    }

    def __init__(self, queryset, cache_choices=False, required=True,
                 widget=SelectMultiple, label=None, initial=None,
                 help_text=None, *args, **kwargs):
        super(AnnotationModelMultipleChoiceField, self).__init__(queryset, None,
            cache_choices, required, widget, label, initial, help_text,
            *args, **kwargs)

    def clean(self, value):
        if self.required and not value:
            raise ValidationError(self.error_messages['required'])
        elif not self.required and not value:
            return []
        if not isinstance(value, (list, tuple)):
            raise ValidationError(self.error_messages['list'])
        final_values = []
        for val in value:
            try:
                long(val)
            except:
                raise ValidationError(self.error_messages['invalid_choice'])
            else:
            #try:
                #obj = self.queryset.get(pk=val)
            #except self.queryset.model.DoesNotExist:
                #raise ValidationError(self.error_messages['invalid_choice'] % val)
            #else:
                #final_values.append(val)
                res = False
                for q in self.queryset:
                    if hasattr(q.id, 'val'):
                        if long(val) == q.id.val:
                            res = True
                    else:
                        if long(val) == q.id:
                            res = True
                if not res:
                    raise ValidationError(self.error_messages['invalid_choice'])
                else:
                    final_values.append(val)
        return final_values