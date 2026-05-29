package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.PitchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubPitchDTO {
    private Long id;
    private String name;
    private PitchType pitchType;
    private Long productId;
}
