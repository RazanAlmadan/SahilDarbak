package com.example.sahldarbak.DTO.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityModerationDTO {

    private boolean approved;
    private String reason;
}
