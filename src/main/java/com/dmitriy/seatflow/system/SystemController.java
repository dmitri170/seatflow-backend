package com.dmitriy.seatflow.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/ping")
    public SystemStatusResponse ping() {
        return new SystemStatusResponse("UP", "seatflow-backend");
    }
}
