import omero.*;
import omero.rtypes.*;

c = omero.client();
s = c.createSession();
q = s.getQueryService();

FIND_PLATES = 'select p from Plate p where p.externalIdentifier is not null';
FIND_PLATE = 'select plate from Plate plate where plate.externalIdentifier = :barcode';

filter = omero.sys.Filter();
filter.limit = rint(100);
params = omero.sys.ParametersI();
params.theFilter = filter;

plates = q.findAllByQuery(FIND_PLATES, params);
length = plates.size();
choice = randint(0,length-1);
bcode = plates.get(choice).getExternalIdentifier();

params.add('barcode',bcode);
plate = q.findByQuery(FIND_PLATE, params);
disp(sprintf('Found plate %d', plate.getId().getValue()));
