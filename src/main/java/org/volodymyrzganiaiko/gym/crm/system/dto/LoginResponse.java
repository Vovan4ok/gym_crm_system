package org.volodymyrzganiaiko.gym.crm.system.dto;

public record LoginResponse(String token) {
    @Override
    public String toString() {
        return "LoginResponse{}";
    }
}
