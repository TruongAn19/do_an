package com.example.quanly.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.repository.RacketRepository;
// import com.example.quanly.service.spectification.ProductSpec;

@Service
public class RacketService {
    @Autowired
    private RacketRepository racketRepository;

    public List<Racket> getAvailableRacketsByCourt(Product court) {
        return racketRepository.findByProductAndAvailableTrue(court);
    }

    // Phụ kiện
    public Page<Racket> getAllRacket(Pageable pageable) {
        return racketRepository.findAll(pageable);
    }

    public Racket handSaveRacket(Racket racket) {
        return this.racketRepository.save(racket);
    }

    public void deleteRacket(long racketId) {
        this.racketRepository.deleteById(racketId);
    }

    public Optional<Racket> getRacketById(long racketId) {
        return this.racketRepository.findById(racketId);
    }

    // public Page<Product> getAllByProductWithSpec(Pageable pageable, Racket re) {
    //     if (re.getFactory() == null
    //             && productCriteriaDTO.getPrice() == null) {
    //         return this.racketRepository.findAll(pageable);
    //     }

    //     Specification<Product> combinedSpec = Specification.where(null);

    //     // if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
    //     //     Specification<Product> currentSpecs = ProductSpec.matchListFactory(productCriteriaDTO.getFactory().get());
    //     //     combinedSpec = combinedSpec.and(currentSpecs);
    //     // }

    //     if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
    //         Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
    //         combinedSpec = combinedSpec.and(currentSpecs);
    //     }

    //     combinedSpec = combinedSpec.and(ProductSpec.addressIsNullOrEmpty());

    //     return this.racketRepository.findAll(combinedSpec, pageable);
    // }
}
