package io.nwdaf.analytics.util;

import io.nwdaf.analytics.api.Targets;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrometheusDataCollector {

    private static final int MAX_VALUES = 20;
    private static final int INTERVAL_SEC = 5;

    public void startCollecting() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        String server_uop = new Targets().getUniversityOfPatrasPrometheus();
        String server_5GS = new Targets().getOpen5GSPrometheus();

        Runnable fetchTask = () -> {
            MetricFetcher.fetchUesActive(server_5GS);
            MetricFetcher.fetchS5cRxCreateSession(server_5GS);
            MetricFetcher.fetchCpuSeconds(server_5GS);
            MetricFetcher.fetchMemoryBytes(server_5GS);

            MetricFetcher.fetchUeCount(server_uop);
            MetricFetcher.fetchCpuPercentage(server_uop);
            MetricFetcher.fetchMemoryUsage(server_uop);

            if (allMetricsCollected()) {
                scheduler.shutdown();
                writeCsv();
            }
        };

        scheduler.scheduleAtFixedRate(fetchTask, 0, INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private boolean allMetricsCollected() {
        return MetricDataStore.uesActiveValues.size() >= MAX_VALUES &&
                MetricDataStore.s5cRxCreateSessionValues.size() >= MAX_VALUES &&
                MetricDataStore.cpuSecondsValues.size() >= MAX_VALUES &&
                MetricDataStore.memoryBytesValues.size() >= MAX_VALUES &&
                MetricDataStore.ueCountValues.size() >= MAX_VALUES &&
                MetricDataStore.cpuPercentageValues.size() >= MAX_VALUES &&
                MetricDataStore.memoryUsageValues.size() >= MAX_VALUES;
    }

    private void writeCsv() {
        try (FileWriter writer = new FileWriter("metrics.csv")) {
            writer.write("uesActive,s5cRxCreateSession,cpuSeconds,memoryBytes,ueCount,cpuPercentage,memoryUsage\n");
            for (int i = 0; i < MAX_VALUES; i++) {
                writer.write(getValue(MetricDataStore.uesActiveValues, i) + ",");
                writer.write(getValue(MetricDataStore.s5cRxCreateSessionValues, i) + ",");
                writer.write(getValue(MetricDataStore.cpuSecondsValues, i) + ",");
                writer.write(getValue(MetricDataStore.memoryBytesValues, i) + ",");
                writer.write(getValue(MetricDataStore.ueCountValues, i) + ",");
                writer.write(getValue(MetricDataStore.cpuPercentageValues, i) + ",");
                writer.write(getValue(MetricDataStore.memoryUsageValues, i) + "\n");
            }
            System.out.println("CSV file created: metrics.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getValue(java.util.List<String> list, int index) {
        return index < list.size() ? list.get(index) : "";
    }
}