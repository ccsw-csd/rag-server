package com.cca.ia.rag.prompt;

import com.cca.ia.rag.prompt.dto.DashboardStatsDto;
import com.cca.ia.rag.prompt.model.DashboardStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceDefault implements DashboardService {

    @Autowired
    private DashboardStatsRepository dashboardStatsRepository;

    @Override
    public DashboardStatsDto getStats() {

        DashboardStatsDto result = new DashboardStatsDto();

        result.setStatsData(dashboardStatsRepository.getGlobalStats());
        result.setTopTags(dashboardStatsRepository.getTopTags());
        result.setTopAuthors(dashboardStatsRepository.getTopPersons());
        result.setTopViews(dashboardStatsRepository.getTopViews());

        return result;
    }

}
