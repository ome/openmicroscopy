function CP(pipeline, prefs)
tic;
%load('/Users/donald/Desktop/outputTest/pipeLineUpload.mat');

load(pipeline);
load(prefs);


handles.Settings = Settings;
handles.Pipeline = Settings;
handles.Measurements = Settings;
handles.Preferences = SavedPreferences;
handles.Current.NumberOfImageSets = 1;
handles.Current.SaveOutputHowOften = 1;
handles.Current.TimeStarted = datestr(now);
handles.Current.NumberOfModules = length(handles.Settings.ModuleNames);
handles.Current.SetBeingAnalyzed = 1;
handles.Current.CurrentModuleNumber=char('1');
handles.Current.NumberOfModules = length(handles.Settings.ModuleNames);

SlotNumber = 1;
handles.Current.(['FigureNumberForModule' handles.Current.CurrentModuleNumber]) = SlotNumber;
break_outer_loop = 0;
startingImageSet = 1;
handles.Current.StartingImageSet = startingImageSet;
while handles.Current.SetBeingAnalyzed <= handles.Current.NumberOfImageSets
    setbeinganalyzed = handles.Current.SetBeingAnalyzed;
    %%% This is used to check for errors and allow restart.

  
    SlotNumber=1;
    NumberOfWindows = 0;
    handles.Current.NumberOfModules
    while SlotNumber<=handles.Current.NumberOfModules

        ModuleNumberAsString = TwoDigitString(SlotNumber);
        ModuleName = char(handles.Settings.ModuleNames(SlotNumber));
        if ~iscellstr(handles.Settings.ModuleNames(SlotNumber))
        else

            handles.Current.CurrentModuleNumber = ModuleNumberAsString;
            try

                if(handles.Current.SetBeingAnalyzed ==1)
                    handles.Current.(['FigureNumberForModule' TwoDigitString(SlotNumber)]) = ceil(max(findobj))+1;
                end


                CanMoveToNextModule = false;

                %%% Runs the appropriate module, with the handles structure as an
                %%% input argument and as the output
                %%% argument.
                handles.Measurements.Image.ModuleErrorFeatures(str2double(TwoDigitString(SlotNumber))) = {ModuleName};
                handles = feval(ModuleName,handles);

                %%% If the call to feval succeeded, then the module succeeded and we can move to the next module.
                %%% (if there is an error, it will be caught below, at the point marked MODULE ERROR)
                CanMoveToNextModule = true;
            catch

            end

        end
        %%% If the module passed out a new value for
        %%% StartingImageSet, then we set startingImageSet
        %%% to be that value and break all the way our to
        %%% the cycle loop. The RestartImageSet in
        %%% handles is deleted because we never want it in
        %%% the output file.
        startingImageSet = handles.Current.StartingImageSet;
        if (setbeinganalyzed < startingImageSet)
            handles.Current.SetBeingAnalyzed = startingImageSet;
        end
        %%% if we can move to the next module, do so
        if CanMoveToNextModule,
            SlotNumber = SlotNumber + 1
        end
    end %%% ends loop over slot number
    %%% Completes the breakout to the image loop.
    if (setbeinganalyzed < startingImageSet)
        if startingImageSet ==2
            handles.Current.StartingImageSet = 1;
        end
        continue;
    end;

    if (break_outer_loop),
        break;  %%% this break is out of the outer loop of image analysis
    end

    %%% Save all data that is in the handles structure to the output file
    %%% name specified by the user, but only save it
    %%% in the increments that the user has specified
    %%% (e.g. every 5th cycle, every 10th image
    %%% set, as set by the SpeedUpCellProfiler
    %%% module), or if it is the last cycle.  If
    %%% the user has not used the SpeedUpCellProfiler
    %%% module, then
    %%% handles.Current.SaveOutputHowOften is the
    %%% number 1, so the output file will be saved
    %%% every time.
    %%% Save everything, but don't want to write out
    %%% StartingImageSet field.
    handles.Current = rmfield(handles.Current,'StartingImageSet');
    if (rem(handles.Current.SetBeingAnalyzed,handles.Current.SaveOutputHowOften) == 0) || (handles.Current.SetBeingAnalyzed == 1) || (handles.Current.SetBeingAnalyzed == handles.Current.NumberOfImageSets)
        %Removes images from the Pipeline
        if strcmp(handles.Preferences.StripPipeline,'Yes')
            ListOfFields = fieldnames(handles.Pipeline);
            restorePipe = handles.Pipeline;
            tempPipe = handles.Pipeline;
            for i = 1:length(ListOfFields)
                if all(size(tempPipe.(ListOfFields{i}))~=1)
                    tempPipe = rmfield(tempPipe,ListOfFields(i));
                end
            end
            handles.Pipeline = tempPipe;
        end
        try
            %eval(['save ''',fullfile(handles.Current.DefaultOutputDirectory, handles.Current.OutputFile), ''' ''handles'';']);
        catch
            break;
        end
        if strcmp(handles.Preferences.StripPipeline,'Yes')
            %%% restores the handles.Pipeline structure if
            %%% it was removed above.
            handles.Pipeline = restorePipe;
        end
    end
    %%% Restore StartingImageSet for those modules that
    %%% need it.
    handles.Current.StartingImageSet = startingImageSet;

    %%% The setbeinganalyzed is increased by one and stored in the handles structure.
    setbeinganalyzed = setbeinganalyzed + 1;
    handles.Current.SetBeingAnalyzed = setbeinganalyzed
    TimerData.SetBeingAnalyzed = setbeinganalyzed;

end %%% This "end" goes with the "while" loop (going through the cycles).



function twodigit = TwoDigitString(val)
%TwoDigitString is a function like num2str(int) but it returns a two digit
%representation of a string for our purposes.
if ((val > 99) || (val < 0)),
    error(['TwoDigitString: Can''t convert ' num2str(val) ' to a 2 digit number']);
end
twodigit = sprintf('%02d', val);
