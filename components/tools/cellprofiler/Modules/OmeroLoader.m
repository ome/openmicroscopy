function handles = OmeroLoader(handles)

% OmeroLoaderModule:
% Category: File Processing
%
% SHORT DESCRIPTION:
% Load all images in the dataset.
% *************************************************************************
%
% OmeroLoader Help
% 
%

% CellProfiler is distributed under the GNU General Public License.
% See the accompanying file LICENSE for details.
%
%
%
% Website: http://www.cellprofiler.org
%
% $Revision: 4905 $
% OmeroLoader 
% Author:
%   Donald MacDonald (Donald@lifesci.dundee.ac.uk)
% OpenMicroscopy Environment (OME)
% www.openmicroscopy.org.uk
% University of Dundee


%%%%%%%%%%%%%%%%%
%%% VARIABLES %%%
%%%%%%%%%%%%%%%%%
drawnow

[CurrentModule, CurrentModuleNum, ModuleName] = CPwhichmodule(handles);

%textVAR01 = Which Dataset do you wish to load from?
%defaultVAR01 = 1
DatasetID = char(handles.Settings.VariableValues{CurrentModuleNum,1});

%textVAR02 = Enter Username?
%defaultVAR02 = root
UserName = char(handles.Settings.VariableValues{CurrentModuleNum,2});

%textVAR03 = Enter Password?
%defaultVAR03 = omero
Password = char(handles.Settings.VariableValues{CurrentModuleNum,3});

%pathnametextVAR04 = Enter path to where ice.Config file is located?
%defaultVAR04 = .
Pathname = char(handles.Settings.VariableValues{CurrentModuleNum,4});

%textVAR05 = What do you want to call these images within CellProfiler?
%defaultVAR05 = OrigBlue
%infotypeVAR05 = imagegroup indep
ImageName{1} = char(handles.Settings.VariableValues{CurrentModuleNum,5});

%textVAR06 = What channel position are these in the group?
%defaultVAR06 = /
TextToFind{1} = char(handles.Settings.VariableValues{CurrentModuleNum,6});

%textVAR07 = What do you want to call these images within CellProfiler?
%defaultVAR07 = /
%infotypeVAR07 = imagegroup indep
ImageName{2} = char(handles.Settings.VariableValues{CurrentModuleNum,7});

%textVAR08 = What channel position are these in the group?
%defaultVAR08 = /
TextToFind{2} = char(handles.Settings.VariableValues{CurrentModuleNum,8});

%textVAR09 = What do you want to call these images within CellProfiler?
%defaultVAR09 = /
%infotypeVAR09 = imagegroup indep
ImageName{3} = char(handles.Settings.VariableValues{CurrentModuleNum,9});

%textVAR10 = What channel position are these in the group?
%defaultVAR10 = /
TextToFind{3} = char(handles.Settings.VariableValues{CurrentModuleNum,10});

%textVAR11 = What do you want to call these images within CellProfiler?
%defaultVAR11 = /
%infotypeVAR11 = imagegroup indep
ImageName{4} = char(handles.Settings.VariableValues{CurrentModuleNum,11});

%textVAR12 = What channel position are these in the group?
%defaultVAR12 = /
TextToFind{4} = char(handles.Settings.VariableValues{CurrentModuleNum,12});

%%%VariableRevisionNumber = 2


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% PRELIMINARY CALCULATIONS %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
drawnow

%%% Determines which cycle is being analyzed.
SetBeingAnalyzed = handles.Current.SetBeingAnalyzed;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% FIRST CYCLE FILE HANDLING %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
drawnow

