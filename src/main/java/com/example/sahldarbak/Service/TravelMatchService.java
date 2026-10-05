package com.example.sahldarbak.Service;

import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.TravelMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelMatchService {
    private final TravelMatchRepository travelMatchRepository;

    public List<TravelMatch> get(){ return travelMatchRepository.findAll();}
}
