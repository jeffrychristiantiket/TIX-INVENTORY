package com.tiket.inventory.lib;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteCsvFileHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteCsvFileHelper.class);

  public static String convertToCSV(String[] data, String delimiter) {
    return Stream.of(data)
        .map(WriteCsvFileHelper::escapeSpecialCharacters)
        .collect(Collectors.joining(delimiter));
  }

  public static String escapeSpecialCharacters(String data) {
    if (isNull(data)){
      return "";
    }
    String escapedData = data.replaceAll("\\R", " ");
    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
      data = data.replace("\"", "\"\"");
      escapedData = "\"" + data + "\"";
    }
    return escapedData;
  }

  public static void writeCsvFile(String[] header, List<String[]> dataLines, String csvFileDir, String fileName)
      throws FileNotFoundException {
    //create dir
    File dir = new File(csvFileDir);
    if (!dir.exists()) {
      dir.mkdir();
    }
    //write file
    File csvOutputFile = new File(csvFileDir + fileName);

    if (header != null && header.length > 0) {
      dataLines.add(0, header);
    }

    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      dataLines.stream()
          .map(data -> WriteCsvFileHelper.convertToCSV(data, ","))
          .forEach(pw::println);
    } catch (FileNotFoundException e) {
      LOGGER.error("ERROR at writeCsvFile : ", e);
      throw e;
    }
  }
}
