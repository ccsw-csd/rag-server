package com.cca.ia.rag.chat.database;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class DynamicRepositoryDefault implements DynamicRepository {

    @Override
    public String launchQuery(String query, String url, String username, String password) throws Exception {

        if (query.indexOf("```sql") != -1) {
            query = query.substring(query.indexOf("```sql") + 6, query.lastIndexOf("```"));
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Los resultados obtenidos son:");
        sb.append("<br/><br/>\n");

        try {
            JdbcTemplate jdbcTemplate = jdbcTemplate(url, username, password);

            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

            if (result.size() == 0)
                sb.append("No se han encontrado resultados con esa query\n");
            else if (result.size() == 1)
                sb.append(parseDetail(result));
            else
                sb.append(parseTable(result));

        } catch (Exception e) {
            throw e;
        }

        sb.append("<br/><br/>\n");
        sb.append("<details><summary>Query generada por IA</summary>\n");
        sb.append("\n```sql\n");
        sb.append(query + "\n\n");
        sb.append("```\n</details>\n");
        sb.append("<br/><br/>\n");

        return sb.toString();
    }

    private String parseDetail(List<Map<String, Object>> result) {
        boolean isNumeric = true;
        StringBuilder sb = new StringBuilder();

        List<String> labels = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        sb.append("| Campo | Valor |\n");
        sb.append("| :-- | :------- |\n");

        Map<String, Object> row = result.get(0);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            sb.append("| " + entry.getKey() + " | " + normalizeText(String.valueOf(entry.getValue())) + " |\n");

            labels.add(entry.getKey());
            values.add(entry.getValue());

            isNumeric = isNumeric && isNumeric(String.valueOf(entry.getValue()));
        }

        sb.append("<br/><br/>\n");

        if (isNumeric) {
            sb.append(generateChart(labels, values));
            sb.append("<br/><br/>\n");
        }

        return sb.toString();
    }

    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private String generateChart(List<String> labels, List<Object> values) {

        if (labels.size() <= 2)
            return "";

        StringBuilder sb = new StringBuilder();

        sb.append("```mermaid\n");
        sb.append("xychart-beta horizontal\n");

        boolean isFirst = true;
        sb.append("x-axis [");

        for (String label : labels) {

            if (isFirst == false) {
                sb.append(", ");
            }
            sb.append("\"" + label + "\"");

            isFirst = false;
        }
        sb.append("]\n");

        isFirst = true;
        sb.append("bar [");

        for (Object value : values) {

            if (isFirst == false) {
                sb.append(", ");
            }
            sb.append(value);

            isFirst = false;
        }

        sb.append("]\n");
        sb.append("```\n\n");

        return sb.toString();
    }

    private String parseTable(List<Map<String, Object>> result) {
        StringBuilder sb = new StringBuilder();

        boolean isChartable = result.get(0).entrySet().size() == 2;

        List<String> labels = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        sb.append("|");

        for (Map.Entry<String, Object> entry : result.get(0).entrySet()) {
            sb.append(entry.getKey() + "|");
        }

        sb.append("\n");

        sb.append("|");

        for (Map.Entry<String, Object> entry : result.get(0).entrySet()) {
            sb.append(":");

            for (int i = 0; i < entry.getKey().length() - 1; i++)
                sb.append("-");

            sb.append("|");
        }

        sb.append("\n");

        boolean isLabel = true;
        boolean isNumeric = true;

        for (Map<String, Object> row : result) {
            sb.append("|");

            for (Map.Entry<String, Object> entry : row.entrySet()) {

                if (isChartable && isLabel == true) {
                    isLabel = false;
                    labels.add(String.valueOf(entry.getValue()));
                } else if (isChartable && isLabel == false) {
                    isLabel = true;
                    values.add(entry.getValue());
                    isNumeric = isNumeric && isNumeric(String.valueOf(entry.getValue()));
                }

                sb.append(clipText(normalizeText(String.valueOf(entry.getValue()))) + " | ");
            }

            sb.append("\n");
        }

        sb.append("<br/><br/>\n");

        if (isNumeric && isChartable) {
            sb.append(generateChart(labels, values));
            sb.append("<br/><br/>\n");
        }

        return sb.toString();
    }

    private String clipText(String text) {
        if (text == null)
            return "";

        if (text.length() > 50) {
            return text.substring(0, 50) + "...";
        }

        return text;

    }

    private String normalizeText(String text) {
        if (text == null)
            return "";

        return text.replaceAll("\n", " ").replaceAll("\r", " ");
    }

    private JdbcTemplate jdbcTemplate(String url, String username, String password) throws SQLException {

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.mariadb.jdbc.Driver");
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        DataSource dataSource = dataSourceBuilder.build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }
}
