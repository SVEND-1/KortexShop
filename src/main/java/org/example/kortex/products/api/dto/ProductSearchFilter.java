package org.example.kortex.products.api.dto;



public record ProductSearchFilter(String category,
                                  String query,
                                  Integer size,
                                  Integer page) {
}
