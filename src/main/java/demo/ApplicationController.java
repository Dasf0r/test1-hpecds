package demo;

import java.util.StringTokenizer;

import demo.kpis.KPIsRepo;
import demo.metrics.MetricsRepo;
import demo.service.MCPJsonFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.configurationprocessor.json.JSONException;

@RestController
public class ApplicationController {

    Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    @Autowired
    private ConfigProperties appProperties;

    @Autowired
    MetricsRepo metricsRepository;

    @Autowired
    KPIsRepo kpis;

    //endpoint for processing a specific file
    //the file will be retrieved from the web location
    //the data will be persisted in the mysql database
    @RequestMapping("/process")
    public String processFile(@RequestParam(value = "date", defaultValue = "NoDate") String fileDate) throws JSONException {
        if (fileDate.matches("^(\\d{4}-\\d{2}\\-\\d{2})")) {

            //file matches the pattern
            logger.debug("File name matches pattern");

            StringTokenizer st = new StringTokenizer(fileDate, "-");
            String fileName = "";
            while (st.hasMoreTokens()) fileName += st.nextToken();

            String url = appProperties.getFilesURL() + "MCP_" + fileName + ".json";

            logger.debug("File to check: " + url);
            //check if file exits

            //processing file
            MCPJsonFile mCPJSON = new MCPJsonFile(url, "MCP_" + fileName + ".json", appProperties);

            if (mCPJSON.fileProcessed()) {

                //persist the processed metrics
                boolean rowsInserted = metricsRepository.insertMetrics(fileDate, mCPJSON.getMetrics());
                if (!rowsInserted)
                    return "File has been processed already and exists in the database";

                //save KPIs using MeteRegistry
                kpis.updateCounters(mCPJSON);

                //return results of the processing to the client
                return mCPJSON.getMetrics().toString();
            } else
                return "File for " + fileDate + " could not be processed!";
        } else
            return "The entered date does is not present or does not match the YYYY-MM-DD pattern";
    }

    //endpoint for returning the summary for a processed file
    //the mysql database will be checked for file processed
    //the metrics will be retrieved if that is the case
    @RequestMapping("/metrics")
    public String returnMetrics(@RequestParam(value = "date", defaultValue = "NoDate") String fileDate) {
        if (fileDate.matches("^(\\d{4}-\\d{2}-\\-\\d{2})")) {
            return metricsRepository.getMetrics(fileDate);
        } else
            return "The entered date does is not present or does not match the YYYY-MM-DD pattern";
    }

    //endpoint for returning the kpis of the running process
    @RequestMapping("/kpis")
    public String returnKPIs() throws JSONException {
        return kpis.getKPIs().toString();
    }


}
