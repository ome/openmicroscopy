/**
 * gs_utils - Common functions library
 *
 * Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 * Author: Carlos Neves <carlos(at)glencoesoftware.com>
 */

var gs_static_location_prefix=''; //configure it to access static files, used with 3rdparty/jquery.blockUI-2.66.0.js

/**
 * Given a string that may contain an RGB, RRGGBB or the previous with a # prefix,
 * returns the #RRGGBB counterpart, or if the parse fails, the default value (or null if no default)
 */
function sanitizeHexColor (color, def) {
  color = toRGB(color, def);
  if (color === def || color === null) {
    return color;
  }
  return '#' + OME.rgbToHex(color);
}

/**
 * Converts a color into rgb(r,g,b) notation, right now only hex RGB or RRGGBB inputs.
 */
function toRGB (color, def) {
  if (color.substring(0,4) == 'rgb(') {
    return color;
  }
  if (color.substring(0,1) == '#') {
    color = color.substring(1);
  }
  var r,g,b;
  if (color.length == 3) {
    r = parseInt(color.substring(0,1), 16);
    g = parseInt(color.substring(1,2), 16);
    b = parseInt(color.substring(2,3), 16);
    r += r*0x10;
    g += g*0x10;
    b += b*0x10;
  } else if (color.length == 6) {
    r = parseInt(color.substring(0,2), 16);
    g = parseInt(color.substring(2,4), 16);
    b = parseInt(color.substring(4,6), 16);
  }
  if (r === undefined || isNaN(r) || isNaN(g) || isNaN(b)) {
    return def != undefined ? def : null;
  }
  return 'rgb('+r+','+g+','+b+')';
}

/**
 * parse the URL query string. Shamelessly stolen from thickbox.
 */
function parseQuery (q) {
  var query;
  if (q === undefined) {
    query = location.href.replace(/^[^\?]+\??/,'');
  } else {
    query = q.replace(/^\??/,'');
  }
  var Params = {};
  if ( ! query ) {return Params;}// return empty object
  var Pairs = query.split(/[;&]/);
  for ( var i = 0; i < Pairs.length; i++ ) {
    var KeyVal = Pairs[i].split('=');
    if ( ! KeyVal || KeyVal.length != 2 ) {continue;}
    var key = decodeURIComponent( KeyVal[0] );
    var val = decodeURIComponent( KeyVal[1] );
    val = val.replace(/\+/g, ' ');
    Params[key] = val;
  }
  return Params;
}

var gs_modalJson_cb;

/**
 * Lazy loader for the blockUI plugin.
 */

function gs_loadBlockUI (callback) {
  if (jQuery.blockUI === undefined) {
    jQuery.getScript(gs_static_location_prefix + '3rdparty/jquery.blockUI-2.66.0.js', callback);
    return false;
  }
  return true;
}

function gs_choiceModalDialog (message, choices, callback, blockui_opts, cancel_callback, _modal_cb) {
  if (!gs_loadBlockUI (function () {gs_choiceModalDialog(message, choices, callback, blockui_opts, cancel_callback,_modal_cb);})) {
    return;
  }
  if (_modal_cb) {
    gs_modal_cb = _modal_cb;
  } else {
    gs_modal_cb = function (idx) {
      jQuery.unblockUI();
      if (choices[idx].data != null) {
        callback(choices[idx].data);
      } else if (cancel_callback) {
        cancel_callback();
      }
      return false;
    }
  }
  for (var i=0; i < choices.length; i++) {
    message += '<input type="button" onclick="return gs_modal_cb('+i+');" value="'+choices[i].label+'" />'
  }
  if (!blockui_opts) {
    blockui_opts = {};
  }
  jQuery.blockUI({message: message, css: blockui_opts.css});
  return;
}

