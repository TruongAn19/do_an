//package com.example.quanly.service;
//
//import com.example.quanly.domain.MatchParticipant;
//import com.example.quanly.repository.MatchParticipantRepository;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
//public class MatchParticipantService {
//
//    MatchParticipantRepository participantRepo;
//
//    public MatchParticipant joinPost(MatchParticipant participant) {
//        return participantRepo.save(participant);
//    }
//
//    public List<MatchParticipant> getParticipants(Long postId) {
//        return participantRepo.findByMatchPostId(postId);
//    }
//}
