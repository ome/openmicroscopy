function unlinkAnnotations(session,parentType,parentID,varargin)
% unlinkAnnotations allows you to unlink all or specified
% omero.model.Annotation objects from parent in OMERO server.
%
% SYNTAX
% unlinkAnnotations(session,parentType,parentID)
% unlinkAnnotations(session,parentType,parentID,annt)
%
%
% BEFORE USE
% 
%   client = loadOmero('demo.openmicroscopy.org', 4064)
%   username = 'xxxxxxx'
%   password = 'xxxxxxx'
%   session = client.createSession(username, password)
%
%
% AFTER USE
%
%   clear
%   unloadOmero
%
% REQUIREMENTS
%
% OMERO.matlab toolbox
% https://docs.openmicroscopy.org/latest/omero/developers/Matlab.html
% 
%
% INPUT ARGUMENTS
% session     omero.api.ServiceFactoryPrxHelper object
%
%               client = loadOmero('demo.openmicroscopy.org', 4064)
%               session = client.createSession(username, password)
%
% parentType  'project' | 'dataset' | 'image' | 'screen' | 'plate' | 
%             'plateacquisition' | 'roi'
%
% parentID    vector of non-negative integers
%             parentID can be found via OMERO GUI
%
%
% annt        omero.model.Annotation object or its array | [] (default)
%
%             (Optional) omero.model.Annotation object (supports
%             omero.model.MapAnnotationI, omero.model.TagAnnotationI,
%             omero.model.CommentAnnotationI, and
%             omero.model.FileAnnotationI at the moment) to be unlinked
%             from parent If empty or not specified, all linked
%             MapAnnotation objects will be unlinked.
%            
%             For example, to obtain MapAnnotationI object(s) from an image:
%
%               annt = getObjectAnnotations(session, 'map', 'image', parentID);
%
%             or
%
%               annt = getImageTagAnnotations(session, ids)
% 
%             annt can be empty, scalar or a vector.
%
%             Also you can use following functions or similar.
%
%               annt = getAnnotations(session,ids,'tags')
%               annt = getTagAnnotations(session,tagID)
%
%
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 09-Jun-2018 03:27:05
%
% See also
% linkAnnotation

objects = getObjectTypes();

p = inputParser;
p.addRequired('session',@(x) isjava(x));
p.addRequired('parentType',@(x) ischar(x) && ismember(x, {objects.name}));
p.addRequired('parentID',@(x) isnumeric(x) && isvector(x));

p.addOptional('annt',[],@(x) isempty(x) || all(isa(x, 'omero.model.Annotation')));
p.parse(session,parentType,parentID,varargin{:});

annt = p.Results.annt;


% https://docs.openmicroscopy.org/omero/5.4.6/developers/Model/StructuredAnnotations.html
% Project
% Dataset
% Pixels
% OriginalFile
% PlaneInfo
% Roi
% Channel
% Folder

% https://github.com/openmicroscopy/openmicroscopy/blob/develop/examples/Training/matlab/ReadData.m#L334


switch parentType
   
    case 'project'
        ParentType = 'Project';
    case 'dataset'
        ParentType = 'Dataset';
    case 'image'
        ParentType = 'Image';
    case 'screen'
        ParentType = 'Screen';
    case 'plate'
        ParentType = 'Plate';
    case 'plateacquisition'
        ParentType = 'PlateAcquisition';
    case 'roi'
        ParentType = 'Roi';
end

for m = 1:length(parentID)

    parents = session.getQueryService().findAllByQuery(...
        [sprintf('select obj from %s as obj left outer join fetch obj.annotationLinks as link join fetch link.child as annotation where obj.id =  ',ParentType), ...
        num2str(parentID(m))], []);
    
    n = size(parents);
    for k = 1:n
        
        prt = parents.get(k-1);
        
        al = prt.copyAnnotationLinks();
        
        if ~isempty(annt)
            
            for j = 1:numel(annt)
                
                for i = (1:size(al))-1
                    if al.get(i).getChild().getId().equals(annt(j).getId())
                        
                        prt.unlinkAnnotation(al.get(i).getChild());
                        
                    end
                end
                
            end
            
        else
            
            prt.clearAnnotationLinks % did work (clear all the AnnotationLinks at once)
            
        end
        
        prt = session.getUpdateService().saveAndReturnObject(prt);
        
    end

end


end
