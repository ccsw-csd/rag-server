package com.cca.ia.rag.prompt.model;

import com.cca.ia.rag.prompt.dto.DashboardStatsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DashboardStatsRepository extends CrudRepository<DashboardStatsEntity, String> {

    @Query(value = "select id as id, tag as label, count(1) as value from prompt_tag group by id, tag order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopTags();

    @Query(value = "select ap.id as id, concat(ap.name, ' ', ap.lastname) as label, count(1) as value from prompt p join personal.all_person ap on p.person_id = ap.id group by ap.id, concat(ap.name, ' ', ap.lastname) order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopPersons();

    @Query(value = "select p.id as id, p.title as label, count(1) as value from prompt_view pv join prompt p on pv.prompt_id = p.id group by p.id, p.title order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopViews();

}
