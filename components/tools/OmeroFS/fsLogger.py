"""
    OMERO.fs logger module
    
    
"""

import logging
from logging import handlers

log1 = logging.getLogger("fsserver")
file_handler = handlers.TimedRotatingFileHandler("var/log/fsserver.out",'midnight', 1)
file_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(name)s - %(message)s"))
log1.addHandler(file_handler)
log1.setLevel(logging.INFO)

log2 = logging.getLogger("fsclient")
file_handler = handlers.TimedRotatingFileHandler("var/log/fsclient.out",'midnight', 1)
file_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(name)s - %(message)s"))
log2.addHandler(file_handler)
log2.setLevel(logging.INFO)
