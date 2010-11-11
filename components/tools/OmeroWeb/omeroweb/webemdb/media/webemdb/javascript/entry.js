$(document).ready(function() {
    $(".content").hide();
    $("#SummaryData").show();
    
    xmlLink = $("#emdbxml").attr("href");
    $.get(xmlLink, function(data) {
        
        // find these elements in the xml and put their text in corresponding html elements
        var elementNames=["status","depositionDate","headerReleaseDate","mapReleaseDate","authors", "articleTitle", 
            "journal", "year", "volume", "firstPage", "lastPage", "externalReference", "journalArticle", "resolutionByAuthor", 
            "resolutionMethod", "name", "aggregationState", "molWtTheo","annotationDetails", "contourLevel"];
        for(var i=0; i<elementNames.length; i++){
            name = elementNames[i];
            $(data).find(name).each(function() {
                var text = $(this).text();
                var $target = $("#"+name);
                $target.text(text);
                
                if (name == "externalReference") {      // set this as a link 
                    link = "http://www.ncbi.nlm.nih.gov/pubmed/" + text + "?dopt=Abstract";
                    $target.attr("href", link);
                }
                // these elements are found twice in the html 
                if (name == "articleTitle" || name == "authors") {
                    var $target2 = $("#"+name + "2");
                    $target2.text(text);
                }
            });
        }
        
        var buildTable = function($parent, elements, labels) {
            var tableHtml = "<table>";
            for (var e=0; e<elements.length; e++) {
                t = $parent.find(elements[e]).text();
                if ($parent.find(elements[e]).attr('units')) { t+= " ("+ $parent.find(elements[e]).attr('units') + ")"; }
                tableHtml += "<tr><td align='right' class='label'>" + labels[e] + ":</td><td>" + t + "</td></tr>";
            }
            tableHtml += "</table>";
            return tableHtml;
        }
        
        // build html table from the 'samplePreparation' xml data
        $(data).find("sample").each(function() {
            var elements = ["name", "aggregationState", "molWtTheo"];
            var labels = ["Sample Name", "Aggregation State", "Theoretical Molecular Weight"];
            var html = buildTable($(this), elements, labels);
            $("#sample").append($(html));
        });
        
        
        // we have multiple samples in this list. Make a table with a row per sample
        $(data).find("sampleComponentList").each(function() {
            var html = "<table>";
            html += "<tr><th>ID</th><th>Type</th><th>Name</th><th>Details</th><th>Oligomeric Details</th>" +
                    "<th>Mutant</th><th>Source</th><tr>";
            $(this).find("sampleComponent").each(function() {
                var $sc = $(this);
                var cols = ["entry", "sciName", "details", "oligomericDetails", "mutantFlag", "natSource"];
                html += "<tr>";
                html += "<td>" + $sc.attr('componentID') + "</td>";
                for (var c=0; c<cols.length; c++) {
                    t = $sc.find(cols[c]).text();
                    html += "<td>" + t + "</td>";
                }
                html += "</tr>";
            });
            html += "</table>";
            $("#sampleComponentList").append($(html));
        });
        
        // build html table from the 'samplePreparation' xml data
        $(data).find("samplePreparation").each(function() {
            var elements = ["ph", "sampleConc", "details", "staining", "sampleSupportDetails"];
            var labels = ["pH", "Sample Conc", "Details", "Staining", "Sample Support Details"];
            var html = buildTable($(this), elements, labels);
            $("#samplePreparation").append($(html));
        });
        // build html table from the 'vitrification' xml data
        $(data).find("vitrification").each(function() {
            var elements = ["cryogenName", "humidity", "temperature", "instrument", "method", "timeResolvedState", "details"];
            var labels = ["Cryogen Name", "Humidity", "Temp", "Instr", "Method", "Time Resolved", "Details"];
            var html = buildTable($(this), elements, labels);
            $("#vitrification").append($(html));
        });
        // build html table from the 'imaging' xml data - 2 cols
        $(data).find("imaging").each(function() {
            var $img = $(this);  // xml
            var html = "<table width='100%'><tr><td valign='top'><table>";
            var elements = ["microscope", "acceleratingVoltage", "illuminationMode", "imagingMode", "nominalCs", 
            "nominalDefocusMin", "nominalDefocusMax", "nominalMagnification","calibratedMagnification","electronSource",
            "detector","detectorDistance", "astigmatism", "specimenHolder","specimenHolderModel", "tiltAngleMin",
            "tiltAngleMax","energyFilter", "energyWindow", "temperature", "temperatureMin", "temperatureMax", "beamTilt",
            "electronDose" , "details", "date"];
            var labels = ["Microscope", "Voltage", "Illumination Mode", "Imaging Mode", "Cs", "Defocus Min", "Defocus Max",
                "Nominal Mag", "Calibrated Mag", "Electron Source", "Detector", "Detector distance", "Astigmatism",
                "Specimen Holder", "Holder Model", "Tilt Min", "Tilt Max", "Energy Filter", "Energy Window", "Temp.",
                "Temp. Min", "Temp. Max", "Beam Tilt", "Electron Dose", "Other Details", "Date"];
            for (var e=0; e<elements.length; e++) {
                t = $img.find(elements[e]).text();
                if ($img.find(elements[e]).attr('units')) { t+= " ("+ $img.find(elements[e]).attr('units') + ")"; }
                html += "<tr><td align='right' class='label'>" + labels[e] + ":</td><td>" + t + "</td></tr>";
                if (e+1 == elements.length/2) {
                    html += "</table></td><td valign='top'><table>";
                }
            }
            html += "</table</td></tr></table>"
            $("#imaging").append($(html));
        });
        // processing page...
        // build html table from the 'reconstruction' xml data
        $(data).find("reconstruction").each(function() {
            var elements = ["software", "ctfCorrection", "resolutionByAuthor", "resolutionMethod", "details", "TODO:Unit-Cell?"];
            var labels = ["Software", "CTF Correction", "Resolution By Author", "Resolution Method", "Processing Details", "Unit Cell"];
            var html = buildTable($(this), elements, labels);
            $("#reconstruction").append($(html));
        });
        // build html table from the 'imageScans' xml data
        $(data).find("imageScans").each(function() {
            var elements = ["numDigitalImages", "samplingSize", "odRange", "quantBitNumber", "scanner", "details"];
            var labels = ["Digital Images", "Sampling Size", "OD Range", "Quant. Bit Number", "Scanner", "Details"];
            var html = buildTable($(this), elements, labels);
            $("#imageScans").append($(html));
        });
        // build html table from the 'fitting' xml data
        $(data).find("fitting").each(function() {
            var $fitting = $(this);  // xml
            var elements = ["refProtocol", "targetCriteria", "software", "overallBValue", "refSpace", "details", "pdbChainId"];
            var labels = ["Protocol", "Target Criteria", "Software", "B Value", "Ref. Space", "Details", "PDB chain ID"];
            var html = buildTable($(this), elements, labels);
            html += "<table><tr><td align='right' class='label'>PDB ID:</td><td>";
            $fitting.find("pdbEntryId").each(function() {
                var pdbId = $(this).text();
                html += "<a href='http://www.pdb.org/pdb/explore/explore.do?structureId="+ pdbId + "'>"+ pdbId +"</a> ";
            });
            html += "</td></tr></table>"
            $("#fitting").append($(html));
        });
        // build html table from the 'map' xml data
        $(data).find("map").each(function() {
            var $map = $(this);  // xml
            var html = "<table>";
            // populate the three cols with the text from 3 children of each element
            var fillCols = function(elementName, rowLabel, childNames) {
                var $e = $map.find(elementName);
                html += "<tr><td align='right' class='label'>" + rowLabel + ":</td>";
                for (var c=0; c<childNames.length; c++) {
                    html += "<td width='100'>" + $e.find(childNames[c]).text() + "</td>";
                }
                html += "</tr>";
            }
            fillCols("axisOrder", "Map Axis Order", ["axisOrderFast", "axisOrderMedium", "axisOrderSlow"]);
            fillCols("cell", "Map Extent (&Aring;)", ["cellA", "cellB", "cellC"]);
            fillCols("dimensions", "Dimensions (voxels)", ["numColumns", "numRows", "numSections"]);
            fillCols("origin", "Origin (voxels)", ["originCol", "originRow", "originSec"]);
            fillCols("limit", "Limit (voxels)", ["limitCol", "limitRow", "limitSec"]);
            fillCols("pixelSpacing", "Voxel spacing (&Aring;)", ["pixelX", "pixelY", "pixelZ"]);
            html += "</table>"
            $("#map").append($(html));
        });
        // build html table from the 'statistics' xml data
        var minMapValue = 0;
        var maxMapValue = 100;
        var avgMapValue = 0;
        var stdMapValue = 0;
        $(data).find("map").find("statistics").each(function() {
            minMapValue = parseFloat($(this).find("minimum").text());
            maxMapValue = parseFloat($(this).find("maximum").text());
            avgMapValue = parseFloat($(this).find("average").text());
            stdMapValue = parseFloat($(this).find("std").text());
            var elements = ["minimum", "maximum", "average", "std"];
            var labels = ["Minimum", "Maximum", "Average", "Standard Deviation"];
            var html = buildTable($(this), elements, labels);
            $("#statistics").append($(html));
        });
        
        // now we have the variables we need to create the contour-level slider. 
        $("#slider").slider({
            value:50,
            min: 0,
            max: 100,
            slide: function(event, ui) {
                var v = ui.value;
                var val = minMapValue + ((maxMapValue - minMapValue) * (v / 100));
                // var val = avgMapValue - (stdMapValue/2) + ((v / 100) * stdMapValue);
                $("#sliderValue").text(val.toFixed(2));
            }
        });
        $("#setContourLevel").click(function() {
            var v = $("#slider").slider("value");
            var val = minMapValue + ((maxMapValue - minMapValue) * (v / 100));
            //var val = avgMapValue - (stdMapValue/2) + ((v / 100) * stdMapValue);
            var command = "map mapA contour 0 "+ val.toFixed(2) +";"; 
            execute_oav_command(command);
            
            return false;
        });
    });
    
    // handle navigation between the main page sections. 
    $(".nav").click(function(event) {
        // hide all content sections, then show appropriate one
        $(".content").hide();
        var name = $(this).attr("id");
        var contentId = "#" + name + "Data";
        $(contentId).show();
        
        // change the way that the header is displayed. Bigger when Summary page is displayed. 
        $("#articleTitle").removeClass();
        $("#authors").removeClass();
        if (event.target.id == "Summary") {
            $("#entryImage").attr('height', 200);
            $("#articleTitle").addClass('entryTitle');
            $("#authors").addClass('entryTitle');
            $("#entrySub").show();
        } else {
            $("#entryImage").attr('height', 80);
            $("#articleTitle").addClass('entryTitleSmall');
            $("#authors").addClass('entryTitleSmall');
            $("#entrySub").hide();
        }
        return false;
    });
    
    // toggle between various visualisation views, E.g. Preview, OMERO, OpenAstex
    $(".visOption").click(function(event) {
        var optionId = $(this).attr("id");
        // hide current view & show new one
        $("#contourLevelSlider").hide();
        $(".visViewer").hide();
        $("#oavControls").hide();
        if (optionId == "visAstexBit" || optionId == "visAstexMap" || optionId == "visAstexSmallMap") {
            $("#oavControls").show();
            $("#visAstexPane").show();
            if (!(optionId == "visAstexBit")) {
                 $("#contourLevelSlider").show();
             }
        }
        else {
            var viewerId = optionId + "Pane";
            $("#"+viewerId).show();
        }
        
        // show which option is selected
        $(".visOption").removeClass('visSelected');
        $(this).addClass('visSelected');
    });
    // start with preview selected
    $("#visPreview").click();
    
    $(".visOption").hover(function() { $(this).addClass('visHover') } ,
        function() { $(this).removeClass('visHover') } );
    
    // on first click of OMERO view option, load viewer
    $("#imageId").hide(); // simply somewhere to store ID - always hidden
    $("#viewportLink").hide();  // simply somewhere to store link - always hidden
    var omeroLoaded = false;
    $("#visOmero").click(function() {
        if (omeroLoaded)   return;
        var viewportLink = $("#viewportLink").attr('href');
        $("#visOmeroIframe").attr('src', viewportLink);     // load the viewport page into the iframe
        omeroLoaded = true;
    });
    
    // pass javascript commands to the open astex viewer applet. 
    var execute_oav_command = function(command) {
        document.av.execute(command);
        window.status = command;
    };
    
    $("#contentTable").attr('height', '100%');
    $("#oavLink").hide();   // somewhere to store href to load OAV. - always hidden
    $("#oavControls").hide();   // show these when OAV is displayed 
    
    // map load mapA '{% url webemdb_bit fileId %}'; 
    // use this command to render new maps correctly
    var configureCmd = "\ncenter map mapA;\nmap mapA contour 0 'solid';\nmap mapA contour 0 'green';"
    // on first click of Astex view option, load OA-viewer
    $("#oavBitLink").hide();
    var oavLoaded = false;
    $("#visAstexBit").click(function() {
        if (oavLoaded) {
            // just get the url of the map we want to view, and load it. 
            // TODO: Don't do this if we're already looking at it! 
            var bitUrl = $("#oavBitLink").attr('href');
            var command = "map replace mapA '"+ bitUrl +"';"+ configureCmd;
            // command = "map mapA contour 0 'red';";   // bug-fixing
            execute_oav_command(command);
        } else {
            var oavHref = $("#oavLink").attr('href');
            $("#oav").load(oavHref);
            oavLoaded = true;
        }
    });
    
    $("#oavMapLink").hide();
    $("#oavLinkMap").hide();
    $("#visAstexMap").click(function() {
        if (oavLoaded) {
            // just get the url of the map we want to view, and load it. 
            // TODO: Don't do this if we're already looking at it! 
            var bitUrl = $("#oavMapLink").attr('href');
            var command = "map replace mapA '"+ bitUrl +"';"+ configureCmd;
            // command = "map mapA contour 0 'blue';";   // bug-fixing
            execute_oav_command(command);
        } else {
            var oavHref = $("#oavLinkMap").attr('href');
            $("#oav").load(oavHref);
            oavLoaded = true;
        }
    });
    $("#oavSmallMapLink").hide();
    $("#oavLinkSmallMap").hide();
    $("#visAstexSmallMap").click(function() {
        if (oavLoaded) {
            // just get the url of the map we want to view, and load it. 
            // TODO: Don't do this if we're already looking at it! 
            var bitUrl = $("#oavSmallMapLink").attr('href');
            var command = "map replace mapA '"+ bitUrl +"';"+ configureCmd;
            //command = "map mapA contour 0 'blue';";   // bug-fixing
            execute_oav_command(command);
        } else {
            var oavHref = $("#oavLinkSmallMap").attr('href');
            $("#oav").load(oavHref);
            oavLoaded = true;
        }
    });
    
    
    $(".oavControl").click(function(event) {
        var eId = event.target.id;
        var command = "map mapA contour 0 '" + eId + "';";
        execute_oav_command(command);
    });
    $(".oavLoadSeg").click(function(event) {
        var $a = $(this);
        var segUrl = $a.attr('href');
        // map segment mapName seggerFile level;
        var command = "map segment segA '" + segUrl + "' -1;";
        execute_oav_command(command);
        return false;
    });
    $(".oavLoadPdb").click(function(event) {
        var $a = $(this);
        //var pdbId = $a.text; 
        var pdbId = $a.text();
        var pdbUrl = $a.attr('href');
        // lazy load - in case we have loaded it already
        var command = "molecule lazy " + pdbId + " '" + pdbUrl + "';"
        command += "\ncolor_by_rainbow aminoacid;\nsecstruc all;\nschematic -name protein_schematic all;\ncolor_by_atom;";
        execute_oav_command(command);
        return false;
    });
    $("#toggleMap").click(function(event) {
        var on = $(this).attr('checked');
        var onOff = "on"
        if (!on) {
            onOff = "off"
        }
        var command = "map mapA contour 0 "+ onOff + ";";
        execute_oav_command(command);
    });
    $("#toggleSolid").click(function(event) {
        var on = $(this).attr('checked');
        var onOff = "solid"
        if (!on) {
            onOff = "wire"
        }
        var command = "map mapA contour 0 '"+ onOff + "';";
        execute_oav_command(command);
    });
    
 });