package io.nwdaf.analytics.util;

import io.nwdaf.analytics.api.Targets;
import io.nwdaf.analytics.response.builders.CsvBuilder;
import io.nwdaf.analytics.response.builders.JsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Instant;

public class PrometheusDataCollector {

    private static final int MAX_VALUES = 20;
    private static final int INTERVAL_SEC = 5;
    private final List<Long> timestamps = new ArrayList<>();

    public void startCollecting() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        String server_uop = new Targets().getUniversityOfPatrasPrometheus();
        String server_5GS = new Targets().getOpen5GSPrometheus();

        Runnable fetchTask = () -> {
            long now = System.currentTimeMillis();
            timestamps.add(now);

            MetricFetcher.fetchUesActive(server_5GS);
            MetricFetcher.fetchS5cRxCreateSession(server_5GS);
            MetricFetcher.fetchCpuSeconds(server_5GS);
            MetricFetcher.fetchMemoryBytes(server_5GS);

            MetricFetcher.fetchUeCount(server_uop);
            MetricFetcher.fetchCpuPercentage(server_uop);
            MetricFetcher.fetchMemoryUsage(server_uop);

            if (allMetricsCollected()) {
                scheduler.shutdown();

                CsvBuilder.writeCsv(
                        timestamps,
                        MetricDataStore.uesActiveValues,
                        MetricDataStore.s5cRxCreateSessionValues,
                        MetricDataStore.cpuSecondsValues,
                        MetricDataStore.memoryBytesValues,
                        MetricDataStore.ueCountValues,
                        MetricDataStore.cpuPercentageValues,
                        MetricDataStore.memoryUsageValues,
                        MAX_VALUES
                );
                JsonBuilder.writeJson(
                        timestamps,
                        MetricDataStore.uesActiveValues,
                        MetricDataStore.s5cRxCreateSessionValues,
                        MetricDataStore.cpuSecondsValues,
                        MetricDataStore.memoryBytesValues,
                        MetricDataStore.ueCountValues,
                        MetricDataStore.cpuPercentageValues,
                        MetricDataStore.memoryUsageValues,
                        MAX_VALUES
                );
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
}