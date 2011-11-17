import settings

def static(request):
    """
    Adds static-related context variables to the context.

    """
    return {'STATIC_COMMON_PREFIX': settings.STATIC_COMMON_URL}
    