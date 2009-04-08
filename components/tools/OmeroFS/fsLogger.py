"""
    OMERO.fs logger module
    
    
"""

import logging
from logging import handlers

log = logging.getLogger("fs")
#file_handler = logging.FileHandler("var/log/fs.out")
file_handler = handlers.TimedRotatingFileHandler("var/log/fs.out",'midnight', 1)
file_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(name)s - %(message)s"))
log.addHandler(file_handler)
log.setLevel(logging.INFO)
