from django.http import HttpResponse

class HttpJavascriptRedirect(HttpResponse):
    def __init__(self,content):
        content = '<html><body onLoad="javascript:window.top.location.href=\'%s\'"></body></html>' % content
        HttpResponse.__init__(self,content)


class HttpJavascriptResponse(HttpResponse):
    def __init__(self,content):
        HttpResponse.__init__(self,content,mimetype="text/javascript")


class HttpLoginRedirect(HttpResponse):
    def __init__(self,content): 
        content = """<html><body onLoad="top.location.replace('%s');"></body></html>""" % content
        HttpResponse.__init__(self,content)
