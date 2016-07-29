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
    var that = this;

    var colors = ["FF0000", "00FF00", "0000FF", "FFFFFF", "FFFF00", "EE82EE"];
    var colorNames = ["red", "green", "blue", "white", "yellow", "magenta"];
    // these are the lutNames we know about and are included in the lut preview
    var lutNames = ["16_colors.lut",
        "3-3-2_rgb.lut",
        "5_ramps.lut",
        "6_shades.lut",
        "blue_orange_icb.lut",
        "brgbcmyw.lut",
        "cool.lut",
        "cyan_hot.lut",
        "edges.lut",
        "fire.lut",
        "gem.lut",
        "grays.lut",
        "green_fire_blue.lut",
        "hilo.lut",
        "ica.lut",
        "ica2.lut",
        "ica3.lut",
        "ice.lut",
        "magenta_hot.lut",
        "orange_hot.lut",
        "phase.lut",
        "rainbow_rgb.lut",
        "red-green.lut",
        "red_hot.lut",
        "royal.lut",
        "sepia.lut",
        "smart.lut",
        "spectrum.lut",
        "thal.lut",
        "thallium.lut",
        "unionjack.lut",
        "yellow_hot.lut"];
    var picker = null;

    /* The basic setup */
    var self = jQuery(this);

    var callback = function (color) {
      self.attr('data-picked-color', color);
      jQuery('input#'+self[0].cfg.prefix+'-tb').attr('value', color.substring(1).toUpperCase());
    };

    var ok_callback = function () {
      // On 'OK' we get the color saved by 'callback' above and apply it to the color-btn, then trigger
      var data_color = self.attr('data-picked-color');
      if (data_color) {
        self.attr('data-color', data_color);
        self.trigger('changed');
      }
    };

    var null_cb = function (color) {};

    this._prepare_picker = function () {
      jQuery("body").prepend('<div class="'+this.cfg.prefix+'" id="'+this.cfg.prefix+'-box"></div>');
      var box = jQuery("#"+this.cfg.prefix+"-box").append('<h1>Choose color</h1>');
      box.postit();

      // Add Lookup Table list - gets populated in show_picker() below.
      $('<div id="' + this.cfg.prefix + '-luts" class="lutpicker"></div>').appendTo(box);

      // Colorpicker - uses farbtastic.js
      var $cpickerPane = $("<div class='cpickerPane'></div>").appendTo(box);
      $('<div id="'+this.cfg.prefix+'"></div>').appendTo($cpickerPane);
      $cpickerPane.append('<div style="text-align: center;">Hex RGB <input type="text" id="'+this.cfg.prefix+'-tb" /></div>');
      $('<button id="cbpicker-OK-btn" style="float:right">OK</button>').appendTo($cpickerPane);
      $('<button style="float:right">Cancel</button>').appendTo($cpickerPane).click(function(){
        jQuery("#"+that.cfg.prefix+"-box").hide();
      });
      // Don't show Color-picker initially
      $cpickerPane.hide();

      self.trigger('prepared');
      picker = jQuery.farbtastic("#"+this.cfg.prefix);
      jQuery('input#'+this.cfg.prefix+'-tb').bind('change', function () {
          var new_color = sanitizeHexColor(jQuery('input#'+self[0].cfg.prefix+'-tb').attr('value'));
          if (new_color !== null) {
            picker.setColor(new_color);
            jQuery(this).attr('value', new_color.substring(1).toUpperCase());
          } else {
            jQuery(this).attr('value', picker.pack(picker.rgb).substring(1).toUpperCase());
          }
        });
    };

    this.show_picker = function () {
      if (!picker) {
        if (jQuery('#'+this.cfg.prefix+'-box').length === 0) {
          this._prepare_picker();
        } else {
          picker = jQuery.farbtastic("#"+this.cfg.prefix);
        }
      }

      // lookup LUTs
      var $luts = $("#" + this.cfg.prefix + "-luts");
      if ($luts.is(':empty')) {
        $.getJSON(cfg.server + '/luts/', function(data){
          var colorRows = [];
          for (var e=0; e<colors.length; e++) {
            var c = colors[e],
              n = colorNames[e];
            colorRows.push('<div><input id="' + c + '" type="radio" name="lut" value="' + c + '"><label for="' + c + '"><span style="background: #' + c + '"> &nbsp</span>' + n + '</label></div>');
          }
          var lutRows = data.luts.map(function(lut){
            var idx = lutNames.indexOf(lut.name);
            var preview = '';
            // background image is luts_10.png which is 10 pixels per lut(row) but size is set to 200% so each row is 20 pixels
            if (idx > -1) {
              preview = 'class="lutPreview" style="background-position: 0 -' + (idx * 20) + 'px"';
            }
            var lutHtml = '<div><input id="' + lut.name + '" type="radio" name="lut" value="' + lut.name + '">';
            lutHtml += '<label for="' + lut.name + '">';
            lutHtml += '<span ' + preview + '> &nbsp</span>';
            lutHtml += (lut.name.replace('.lut', '')) + '</label></div>';
            return lutHtml;
          });
          var html = '<div>' + colorRows.join("") + lutRows.join("") + '</div>';
          $luts.html(html);
        });
      }

      // bind appropriate handler (wraps ref to button)
      $("#cbpicker-OK-btn").unbind('click').bind('click', ok_callback)
        .bind('click',function(){
          jQuery("#"+that.cfg.prefix+"-box").hide();
        });
      self.removeAttr('data-picked-color');

      var color = '#' + OME.rgbToHex(self.attr("data-color"));
      picker.linkTo(null_cb).setColor(color).linkTo(callback);
      jQuery("#"+this.cfg.prefix+"-tb").attr('value', color.substring(1).toUpperCase());
      jQuery("#"+this.cfg.prefix+"-defc").css("background-color", self.css("background-color"));
      jQuery("#"+this.cfg.prefix+"-box").mousedown(function () {self.trigger('mousedown');}).show();
      jQuery("#"+this.cfg.prefix+"-box").unbind('closed').bind('closed', function () {self.trigger('hiding');});
      self.trigger('showing');
      //self.addClass('picking');
    };

    this.hide_picker = function () {
      jQuery("#"+this.cfg.prefix+"-box").hide();
      //self.removeClass('picking');
    };

    /* Event handlers */
    self.click(this.show_picker);
  });
};
