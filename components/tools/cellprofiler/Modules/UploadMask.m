function handles = UploadImages(handles)

% Help for the Upload Mask module:
% Category: File Processing
%
% SHORT DESCRIPTION:
% Upload the mask images to OMERO, as ROI on the image. 
% *************************************************************************
% Saves to the server. 
%
% See also OMEROLoader.

% CellProfiler is distributed under the GNU General Public License.
% See the accompanying file LICENSE for details.
%
% Developed by the Whitehead Institute for Biomedical Research.
% Copyright 2003,2004,2005.
%
% Website: http://www.cellprofiler.org
% $Revision: 4905 $
% UploadImages 
% Author:
%   Donald MacDonald (Donald@lifesci.dundee.ac.uk)
% OpenMicroscopy Environment (OME)
% www.openmicroscopy.org.uk
% University of Dundee


%%%%%%%%%%%%%%%%%
%%% VARIABLES %%%
%%%%%%%%%%%%%%%%%
drawnow

%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%
%If you change anything here, make sure the image tool SaveImageAs is
%consistent, in CPimagetool.
%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%

[CurrentModule, CurrentModuleNum, ModuleName] = CPwhichmodule(handles);


%textVAR01 = Which Dataset do you wish to save channel 1 from?
%defaultVAR01 = CellImage
ObjectName{1} = char(handles.Settings.VariableValues{CurrentModuleNum,1});

%textVAR02 = Which Dataset do you wish to save channel 2 from?
%defaultVAR02 = /
ObjectName{2} = char(handles.Settings.VariableValues{CurrentModuleNum,2});

%textVAR03 = Which Dataset do you wish to save channel 3 from?
%defaultVAR03 = /
ObjectName{3} = char(handles.Settings.VariableValues{CurrentModuleNum,3});

%textVAR04 = Which Dataset do you wish to save channel 4 from?
%defaultVAR04 = /
ObjectName{4} = char(handles.Settings.VariableValues{CurrentModuleNum,4});

%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%
%If you change anything here, make sure the image tool SaveImageAs is
%consistent, in CPimagetool.
%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%VariableRevisionNumber = 12

%%%%%%%%%%%%%%%%%%%%%%%
%%% FILE PROCESSING %%%
%%%%%%%%%%%%%%%%%%%%%%%
drawnow

SetBeingAnalyzed = handles.Current.SetBeingAnalyzed;
session = handles.Current.session;
iUpdate = session.getUpdateService();
omeroService = session.createGateway();
% Processing will continue when a user has selected to save the tiled image
% on "First cycle" or "Every cycle". Instead of an error occurring,
% the program will behave as if the user entered "Last cycle"
% by not saving the image until the last cycle. At the end of the last cycle,
% the user will get a help dialog popup window.
warning off MATLAB:intConvertNonIntVal;
warning off MATLAB:intConvertOverflow;
warning off MATLAB:intMathOverflow;

fieldname = strcat('FileCnt', num2str(SetBeingAnalyzed));
currentFileDetails = handles.Pipeline.(fieldname);
[pixelsId, z, t] = parseFileDetails(currentFileDetails);
pixels = omeroService.getPixels(pixelsId);
ImageId = pixels.getImage().getId().getValue();
maskComponent = pojos.util.UploadMask(ImageId);
    
% if SetBeingAnalyzed == 1 
%     %%% CREATE COPY OF THE CURRENT PIXELS.
%     channelList = java.util.ArrayList;
%     for i = 1:numImages(ImageName)
%         fieldname = strcat('Filename',FileName{i});
%         fileName = handles.Pipeline.(fieldname);
%         [path, fname, ext, v] = fileparts(char(fileName));
%           
%         [filename, z, t, channel] = parseFileName(char(fname));
%         channelList.add(java.lang.Integer(str2num(channel)));
%     end
%     
%     newPixelsId =  copyPixels(omeroService, pixelsId, channelList, Methodology);      
%     handles.Pipeline.('uploadPixelsID') = newPixelsId;
%     pixels = getPixels(omeroService, newPixelsId);
%     if(str2num(BitDepth)==8)
%         pixelsType = getPixelType(omeroService, 'uint8');
%     else
%         pixelsType = getPixelType(omeroService, 'uint16');
%     end;
%     pixels.pixelsType = pixelsType;
%     updatePixels(omeroService,pixels);
% 
% end
RANGE=2^31-1;
numChannel=0;
for i = 1:4
    if(strcmp(ObjectName{i},'/')==0)
        numChannel=numChannel+1;
    end
