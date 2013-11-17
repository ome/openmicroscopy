// http://www.sitepoint.com/javascript-json-serialization/
JSON.stringify = JSON.stringify || function (obj) {
    var t = typeof (obj);
    if (t != "object" || obj === null) {
        // simple data type
        if (t == "string") obj = '"'+obj+'"';
        return String(obj);
    }
    else {
        // recurse array or object
        var n, v, json = [], arr = (obj && obj.constructor == Array);
        for (n in obj) {
            v = obj[n]; t = typeof(v);
            if (t == "string") v = '"'+v+'"';
            else if (t == "object" && v !== null) v = JSON.stringify(v);
            json.push((arr ? "" : '"' + n + '":') + String(v));
        }
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
    }
};


$(function(){

    $(".modal-dialog").draggable();

    $('#previewInfoTabs a').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    });



    // If we're on Windows, update tool-tips for keyboard short cuts:
    if (navigator.platform.toUpperCase().indexOf('WIN') > -1) {
        $('.btn-sm').each(function(){
            var $this = $(this);
                $this.attr('data-original-title', $this.attr('data-original-title').replace("⌘", "Ctrl+"));
        });
    }
    // refresh tooltips
   $('.btn-sm, .navbar-header').tooltip({container: 'body', placement:'bottom', toggle:"tooltip"});


});