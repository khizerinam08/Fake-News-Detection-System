package com.detector.utility;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StringController {

    @PostMapping("/send-receive")
    public Map<String, String> sendAndReceive(@RequestBody Map<String, String> request) {
        String receivedString = request.get("input");
        System.out.println("Received from Android: " + receivedString);

        // Perform your processing and calculate the contradiction score
        String responseString = "Your contradiction score is 0.85";  // Example response

        Map<String, String> response = new HashMap<>();
        response.put("response", responseString);
        return response;
    }
}