%%% Extracting the list of files to be analyzed occurs only the first time
%%% through this module.
if SetBeingAnalyzed == 1
    iceConfigPath = strcat(Pathname,'/ice.config');
    omeroService = createOmerojService(iceConfigPath,UserName, Password);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%% Check that the Project, Dataset and images exist in Dataset. %
    %%% TODO:                                                        %
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    % Get all filenames in the specified directory wich contains the specified extension (e.g., .avi or .stk).
    % Note that there is no check that the extensions actually is the last part of the filename.
    datasetAsNum = [str2num(DatasetID)];
    dataset = getDataset(omeroService, datasetAsNum, 1);
    list = getImagesFromDataset(omeroService, dataset);

    fileIds=[];
    cnt = 0;
    for i = 1:list.size()
        pixelsList = getPixelsFromImage(omeroService, list.get(i-1).id.val);
        if(~isempty(pixelsList))
            for j = 0:pixelsList.size-1;
                pixels = pixelsList.get(j);
                if(isempty(pixels.relatedTo))
                    cnt = cnt +1;
                    fileIds(cnt)= pixels.id.val;
                end;
            end
        end
    end
    %%% Checks whether any files have been found
    if isempty(fileIds)
        error(['Image processing was canceled in the ', ModuleName, ' module because there are no movie files in the chosen directory (or subdirectories, if you requested them to be analyzed as well).'])
    end
    imagesPerSet = numImages(ImageName);
    handles.Pipeline.('imagesPerSet') = imagesPerSet;
    NumberOfImageSets = 0;
    for i = 1:length(fileIds),
        pixels = getPixels(omeroService, fileIds(i));
        for z  = 0:pixels.sizeZ.val-1,
            for t = 0:pixels.sizeT.val-1,
                fieldName =  strcat('FileCnt',num2str(NumberOfImageSets+1));
                handles.Pipeline.(fieldName) = strcat('FileId',num2str(fileIds(i)),'z',num2str(z),'t',num2str(t));
                NumberOfImageSets = NumberOfImageSets + 1;
            end
        end
    end
    handles.Current.NumberOfImageSets = NumberOfImageSets;

    clear fileIds
    clear pixels
    clear fieldName
    if (~omeroService.isClosed())
        omeroService.close();
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% LOADING IMAGES EACH TIME %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
drawnow
iceConfigPath = strcat(Pathname,'/ice.config');
handles.Pipeline.('iceConfigPath') = iceConfigPath;
handles.Pipeline.('UserName') = UserName;
handles.Pipeline.('Password') = Password;
omeroService = createOmerojService(iceConfigPath,UserName, Password);

for n = 1:handles.Pipeline.imagesPerSet
        %%% This try/catch will catch any problems in the load images module.
        try
            fieldname = strcat('FileCnt', num2str(SetBeingAnalyzed));
            currentFileDetails = handles.Pipeline.(fieldname);
            [pixelsId, z, t] = parseFileDetails(currentFileDetails);
            [LoadedImage, handles] = CPOMEROimread(omeroService, currentFileDetails, TextToFind{n}, handles);
            if (max(LoadedImage(:)) <= .0625) && (handles.Current.SetBeingAnalyzed == 1)
                A = strmatch('RescaleIntensity', handles.Settings.ModuleNames);
                if length(A) < length(ImageName)
                    CPwarndlg(['Warning: the images loaded by ', ModuleName, ' are very dim (they are using 1/16th or less of the dynamic range of the image file format). This often happens when a 12-bit camera saves in 16-bit image format. If this is the case, use the Rescale Intensity module in "Enter max and min" mode to rescale the images using the values 0, 0.0625, 0, 1.'],'Outside 0-1 Range','replace');
                end
            end

            fieldname = strcat('Filename', ImageName{n});
            handles.Pipeline.(ImageName{n}) = LoadedImage;
            pixels = getPixels(omeroService, pixelsId);
            imageId = pixels.image.id.val;
            [path, fname, ext, v] = fileparts(getFileName(omeroService, imageId));
            
            fName = strcat(fname, 'z', num2str(z), 't', num2str(t),'c',TextToFind{n},ext);
            handles.Pipeline.(fieldname)(SetBeingAnalyzed) = {fName};
        catch ErrorMessage = lasterr;
            ErrorNumber = {'first','second','third','fourth'};
            error(['Image processing was canceled in the ', ModuleName, ' module because an error occurred when trying to load the ', ErrorNumber{n}, ' set of images. Please check the settings. A common problem is that there are non-image files in the directory you are trying to analyze. Matlab says the problem is: ', ErrorMessage])
        end % Goes with: catch
    
        % Create a cell array with the filenames
       FileNames{n} = {strcat(getFileName(omeroService, imageId), ':', num2str(z), ':', num2str(t))};
