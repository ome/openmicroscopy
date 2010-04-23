from django.http import HttpResponse

class HttpJavascriptRedirect(HttpResponse):
    def __init__(self,content):
        content = '<html><body onLoad="%s;"></body></html>' % content
        HttpResponse.__init__(self,content)


class HttpJavascriptResponse(HttpResponse):
    def __init__(self,content):
        HttpResponse.__init__(self,content,mimetype="text/javascript")
