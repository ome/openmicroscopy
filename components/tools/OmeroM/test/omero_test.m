%  Simple OMERO.matlab test driver
%  Copyright (c) 2009, Glencoe Software, Inc.
%  See LICENSE for details.

try

  omero_home = getenv('OMERO_HOME');
  addpath(fullfile(omero_home,'lib','matlab'))

  [c,s,g] = loadOmero;
  disp(s.getConfigService().getVersion());

  try

    filter = omero.sys.Filter();
    filter.limit = omero.rtypes.rint(1);
    images = s.getQueryService().findAll('Image', filter);
    image = images.get(0);
    id = image.getId().getValue();
    plane_z0 = getPlaneFromImageId(g,id,0,0,0);
    stack = getPlaneStack(g,id,0,0);

  catch ME2
    disp('FAILED');
    disp(ME2);
    disp(ME2.message);
  end

  c.closeSession();
  clear stack;
  clear plane_z0;
  clear id;
  clear image;
  clear images;
  clear filter;
  clear g;
  clear s;
  clear c;
  unloadOmero;

catch ME

  disp('FAILED');
  disp(ME);
  disp(ME.message);

end
exit;
