package org.kmb.eventhub.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResponseList<T> {
    private List<T> list;
    private Long total;
    private Integer currentPage;
    private Integer pageSize;
}
