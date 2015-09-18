%
% Access Image data stored in OMERO image database
% ------------------------------------------------
%
% The OmeroMatlab toolbox can be used to access image data stored
% in an OMERO server. You must have a valid login on the server
% in order to use this toolbox.
%
% Basic usage:
% -----------
%   omero_client = loadOmero;
%   session = omero_client.createSession();
%   try
%     query_svc = session.getQueryService();
%     params = omero.sys.ParametersI();
%     images = query_svc.findAllByQuery('select i from Image i where i.name like ''%2009%'' ', params);
%   catch ME1
%     % Handle it somehow
%
%   % But be sure to close the connetion.
%   omero_client.closeSession()
%
% See the <a href="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroMatlab">OMERO Matlab developers' page</a> for more information.
%
