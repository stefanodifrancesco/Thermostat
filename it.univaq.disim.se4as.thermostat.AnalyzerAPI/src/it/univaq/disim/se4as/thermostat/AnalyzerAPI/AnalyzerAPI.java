package it.univaq.disim.se4as.thermostat.AnalyzerAPI;

import java.util.List;

import it.univaq.disim.se4as.thermostat.Models.TemperatureTrend;

public interface AnalyzerAPI {

	public void startAnalysis();

	public List<TemperatureTrend> calculateTrends(List<String> rooms);
	
	

}
