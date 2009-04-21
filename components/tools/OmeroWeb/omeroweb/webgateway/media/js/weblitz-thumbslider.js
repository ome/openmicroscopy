/**
 * Weblitz thumbslider
 *
 * Depends on jquery, jquery-plugin-thumbslider
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */


/**
 * @event thumbClick
 * @event thumbNew
 */
;jQuery.fn.WeblitzThumbSlider = function (options) {
  return this.each(function(){
      var opts = jQuery.extend({
            baseurl: '',
            withParts: true,
            filterPart: null,
            addEmptyPart: false
            }, options);
      var _this = this;
      var self = jQuery(this);
      _this.reset = function () {
        _this.parts = {0: 0};
        $(_this).unbind('thumbNew').unbind('partCreated').unbind('thumbClick').unbind('thumbError');
      };
      
      _this.reset();

      _this.loadThumbs = function (dataset) {
        jQuery.getJSON(opts.baseurl+'/dataset/'+dataset+'/children/', {baseurl: opts.baseurl}, function(data) {
            var imgclick = function (data) {
              return function () {
                if (jQuery(this).is('.ts-focus')) {
                  return;
                }
                self.trigger('thumbClick', [data, this]);
                var fb = $(this).parents('div.thumbslider');
                $.ThumbSlider(fb).setFocus($(this));
              };
            };
            var loaderror = function (thumb) {
              self.trigger('thumbError', thumb);
            };
            for (i in data) {
              if (opts.filterPart && opts.filterPart != data[i].part) {
                continue;
              }
              var tslider;
              var elm;
              var partCreated=false;
              var thisPart = opts.withParts?data[i].part:'';
              if (_this.parts[thisPart] === undefined) {
                if (thisPart != '') {
                  jQuery('<div class="figure-box-part">'+thisPart+'</div>').appendTo(self);
                  elm = jQuery('<div class="figure-box" id="fb-'+dataset+'-'+_this.parts[0]+'">').appendTo(self);
                  partCreated=true;
                } else {
                  elm = jQuery('<div class="figure-box" id="fb-'+dataset+'-'+_this.parts[0]+'">').prependTo(self);
                  if (opts.addEmptyPart) {
                    jQuery('<div class="figure-box-part"></div>').prependTo(self);
                  }
                  partCreated=true;
                }
                elm.show();
                tslider = jQuery.ThumbSlider(elm);
                tslider.clear();
                tslider.addThumb('<img src="/appmedia/webgateway/img/ajax-loader.gif" alt="loading..."\
                                   class="ajax-wait" />');
                _this.parts[thisPart] = {elm: elm, idx: _this.parts[0]};
                _this.parts[0]++;
                elm.one('thumbsLoaded', function() {
                    jQuery('.ajax-wait', jQuery(this)).remove();
                    elm.trigger('images-loaded');
                  });
              } else {
                elm = _this.parts[thisPart].elm;
                tslider = jQuery.ThumbSlider(elm);
              }

              data[i]['part_idx'] = _this.parts[thisPart].idx;
              var img = jQuery('<img id="thumb-'+dataset+'-'+data[i].id+'" src="'+data[i].thumb_url+'" style="display: none"  name="'+data[i].name+'">');
              img.click(imgclick(data[i]));
              img = tslider.addThumb(img, null, loaderror);
              partCreated && self.trigger('partCreated', [elm, thisPart, _this.parts[thisPart].idx]);
              self.trigger('thumbNew', [data[i], img]);
            }
          });
      };
    });
};
