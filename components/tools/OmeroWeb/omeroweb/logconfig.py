import threading
import logging
import logging.handlers
from django.conf import settings

_LOCALS = threading.local()

def get_logger(log_filename, log_level=logging.NOTSET):
    logger = getattr(_LOCALS, 'logger', None)
    if logger is not None:
        return logger

    logging.basicConfig(level=log_level,
                        format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                        datefmt='%a, %d %b %Y %H:%M:%S',
                        filename=log_filename,
                        filemode='a')
    
    logger = logging.getLogger()
    #hdlr = logging.FileHandler(log_filename)
    #hdlr = logging.handlers.TimedRotatingFileHandler(log_filename,'midnight',1)
    
    # Windows will not allow renaming (or deleting) a file that's open. 
    # There's nothing the logging package can do about that.
    #try:
    #    sys.getwindowsversion()
    #except:
    #    hdlr.doRollover()
        
    #formatter = logging.Formatter('%(asctime)s %(name)-12s %(levelname)-8s %(message)s','%a, %d %b %Y %H:%M:%S')
    #hdlr.setFormatter(formatter)
    #logger.addHandler(hdlr)
    logger.setLevel(log_level)

    return logger