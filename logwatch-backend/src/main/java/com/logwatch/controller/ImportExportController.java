package com.logwatch.controller;

import com.logwatch.dto.ImportResult;
import com.logwatch.service.ImportExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/import")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @PostMapping("/csv")
    public ResponseEntity<ImportResult> importCsv(@RequestParam("file") MultipartFile file)
            throws IOException {
        ImportResult result = importExportService.importCsv(file.getInputStream());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/json")
    public ResponseEntity<ImportResult> importJson(@RequestParam("file") MultipartFile file)
            throws IOException {
        ImportResult result = importExportService.importJson(file.getInputStream());
        return ResponseEntity.ok(result);
    }
}
