function get_shape_config(stroke_width, stroke_alpha, stroke_color, fill_alpha, fill_color) {
    // default configuration for shape config
    stroke_width = typeof stroke_width !== "undefined" ? stroke_width : 1.0;
    stroke_alpha = typeof stroke_alpha !== "undefined" ? stroke_alpha : 0.765625;
    stroke_color = typeof stroke_color !== "undefined" ? stroke_color : "#c4c4c4";
    fill_alpha = typeof fill_alpha !== "undefined" ? fill_alpha : 0.25;
    fill_color = typeof fill_color !== "undefined" ? fill_color : "#000000";

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
        "transform": typeof transform == "undefined" ? transform : "none",
        "theZ": z_plane,
        "theT": t_plane,
        "type": shape_type
    };
}

function get_ome_rectangle(x, y, height, width, z_plane, t_plane, transform, shape_config) {
    // if no shape_config was given, use the default one
    shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

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

function get_ome_ellipse(center_x, center_y, radius_x, radius_y, transform,
                         z_plane, t_plane, shape_config) {
    // if no shape_config was given, use the default one
    shape_config = typeof shape_config !== "undefined" ? shape_config : get_shape_config();

    var ellipse_conf = {
        "cx": center_x,
        "cy": center_y,
        "rx": radius_x,
        "ry": radius_y
    };

    $.extend(ellipse_conf, get_generic_shape(transform, z_plane, t_plane, "Ellipse"));
    $.extend(ellipse_conf, shape_config);

    return ellipse_conf;
}

