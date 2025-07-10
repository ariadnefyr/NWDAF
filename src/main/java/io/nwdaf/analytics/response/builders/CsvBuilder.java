package io.nwdaf.analytics.response.builders;

import io.nwdaf.analytics.model.NsiLoadLevelInfo;
import io.nwdaf.analytics.model.ResourceUsage;
import io.nwdaf.analytics.model.NumberAverage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvBuilder {

    public static void writeCsv(
            List<Long> timestamps,
            List<NsiLoadLevelInfo> nsiLoadLevelInfos,
            int maxRows
    ) {
        try (FileWriter writer = new FileWriter("nsi_load_level_metrics.csv")) {
            writer.write("timestamp,cpuUsage,memoryUsage,numOfUes,numOfPduSess,numOfExceedLoadLevelThr,exceedLoadLevelThrInd,resUsgThrCrossTimePeriod,confidence\n");
            //writer.write("timestamp,snssai,nsiId,cpuUsage,memoryUsage,numOfUes,numOfPduSess,numOfExceedLoadLevelThr,exceedLoadLevelThrInd,resUsgThrCrossTimePeriod,confidence\n");
            for (int i = 0; i < maxRows && i < nsiLoadLevelInfos.size() && i < timestamps.size(); i++) {
                NsiLoadLevelInfo info = nsiLoadLevelInfos.get(i);
                String resUsgThrCrossTimePeriodStr = "";
                if (info.getResUsgThrCrossTimePeriod() != null && !info.getResUsgThrCrossTimePeriod().isEmpty()) {
                    // Concatenate all time windows as [startTime-endTime]...
                    StringBuilder sb = new StringBuilder();
                    info.getResUsgThrCrossTimePeriod().forEach(tw -> {
                        sb.append("[")
                          .append(tw.getStartTime() != null ? tw.getStartTime().toString() : "")
                          .append(" - ")
                          .append(tw.getStopTime() != null ? tw.getStopTime().toString() : "")
                          .append("]");
                    });
                    resUsgThrCrossTimePeriodStr = sb.toString();
                }
                writer.write(
                        //(info.getSnssai() != null ? info.getSnssai().toString() : "") + "," +
                        //(info.getNsiId() != null ? info.getNsiId() : "") + "," +
                        getTimestamp(timestamps, i) + "," +
                        (info.getResUsage() != null && info.getResUsage().getCpuUsage() != null ? info.getResUsage().getCpuUsage() : "") + "," +
                        (info.getResUsage() != null && info.getResUsage().getMemoryUsage() != null ? info.getResUsage().getMemoryUsage() : "") + "," +
                        (info.getNumOfUes() != null && info.getNumOfUes().getNumber() != null ? info.getNumOfUes().getNumber() : "") + "," +
                        (info.getNumOfPduSess() != null && info.getNumOfPduSess().getNumber() != null ? info.getNumOfPduSess().getNumber() : "") + "," +
                        (info.getNumOfExceedLoadLevelThr() != null ? info.getNumOfExceedLoadLevelThr() : "") + "," +
                        (info.getExceedLoadLevelThrInd() != null ? info.getExceedLoadLevelThrInd() : "") + "," +
                        "\"" + resUsgThrCrossTimePeriodStr + "\"" + "," +
                        (info.getConfidence() != null ? info.getConfidence() : "") + "," + "\n"
                );
            }
            System.out.println("CSV file created: nsi_load_level_metrics.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimestamp(List<Long> timestamps, int index) {
        if (index >= timestamps.size()) return "";
        return java.time.Instant.ofEpochMilli(timestamps.get(index)).toString();
    }
}