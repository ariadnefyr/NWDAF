package io.nwdaf.analytics.response.builders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import io.nwdaf.analytics.api.Targets;
import io.nwdaf.analytics.model.EventFilter;
import io.nwdaf.analytics.model.NFType;
import io.nwdaf.analytics.model.NfLoadLevelInformation;
import io.nwdaf.analytics.model.AnalyticsMetadataInfo;
import io.nwdaf.analytics.model.TimeWindow;
import io.nwdaf.analytics.model.TargetUeInformation;

public class NfLoadLevelResponseBuilder {

	public NfLoadLevelResponseBuilder() {

	}

	public List <NfLoadLevelInformation> nfLoadLevelInformation(EventFilter eventFilter, TargetUeInformation tgtUe) {
		List <NfLoadLevelInformation> nfLoadLevelInformation = new ArrayList<NfLoadLevelInformation>();

		Boolean anyUe = tgtUe.isAnyUe();
		//System.out.println("anyUe: " + anyUe);
		if(anyUe) {
			//Find all the UEs of the given area
			//String command = "curl http://150.140.195.253:9090/api/v1/query?query=netdata_UE_STATS_GNODEB_bps_average -o /home/gctz/Desktop/Diplwmatikh/Multi_TS/Analytics_info/prometheus_yaml_files/test.json";
			//String command = "curl "+new Targets().getPapajohnVm1GenericvnfVm1Prometheus()+"/api/v1/query?query=netdata_cgroup_cpu_per_core_percentage_average";
			String command = "curl "+new Targets().getUniversityOfPatrasPrometheus()+"/api/v1/query?query=netdata_cgroup_cpu_per_core_percentage_average";


			Process process;
			String result = null;
			try {
				process = Runtime.getRuntime().exec(command);
				//process.getInputStream();
				result = new BufferedReader(
						new InputStreamReader(process.getInputStream()))
						.lines()
						.collect(Collectors.joining("\n"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (result != null && !result.isEmpty()) {
				try {
					Object document = Configuration.defaultConfiguration().jsonProvider().parse(result);

					// Get the full result array
					List<LinkedHashMap<String, Object>> results = JsonPath.read(document, "$.data.result[*]");

					for (LinkedHashMap<String, Object> entry : results) {
						LinkedHashMap<String, Object> metric = (LinkedHashMap<String, Object>) entry.get("metric");
						List<Object> valueArray = (List<Object>) entry.get("value");

						if (metric != null && valueArray != null && valueArray.size() >= 2) {
							String chartName = (String) metric.get("chart");
							String valueStr = (String) valueArray.get(1); // Value is a string like "0.1322"
							Object tsObj = valueArray.get(0); // Prometheus timestamp as float (seconds)
							double tsDouble = Double.parseDouble(tsObj.toString());
							long seconds = (long) tsDouble; // or round to nearest second

							try {
								float cpuFloat = Float.parseFloat(valueStr) * 100;
								int cpuUsage = Math.round(cpuFloat);

								if (cpuUsage > 0) {  // Filter: only keep CPUs with actual load
									NfLoadLevelInformation currentNfLoadLevelInfos = new NfLoadLevelInformation();
									currentNfLoadLevelInfos.setNfSetId(chartName);
									currentNfLoadLevelInfos.setNfInstanceId(UUID.randomUUID());
									currentNfLoadLevelInfos.setNfType(new NFType("UPF"));
									currentNfLoadLevelInfos.setNfCpuUsage(cpuUsage);

									AnalyticsMetadataInfo currentNfMetadataInfos = new AnalyticsMetadataInfo();
									OffsetDateTime startTime = OffsetDateTime.now().minusMinutes(5);  // simulate 5-minute window
									OffsetDateTime endTime = OffsetDateTime.now();
									TimeWindow window = new TimeWindow();
									window.setStartTime(startTime);
									window.setStopTime(endTime);
									currentNfMetadataInfos.setDataWindow(window);

									nfLoadLevelInformation.add(currentNfLoadLevelInfos);

									// Optional: log what you're adding
									//System.out.println("Added chart: " + chartName + ", CPU: " + cpuUsage + "%");
								} /*else {
									System.out.println("Skipped chart: " + chartName + ", CPU: " + cpuUsage + "% (zero load)");
								}*/

							} catch (NumberFormatException e) {
								System.err.println("Invalid CPU value: " + valueStr);
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Failed to parse Prometheus JSON.");
				}
			} else {
				System.err.println("No response from Prometheus.");
			}
		}
		else {
			List<String> supis = tgtUe.getSupis();
		}
		System.out.println("Returning " + nfLoadLevelInformation.size() + " NF Load entries");

		//DatasetBuilder.writeDataToCsv(nfLoadLevelInformation, "nf_load_data.csv");
		//DatasetBuilder.writeMetadataToJson(nfLoadLevelInformation, "nf_load_metadata.json");

		return nfLoadLevelInformation;

	}

}
