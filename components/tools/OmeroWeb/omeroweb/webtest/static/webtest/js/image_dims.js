$(document).ready(function() {
   
   $(".spim_name").hide();
   $(".stagePos").hide();
   
   $("#show_all_stage_pos").click(function() {
       $(".stagePos").toggle();
   });
   
   var image_viewer = null;
   
   // double-click opens image-viewer (if not open) or sets image, z, t on open image.
   $(".img_panel").dblclick(function() {
       
       var izct = $(this).attr('izct').replace("(","").replace(")","");     // i,z,c,t
       var ids = izct.split(", ");
       var t = parseInt(ids[3]) + 1;    // viewer uses 1-based index
       var z = parseInt(ids[1]) + 1;
       var viewerUrl = '/webgateway/img_detail/' + parseInt(ids[0]) + "?z=" + z +"&t=" + t;
       
       if (image_viewer == null) {
           image_viewer=window.open(viewerUrl,'','height=550,width=600,right=50');
       }
       else {
           image_viewer.location.href = viewerUrl;
       }
       return false;
   });
   
   // single click on an image highlights it, selects plane in viewer (if open) and displays spim data
   $(".img_panel").click(function() {
       
       // select panel
       $(".img_panel").removeClass('selected_panel');
       $(this).addClass('selected_panel');
       
       // need IDs for the image, z, c, t plane just clicked. 
       var izct = $(this).attr('izct').replace("(","").replace(")","");     // i,z,c,t
       var ids = izct.split(", ");
          
       // show in viewer if open
       if (image_viewer != null) {
           var t = parseInt(ids[3]) + 1;    // viewer uses 1-based index
           var z = parseInt(ids[1]) + 1;
           var viewerUrl = '/webgateway/img_detail/' + parseInt(ids[0]) + "?z=" + z +"&t=" + t;
           image_viewer.location.href = viewerUrl;
       }
       
       // get ID and show name
       var name_id = "#name" + parseInt(ids[0]);
       $(".spim_name").hide().filter(name_id).show();
       
       // Stage position - if Channel is None, show all channels: filter by: starts-with iz and ends-with t
       if (ids[2] == "None") {
           var start_sel = '[izct^="'+ ids[0] + "," + ids[1] + '"]';
           var end_sel = '[izct$="'+ ids[3] + '"]';
           $(".stagePos").hide().filter(start_sel).filter(end_sel).show();
       } else {
            ids[2] = parseInt(ids[2]) - 1;
            var selector = '[izct="'+ ids.join(",") + '"]';
            $(".stagePos").hide().filter(selector).show();
       }
       return false;
   });
});