package com.project.starforest.dto.store;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class requestCartDTO {
    private String user_email;
    
    private boolean result;
}