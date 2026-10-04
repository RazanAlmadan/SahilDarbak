package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Service.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/child")
@RequiredArgsConstructor
public class ChildController {


    private final ChildService childService;


    // GET ALL
    @GetMapping("/get-children")
    public ResponseEntity<?> getChildren() {
        return ResponseEntity.status(200).body(childService.getChildren());
    }


    // ADD
    @PostMapping("/add-child/{travelRequestId}")
    public ResponseEntity<?> addChild(@PathVariable Integer travelRequestId, @Valid @RequestBody Child child) {
        childService.addChild(travelRequestId, child);
        return ResponseEntity.status(200).body(new ApiResponse("child added successfully"));
    }


    // UPDATE
    @PutMapping("/update-child/{childId}")
    public ResponseEntity<?> updateChild(@PathVariable Integer childId, @Valid @RequestBody Child child) {
        childService.updateChild(childId, child);
        return ResponseEntity.status(200).body(new ApiResponse("child updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete-child/{childId}")
    public ResponseEntity<?> deleteChild(@PathVariable Integer childId) {
        childService.deleteChild(childId);
        return ResponseEntity.status(200).body(new ApiResponse("child deleted successfully"));
    }
}
