function handles = UploadImages(handles)

% Help for the Upload Images module:
% Category: File Processing
%
% SHORT DESCRIPTION:
% Saves any image produced in the image creation module to the server. 
% *************************************************************************
% Saves to the server. 
%
% See also BlitzLoader.

% CellProfiler is distributed under the GNU General Public License.
% See the accompanying file LICENSE for details.
%
% Developed by the Whitehead Institute for Biomedical Research.
% Copyright 2003,2004,2005.
%
% Website: http://www.cellprofiler.org
%
% $Revision: 4905 $

%%%%%%%%%%%%%%%%%
%%% VARIABLES %%%
%%%%%%%%%%%%%%%%%
drawnow

%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%
%If you change anything here, make sure the image tool SaveImageAs is
%consistent, in CPimagetool.
%%%%%%%%%%%%%%%%%%%%%%%%   WARNING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%

[CurrentModule, CurrentModuleNum, ModuleName] = CPwhichmodule(handles);

%textVAR01 = What did you call the images you want to save? If you would like to save an entire figure, enter the module number here
%infotypeVAR01 = imagegroup
%defaultVAR01 = /
ImageName{1} = char(handles.Settings.VariableValues{CurrentModuleNum,1});
%inputtypeVAR01 = popupmenu custom

%textVAR02 = Which images' original filenames do you want use as a base for these new images' filenames? Your choice MUST be images loaded directly with a Load module. Alternately, type N to use sequential numbers for the file names, or type =DesiredFilename to use the single file name you specify (replace DesiredFilename with the name you actually want) for all files (this is *required* when saving an avi movie).
%infotypeVAR02 = imagegroup
%defaultVAR02 = /
FileName{1} = char(handles.Settings.VariableValues{CurrentModuleNum,2});
%inputtypeVAR02 = popupmenu custom

%textVAR03 = What did you call the images you want to save? If you would like to save an entire figure, enter the module number here
%infotypeVAR03 = imagegroup
%defaultVAR03 = /
ImageName{2} = char(handles.Settings.VariableValues{CurrentModuleNum,3});
%inputtypeVAR03 = popupmenu custom

%textVAR04 = Which images' original filenames do you want use as a base for these new images' filenames? Your choice MUST be images loaded directly with a Load module. Alternately, type N to use sequential numbers for the file names, or type =DesiredFilename to use the single file name you specify (replace DesiredFilename with the name you actually want) for all files (this is *required* when saving an avi movie).
%infotypeVAR04 = imagegroup
%defaultVAR04 = /
FileName{2} = char(handles.Settings.VariableValues{CurrentModuleNum,4});
%inputtypeVAR04 = popupmenu custom

%textVAR05 = What did you call the images you want to save? If you would like to save an entire figure, enter the module number here
%infotypeVAR05 = imagegroup
%defaultVAR05 = /
ImageName{3} = char(handles.Settings.VariableValues{CurrentModuleNum,5});
%inputtypeVAR05 = popupmenu custom

%textVAR06 = Which images' original filenames do you want use as a base for these new images' filenames? Your choice MUST be images loaded directly with a Load module. Alternately, type N to use sequential numbers for the file names, or type =DesiredFilename to use the single file name you specify (replace DesiredFilename with the name you actually want) for all files (this is *required* when saving an avi movie).
%infotypeVAR06 = imagegroup
%defaultVAR06 = /
FileName{3} = char(handles.Settings.VariableValues{CurrentModuleNum,6});
%inputtypeVAR06 = popupmenu custom

%textVAR07 = What did you call the images you want to save? If you would like to save an entire figure, enter the module number here
%infotypeVAR07 = imagegroup
%defaultVAR07 = /
ImageName{4} = char(handles.Settings.VariableValues{CurrentModuleNum,7});
%inputtypeVAR07 = popupmenu custom

%textVAR08 = Which images' original filenames do you want use as a base for these new images' filenames? Your choice MUST be images loaded directly with a Load module. Alternately, type N to use sequential numbers for the file names, or type =DesiredFilename to use the single file name you specify (replace DesiredFilename with the name you actually want) for all files (this is *required* when saving an avi movie).
%infotypeVAR08 = imagegroup
%defaultVAR08 = /
FileName{4} = char(handles.Settings.VariableValues{CurrentModuleNum,8});
%inputtypeVAR08 = popupmenu custom

