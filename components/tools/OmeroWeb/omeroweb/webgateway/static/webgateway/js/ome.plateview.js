/**
 * weblitz-plateview - Weblitz plate viewport
 *
 * Depends on jquery
 *
 * Copyright (c) 2011 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 * Author: Carlos Neves <carlos(at)glencoesoftware.com>
 */

/* Public constructors */

;jQuery.fn.WeblitzPlateview = function (options) {
  return this.each
  (
   function () {
     if (!this.WeblitzPlateview)
	 this.WeblitzPlateview = new jQuery._WeblitzPlateview (this, options);
   });
};

jQuery.WeblitzPlateview = function (elm, options) {
  var container = jQuery(elm).get(0);
  if (container === undefined) return null;
    var rv = container.WeblitzPlateview || (container.WeblitzPlateview = new jQuery._WeblitzPlateview(container, options));
  return rv;
};

/* (make believe) Private constructor */

/**
 * _WeblitzPlateview class created in the jQuery namespace holds all logic for interfacing with the weblitz
 * plate and ajax server.
 */

jQuery._WeblitzPlateview = function (container, options) {
  var opts = jQuery.extend({
      baseurl: '',
      width: 64,
      height: 48,
      useParentPrefix: true,
    }, options);

  opts.size = Math.max(opts.width, opts.height);
  this.self = jQuery(container);
  this.self.addClass('weblitz-plateview');
  this.origHTML = this.self.html();
  this.self.html("");
  var _this = this;
  var thisid = this.self.attr('id');

    var _reset = function (result, data) {
      _this.self.html("");
      var table = $('<table></table>').appendTo(_this.self);
      var tr = $('<tr></tr>').appendTo(table);
      tr.append('<th>&nbsp;</th>');
      for (i in data.collabels) {
	  tr.append('<th>'+data.collabels[i]+'</th>');
      }
      var tclick = function (tdata,thumb) {
	  return function () {
              _this.self.trigger('thumbClick', [tdata, this]);
	  }
      };
      for (i in data.rowlabels) {
	  tr = $('<tr></tr>').appendTo(table);
          tr.append('<th>'+data.rowlabels[i]+'</th>');
          for (j in data.grid[i]) {
              if (data.grid[i][j] == null) {
		  tr.append('<td><div class="placeholder" style="width:'+opts.width+'px;height:'+opts.height+'px;line-height:'+opts.height+'px;">&nbsp;</div></td>');
	      } else {
                  data.grid[i][j]._wellpos = data.rowlabels[i]+data.collabels[j];
                  var parentPrefix = '';
                  if (opts.useParentPrefix) {
                      parentPrefix = thisid+'-';
                  }
		  var td = $('<td class="well" id="'+parentPrefix+'well-'+data.grid[i][j].wellId+'"><div class="waiting" style="width:'+opts.width+'px;height:'+opts.height+'px;"></div><img id="'+parentPrefix+'image-'+data.grid[i][j].id+'" class="loading" src="'+ data.grid[i][j].thumb_url+'" name="'+(data.rowlabels[i] + data.collabels[j])+'"></td>');
		  $('img', td)
		      .click(tclick(data.grid[i][j]))
		      .load(function() { 
                                $(this).removeClass('loading').siblings().remove();
                          _this.self.trigger('thumbLoad', [$(this).parent(), $(this)]);
                           })
		      .data('wellpos', data.rowlabels[i] + data.collabels[j]);
		  tr.append(td);
		  _this.self.trigger('thumbNew', [data.grid[i][j], $('img', td)]);
	      }
	  }
      }
  }

  this.load = function (pid,field) {
      $('table img', _this.self).remove();
      $('table div.placeholder', _this.self).removeClass('placeholder').addClass('loading');
      if (field === undefined) {
	  field = 0;
      }
      gs_json(opts.baseurl+'/plate/'+pid+'/'+field+'/', {size:opts.size}, _reset);
      //gs_modalJson(opts.baseurl+'/plate/'+pid+'/'+field+'/?callback=?', {}, _reset);
      //jQuery.getJSON(opts.baseurl+'/plate/'+pid+'/'+field+'/?callback=?', _reset);
  }

  this.setFocus = function (elm, evt) {
    if (!$(this).data('noFocus')) {
	$('img', elm.parents('table').eq(0)).removeClass('pv-focus');
	elm.addClass('pv-focus');
    }
  };

  this.rmFocus = function (elm) {
    if (elm) {
      elm.removeClass('pv-focus');
    } else {
	$('img', _this.self).removeClass('pv-focus');
    }
  }
}
