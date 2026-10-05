package com.example.sahldarbak.DTO;

import com.example.sahldarbak.Model.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedInviteDTO {
    private Integer inviteId;
    private String message;
    private String status;
    private LocalDate createdAt;
    private Profile sender;
}