package com.qa.framework.core.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parallel TestNG data providers for JSON, Excel, CSV, and YAML under {@code testdata/} on the classpath.
 */
public final class DataProviderUtils {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private DataProviderUtils() {
    }

    @DataProvider(name = "json", parallel = true)
    public static Iterator<Object[]> json(org.testng.ITestContext context) throws IOException {
        String path = requirePath(context, "jsonPath");
        return fromJson(path);
    }

    @DataProvider(name = "csv", parallel = true)
    public static Iterator<Object[]> csv(org.testng.ITestContext context) throws IOException {
        String path = requirePath(context, "csvPath");
        return fromCsv(path);
    }

    @DataProvider(name = "excel", parallel = true)
    public static Iterator<Object[]> excel(org.testng.ITestContext context) throws IOException {
        String path = requirePath(context, "excelPath");
        String sheet = context.getCurrentXmlTest().getParameter("sheet");
        return fromExcel(path, sheet);
    }

    @DataProvider(name = "yaml", parallel = true)
    public static Iterator<Object[]> yaml(org.testng.ITestContext context) throws IOException {
        String path = requirePath(context, "yamlPath");
        return fromYaml(path);
    }

    public static Iterator<Object[]> fromJson(String classpathResource) throws IOException {
        JsonNode root = JSON.readTree(requireResource(classpathResource));
        List<Object[]> rows = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                rows.add(new Object[]{JSON.convertValue(node, Map.class)});
            }
        } else {
            rows.add(new Object[]{JSON.convertValue(root, Map.class)});
        }
        return rows.iterator();
    }

    public static Iterator<Object[]> fromYaml(String classpathResource) throws IOException {
        JsonNode root = YAML.readTree(requireResource(classpathResource));
        List<Object[]> rows = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                rows.add(new Object[]{YAML.convertValue(node, Map.class)});
            }
        } else {
            rows.add(new Object[]{YAML.convertValue(root, Map.class)});
        }
        return rows.iterator();
    }

    public static Iterator<Object[]> fromCsv(String classpathResource) throws IOException {
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(new InputStreamReader(requireResource(classpathResource), StandardCharsets.UTF_8))) {
            List<Object[]> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> map = new LinkedHashMap<>();
                record.toMap().forEach(map::put);
                rows.add(new Object[]{map});
            }
            return rows.iterator();
        }
    }

    public static Iterator<Object[]> fromExcel(String classpathResource, String sheetName) throws IOException {
        try (InputStream in = requireResource(classpathResource);
             Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = sheetName == null || sheetName.isBlank()
                    ? workbook.getSheetAt(0)
                    : workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return List.<Object[]>of().iterator();
            }
            Row header = rowIterator.next();
            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            header.forEach(cell -> headers.add(formatter.formatCellValue(cell)));

            List<Object[]> rows = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    map.put(headers.get(i), cell == null ? "" : formatter.formatCellValue(cell));
                }
                rows.add(new Object[]{map});
            }
            return rows.iterator();
        }
    }

    private static String requirePath(org.testng.ITestContext context, String parameter) {
        String path = context.getCurrentXmlTest().getParameter(parameter);
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Missing TestNG parameter: " + parameter);
        }
        return path;
    }

    private static InputStream requireResource(String classpathResource) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
        }
        return in;
    }
}
