package com.bajajfinserv.health.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolutionRequest {
    @JsonProperty("finalQuery")
    private String finalQuery;
    
    public String getFinalQuery() {
        return finalQuery;
    }
    
    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
} 