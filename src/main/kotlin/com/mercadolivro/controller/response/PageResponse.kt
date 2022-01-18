package com.mercadolivro.controller.response

data class PageResponse<T> (
    var items: List<T>,
    var totalPages: Int,
    var totalItems: Long,
    var currentPage: Int
)