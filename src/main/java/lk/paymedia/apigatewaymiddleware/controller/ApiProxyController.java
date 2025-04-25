package lk.dk.apigatewaymiddleware.controller;

import lk.dk.apigatewaymiddleware.service.ApiProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ApiProxyController {

    private final ApiProxyService apiProxyService;

    public ApiProxyController(ApiProxyService apiProxyService) {
        this.apiProxyService = apiProxyService;
    }

    @PostMapping("/**")
    public ResponseEntity<String> handlePost(HttpServletRequest request, @RequestBody String body) {
        String path = request.getRequestURI().replace("/api", "");
        return apiProxyService.forwardRequest(path, "POST", body);
    }

    @GetMapping("/**")
    public ResponseEntity<String> handleGet(HttpServletRequest request) {
        String path = request.getRequestURI().replace("/api", "");
        return apiProxyService.forwardRequest(path, "GET", null);
    }
}
