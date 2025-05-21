package com.example.quanly.domain.dto;

import java.util.List;
import java.util.Optional;

public class ProductCriteriaDTO {
    private Optional<String> page;
    private Optional<List<String>> address;
    private Optional<List<String>> price;
    private Optional<String> sort;
    public Optional<String> getPage() {
        return page;
    }
    public void setPage(Optional<String> page) {
        this.page = page;
    }
    public Optional<List<String>> getAddress() {
        return address;
    }
    public void setAddress(Optional<List<String>> address) {
        this.address = address;
    }
    public Optional<List<String>> getPrice() {
        return price;
    }
    public void setPrice(Optional<List<String>> price) {
        this.price = price;
    }
    public Optional<String> getSort() {
        return sort;
    }
    public void setSort(Optional<String> sort) {
        this.sort = sort;
    }

    
}
