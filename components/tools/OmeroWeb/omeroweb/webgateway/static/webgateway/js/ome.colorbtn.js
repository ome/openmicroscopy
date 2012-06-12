/**
 * Colored button triggering a color picker plugin for JQuery.
 *
 * Todo: better documentation and example usage.
 *
 * Depends on jquery, farbtastic, jquery-plugin-postit
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

$.fn.colorbtn = function(cfg) {
  return this.each(function(){
    this.cfg = {
      prefix: cfg && cfg.prefix ? cfg.prefix : "cbpicker"
    };

    var colors = ["f00", "0f0", "00f", "fff", "000", "", "808080", "ffc800", "ff0", "4b0082", "ee82ee"];
    var picker = null;

    /* The basic setup */
    var self = jQuery(this);

    var callback = function (color) {
      self.css('background-color', color);
      jQuery('input#'+self[0].cfg.prefix+'-tb').attr('value', color.substring(1).toUpperCase());
      self.trigger('changed');
    };
    var null_cb = function (color) {};

    this._prepare_picker = function () {
      jQuery("body").prepend('<div class="'+this.cfg.prefix+'" id="'+this.cfg.prefix+'-box"></div>');
      var box = jQuery("#"+this.cfg.prefix+"-box").append('<h1>Choose color</h1><div id="'+this.cfg.prefix+'"></div>');
      box.postit().append('<div style="text-align: center;">Hex RGB <input type="text" id="'+this.cfg.prefix+'-tb" /></div><div style="text-align: center;"></div>');
      var btns = box.find('div:last');
      btns.append('<div class="postit-resize-bar"></div>');
      var btn_click = function () {
        picker.setColor('#'+rgbToHex(jQuery(this).css("background-color")));
      };
      btns.append('<span>Preset</span>');
      for (e in colors) {
        if (colors[e] == "") {
          btns.append('<br />');
          btns.append('<span>Colors</span>');
	} else {
          btns.append('<button style="background-color: #'+colors[e]+'">&nbsp;</button>');
          btns.find('button:last').click(btn_click);
        }
      }
      //btns.append('<br /><button style="width: 5em;" id="'+this.cfg.prefix+'-defc">&nbsp;</button>');
      //btns.find('button:last').click(btn_click);
      btns.append('<div class="postit-resize-bar"></div>');
      self.trigger('prepared');
      picker = jQuery.farbtastic("#"+this.cfg.prefix);
      jQuery('input#'+this.cfg.prefix+'-tb').bind('change', function () {
          var new_color = sanitizeHexColor(jQuery('input#'+self[0].cfg.prefix+'-tb').attr('value'));
          if (new_color != null) {
            picker.setColor(new_color);
            jQuery(this).attr('value', new_color.substring(1).toUpperCase());
          } else {
            jQuery(this).attr('value', picker.pack(picker.rgb).substring(1).toUpperCase());
          }
        });
    }

    this.show_picker = function () {
      if (!picker) {
        if (jQuery('#'+this.cfg.prefix+'-box').length == 0) {
          this._prepare_picker();
        } else {
          picker = jQuery.farbtastic("#"+this.cfg.prefix);
        }
      }
      var color = '#'+rgbToHex(self.css("background-color"));
      picker.linkTo(null_cb).setColor(color).linkTo(callback);
      jQuery("#"+this.cfg.prefix+"-tb").attr('value', color.substring(1).toUpperCase());
      jQuery("#"+this.cfg.prefix+"-defc").css("background-color", self.css("background-color"));
      jQuery("#"+this.cfg.prefix+"-box").mousedown(function () {self.trigger('mousedown');}).show();
      jQuery("#"+this.cfg.prefix+"-box").unbind('closed').bind('closed', function () {self.trigger('hiding');});
      self.trigger('showing');
      //self.addClass('picking');
    }

    this.hide_picker = function () {
      jQuery("#"+this.cfg.prefix+"-box").hide();
      //self.removeClass('picking');
    }

    /* Event handlers */
    self.click(this.show_picker);
  });
}

  function rgbToHex(rgb) {
    if (rgb.substring(0,1) == '#') {
      return rgb.substring(1);
    }
    var rgbvals = /rgb\((.+),(.+),(.+)\)/i.exec(rgb);
    if (!rgbvals) return rgb;
    var rval = parseInt(rgbvals[1]).toString(16);
    var gval = parseInt(rgbvals[2]).toString(16);
    var bval = parseInt(rgbvals[3]).toString(16);
    if (rval.length == 1) rval = '0' + rval;
    if (gval.length == 1) gval = '0' + gval;
    if (bval.length == 1) bval = '0' + bval;
    return (
      rval +
      gval +
      bval
    ).toUpperCase(); 
  }

