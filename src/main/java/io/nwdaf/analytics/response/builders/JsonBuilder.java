package io.nwdaf.analytics.response.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class JsonBuilder {

    public static void writeJson(
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
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("dataset_name", "5G NWDAF NSI LOAD LEVEL Dataset");
        root.put("description", "Dataset capturing NSI LOAD LEVEL metrics values and related metadata for 5G core network");
        root.put("creation_date", Instant.now().toString());
        root.put("time_zone", "UTC");
        root.put("license", "Creative Commons Attribution 4.0 International (CC BY 4.0)");
        root.put("access_rights", "public");
        root.put("author", "Ariadni Fyrogeni, University of Patras");

        List<Map<String, Object>> records = new ArrayList<>();

        for (int i = 0; i < maxRows; i++) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("timestamp", getTimestamp(timestamps, i));
            record.put("nf_type", "");
            record.put("nf_instance_id", getInstance(uesActive, i));
            record.put("nf_set_id", getJob(uesActive, i));
            record.put("network_slice", "");

            List<Map<String, Object>> metrics = new ArrayList<>();
            metrics.add(metricObj("UEs Active", "", getValue(uesActive, i), ""));
            metrics.add(metricObj("Active UEs", "Number of active UEs.", getValue(uesActive, i), ""));
            metrics.add(metricObj("s5c Rx Sessions requested to be created", "Create Session requests received.", getValue(s5cRxCreateSession, i), ""));
            metrics.add(metricObj("CPU time", "CPU time consumed by the process.", getValue(cpuSeconds, i), "seconds"));
            metrics.add(metricObj("Memory", "Memory currently used (resident set size).", getValue(memoryBytes, i), "bytes"));
            metrics.add(metricObj("UE Count", "Average number of UEs connected to the gNB. ", getValue(ueCount, i), ""));
            metrics.add(metricObj("CPU Percentage", "Percentage of the CPU used for the procedure.", getValue(cpuPercentage, i), "%"));
            metrics.add(metricObj("Memory Usage", "Memory currently used in MiB.", getValue(memoryUsage, i), "MiB"));

            record.put("metrics", metrics);
            record.put("traffic_condition", "normal");
            record.put("event_annotation", "none");
            record.put("weather_condition", "clear");

            records.add(record);
        }

        root.put("records", records);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(new File("metrics.json"), root);
            System.out.println("JSON file created: metrics.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimestamp(List<Long> timestamps, int index) {
        if (index >= timestamps.size()) return "";
        return Instant.ofEpochMilli(timestamps.get(index)).toString();
    }

    private static Object getValue(List<String> list, int index) {
        if (index >= list.size()) return null;
        String entry = list.get(index);
        int valueIdx = entry.indexOf("value=");
        if (valueIdx == -1) return null;
        int commaIdx = entry.indexOf(",", valueIdx);
        if (commaIdx == -1) commaIdx = entry.indexOf("]", valueIdx);
        if (commaIdx == -1) return null;
        String valueStr = entry.substring(valueIdx + 6, commaIdx).trim();
        try {
            if (valueStr.contains(".")) return Double.parseDouble(valueStr);
            return Long.parseLong(valueStr);
        } catch (Exception e) {
            return valueStr;
        }
    }

    private static String getInstance(List<String> list, int index) {
        if (index >= list.size()) return "";
        String entry = list.get(index);
        int idx = entry.indexOf("instance=");
        if (idx == -1) return "";
        int commaIdx = entry.indexOf(",", idx);
        if (commaIdx == -1) commaIdx = entry.indexOf("]", idx);
        if (commaIdx == -1) return "";
        return entry.substring(idx + 9, commaIdx).trim();
    }

    private static String getJob(List<String> list, int index) {
        if (index >= list.size()) return "";
        String entry = list.get(index);
        int idx = entry.indexOf("job=");
        if (idx == -1) return "";
        int commaIdx = entry.indexOf(",", idx);
        if (commaIdx == -1) commaIdx = entry.indexOf("]", idx);
        if (commaIdx == -1) return "";
        return entry.substring(idx + 4, commaIdx).trim();
    }

    private static Map<String, Object> metricObj(String type, String desc, Object value, String unit) {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("type", type);
        if (!desc.isEmpty()) obj.put("description", desc);
        obj.put("value", value);
        obj.put("unit", unit);
        return obj;
    }
}