end

for i = 1:numChannel
    LabelMatrixImage = handles.Pipeline.(['Segmented' ObjectName{i}]);
    image = int32(double(LabelMatrixImage / max(max(LabelMatrixImage)))*RANGE);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%% UPLOAD MASK TO OMERO AS ROI %%%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    maskComponent.addArray(image, int32(z), int32(t), int32(i));
end
rois = maskComponent.getROI();
for i = 0:rois.size()-1;
    roi = rois.get(i);
    iUpdate.saveObject(roi);
end
%     
% for i = 1:numImages(ImageName)
%     fieldname = strcat('Filename',FileName{i});
%     fileName = handles.Pipeline.(fieldname){SetBeingAnalyzed};
%     [path, fname, ext, v] = fileparts(char(fileName));
%     [filename, z, t, channel] = parseFileName(char(fname));
%     Image = handles.Pipeline.(ImageName{i});
%     if max(Image(:)) > 1 || min(Image(:)) < 0
%         % Warn the users that the value is being changed.
%         % Outside 0-1 RangeWarning Box
%         if isempty(findobj('Tag',['Msgbox_' ModuleName ', ModuleNumber ' num2str(CurrentModuleNum) ': Outside 0-1 Range']))
%             CPwarndlg(['The images you have loaded in the ', ModuleName, ' module are outside the 0-1 range, and you may be losing data.'],[ModuleName ', ModuleNumber ' num2str(CurrentModuleNum) ': Outside 0-1 Range'],'replace');
%         end
%     end
% 
%     if (strcmpi(RescaleImage,'Yes'))
%         LOW_HIGH = stretchlim(Image,0);
%         Image = imadjust(Image,LOW_HIGH,[0 1]);
%         Image = floor(Image.*((2.^str2num(BitDepth))-1));
%     end
%     minValue = min(Image(:));
%     maxValue = max(Image(:));
%     minField = strcat('Min',num2str(i));
%     maxField = strcat('Max',num2str(i));
%     minFieldValue = [];
%     maxFieldValue = [];
%     if(isfield(handles.Pipeline,minField))
%         minFieldValue = handles.Pipeline.(minField);
%         minValue = min(minValue,minFieldValue);
%     end;
%     if(isfield(handles.Pipeline,maxField))
%         maxFieldValue = handles.Pipeline.(maxField);
%         maxValue = max(maxValue,maxFieldValue);
%     end
%         
%     handles.Pipeline.(minField) = minValue;
%     handles.Pipeline.(maxField) = maxValue;
%         
%     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%     %%% UPLOAD IMAGE TO OMERO %%%
%     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%     drawnow
%     uploadPixelsID = handles.Pipeline.('uploadPixelsID');
%     uploadPlane(omeroService, int64(uploadPixelsID), int32(str2num(z)), ...
%     int32(i-1), int32(str2num(t)),  double(squeeze(Image(:,:,1))));
%     pixels = getPixels(omeroService, uploadPixelsID);
%     c = pixels.channels.get(i-1);
%     stats = c.statsInfo;
%     stats.globalMin = omero.RDouble(minValue);
%     stats.globalMax = omero.RDouble(maxValue);
%     updatePixels(omeroService, pixels);
% end
warning on MATLAB:intConvertNonIntVal;
warning on MATLAB:intConvertOverflow;
warning on MATLAB:intMathOverflow;

%%%%%%%%%%%%%%%%%%%%%%%
%%% DISPLAY RESULTS %%%
%%%%%%%%%%%%%%%%%%%%%%%
drawnow

%%% The figure window display is unnecessary for this module, so it is
%%% closed during the starting image cycle.
if SetBeingAnalyzed == handles.Current.StartingImageSet
    ThisModuleFigureNumber = handles.Current.(['FigureNumberForModule',CurrentModule]);
    if any(findobj == ThisModuleFigureNumber)
        close(ThisModuleFigureNumber)
    end
end
