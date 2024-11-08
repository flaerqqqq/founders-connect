package com.example.foundersconnect;

import org.springframework.boot.SpringApplication;

public class TestFoundersConnectApplication {

    public static void main(String[] args) {
        SpringApplication.from(FoundersConnectApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
