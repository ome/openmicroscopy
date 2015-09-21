% Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Load Metadata
try
    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
    
    % Information to edit
    imageId = p.imageid;
    
    % Load image acquisition data.
    fprintf(1, 'Reading image: %g\n', imageId);
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image Id not valid');
    
    % Read channels
    fprintf(1, 'Loading channels for image %g\n', imageId');
    channels = loadChannels(session, image);
    for i = 1 : numel(channels),
        channel = channels(i);
        channelId = channel.getId().getValue();
        channelName = channel.getLogicalChannel().getName();
        emissionWave = channel.getLogicalChannel().getEmissionWave();
        excitationWave = channel.getLogicalChannel().getExcitationWave();
        fprintf(1, 'Reading channel %g:\n', i);
        fprintf(1, '  ID: %g\n', channelId);
        if ~isempty(channelName),
            fprintf(1, '  Name: %s\n', char(channelName.getValue()));
        end
        if ~isempty(emissionWave),
            fprintf(1, '  Emission wavlength: %g nm\n',...
                emissionWave.getValue());
        end
        if ~isempty(excitationWave),
            fprintf(1, '  Excitation wavlength: %g nm\n',...
                excitationWave.getValue());
        end
    end
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
