import datetime

from django.db import models
from omeroweb.webpublic.baseconv import base62

class Link(models.Model):
    """
    Model that represents a shortened URL

    # Initialize by deleting all Link objects
    >>> Link.objects.all().delete()
    
    # Create some Link objects
    >>> link1 = Link.objects.create(url="http://www.google.com/")
    >>> link2 = Link.objects.create(url="http://www.nileshk.com/")

    # Get base 62 representation of id
    >>> link1.to_base62()
    'B'
    >>> link2.to_base62()
    'C'

    # Get short URL's
    >>> link1.short_url()
    'http://uu4.us/B'
    >>> link2.short_url()
    'http://uu4.us/C'

    # Test usage_count
    >>> link1.usage_count
    0
    >>> link1.usage_count += 1
    >>> link1.usage_count
    1
    """
    
    class Meta:
        unique_together = (('url', 'owner'))
    
    url = models.URLField(max_length=2048)
    owner = models.IntegerField()
    group = models.IntegerField()
    submitted = models.DateTimeField(default=datetime.datetime.now())

    def to_base62(self):
        return base62.from_decimal(self.id)
    
    def __unicode__(self):
        return self.to_base62() + ' : ' + self.url
