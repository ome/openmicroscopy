from models import StoredConnection
from django.contrib import admin
from django.utils.translation import ugettext_lazy as _

class StoredConnectionOptions(admin.ModelAdmin):
  list_display = ('base_path', 'config_file', 'username', 'failcount')

admin.site.register(StoredConnection, StoredConnectionOptions)

