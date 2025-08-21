package io.nwdaf.analytics.util;

import io.nwdaf.analytics.api.Targets;
import io.nwdaf.analytics.model.*;
import io.nwdaf.analytics.response.builders.CsvBuilder;
import io.nwdaf.analytics.response.builders.JsonBuilder;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;

public class PrometheusDataCollector {

    private static final int MAX_VALUES = 60;
    private static final int INTERVAL_SEC = 5;
    private final List<Long> timestamps = new ArrayList<>();
    private final List<NsiLoadLevelInfo> nsiLoadLevelInfos = new ArrayList<>();
    private int numOfExceedLoadLevelThr = 0;
    private List<Integer> exceedIndices = new ArrayList<>();
    private List<Long> exceedTimestamps = new ArrayList<>();

    public void startCollecting() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        String server_uop = new Targets().getUniversityOfPatrasPrometheus();
        String server_5GS = new Targets().getOpen5GSPrometheus();
        String server_all_metrics = new Targets().getAllMetricsPrometheus();

        Runnable fetchTask = () -> {
            long now = System.currentTimeMillis();
            timestamps.add(now);

            //MetricFetcher.fetchUesActive(server_5GS);
            //MetricFetcher.fetchCpuSeconds(server_5GS);
            //MetricFetcher.fetchMemoryBytes(server_5GS);
            //MetricFetcher.fetchS5cRxCreateSession(server_5GS);
            //MetricFetcher.fetchNgPduSessionResourceSetupResponse(server_all_metrics);

            MetricFetcher.fetchFivegsSmffunctionPdusessionCreationSucc(server_5GS);
            MetricFetcher.fetchUeCount(server_all_metrics);
            MetricFetcher.fetchCpuPercentage(server_all_metrics);
            MetricFetcher.fetchMemoryUsage(server_all_metrics);

            if (allMetricsCollected()) {
                scheduler.shutdown();
                System.out.println("All metrics collected, processing data...");

                // Map Prometheus metrics to NWDAF model objects
                nsiLoadLevelInfos.clear();
                for (int i = 0; i < MAX_VALUES; i++) {
                    try {
                        nsiLoadLevelInfos.add(mapToNsiLoadLevelInfo(i));
                        System.out.println("Mapped NsiLoadLevelInfo for index " + i);
                    } catch (Exception e) {
                        System.err.println("Exception in mapToNsiLoadLevelInfo at i=" + i + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Calculate resUsgThrCrossTimePeriod for each crossing
                List<Long> crossPeriods = new ArrayList<>();
                for (int j = 1; j < exceedIndices.size(); j++) {
                    long t1 = timestamps.get(exceedIndices.get(j - 1));
                    long t2 = timestamps.get(exceedIndices.get(j));
                    crossPeriods.add(t2 - t1);
                }
                // Assign to each NsiLoadLevelInfo where threshold was crossed
                for (int k = 1; k < exceedIndices.size(); k++) {
                    int idx = exceedIndices.get(k);
                    long t1 = timestamps.get(exceedIndices.get(k - 1));
                    long t2 = timestamps.get(exceedIndices.get(k));
                    TimeWindow tw = new TimeWindow();
                    tw.setStartTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(t1), ZoneOffset.UTC));
                    tw.setStopTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(t2), ZoneOffset.UTC));
                    List<TimeWindow> twList = new ArrayList<>();
                    twList.add(tw);
                    nsiLoadLevelInfos.get(idx).setResUsgThrCrossTimePeriod(twList);
                }

                System.out.println("Writing CSV...");
                CsvBuilder.writeCsv(timestamps, nsiLoadLevelInfos, MAX_VALUES);

                System.out.println("Writing JSON...");
                JsonBuilder.writeJson(timestamps, nsiLoadLevelInfos, MAX_VALUES);

                // Clear lists for next run
                MetricDataStore.ueCountValues.clear();
                MetricDataStore.cpuPercentageValues.clear();
                MetricDataStore.memoryUsageValues.clear();
                MetricDataStore.FivegsSmffunctionPdusessionCreationSuccValues.clear();
                timestamps.clear();
                nsiLoadLevelInfos.clear();
            }
        };

        scheduler.scheduleAtFixedRate(fetchTask, 0, INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private boolean allMetricsCollected() {
        return MetricDataStore.FivegsSmffunctionPdusessionCreationSuccValues.size() >= MAX_VALUES &&
                MetricDataStore.ueCountValues.size() >= MAX_VALUES &&
                MetricDataStore.cpuPercentageValues.size() >= MAX_VALUES &&
                MetricDataStore.memoryUsageValues.size() >= MAX_VALUES ;


                /*
                MetricDataStore.s5cRxCreateSessionValues.size() >= MAX_VALUES &&
                MetricDataStore.uesActiveValues.size() >= MAX_VALUES &&
                MetricDataStore.cpuSecondsValues.size() >= MAX_VALUES &&
                MetricDataStore.memoryBytesValues.size() >= MAX_VALUES &&*/
    }

    // Map Prometheus metrics to NWDAF model objects for a given index
    private NsiLoadLevelInfo mapToNsiLoadLevelInfo(int i) {
        NsiLoadLevelInfo info = new NsiLoadLevelInfo();

        // Example mapping: adjust as needed for your actual SNSSAI, NSI ID, etc.
        info.setSnssai(new Snssai(1, "000000"));
        info.setNsiId(UUID.randomUUID().toString());

        // ResourceUsage mapping
        Integer cpuUsage = null;
        Integer memoryUsage = null;

        ResourceUsage resUsage = new ResourceUsage();
        Double cpuUsageRaw = parseDoubleSafe(MetricDataStore.cpuPercentageValues, i);

        if (cpuUsageRaw != null) {
            cpuUsage = (int) Math.round(cpuUsageRaw * 100);
        }
        resUsage.setCpuUsage(cpuUsage);

        Double memoryUsageRaw = parseDoubleSafe(MetricDataStore.memoryUsageValues, i);
        if (memoryUsageRaw != null) {
            memoryUsage = (int) (memoryUsageRaw / 0.16);
        }
        resUsage.setMemoryUsage(memoryUsage);

        // StorageUsage not available, set null or 0
        resUsage.setStorageUsage(null);
        info.setResUsage(resUsage);

        // NumberAverage mapping for UEs and PDU Sessions
        NumberAverage numOfUes = new NumberAverage();
        numOfUes.setNumber(parseIntSafe(MetricDataStore.ueCountValues, i));
        numOfUes.setVariance(0f); // No variance info from Prometheus
        info.setNumOfUes(numOfUes);

        NumberAverage numOfPduSess = new NumberAverage();

        numOfPduSess.setNumber(parseIntSafe(MetricDataStore.FivegsSmffunctionPdusessionCreationSuccValues, i));
        /*
        // numOfPduSess.setNumber(parseIntSafe(MetricDataStore.s5cRxCreateSessionValues, i));

        // Calculate the number of PDU sessions at this timestamp
        int pduSessionsAtTimestamp = 0;

        if (i != 0) {
            Integer current = parseIntSafe(MetricDataStore.NgPduSessionResourceSetupResponseValues, i);
            Integer previous = parseIntSafe(MetricDataStore.NgPduSessionResourceSetupResponseValues, i - 1);
            if ( previous != null && current == previous) {
                pduSessionsAtTimestamp = pduSessionsAtTimestamp;
            }
            else if (current != null && previous != null && current > previous) {
                pduSessionsAtTimestamp = pduSessionsAtTimestamp + (current - previous);
            }
        }
        numOfPduSess.setNumber(pduSessionsAtTimestamp);*/

        numOfPduSess.setVariance(0f);
        info.setNumOfPduSess(numOfPduSess);

        // Load level information: use CPU percentage as a proxy
        // info.setLoadLevelInformation(parseIntSafe(MetricDataStore.cpuPercentageValues, i));

        // Set timestamp as confidence for demonstration (or use another field)
        info.setConfidence(100);

        // --- Threshold logic ---
        //boolean exceed = (cpuUsage != null && cpuUsage > 80) || (memoryUsage != null && memoryUsage > 85);
        boolean exceed = (cpuUsage != null && cpuUsage > 80) || (memoryUsage != null && memoryUsage >= 94); // Example thresholds, adjust as above
        info.setExceedLoadLevelThrInd(exceed);

        if (exceed) {
            numOfExceedLoadLevelThr++;
            exceedIndices.add(i);
        }
        info.setNumOfExceedLoadLevelThr(numOfExceedLoadLevelThr);

        // resUsgThrCrossTimePeriod will be set after all mapping is done

        return info;
    }

    private Integer parseIntSafe(List<String> list, int i) {
        if (i >= list.size()) return null;
        String entry = list.get(i);
        try {
            // Try to extract value from "value=..." format
            int valueIdx = entry.indexOf("value=");
            if (valueIdx != -1) {
                int commaIdx = entry.indexOf(",", valueIdx);
                if (commaIdx == -1) commaIdx = entry.indexOf("]", valueIdx);
                if (commaIdx == -1) return null;
                String valueStr = entry.substring(valueIdx + 6, commaIdx).trim();
                if (valueStr.contains(".")) {
                    return (int) Double.parseDouble(valueStr);
                }
                return Integer.parseInt(valueStr);
            }
            // Otherwise, try to parse directly
            return Integer.parseInt(entry.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleSafe(List<String> list, int i) {
        if (i >= list.size()) return null;
        String entry = list.get(i);
        try {
            int valueIdx = entry.indexOf("value=");
            if (valueIdx != -1) {
                int commaIdx = entry.indexOf(",", valueIdx);
                if (commaIdx == -1) commaIdx = entry.indexOf("]", valueIdx);
                if (commaIdx == -1) return null;
                String valueStr = entry.substring(valueIdx + 6, commaIdx).trim();
                return Double.parseDouble(valueStr);
            }
            return Double.parseDouble(entry.trim());
        } catch (Exception e) {
            return null;
        }
    }

}