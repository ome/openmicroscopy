from django import forms
from django.utils.encoding import smart_str, smart_unicode

class NonASCIIForm(forms.Form):
    
    def __init__ (self, *args, **kwargs):
        super(NonASCIIForm, self).__init__(*args, **kwargs)
    
    def full_clean(self):
        super(NonASCIIForm, self).full_clean()
        
        for name, field in self.cleaned_data.items():
            if isinstance(field, basestring):
                self.cleaned_data[name] = str(smart_str(field))