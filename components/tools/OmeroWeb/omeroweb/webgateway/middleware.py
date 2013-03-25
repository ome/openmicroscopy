#!/usr/bin/env python
# -*- coding: utf-8 -*-
import re

from django.utils.text import compress_string
from django.utils.cache import patch_vary_headers

re_accepts_gzip = re.compile(r'\bgzip\b')

class GZipMiddleware(object):
    """
    This middleware compresses content if the browser allows gzip compression.
    It sets the Vary header accordingly, so that caches will base their storage
    on the Accept-Encoding header.
    """
    def process_response(self, request, response):
        # It's not worth compressing non-OK or really short responses.
        # omeroweb: the tradeoff for less than 8192k of uncompressed text is not worth it most of the times.
        if response.status_code != 200 or len(response.content) < 8192:
            return response

        # Avoid gzipping if we've already got a content-encoding.
        if response.has_header('Content-Encoding'):
            return response

        # omeroweb: we don't want to compress everything, so doing an opt-in approach
        ctype = response.get('Content-Type', '').lower()
        if not "javascript" in ctype and not "text" in ctype:
            return response

        patch_vary_headers(response, ('Accept-Encoding',))

        # Avoid gzipping if we've already got a content-encoding.
        if response.has_header('Content-Encoding'):
            return response

        # Older versions of IE have issues with gzipped pages containing either
        # Javascript and PDF.
        if "msie" in request.META.get('HTTP_USER_AGENT', '').lower():
            ctype = response.get('Content-Type', '').lower()
            if "javascript" in ctype or ctype == "application/pdf":
                return response

        ae = request.META.get('HTTP_ACCEPT_ENCODING', '')
        if not re_accepts_gzip.search(ae):
            return response

        response.content = compress_string(response.content)
        response['Content-Encoding'] = 'gzip'
        response['Content-Length'] = str(len(response.content))
        return response
