function rv=rmap(varargin)
% omero.rtypes.rmap static method workaround
% varargin converted to struct to java.lang.HashMap
hm=structToHashMap(struct(varargin{:}));
rv=omero.rtypes.rmap(hm);
