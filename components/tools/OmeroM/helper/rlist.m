function rv=rlist(varargin)
% omero.rtypes.rlistg static method workaround
% varargin converted to javaArray of type 'omero.RType'
ja=javaArray('omero.RType',nargin);
for i=1:nargin,
    ja(i)=varargin{i};
end
rv=omero.rtypes.rlist(ja);