function gs_choiceModalJson (message, choices, callback, blockui_opts, cancel_callback) {
//  if (!gs_loadBlockUI (function () {gs_choiceModalJson(message, choices, callback, blockui_opts, cancel_callback);})) {
//    return;
//  }
  var gs_modalJson_cb = function (idx) {
    jQuery.unblockUI();
    if (choices[idx].url != null) {
      gs_modalJson(choices[idx].url, choices[idx].data, callback);
    } else if (cancel_callback) {
      cancel_callback();
    }
    return false;
  }
  return gs_choiceModalDialog(message,choices,callback,blockui_opts,cancel_callback,gs_modalJson_cb);
//  for (i in choices) {
//    message += '<input type="button" onclick="return gs_modalJson_cb('+i+');" value="'+choices[i].label+'" />'
//  }
//  if (!blockui_opts) {
//    blockui_opts = {};
//  }
//  jQuery.blockUI({message: message, css: blockui_opts.css});
//  return;
}

/**
 * Calls a jsonp url, just like $.getJson, but also looks out for errors.
 * The call is made in a make-believe synchronous fashion, by adding a semi-transparent overlay and disabling controls.
 */
function gs_modalJson (url, data, callback) {
  if (!gs_loadBlockUI (function () {gs_modalJson(url,data,callback);})) {
    return;
  }
  jQuery.blockUI();
  var cb = function (result, rv) {
    jQuery.unblockUI();
    if (callback) {
      callback(result, rv);
    }
  }
  gs_json (url, data, cb);
}

function gs_json (url, data, callback) {
  var cb = function (result) {
    return function (data, textStatus, errorThrown) {
      if (callback) {
        callback (result, result ? data:errorThrown || textStatus);
      }
    }
  }

  return jQuery.ajax({
      type: data ? "POST":"GET",
        url: url,
        data: data,
        success: cb(true),
        error: cb(false),
        dataType: "jsonp",
        traditional: true
        });
}

/**
 * Trims text to a maximum length, or up to the first line break optionally
 * hyst is an hysteresis value stating the minimum trimmed nr of chars for trimming to occur.
 */
function gs_text_trim (text, length, hyst, nobreakline, snl) {
  if (hyst === undefined) {
    hyst = 0;
  }
  var p = nobreakline && text.indexOf('\n') || -1;
  var trimmed = text;
  // Cut to newline?
  if (p>0 && p<length) {
    length = p;
  }
  // Enough gain to actually apply the trim?
  if (length+hyst < text.length) {
    text = text.substring(0, length) + '...';
  }

  return snl && text.replace(/\n/g, snl) || text;
}

/**
 * Grabs details for a specific image and prepares a bunch of links.
 */
function gs_getResultLineLinks (data, baseurl, renderurl) {
  if (data === null || data.datasetId === null || data.projectId === null) {
    return null;
  }
  if (renderurl == null) {
    renderurl = baseurl;
  }
    var figurl;
    var imgurl;
    if (data.screenId && data.screenId != 0) {
        figurl = baseurl+'browse/'+data.projectId+'/S'+data.screenId+'/P'+data.datasetId+'/'
        imgurl = baseurl+'browse/'+data.projectId+'/S'+data.screenId+'/'+data.imageId+'/';
    } else {
        figurl = baseurl+'browse/'+data.projectId+'/'+data.datasetId+'/'
	imgurl = baseurl+'browse/'+data.projectId+'/'+data.datasetId+'/'+data.imageId+'/';
    }
  return {
    figure: figurl,
    img: imgurl,
    thumb: renderurl+'render_thumbnail/'+data.imageId+'/',
    viewer: baseurl+'img_detail/'+data.imageId+'/'+data.datasetId+'/',
    paper: baseurl+'browse/'+data.projectId+'/',
    fv_click: function (did, iid) {
      return function () {
        gs_popViewer(did, iid, baseurl);
        return false;
      };
    }
    };
};
        
/**
 * Grabs details for a specific image and prepares add a DOM node and descendants for search results like l&f.
 */
