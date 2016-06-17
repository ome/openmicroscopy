
    // Example openwith script
    // TEMP - for evaluation of https://github.com/openmicroscopy/openmicroscopy/pull/4630
    // TODO: remove this before merging PR!
    var isEnabled = function(selected) {
        console.log('GenBank Protein, isEnabled()', selected);
        if (selected.length !== 1) return false;

        // return True if name is a number (E.g. Gene ID)
        var name = selected[0].name;
        return (parseInt(name, 10) == name);
    };

    var handleAction = function(selected, url) {
        console.log('GenBank Protein, handleAction()', selected, url);
        var name = selected[0].name;
        window.open(url + name, 'new');
    };

    OME.setOpenWithEnabledHandler("GenBank Protein", isEnabled);
    OME.setOpenWithActionHandler("GenBank Protein", handleAction);



    // We also configure a second 'Open With' plugin
    // NB: This plugin doesn't specify it's own script - we are just
    // piggy-backing on the 'GenBank Protein' plugin for convenience

    var isJpgEnabled = function(selected) {
        if (selected.length === 1) {
            var s = selected[0];
            return (s.type === 'fileannotation' && s.file && s.file.name.endsWith(".jpg"));
        }
    };
    var handleJpgAction = function(selected, url) {
        // url for this plugin is webindex
        // we want to open original file...
        if (selected.length === 1) {
            var s = selected[0],
                origFileId = s.file.id;
            window.open(url + 'get_original_file/' + origFileId + '/', 'new');
        }
    };

    OME.setOpenWithEnabledHandler("jpg viewer", isJpgEnabled);
    OME.setOpenWithActionHandler("jpg viewer", handleJpgAction);
