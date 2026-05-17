package com.unbags.ordering.ai.assistant;

import com.unbags.ordering.dto.DishResponse;

import java.util.ArrayList;
import java.util.List;

public class DishResolutionResult {

    private DishResponse dish;
    private List<DishResponse> candidates = new ArrayList<>();

    public DishResolutionResult() {
    }

    public DishResolutionResult(DishResponse dish, List<DishResponse> candidates) {
        this.dish = dish;
        this.candidates = candidates;
    }

    public DishResponse getDish() {
        return dish;
    }

    public void setDish(DishResponse dish) {
        this.dish = dish;
    }

    public List<DishResponse> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<DishResponse> candidates) {
        this.candidates = candidates;
    }

    public boolean hasUniqueDish() {
        return dish != null;
    }
}
