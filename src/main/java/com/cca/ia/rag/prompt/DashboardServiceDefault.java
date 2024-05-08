package com.cca.ia.rag.prompt;

import com.cca.ia.rag.prompt.dto.DashboardStatsDto;
import com.cca.ia.rag.prompt.model.DashboardStatsEntity;
import com.cca.ia.rag.prompt.model.DashboardStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceDefault implements DashboardService {

    @Autowired
    private DashboardStatsRepository dashboardStatsRepository;

    @Override
    public DashboardStatsDto getStats() {

        DashboardStatsDto result = new DashboardStatsDto();

        List<DashboardStatsEntity> stats = (List<DashboardStatsEntity>) dashboardStatsRepository.findAll();
        fillStats(result, stats);

        result.setTopTags(dashboardStatsRepository.getTopTags());
        result.setTopAuthors(dashboardStatsRepository.getTopPersons());
        result.setTopViews(dashboardStatsRepository.getTopViews());

        return result;
    }

    private void fillStats(DashboardStatsDto result, List<DashboardStatsEntity> stats) {

        List<DashboardStatsDto.ChartData> dataList = new ArrayList<>();

        for (DashboardStatsEntity stat : stats) {
            dataList.add(new DashboardStatsDto.ChartDataImpl(stat.getCode(), stat.getValue()));
        }

        result.setStatsData(dataList);

    }
}