function gs_showResultLine (container, data, baseurl, renderurl) {
  if (data === null || data.datasetId === null || data.projectId === null) {
    return null;
  }
  var result = jQuery('<div class="search-result">').appendTo(container);
  var head = jQuery('<div class="search-result-header">').appendTo(result);
  data['links'] = gs_getResultLineLinks(data, baseurl, renderurl);
  head.append('<a href="'+data.links.paper+'">- '+data.project+' -</a>');
  head.append('<div class="detail">'+gs_text_trim(data.projectDescription,100)+'</div>');
  head.append('<a href="'+data.links.img+'"><img src="'+data.links.thumb+'" /></a>');
  var detail = jQuery('<div class="search-result-detail">').appendTo(result);
  detail.append('<a href="'+data.links.img+'" alt="Open complete figure">'+data.dataset+' : '+data.name+'</a>');
  detail.append('<div class="detail">'+gs_text_trim(data.description,250,false,' ')+'</div>');
  var foot = jQuery('<div class="search-result-footnotes"><span> [ </span></div>').appendTo(result);
  var fv = jQuery('<a href="'+data.links.viewer+'" alt="Open Full Viewer">Full Viewer</a>').appendTo(foot);
  foot.append('&nbsp;<a href="'+data.links.paper+'" alt="Paper">Paper</a>&nbsp;');
  fv.click(data.links.fv_click(data.datasetId, data.imageId));
  foot.append('<a href="'+data.links.figure+'" alt="Figure">Figure</a>&nbsp;');
  foot.append('<span>] by <i>'+data.author+'</i> - <i>'+data.timestamp+'</i></span>');
  return result;
};
        
/**
 * Open the full viewer for a specific image.
 * Passing the dataset is needed to allow showing 'Figure List' on the viewer toolbar.
 */
function gs_popViewer (did, iid, baseurl) {
  if (iid == null) {
    return true;
  }
  if (did == null && typeof iid == 'string') {
    iid = iid.split('/');
    did = parseInt(iid[1]);
    iid = parseInt(iid[0]);
  }
  var w = window.open(baseurl+'img_detail/' + iid + '/' + did, '_blank',
              "toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes, scrollbars=yes,resizable=yes,width=800,height=800");
  return false;
}


/**
 * Search images and fill in results.
 */
function gs_searchImgs (text, baseurl, renderurl, result_cb) {
  if (text.length > 0) {
    jQuery('#search-results-summary').removeClass('ajax-error').html('searching for "'+text+'"');
    jQuery('#search-results').html('<img src="../img/ajax-loader.gif" alt="loading..." />');
    if (renderurl == null) {
      renderurl = baseurl;
    }
    $.getJSON(baseurl+'search/', {text: text, ctx: 'imgs', grabData: true, key: 'meta'}, function(data) {
shown = 0;
      if (data.length) {
        jQuery('#search-results').html('');
        for (e in data) {
          var elm = gs_showResultLine(jQuery('#search-results'), data[e], baseurl, renderurl);
          if (elm != null) {
            result_cb && result_cb(data[e], elm);
            shown++;
          }
        }
      }
      if (shown == 0) {
        jQuery('#search-results').html('no results');
        jQuery('#search-results-summary').html('search for "'+text+'": no results.');
      } else {
        jQuery('#search-results-summary').html('search for "'+text+'":<br /> showing 1 to '+shown+' of '+shown+' total.');
      }
    });
  }
}

function downloadLandingDialog (anchor, msg, cb) {
    if (!msg) {
	msg = "<h2>Your download will start in a few moments</h2>";
    }
    var ccb = function (e) {
	cb && cb(e);
    }
    gs_choiceModalDialog(msg,
                         [{label: 'close', data: 1}],
			 ccb,
			 {css: {width: '50%', left: '25%'}}
			); 
    if (anchor) {
	var dliframe = $('iframe[name=dliframe]');
	if (!dliframe.length) {
	    dliframe = $('<iframe name="dliframe" width="0" height="0"></iframe>').appendTo('body');
	}
	dliframe.attr('src', $(anchor).attr('href'));
	//var w = window.open($(anchor).attr('href'));
	//location.href = $(anchor).attr('href');
    }
    return false;
}

