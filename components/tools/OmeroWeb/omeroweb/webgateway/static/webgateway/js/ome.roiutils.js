function get_shape_config(stroke_width, stroke_alpha, stroke_color, fill_alpha, fill_color) {
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

function get_generic_shape(transform, z_plane, t_plane, shape_type) {
    return {
        "transform": typeof transform !== "undefined" ? transform : "none",
        "theZ": z_plane,
        "theT": t_plane,
        "type": shape_type
    };
}

function get_ome_rectangle(x, y, height, width, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var rect_conf = {
        "x": x,
        "y": y,
        "height": height,
        "width": width
    };

    $.extend(rect_conf, get_generic_shape(transform, z_plane, t_plane, "Rectangle"));
    $.extend(rect_conf, shape_config);

    return rect_conf;
}

function get_ome_point(center_x, center_y, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var point_conf = {
        "cx": center_x,
        "cy": center_y,
    };

    $.extend(point_conf, get_generic_shape(transform, z_plane, t_plane, "Point"));
    $.extend(point_conf, shape_config);

    return point_conf;
}

function get_ome_ellipse(center_x, center_y, radius_x, radius_y, z_plane, t_plane,
                         transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var ellipse_conf = {
        "rx": radius_x,
        "ry": radius_y
    };

    $.extend(ellipse_conf, get_ome_point(center_x, center_y, transform, z_plane,
        t_plane, "Ellipse"));
    $.extend(ellipse_conf, shape_config);

    return ellipse_conf;
}

function get_ome_line(x1, y1, x2, y2, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var line_conf = {
        "x1": x1,
        "y1": y1,
        "x2": x2,
        "y2": y2
    };

    $.extend(line_conf, get_generic_shape(transform, z_plane, t_plane, "Line"));
    $.extend(line_conf, shape_config);

    return line_conf;
}

function get_ome_label(x, y, text_value, z_plane, t_plane, transform, font_family, font_size,
                       font_style, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    // default font family and font_style
    var ffamily = typeof font_family !== "undefined" ? font_family : "sans-serif";
    var fstyle = typeof font_style !== "undefined" ? font_style : "Normal";
    // TODO: check how to obtain a default font size
    var font_config = {
        "fontFamily": ffamily,
        "fontStyle": fstyle,
        "fontSize": font_size
    };

    var label_conf = {
        "x": x,
        "y": y,
        "textValue": text_value
    };

    $.extend(label_conf, font_config);
    $.extend(label_conf, get_generic_shape(transform, z_plane, t_plane, "Label"));
    $.extend(label_conf, shape_config);

    return label_conf;
}

function get_ome_polygon(points, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    var shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var polygon_conf = {
        "point": points
    };

    $.extend(polygon_conf, get_generic_shape(transform, z_plane, t_plane, "Polygon"));
    $.extend(polygon_conf, shape_config);

    return polygon_conf;
}