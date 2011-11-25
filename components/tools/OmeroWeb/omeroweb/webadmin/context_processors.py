import settings

def static(request):
    """
    Adds static-related context variables to the context.

    """
    return {'STATIC_WEBADMIN_PREFIX': settings.STATIC_WEBADMIN_URL}
