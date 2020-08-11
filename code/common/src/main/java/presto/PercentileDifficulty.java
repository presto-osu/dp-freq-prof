package presto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;

public interface PercentileDifficulty {
  default double[] readSortedDifficulties(int size, String constraintsCSVFile) {
    double[] difficulties = new double[size];
    CSVParser lpParser = new CSVParserBuilder().withSeparator('\t').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(constraintsCSVFile))
            .withCSVParser(lpParser)
            .build()) {
      String[] nextLine;
      int i = 0;
      while ((nextLine = reader.readNext()) != null) {
        difficulties[i++] = Double.parseDouble(nextLine[1]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Arrays.sort(difficulties);
    return difficulties;
  }
}
