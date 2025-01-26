package com.example.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManipulationRule {
    private String matchType; // URL, Header, Body
    private String matchPattern; // Örnek: "User-Agent"
    private String action; // Add, Replace, Remove
    private String replacement; // Örnek: "Custom User-Agent"
}
