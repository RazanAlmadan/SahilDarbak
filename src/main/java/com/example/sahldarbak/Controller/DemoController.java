package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @DeleteMapping("/reset")
    public ResponseEntity<?> reset() {
        demoService.reset();
        return ResponseEntity.status(200).body("demo reset done");
    }
}