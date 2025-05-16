package io.nwdaf.analytics.util;

import io.nwdaf.analytics.model.NfLoadLevelInformation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatasetExporter {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public static void writeDataToCsv(List<NfLoadLevelInformation> infoList, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("timestamp,nfCpuUsage\n");

            String timestamp = formatter.format(Instant.now());
            for (NfLoadLevelInformation info : infoList) {
                writer.write(String.format("%s,%d\n",
                        timestamp,
                        info.getNfCpuUsage()));
            }

            System.out.println("Dataset written to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMetadataToJson(List<NfLoadLevelInformation> infoList, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        // General metadata
        root.put("dataset_name", "5G NF Load Analytics Dataset");
        root.put("description", "Dataset capturing CPU usage and related metadata for 5G network functions");
        root.put("creation_date", formatter.format(Instant.now()));
        root.put("time_zone", "UTC");
        root.put("license", "Creative Commons Attribution 4.0 International (CC BY 4.0)");
        root.put("access_rights", "public");
        root.put("author", "Ariadni Fyrogeni, University of Patras");

        // Data records
        ArrayNode records = mapper.createArrayNode();
        String timestamp = formatter.format(Instant.now());

        for (NfLoadLevelInformation info : infoList) {
            ObjectNode record = mapper.createObjectNode();
            record.put("timestamp", timestamp);
            record.put("nf_type", info.getNfType() != null ? info.getNfType().toString() : "");
            record.put("nf_instance_id", info.getNfInstanceId() != null ? info.getNfInstanceId().toString() : "");
            record.put("nf_set_id", info.getNfSetId() != null ? info.getNfSetId() : "");
            record.put("network_slice", info.getSnssai() != null ? info.getSnssai().toString() : "");

            ObjectNode metric = mapper.createObjectNode();
            metric.put("value", info.getNfCpuUsage());
            metric.put("type", "CPU Usage");
            metric.put("unit", "%");
            record.set("metric", metric);

            record.put("traffic_condition", "normal");  // Placeholder
            record.put("event_annotation", "none");     // Placeholder
            record.put("weather_condition", "clear");   // Placeholder

            records.add(record);
        }

        root.set("records", records);

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(fileName), root);
            System.out.println("Metadata written to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
