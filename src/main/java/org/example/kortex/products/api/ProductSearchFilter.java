package org.example.kortex.products.api;



public record ProductSearchFilter(String category,
                                  String query,
                                  Integer size,
                                  Integer page) {
}
