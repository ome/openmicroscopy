
import React from 'react';

var ImageIcon = React.createClass({

    handleIconClick: function(event) {
        // this.setState ({selected: true});
        this.props.handleIconClick(this.props.image.id, event);
    },

    // getInitialState: function() {
    //     return {selected: this.props.image.selected};
    // },

    getIconSizes: function() {
        var image = this.props.image,
            width = this.props.iconSize,
            height = this.props.iconSize,
            wh = image.data.obj.sizeX / image.data.obj.sizeY;
        if (wh < 1) {
            width = width * wh;
        } else if (wh > 1) {
            height = height / wh;
        }
        return {'width': width, 'height': height};
    },

    // After rendering, scroll selectd icon into view
    // NB: scrollIntoViewIfNeeded() is provided by polyfill
    componentDidUpdate: function() {
        if (this.props.image.selected && this.refs.icon) {
            this.refs.icon.scrollIntoViewIfNeeded();
        }
    },

    render: function() {

        var image = this.props.image,
            iconSizes = this.getIconSizes(),
            cls = [],
            thumbVersion = image.thumbVersion,
            divStyle = {
                width: this.props.iconSize + "px",
                height: this.props.iconSize + "px",
            };

        // if thumb is not yet created, don't cache!
        if (thumbVersion === -1) {
            thumbVersion = (Math.random() + "").slice(2);
        }

        if (image.selected) {cls.push('ui-selected');}
        if (image.fsSelected) {cls.push('fs-selected');}

        return (
            <li className={"row " + cls.join(" ")}
                id={"image_icon-" + image.id}
                ref="icon"
                data-fileset={image.data.obj.filesetId}
                data-type="image"
                data-id={image.id}
                data-perms={image.data.obj.permsCss}
                tabIndex={0}
                onClick={this.handleIconClick}
            >
                <div className="image" style={divStyle}>
                    <img alt="image"
                        width={iconSizes.width + "px"}
                        height={iconSizes.height + "px"}
                        src={"/webgateway/render_thumbnail/" + image.id + "/?version=" + image.thumbVersion}
                        title={image.name} />
                </div>
                <div className="desc" valign="middle">
                    {image.name}
                    <span className="hidden_sort_text">{image.name}</span>
                </div>
                <div className="date" valign="middle">{image.date}</div>
                <div className="sizeX" valign="middle">{image.data.obj.sizeX}</div>
                <div className="sizeY" valign="middle">{image.data.obj.sizeY}</div>
                <div className="sizeZ" valign="middle">{image.data.obj.sizeZ}</div>
            </li>
        )
    }
});

export default ImageIcon
