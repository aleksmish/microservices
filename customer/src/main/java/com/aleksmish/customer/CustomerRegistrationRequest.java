package com.aleksmish.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}