end
if (~omeroService.isClosed())
    omeroService.close();
end
%%%%%%%%%%%%%%%%%%%%%%%
%%% DISPLAY RESULTS %%%
%%%%%%%%%%%%%%%%%%%%%%%
drawnow

ThisModuleFigureNumber = handles.Current.(['FigureNumberForModule',CurrentModule]);
if any(findobj == ThisModuleFigureNumber);
    if handles.Current.SetBeingAnalyzed == handles.Current.StartingImageSet
        CPresizefigure('','NarrowText',ThisModuleFigureNumber)
    end
    for n = 1:numImages(ImageName)
        %%% Activates the appropriate figure window.
        currentfig = CPfigure(handles,'Text',ThisModuleFigureNumber);
        if iscell(ImageName)
            TextString = [ImageName{n},': ', FileNames{n}];
        else
            TextString = [ImageName,': ',FileNames];
        end
        uicontrol(currentfig,'style','text','units','normalized','fontsize',handles.Preferences.FontSize,'HorizontalAlignment','left','string',TextString,'position',[.05 .85-(n-1)*.15 .95 .1],'BackgroundColor',[.7 .7 .9])
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% SAVE DATA TO HANDLES %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%% NOTE: The structure for filenames and pathnames will be a cell array of cell arrays

%%% First, fix feature names and the pathname
PathNames = cell(1,numImages(ImageName));
FileNamesText = cell(1,numImages(ImageName));
PathNamesText = cell(1,numImages(ImageName));
for n = 1:numImages(ImageName)
    PathNames{n} = Pathname;
    FileNamesText{n} = [ImageName{n}];
    PathNamesText{n} = [ImageName{n}];
end

%%% Since there may be several load/save modules in the pipeline which all
%%% write to the handles.Measurements.Image.FileName field, we store
%%% filenames in an "appending" style. Here we check if any of the modules
%%% above the current module in the pipeline has written to
%%% handles.Measurements.Image.Filenames. Then we should append the current
%%% filenames and path names to the already written ones. If this is the
%%% first module to put anything into the handles.Measurements.Image
%%% structure, then this section is skipped and the FileNamesText fields
%%% are created with their initial entry coming from this module.

if  isfield(handles,'Measurements') && isfield(handles.Measurements,'Image') &&...
        isfield(handles.Measurements.Image,'FileNames') && length(handles.Measurements.Image.FileNames) == SetBeingAnalyzed
    % Get existing file/path names. Returns a cell array of names
    ExistingFileNamesText = handles.Measurements.Image.FileNamesText;
    ExistingFileNames     = handles.Measurements.Image.FileNames{SetBeingAnalyzed};
    ExistingPathNamesText = handles.Measurements.Image.PathNamesText;
    ExistingPathNames     = handles.Measurements.Image.PathNames{SetBeingAnalyzed};
    % Append current file names to existing file names
    FileNamesText = cat(2,ExistingFileNamesText,FileNamesText);
    FileNames     = cat(2,ExistingFileNames,FileNames);
    PathNamesText = cat(2,ExistingPathNamesText,PathNamesText);
    PathNames     = cat(2,ExistingPathNames,PathNames);
end

%%% Write to the handles.Measurements.Image structure
handles.Measurements.Image.FileNamesText                   = FileNamesText;
handles.Measurements.Image.FileNames(SetBeingAnalyzed)         = {FileNames};
handles.Measurements.Image.PathNamesText                   = PathNamesText;
handles.Measurements.Image.PathNames(SetBeingAnalyzed)         = {PathNames};
