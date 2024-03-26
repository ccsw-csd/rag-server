package com.cca.ia.rag.ingestor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class IngestorController {

    private final IngestorService dataLoadingService;

    //private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IngestorController(IngestorServiceVectorDB dataLoadingService) {
        this.dataLoadingService = dataLoadingService;
        //this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/load")
    public ResponseEntity<String> load() {
        try {
            this.dataLoadingService.load();
            return ResponseEntity.ok("Data loaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while loading data: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public int count() {
        String sql = "SELECT COUNT(*) FROM vector_store";
        //this.dataLoadingService.get();
        //return jdbcTemplate.queryForObject(sql, Integer.class);
        return 1;
    }

    @PostMapping("/delete")
    public void delete() {
        String sql = "DELETE FROM vector_store";
        //jdbcTemplate.update(sql);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred in the controller: " + e.getMessage());
    }
}
