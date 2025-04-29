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

    public List<Racket> getAvailableRacketsByCourt(Long courtId) {
        return racketRepository.findByProductAndAvailableTrue(courtId);
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
    public Integer countRacket() {
        Integer total = this.racketRepository.countRackeQuantity();
        return total == null ? 0 : total;
    }

}
