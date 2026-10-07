package com.example.sahldarbak.DTO;

import com.example.sahldarbak.Model.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockedUserDTO {
    private Integer blockId;
    private Profile profile;
}