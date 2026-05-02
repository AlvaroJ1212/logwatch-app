package com.logwatch.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int totalRows;
    private int importedRows;
    private int rejectedRows;
    private List<String> errors = new ArrayList<>();

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getImportedRows() { return importedRows; }
    public void setImportedRows(int importedRows) { this.importedRows = importedRows; }

    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public void addError(String error) { this.errors.add(error); }
}
