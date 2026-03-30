package com.project.order_processing_app.controller;

import com.project.order_processing_app.dto.response.DashboardResponse;
import com.project.order_processing_app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Applies ADMIN guard to ALL methods in this controller
public class AdminController {

    private final DashboardService dashboardService;

    

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}