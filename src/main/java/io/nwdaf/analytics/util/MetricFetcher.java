package io.nwdaf.analytics.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MetricFetcher {

    /*
    // Fetch "ran_ue"
    public static void fetchRanUe(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "ran_ue");
        extractValues(response, MetricDataStore.ranUeValues);
    }*/

    // Fetch "ues_active"
    public static void fetchUesActive(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "ues_active");
        extractValues(response, MetricDataStore.uesActiveValues);
    }

    // Fetch "s5c_rx_createsession"
    public static void fetchS5cRxCreateSession(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "s5c_rx_createsession");
        extractValues(response, MetricDataStore.s5cRxCreateSessionValues);
    }

    // Fetch "process_cpu_seconds_total"
    public static void fetchCpuSeconds(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "process_cpu_seconds_total%7Binstance%3D%22smf:9090%22%7D");
        extractValues(response, MetricDataStore.cpuSecondsValues);
    }

    // Fetch "process_resident_memory_bytes"
    public static void fetchMemoryBytes(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "process_resident_memory_bytes%7Binstance%3D%22smf:9090%22%7D");
        extractValues(response, MetricDataStore.memoryBytesValues);
    }

    // Fetch "ue_count"
    public static void fetchUeCount(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "ue_count%7Binstance%3D%22172.16.10.202:52000%22%7D");
        extractValues(response, MetricDataStore.ueCountValues);
    }

    // Fetch "netdata_cgroup_cpu_percentage_average" of instance "Compute006"
    public static void fetchCpuPercentage(String serverIp) {
        //String response = PrometheusClient.queryMetric(serverIp, "netdata_cgroup_cpu_percentage_average%7Binstance%3D%22Compute006%22%7D");
        String response = PrometheusClient.queryMetric(serverIp, "netdata_cgroup_cpu_percentage_average%7Binstance%3D%22172.16.10.202:19999%22%2C%20chart%3D%22cgroup_a912d49644ed.cpu%22%2C%20dimension%3D%22system%22%7D");
        extractValues(response, MetricDataStore.cpuPercentageValues);
    }

    // Fetch "netdata_cgroup_mem_usage_MiB_average" of instance "Compute006"
    public static void fetchMemoryUsage(String serverIp) {
        String response = PrometheusClient.queryMetric(serverIp, "netdata_cgroup_mem_usage_MiB_average%7Binstance%3D%22172.16.10.202:19999%22%2C%20chart%3D%22cgroup_a912d49644ed.mem_usage%22%2C%20dimension%3D%22ram%22%7D");
        extractValues(response, MetricDataStore.memoryUsageValues);
    }

    private static void extractValues(String json, List<String> targetList) {
        JSONObject obj = new JSONObject(json);

        // Safety checks
        if (!obj.has("data")) return;
        JSONArray results = obj.getJSONObject("data").getJSONArray("result");

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            JSONObject metricMeta = result.getJSONObject("metric");
            String instance = metricMeta.optString("instance", "unknown");
            String job = metricMeta.optString("job", "unknown");

            JSONArray valueArray = result.getJSONArray("value");
            String timestamp = valueArray.get(0).toString();
            String value = valueArray.get(1).toString();

            // Combine the metadata with value
            String entry = String.format("[timestamp=%s, value=%s, instance=%s, job=%s]",
                    timestamp, value, instance, job);
            targetList.add(entry);
        }
    }

}
