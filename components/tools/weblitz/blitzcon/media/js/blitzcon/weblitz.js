/**
 * Weblitz dependency resolver
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */

function include_js_dom(url) {
  jQuery.getScript(url);
  return false;
}

function include_css_dom(url) {
    var doc = document.getElementsByTagName('head').item(0);
    var tag = document.createElement('link');
    tag.setAttribute('rel', 'stylesheet');
    tag.setAttribute('type', 'text/css');
    tag.setAttribute('media', 'all');
    tag.setAttribute('href', url);
    doc.appendChild(tag);
    return false;
}

var includes = [//["window['jQuery']", '/media/js/blitzcon/jquery-1.2.1.js', null],
            ["jQuery.fn.offsetLite",
             '/media/js/blitzcon/jquery.dimensions.js', null]
            ,["jQuery.fn.viewportImage",
              '/media/js/blitzcon/jquery-plugin-viewportImage.js', null]
            ,["jQuery.fn.slider",
              '/media/js/blitzcon/jquery-plugin-slider.js', '/media/css/blitzcon/jquery-plugin-slider.css']
            ,["jQuery.fn.WeblitzViewport",
              '/media/js/blitzcon/weblitz-viewport.js', '/media/css/blitzcon/weblitz-viewport.css']
            ,["jQuery.fn.ThumbSlider",
              '/media/js/blitzcon/jquery-plugin-thumbslider.js', '/media/css/blitzcon/jquery-plugin-thumbslider.css']
            //,["jQuery.fn.autocomplete", '/media/js/blitzcon/jquery.autocomplete.js', '/media/css/blitzcon/jquery.autocomplete.css']
            //,["jQuery.fn.aaa", '/media/js/blitzcon/jquery.date_input.js', '/media/css/blitzcon/date_input.css']

           ];


function check_deps () {
  var doall = false;
  for (i in includes) {
    if (doall || eval(includes[i][0]) == undefined) {
      doall = true;
      include_js_dom(includes[i][1]);
      if (includes[i][2]) {
        include_css_dom(includes[i][2]);
      }
    }
  }
}

check_deps();
