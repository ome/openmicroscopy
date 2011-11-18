import settings

def static(request):
    """
    Adds static-related context variables to the context.

    """
    return {'STATIC_WEBCLIENT_PREFIX': settings.STATIC_WEBCLIENT_URL}
    