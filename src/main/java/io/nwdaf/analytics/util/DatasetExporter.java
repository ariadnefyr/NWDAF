package io.nwdaf.analytics.util;

import io.nwdaf.analytics.model.NfLoadLevelInformation;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatasetExporter {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public static void writeToCsv(List<NfLoadLevelInformation> infoList, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("timestamp,nfSetId,nfCpuUsage\n");

            String timestamp = formatter.format(Instant.now());
            for (NfLoadLevelInformation info : infoList) {
                writer.write(String.format("%s,%s,%d\n",
                        timestamp,
                        info.getNfSetId(),
                        info.getNfCpuUsage()));
            }

            System.out.println("Dataset written to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
