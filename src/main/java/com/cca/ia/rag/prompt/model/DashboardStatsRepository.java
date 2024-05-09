package com.cca.ia.rag.prompt.model;

import com.cca.ia.rag.prompt.dto.DashboardStatsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface DashboardStatsRepository extends Repository<PromptEntity, Long> {

    @Query(value = "select null as id, 'total_prompts' as label, count(1) as value from prompt union all " //
            + "select null as id, 'last_prompts' as label, count(1) as value from prompt p where (p.date >= cast((now() - interval 1 month) as date)) union all " //
            + "select null as id, 'total_tags' as label, count(1) as value from prompt_tag union all " //
            + "select null as id, 'last_tags' as label, count(1) as value from prompt_tag where prompt_tag.prompt_id in (select p.id from prompt p where (p.date >= cast((now() - interval 1 month) as date))) union all " //
            + "select null as id, 'total_authors' as label, count(distinct prompt.person_id) as value from prompt union all " //
            + "select null as id, 'last_authors' as label, count(distinct p.person_id) as value from prompt p where (p.date >= cast((now() - interval 1 month) as date)) union all " //
            + "select null as id, 'total_views' as label, count(1) as value from prompt_view union all " //
            + "select null as id, 'last_views' as label, count(1) as value from prompt_view p where (p.date >= cast((now() - interval 1 month) as date))", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getGlobalStats();

    @Query(value = "select id as id, tag as label, count(1) as value from prompt_tag group by id, tag order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopTags();

    @Query(value = "select ap.id as id, concat(ap.name, ' ', ap.lastname) as label, count(1) as value from prompt p join personal.all_person ap on p.person_id = ap.id group by ap.id, concat(ap.name, ' ', ap.lastname) order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopPersons();

    @Query(value = "select p.id as id, p.title as label, count(1) as value from prompt_view pv join prompt p on pv.prompt_id = p.id group by p.id, p.title order by count(1) desc limit 10", nativeQuery = true)
    List<DashboardStatsDto.ChartData> getTopViews();

}
