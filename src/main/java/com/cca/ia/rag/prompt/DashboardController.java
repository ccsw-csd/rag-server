package com.cca.ia.rag.prompt;

import com.cca.ia.rag.prompt.dto.DashboardStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("")
    public DashboardStatsDto getStats() throws Exception {
        return dashboardService.getStats();
    }

}
