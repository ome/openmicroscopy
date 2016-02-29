import React from 'react';

var Pagination = React.createClass({

    handleNextPage: function() {
        this.props.setPage(this.props.page + 1);
    },

    handlePrevPage: function() {
        this.props.setPage(this.props.page - 1);
    },

    handleChangePage: function(event) {
        var page = parseInt(event.target.getAttribute('value'), 10);
        this.props.setPage(page);
    },

    render: function() {

        var page = this.props.page,
            pageSize = this.props.pageSize,
            imgCount = this.props.filteredCount,
            pageCount = Math.ceil(imgCount/pageSize)

        // pagination not supported/needed, or container is loading etc
        if (!page || !imgCount || pageCount === 1) {
            return (
                <div />
            )
        }

        var prevEnabled = page > 1,
            nextEnabled = page < pageCount;

        var pages = [];
        for (let p=1; p<=pageCount; p++) {
            pages.push(
                <input
                    key={p}
                    disabled={p === page ? "disabled": false}
                    onClick={this.handleChangePage}
                    className="button_pagination"
                    type="button"
                    value={ p }/>);
        }

        return (
            <div>
                <div className="clear" />
                <div className="paging">
                    {"Page: " + page}
                    <button disabled={!prevEnabled} onClick={this.handlePrevPage}>Prev</button>
                    <button disabled={!nextEnabled} onClick={this.handleNextPage}>Next</button>
                    <br />
                    {pages}
                </div>
            </div>
        )
    }
});

export default Pagination
