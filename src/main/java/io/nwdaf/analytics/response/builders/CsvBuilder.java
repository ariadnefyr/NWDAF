package io.nwdaf.analytics.response.builders;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvBuilder {

    public static void writeCsv(
            List<Long> timestamps,
            List<String> uesActive,
            List<String> s5cRxCreateSession,
            List<String> cpuSeconds,
            List<String> memoryBytes,
            List<String> ueCount,
            List<String> cpuPercentage,
            List<String> memoryUsage,
            int maxRows
    ) {
        try (FileWriter writer = new FileWriter("metrics.csv")) {
            writer.write("timestamp,uesActive,s5cRxCreateSession,cpuSeconds,memoryBytes,ueCount,cpuPercentage,memoryUsage\n");
            for (int i = 0; i < maxRows; i++) {
                writer.write(getTimestamp(timestamps, i) + ",");
                writer.write(getMetricValue(uesActive, i) + ",");
                writer.write(getMetricValue(s5cRxCreateSession, i) + ",");
                writer.write(getMetricValue(cpuSeconds, i) + ",");
                writer.write(getMetricValue(memoryBytes, i) + ",");
                writer.write(getMetricValue(ueCount, i) + ",");
                writer.write(getMetricValue(cpuPercentage, i) + ",");
                writer.write(getMetricValue(memoryUsage, i) + "\n");
            }
            System.out.println("CSV file created: metrics.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimestamp(List<Long> timestamps, int index) {
        if (index >= timestamps.size()) return "";
        return java.time.Instant.ofEpochMilli(timestamps.get(index)).toString();
    }

    private static String getMetricValue(List<String> list, int index) {
        if (index >= list.size()) return "";
        String entry = list.get(index);
        int valueIdx = entry.indexOf("value=");
        if (valueIdx == -1) return "";
        int commaIdx = entry.indexOf(",", valueIdx);
        if (commaIdx == -1) commaIdx = entry.indexOf("]", valueIdx);
        if (commaIdx == -1) return "";
        return entry.substring(valueIdx + 6, commaIdx).trim();
    }
}