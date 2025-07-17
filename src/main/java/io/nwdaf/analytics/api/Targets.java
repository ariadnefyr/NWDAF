package io.nwdaf.analytics.api;

public class Targets {

	private String lime5GBts = "http://172.16.10.208:19999";
	
	private String papajohnVm1GenericvnfVm1Netdata = "http://10.10.10.58:19999";
	private String papajohnVm1GenericvnfVm1Prometheus = "http://localhost:9090";

	private String universityOfPatrasPrometheus = "http://150.140.195.195:9090";
	private String open5GSPrometheus = "http://localhost:9090";
	private String allMetricsPrometheus = "http://172.16.100.128:29090";

	private String masterNode = "http://150.140.195.195:19999";
	
	
	public String getLime5GBts() {
		return lime5GBts;
	}

	public void setLime5GBts(String lime5gBts) {
		lime5GBts = lime5gBts;
	}

	
	public String getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(String masterNode) {
		this.masterNode = masterNode;
	}

	public String getPapajohnVm1GenericvnfVm1Netdata() {
		return papajohnVm1GenericvnfVm1Netdata;
	}

	public void setPapajohnVm1GenericvnfVm1Netdata(String papajohnVm1GenericvnfVm1Netdata) {
		this.papajohnVm1GenericvnfVm1Netdata = papajohnVm1GenericvnfVm1Netdata;
	}

	public String getPapajohnVm1GenericvnfVm1Prometheus() {
		return papajohnVm1GenericvnfVm1Prometheus;
	}

	public void setPapajohnVm1GenericvnfVm1Prometheus(String papajohnVm1GenericvnfVm1Prometheus) {
		this.papajohnVm1GenericvnfVm1Prometheus = papajohnVm1GenericvnfVm1Prometheus;
	}

	public String getUniversityOfPatrasPrometheus() {
		return universityOfPatrasPrometheus;
	}

	public void setUniversityOfPatrasPrometheus(String universityOfPatrasPrometheus) {
		this.universityOfPatrasPrometheus = universityOfPatrasPrometheus;
	}

	public String getOpen5GSPrometheus() {
		return open5GSPrometheus;
	}

	public void setOpen5GSPrometheus(String open5GSPrometheus) {
		this.open5GSPrometheus = open5GSPrometheus;
	}

	public String getAllMetricsPrometheus() {
		return allMetricsPrometheus;
	}

	public void setAllMetricsPrometheus(String allMetricsPrometheus) {
		this.allMetricsPrometheus = allMetricsPrometheus;
	}
}
