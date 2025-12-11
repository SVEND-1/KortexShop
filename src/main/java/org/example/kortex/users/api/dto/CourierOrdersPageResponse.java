package org.example.kortex.users.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class CourierOrdersPageResponse {
    private List<CourierOrderDTO> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
}