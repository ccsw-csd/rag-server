package com.cca.ia.rag.prompt.dto;

import java.util.List;

public class DashboardStatsDto {

    public static interface ChartData {

        String getLabel();

        Long getValue();

        Long getId();
    }

    public static class ChartDataImpl implements ChartData {
        private String label;
        private Long value;

        private Long id;

        public ChartDataImpl(String label, Long value) {
            this(label, value, null);
        }

        public ChartDataImpl(String label, Long value, Long id) {
            this.label = label;
            this.value = value;
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public Long getValue() {
            return value;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    private List<ChartData> statsData;

    private List<ChartData> topTags;

    private List<ChartData> topAuthors;

    private List<ChartData> topViews;

    public List<ChartData> getStatsData() {
        return statsData;
    }

    public void setStatsData(List<ChartData> statsData) {
        this.statsData = statsData;
    }

    public List<ChartData> getTopTags() {
        return topTags;
    }

    public void setTopTags(List<ChartData> topTags) {
        this.topTags = topTags;
    }

    public List<ChartData> getTopAuthors() {
        return topAuthors;
    }

    public void setTopAuthors(List<ChartData> topAuthors) {
        this.topAuthors = topAuthors;
    }

    public List<ChartData> getTopViews() {
        return topViews;
    }

    public void setTopViews(List<ChartData> topViews) {
        this.topViews = topViews;
    }
}
