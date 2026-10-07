package com.example.sahldarbak.Service;

import com.example.sahldarbak.Repository.BlockedUserRepository;
import com.example.sahldarbak.Repository.TravelMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final TravelMatchRepository travelMatchRepository;
    private final BlockedUserRepository blockedUserRepository;

    public void reset() {
        travelMatchRepository.deleteAll();
        blockedUserRepository.deleteAll();
    }
}