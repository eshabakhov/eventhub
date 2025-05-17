import React from "react";

const Pagination = ({totalPages, currentPage, handlePageClick}) => {
    return (
        <div className={`pagination-controls ${totalPages < 2 ? "hidden" : ""}`}>
            <button
                className="back-forward"
                onClick={() => handlePageClick(currentPage - 1)}
                disabled={currentPage === 1}
            >
                &lt;
            </button>

            {/* Первая страница */}
            <button
                className="page-button"
                onClick={() => handlePageClick(1)}
                disabled={currentPage === 1}
            >
                1
            </button>

            {/* Многоточие после первой страницы */}
            {currentPage > 3 && <span className="pagination-ellipsis">...</span>}

            {/* Текущая - 1 */}
            {currentPage > 2 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(currentPage - 1)}
                >
                    {currentPage - 1}
                </button>
            )}

            {/* Текущая страница */}
            {currentPage !== 1 && currentPage !== totalPages && (
                <button className="page-button active" disabled>
                    {currentPage}
                </button>
            )}

            {/* Текущая + 1 */}
            {currentPage < totalPages - 1 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(currentPage + 1)}
                >
                    {currentPage + 1}
                </button>
            )}

            {/* Многоточие перед последней страницей */}
            {currentPage < totalPages - 2 && <span className="pagination-ellipsis">...</span>}

            {/* Последняя страница */}
            {totalPages > 1 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(totalPages)}
                    disabled={currentPage === totalPages}
                >
                    {totalPages}
                </button>
            )}

            <button
                className="back-forward"
                onClick={() => handlePageClick(currentPage + 1)}
                disabled={currentPage === totalPages}
            >
                &gt;
            </button>
        </div>
    );
}

export default Pagination;