package com.project.demo.rest.reportes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportesController {

    private final JdbcTemplate jdbc;

    @Autowired
    public ReportesController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record RowDTO(String canton, long total) {}

     @GetMapping(value="/abandonados", produces=MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> abandonadosJson(
    @RequestParam long from, @RequestParam long to,
    @RequestParam(required=false) Long cantonId,
    @RequestParam(required=false) Long municipalityId,
    @RequestParam(required=false) String speciesId,
    @RequestParam(required=false) String district
) {
    try {
        var q = buildSql(from, to, cantonId, municipalityId, speciesId, district);
        List<RowDTO> result = jdbc.query(q.sql, q.params.toArray(),
            (rs, i) -> new RowDTO(rs.getString("canton"), rs.getLong("total")));
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
    @GetMapping(value="/abandonados.csv", produces="text/csv; charset=UTF-8")
    public ResponseEntity<String> abandonadosCsv(
        @RequestParam long from, @RequestParam long to,
        @RequestParam(required=false) Long cantonId,
        @RequestParam(required=false) Long municipalityId,
        @RequestParam(required=false) String speciesId,
        @RequestParam(required=false) String district
    ) {
        var q = buildSql(from, to, cantonId, municipalityId, speciesId, district);
        var rows = jdbc.queryForList(q.sql, q.params.toArray());
        StringBuilder csv = new StringBuilder("\uFEFFcanton,total\n");
        for (var r : rows) {
            String canton = String.valueOf(r.get("canton")).replace("\"", "\"\"");
            csv.append("\"").append(canton).append("\",").append(r.get("total")).append("\n");
        }
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=abandonados.csv")
            .body(csv.toString());
    }

    private static class Q { String sql; List<Object> params; }
    private Q buildSql(long from, long to, Long cantonId, Long municipalityId,
                      String speciesId, String district) {
        StringBuilder sql = new StringBuilder("""
    SELECT c.name AS canton, COUNT(*) AS total
    FROM abandoned_animal aa
    JOIN canton c ON c.id = aa.canton_id
    WHERE CAST(aa.is_abandoned AS UNSIGNED) = 1
      AND aa.created_at BETWEEN FROM_UNIXTIME(?/1000) AND FROM_UNIXTIME(?/1000)
""");
        var params = new ArrayList<Object>();
        params.add(from); params.add(to);

        if (cantonId != null) { sql.append(" AND aa.canton_id = ? "); params.add(cantonId); }
        if (municipalityId != null) { sql.append(" AND m.id = ? "); params.add(municipalityId); }

        java.util.function.Function<String, List<String>> splitCsv = s ->
            Arrays.stream(s.split(",")).map(String::trim)
                .filter(v -> !v.isEmpty() && !"__all".equalsIgnoreCase(v)).toList();

        if (speciesId != null && !speciesId.isBlank()) {
            var ids = splitCsv.apply(speciesId);
            if (!ids.isEmpty()) {
                sql.append(" AND aa.species_id IN (")
                   .append("?,".repeat(ids.size()).replaceFirst(",$", "")).append(") ");
                params.addAll(ids);
            }
        }
        if (district != null && !district.isBlank()) {
            var dists = splitCsv.apply(district);
            if (!dists.isEmpty()) {
                sql.append(" AND aa.district IN (")
                   .append("?,".repeat(dists.size()).replaceFirst(",$", "")).append(") ");
                params.addAll(dists);
            }
        }
        sql.append(" GROUP BY c.name ORDER BY total DESC ");
        var q = new Q(); q.sql = sql.toString(); q.params = params; return q;
    }
}