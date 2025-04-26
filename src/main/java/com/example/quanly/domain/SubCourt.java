package com.example.quanly.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "sub_courts")
public class SubCourt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // VD: "Sân 1", "Sân 2", ...

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "subCourt", cascade = CascadeType.ALL)
    private List<SubCourtAvailableTime> subCourtAvailableTimes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<SubCourtAvailableTime> getSubCourtAvailableTimes() {
        return subCourtAvailableTimes;
    }

    public void setSubCourtAvailableTimes(List<SubCourtAvailableTime> subCourtAvailableTimes) {
        this.subCourtAvailableTimes = subCourtAvailableTimes;
    }

    
}