%textVAR09 = Enter the bit depth at which to save the images (Note: some image formats do not support saving at a bit depth of 12 or 16; see Matlab's imwrite function for more details.)
%choiceVAR09 = 8
%choiceVAR09 = 12
%choiceVAR09 = 16
BitDepth = char(handles.Settings.VariableValues{CurrentModuleNum,9});
%inputtypeVAR09 = popupmenu

%textVAR10 = Do you want to rescale the images to use a full 8 bit (256 graylevel) dynamic range (Y or N)? Use the RescaleIntensity module for other rescaling options.
%choiceVAR10 = No
%choiceVAR10 = Yes
RescaleImage = char(handles.Settings.VariableValues{CurrentModuleNum,10});
%inputtypeVAR10 = popupmenu

%textVAR11 = What do you want to call these new pixels in OMERO?
Methodology = char(handles.Settings.VariableValues{CurrentModuleNum,11});
%defaultVAR11 = CellProfiler

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
iceConfigPath = handles.Pipeline.('iceConfigPath');
UserName = handles.Pipeline.('UserName');
Password = handles.Pipeline.('Password');


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
    
blitzGateway = createGateway(iceConfigPath,UserName, Password);
if SetBeingAnalyzed == 1 
    %%% CREATE COPY OF THE CURRENT PIXELS.
    
    for i = 1:numImages(ImageName)
        fieldname = strcat('Filename',FileName{i});
        fileName = handles.Pipeline.(fieldname);
        [path, fname, ext, v] = fileparts(char(fileName));
          
        [filename, z, t, channel] = parseFileName(char(fname));
        ChannelList(i) = str2num(channel);
    end
    
    newPixelsId =  copyPixelsOMERO(blitzGateway, pixelsId, ChannelList, Methodology);      
    handles.Pipeline.('uploadPixelsID') = newPixelsId.longValue();
    pixels = blitzGateway.getPixels(newPixelsId.longValue());
    if(str2num(BitDepth)==8)
        pixelsType = blitzGateway.getPixelsType('uint8');
    else
        pixelsType = blitzGateway.getPixelsType('uint16');
    end;
    pixels.pixelsType = pixelsType;
    blitzGateway.updatePixels(pixels);

end
    
    
for i = 1:numImages(ImageName)
    fieldname = strcat('Filename',FileName{i});
    fileName = handles.Pipeline.(fieldname){SetBeingAnalyzed};
    [path, fname, ext, v] = fileparts(char(fileName));
    [filename, z, t, channel] = parseFileName(char(fname));
    Image = handles.Pipeline.(ImageName{i});
    if max(Image(:)) > 1 || min(Image(:)) < 0
        % Warn the users that the value is being changed.
        % Outside 0-1 RangeWarning Box
        if isempty(findobj('Tag',['Msgbox_' ModuleName ', ModuleNumber ' num2str(CurrentModuleNum) ': Outside 0-1 Range']))
            CPwarndlg(['The images you have loaded in the ', ModuleName, ' module are outside the 0-1 range, and you may be losing data.'],[ModuleName ', ModuleNumber ' num2str(CurrentModuleNum) ': Outside 0-1 Range'],'replace');
        end
    end

    if (strcmpi(RescaleImage,'Yes'))
        LOW_HIGH = stretchlim(Image,0);
        Image = imadjust(Image,LOW_HIGH,[0 1]);
        Image = floor(Image.*((2.^str2num(BitDepth))-1));
    end
    minValue = min(Image(:));
    maxValue = max(Image(:));
    minField = strcat('Min',ImageName{i});
    maxField = strcat('Max',ImageName{i});
    minFieldValue = [];
    maxFieldValue = [];
    if(isfield(handles.Pipeline,minField))
        minFieldValue = handles.Pipeline.(minField);
        minValue = min(minValue,minFieldValue);
    end;
    if(isfield(handles.Pipeline,maxField))
        maxFieldValue = handles.Pipeline.(maxField);
        maxValue = max(maxValue,maxFieldValue);
    end
        
    handles.Pipeline.(minField) = minValue;
    handles.Pipeline.(maxField) = maxValue;
        
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%% UPLOAD IMAGE TO HARD DRIVE %%%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    drawnow
    uploadPixelsID = handles.Pipeline.('uploadPixelsID');
    blitzGateway.uploadPlane(uploadPixelsID, str2num(channel), str2num(t), str2num(z), Image);
    pixels = getPixelsOMERO(blitzGateway, uploadPixelsID);
    c = pixels.channels.get(str2num(channel));
    stats = c.statsInfo;
    stats.globalMin = omero.RDouble(minValue);
    stats.globalMax = omero.RDouble(maxValue);
    blitzGateway.updatePixels(pixels);
end
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