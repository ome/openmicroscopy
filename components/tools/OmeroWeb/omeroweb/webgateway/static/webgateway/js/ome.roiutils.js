// get a shape configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_shape_config = function(stroke_width, stroke_alpha, stroke_color, fill_alpha, fill_color) {
    // default configuration for shape config
    var stroke_width = typeof stroke_width !== "undefined" ? stroke_width : 1.0;
    var stroke_alpha = typeof stroke_alpha !== "undefined" ? stroke_alpha : 0.765625;
    var stroke_color = typeof stroke_color !== "undefined" ? stroke_color : "#c4c4c4";
    var fill_alpha = typeof fill_alpha !== "undefined" ? fill_alpha : 0.25;
    var fill_color = typeof fill_color !== "undefined" ? fill_color : "#000000";

    return {
        "strokeWidth": stroke_width,
        "strokeAlpha": stroke_alpha,
        "strokeColor": stroke_color,
        "fillAlpha": fill_alpha,
        "fillColor": fill_color
    };
}

// generic shape configuration: Z, T, transform and shape type
$.fn.get_generic_shape = function(transform, z_plane, t_plane, shape_type) {
    return {
        "transform": typeof transform !== "undefined" ? transform : "none",
        "theZ": z_plane,
        "theT": t_plane,
        "type": shape_type
    };
}

// utility function to get default font size based on image's width and height
$.fn.get_font_size = function(img_width, img_height) {
    var max_font_size = 512;
    var default_font_size = 12

    if (img_width && img_height) {
        if (img_width <= max_font_size && img_height <= max_font_size)
            return default_font_size;
        var rx = default_font_size*(img_width/max_font_size);
        var ry = default_font_size*(img_height/max_font_size);

        return Math.floor(Math.max(rx, ry));
    } else {
        return default_font_size;
    }
}

// generic text configuration: text value, font family, font size and style
$.fn.get_text_config = function(text_value, font_family, font_size, font_style) {
    // default font family and font_style
    var ffamily = typeof font_family !== "undefined" ? font_family : "sans-serif";
    var fstyle = typeof font_style !== "undefined" ? font_style : "Normal";
    var fsize = typeof font_size !== "undefined" ? font_size : $.fn.get_font_size();
    return {
        "fontFamily": ffamily,
        "fontStyle": fstyle,
        "fontSize": fsize,
        "textValue": text_value
    };
}

// add a dictionary with a text definition to given shape
$.fn.add_text_to_shape = function(shape_conf, text_conf) {
    $.extend(shape_conf, text_conf);
    return shape_conf;
}

// get a rectangle configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_rectangle = function(x, y, height, width, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var rect_conf = {
        "x": x,
        "y": y,
        "height": height,
        "width": width
    };

    $.extend(rect_conf, $.fn.get_generic_shape(transform, z_plane, t_plane, "Rectangle"));
    $.extend(rect_conf, shape_config);

    return rect_conf;
}

// get a point configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_point = function(center_x, center_y, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var point_conf = {
        "cx": center_x,
        "cy": center_y
    };

    $.extend(point_conf, $.fn.get_generic_shape(transform, z_plane, t_plane, "Point"));
    $.extend(point_conf, shape_config);

    return point_conf;
}

// get an ellipse configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_ellipse = function(center_x, center_y, radius_x, radius_y, z_plane, t_plane,
                         transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var ellipse_conf = {
        "rx": radius_x,
        "ry": radius_y
    };

    $.extend(ellipse_conf, $.fn.get_ome_point(center_x, center_y, z_plane, t_plane,
        transform, shape_config));
    ellipse_conf.type = "Ellipse";

    return ellipse_conf;
}

// get a line configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_line = function(x1, y1, x2, y2, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var line_conf = {
        "x1": x1,
        "y1": y1,
        "x2": x2,
        "y2": y2
    };

    $.extend(line_conf, $.fn.get_generic_shape(transform, z_plane, t_plane, "Line"));
    $.extend(line_conf, shape_config);

    return line_conf;
}

// get a label configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_label = function(x, y, text_value, z_plane, t_plane, transform,
                              font_family, font_size, font_style, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var text_conf = $.fn.get_text_config(text_value, font_family, font_size, font_style);

    var label_conf = {
        "x": x,
        "y": y
    };

    $.extend(label_conf, $.fn.get_generic_shape(transform, z_plane, t_plane, "Label"));
    $.extend(label_conf, shape_config);
    label_conf = $.fn.add_text_to_shape(label_conf, text_conf);

    return label_conf;
}

// get a polygon configuration described as a dictionary compatible with OMERO.web viewer
$.fn.get_ome_polygon = function(points, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : $.fn.get_shape_config();

    var polygon_conf = {
        "point": points
    };

    $.extend(polygon_conf, $.fn.get_generic_shape(transform, z_plane, t_plane, "Polygon"));
    $.extend(polygon_conf, shape_config);

    return polygon_conf;
}