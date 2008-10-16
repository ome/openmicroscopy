"""
    OMERO.fs logger module
    
    
"""

import logging

log = logging.getLogger("omerofs.server")
console_handler = logging.StreamHandler()
console_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(message)s"))
log.addHandler(console_handler)
log.setLevel(20)
