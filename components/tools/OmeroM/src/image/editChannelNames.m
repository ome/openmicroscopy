function T = editChannelNames(session,img,varargin)
% editChannelNames allows you to change Channel Names for an image in OMERO
% server.
%
% SYNTAX
% T = editChannelNames(session,img)
% T = editChannelNames(session,img,newchanNames)
% T = editChannelNames(____,'Param',value)
%
% T = editChannelNames(session,img) returns the current channel names. If
% not specified yet, they will be empty characters, although it may look
% like 0, 1, or 2 etc on OMERO GUI.
%
% T = editChannelNames(session,img,newchanNames) will set the channel names
% to newchanNames.
%
% INPUT ARGUMENTS
% session     omero.api.ServiceFactoryPrxHelper object
%
% img         positive integer | omero.model.ImageI object
%             An Image ID or an omero.model.ImageI object for OMERO.
%
% newchanNames
%             cell vector of character vectors | string vector
%             The new channel names. If you specify 'channelIDs', the
%             length of channelIDs and that of newchanNames must tally.
%
% OPTIONAL PARAMETER/VALUE PAIRS
% 'channelIDs'
%             [] | vector of positive integers
%             (Optional) Channel IDs for specific editing. If you specify
%             'channelIDs', the length of channelIDs and that of
%             newchanNames must tally.
%
% OUTPUT ARGUMENTS
% T           table array
%             With variables, 'Id', 'Name', and, if newchanNames is
%             specified, 'NewName'.
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 10-Jul-2018 12:04:46
%
% See also
% loadChannels, getImages

p = inputParser;
p.addRequired('session',@(x) isscalar(x));
p.addRequired('img',@(x) isscalar(x));
p.addOptional('newchanNames',[],@(x) isvector(x) && iscellstr(x) || isstring(x));
p.addParameter('ChannelIDs',[],@(x) isvector(x) && all(fix(x) == x & x > 0));

p.parse(session,img,varargin{:});

newchanNames = p.Results.newchanNames;
channelIDs = p.Results.ChannelIDs;

if ~isempty(channelIDs)
    
    assert(length(channelIDs) == length(newchanNames),...
        'When you specify channelIDs, the length of channelIDs and newchanNames must tally.') 
    
end

if isstring(newchanNames)
    newchanNames = cellstr(newchanNames);
end


if isnumeric(img)

    img_ = img;

    img = getImages(session,img_);

end


channels = loadChannels(session, img);

channelId = zeros(numel(channels),1);
channelName = cell(numel(channels),1);
channelName_ = cell(numel(channels),1);


import java.util.ArrayList
li = ArrayList;

j = 0;
for i = 1:numel(channels)
    ch = channels(i);
    channelId(i,1) = double(ch.getId().getValue());
    
    if ~isempty(ch.getLogicalChannel().getName())
    
        channelName{i,1} = char(ch.getLogicalChannel().getName().getValue()); %java.lang.String

    else
        
        channelName{i,1} ='';
        
    end
    
    if ~isempty(newchanNames)
        if isempty(channelIDs) || ismember(channelId(i,1),channelIDs)
            j = j + 1;

            channels(i).getLogicalChannel().setName(rstring(newchanNames{j})); % overwrite
            channelName_{i,1} = char(ch.getLogicalChannel().getName().getValue());
        else
            channelName_{i,1} = channelName{i,1};
            
        end 
    end
    
    li.add(channels(i));
end

if ~isempty(newchanNames)
    T = table(channelId,channelName,channelName_,'VariableNames',{'Id','Name','NewName'});

    cs = session.getContainerService();
    cs.updateDataObjects(li,[]);% tricky to find the right type. see http://www.openmicroscopy.org/community/viewtopic.php?f=6&t=8536
    
else
    T = table(channelId,channelName,'VariableNames',{'Id','Name'});
end


end
