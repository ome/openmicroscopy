def _formatReport(delete_handle):
    """
    Added as workaround to the changes made in #3006.
    """
    delete_reports = delete_handle.report()
    rv = []
    for report in delete_reports:
        if report.error:
            rv.append(report.error)
        elif report.warning:
            rv.append(report.warning)
    return "; ".join(rv)
    # Might want to take advantage of other feedback here

def _purgeCallback(request):
    
    callbacks = request.session.get('callback').keys()
    if len(callbacks) > 200:
        for (cbString, count) in zip(request.session.get('callback').keys(), range(0,len(callbacks)-200)):
            del request.session['callback'][cbString]