import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RuleMigrationService {

    @Autowired
    private RuleMigrationRepository ruleMigrationRepository;

    public String migrateRules() {
        try {
            // Load the master file from the classpath
            File masterFile = new File(getClass().getClassLoader().getResource("rules_master.xlsx").getFile());

            // Fetch the latest processed version from the database
            Optional<RuleMigrationEntry> latestMigration = ruleMigrationRepository.findTopByOrderByProcessedAtDesc();
            int latestProcessedVersion = latestMigration.map(entry -> extractVersionNumber(entry.getVersion())).orElse(0);

            // Fetch all version files and sort them
            List<File> versionFiles = getAllVersionFiles();

            // Filter version files to only include those with versions greater than the latest processed version
            List<File> newVersionFiles = versionFiles.stream()
                    .filter(file -> extractVersionNumber(file) > latestProcessedVersion)
                    .sorted(Comparator.comparing(this::extractVersionNumber)) // Ensure sorted order
                    .collect(Collectors.toList());

            // Check if there are any new versions to process
            if (newVersionFiles.isEmpty()) {
                return "No new versions to migrate.";
            }

            // Process new versions and merge into the master file
            mergeExcelFiles(masterFile, newVersionFiles);

            // Save migration entry with processedAt timestamp for the latest version
            String latestVersion = newVersionFiles.get(newVersionFiles.size() - 1).getName();
            ruleMigrationRepository.save(new RuleMigrationEntry(latestVersion, LocalDateTime.now()));

            return "Rules migration completed successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during rule migration: " + e.getMessage();
        }
    }

    private List<File> getAllVersionFiles() throws Exception {
        // Get all resources from the classpath that match the version file naming convention
        URL resourceURL = getClass().getClassLoader().getResource("");
        File resourceDirectory = Paths.get(resourceURL.toURI()).toFile();

        File[] files = resourceDirectory.listFiles((dir, name) -> name.startsWith("rules_v") && name.endsWith(".xlsx"));

        if (files != null) {
            return List.of(files);
        }
        return new ArrayList<>();
    }

    private int extractVersionNumber(File file) {
        String fileName = file.getName();
        String versionPart = fileName.replace("rules_v", "").replace(".xlsx", "");
        return Integer.parseInt(versionPart);
    }

    private void mergeExcelFiles(File masterFile, List<File> versionFiles) throws IOException {
        // Load the master Excel file
        try (FileInputStream masterFileStream = new FileInputStream(masterFile);
             Workbook masterWorkbook = WorkbookFactory.create(masterFileStream)) {

            for (File versionFile : versionFiles) {
                try (FileInputStream versionFileStream = new FileInputStream(versionFile);
                     Workbook versionWorkbook = WorkbookFactory.create(versionFileStream)) {

                    mergeWorkbooks(masterWorkbook, versionWorkbook);
                }
            }

            // Save the updated master workbook back to the file system
            try (FileOutputStream masterOutputStream = new FileOutputStream(masterFile)) {
                masterWorkbook.write(masterOutputStream);
            }
        }
    }

    private void mergeWorkbooks(Workbook masterWorkbook, Workbook versionWorkbook) {
        Sheet masterSheet = masterWorkbook.getSheetAt(0);
        Sheet versionSheet = versionWorkbook.getSheetAt(0);

        int masterLastRow = masterSheet.getLastRowNum();
        for (int i = 0; i <= versionSheet.getLastRowNum(); i++) {
            Row versionRow = versionSheet.getRow(i);
            Row newRow = masterSheet.createRow(++masterLastRow);
            
            // Copy each cell from the version row to the master row
            for (int j = 0; j < versionRow.getLastCellNum(); j++) {
                Cell newCell = newRow.createCell(j);
                copyCell(versionRow.getCell(j), newCell);
            }
        }
    }

    private void copyCell(Cell source, Cell destination) {
        switch (source.getCellType()) {
            case STRING -> destination.setCellValue(source.getStringCellValue());
            case NUMERIC -> destination.setCellValue(source.getNumericCellValue());
            case BOOLEAN -> destination.setCellValue(source.getBooleanCellValue());
            case FORMULA -> destination.setCellFormula(source.getCellFormula());
            default -> destination.setCellValue(source.toString());
        }
    }

    public Optional<RuleMigrationEntry> getLatestMigration() {
        return ruleMigrationRepository.findTopByOrderByProcessedAtDesc();
    }
}
