package io.nwdaf.analytics.response.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.nwdaf.analytics.model.NsiLoadLevelInfo;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class JsonBuilder {

    public static void writeJson(
            List<Long> timestamps,
            List<NsiLoadLevelInfo> nsiLoadLevelInfos,
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
        for (int i = 0; i < maxRows && i < nsiLoadLevelInfos.size() && i < timestamps.size(); i++) {
            NsiLoadLevelInfo info = nsiLoadLevelInfos.get(i);

            Map<String, Object> record = new LinkedHashMap<>();
            record.put("timestamp", getTimestamp(timestamps, i));
            record.put("nf_type", "SMF");
            record.put("nf_instance_id", ""); // Fill as needed
            record.put("nf_set_id", ""); // Fill as needed
            record.put("network_slice", info.getSnssai() != null ? info.getSnssai().toString() : "");

            List<Map<String, Object>> metrics = new ArrayList<>();
            /*metrics.add(metricObj("Active UEs", "", "", "Number of active UEs.",
                info.getNumOfUes() != null && info.getNumOfUes().getNumber() != null ? info.getNumOfUes().getNumber() : "", ""));
            metrics.add(metricObj("CPU time", "", "", "CPU time consumed by the process.",
                info.getResUsage() != null && info.getResUsage().getCpuUsage() != null ? info.getResUsage().getCpuUsage() : "", "seconds"));
            metrics.add(metricObj("Memory", "", "", "Memory currently used (resident set size).",
                info.getResUsage() != null && info.getResUsage().getMemoryUsage() != null ? info.getResUsage().getMemoryUsage() : "", "bytes"));*/
            metrics.add(metricObj("s5c Rx Sessions requested to be created", "numOfPduSess", "NumberAverage", "Indicates the average and variance number of PDU session established at the S-NSSAI and the optionally associated network slice instance.",
                    info.getNumOfPduSess() != null && info.getNumOfPduSess().getNumber() != null ? info.getNumOfPduSess().getNumber() : "", ""));
            metrics.add(metricObj("UE Count", "numOfUes", "NumberAverage", "Indicates the average and variance number of UE registered at the S-NSSAI and the optionally associated network slice instance.",
                info.getNumOfUes() != null && info.getNumOfUes().getNumber() != null ? info.getNumOfUes().getNumber() : "", ""));
            metrics.add(metricObj("CPU Percentage", "cpuUsage", "Uinteger", "Average usage of virtual CPU.",
                info.getResUsage() != null && info.getResUsage().getCpuUsage() != null ? info.getResUsage().getCpuUsage() : "", "%"));
            metrics.add(metricObj("Memory Usage", "memoryUsage", "Uinteger", "Average usage of memory, in MiB.",
                info.getResUsage() != null && info.getResUsage().getMemoryUsage() != null ? info.getResUsage().getMemoryUsage() : "", "MiB"));

            // Add the three new metrics
            metrics.add(metricObj(
                "Number of Exceed Load Level Threshold", "numOfExceedLoadLevelThr", "integer",
                "Indicates the number of times the resource usage threshold of the network slice instance is reached or exceeded if a threshold value is provided by the consumer.",
                info.getNumOfExceedLoadLevelThr() != null ? info.getNumOfExceedLoadLevelThr() : "", ""
            ));
            metrics.add(metricObj(
                "Exceed Load Level Threshold Indicator", "exceedLoadLevelThrInd", "Boolean",
                "Indicates whether the Load Level Threshold is met or exceeded by the statistics value.",
                info.getExceedLoadLevelThrInd() != null ? info.getExceedLoadLevelThrInd() : false, ""
            ));
            String resUsgThrCrossTimePeriodStr = "";
            if (info.getResUsgThrCrossTimePeriod() != null && !info.getResUsgThrCrossTimePeriod().isEmpty()) {
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
            metrics.add(metricObj(
                "Resource Usage Threshold Cross Time Period", "resUsgThrCrossTimePeriod", "array(TimeWindow)",
                "Each element indicates the time elapsed between times each threshold is met or exceeded or crossed. The start time and end time are the exact time stamps of the resource usage threshold is reached or exceeded.",
                resUsgThrCrossTimePeriodStr, ""
            ));

            record.put("metrics", metrics);
            record.put("experiment_id", "experiment_001");
            record.put("traffic_condition", "normal");
            record.put("event_annotation", "none");
            record.put("weather_condition", "clear");

            records.add(record);
        }
        root.put("records", records);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(new File("nsi_load_level_metrics.json"), root);
            System.out.println("JSON file created: nsi_load_level_metrics.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimestamp(List<Long> timestamps, int index) {
        if (index >= timestamps.size()) return "";
        return java.time.Instant.ofEpochMilli(timestamps.get(index)).toString();
    }

    private static Map<String, Object> metricObj(String name, String attributeName, String attributeType, String description, Object value, String unit) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("name", name);
        metric.put("3GPP attribute name", attributeName);
        metric.put("3GPP attribute type", attributeType);
        metric.put("description", description);
        metric.put("value", value);
        metric.put("unit", unit);
        return metric;
    }
}