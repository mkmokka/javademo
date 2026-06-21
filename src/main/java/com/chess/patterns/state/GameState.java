package com.chess.patterns.state;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface GameState {
    @JsonProperty("statusDescription")
    String getStatusDescription();
    boolean canMove();
}
