package demo.kpis;

import demo.service.MCPJsonFile;
import demo.ApplicationController;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Repository;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class KPIsRepo {

    public final MeterRegistry registry;

    @Autowired
    public KPIsRepo(MeterRegistry registry) {
        this.registry = registry;
    }

    Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @PostConstruct
    public void init_counters() {
        registry.counter("custom.nr_processed_json_files");
        registry.counter("custom.nr_rows");
        registry.counter("custom.nr_calls");
        registry.counter("custom.nr_messages");
    }

    public void updateCounters(MCPJsonFile mCPJSON) {
        registry.counter("custom.nr_processed_json_files").increment(1.0);
        registry.counter("custom.nr_rows").increment(mCPJSON.getRowsProcessed());
        registry.counter("custom.nr_calls").increment(mCPJSON.getnumberOfcalls());
        registry.counter("custom.nr_messages").increment(mCPJSON.getnumberOfmessages());

        // Agregar contadores para nuevos orígenes y destinos dinámicamente
        mCPJSON.getnumberCallsOrig().forEach((k, v) -> registry.counter("custom.orig." + k).increment(v));
        mCPJSON.getnumberCallsDest().forEach((k, v) -> registry.counter("custom.dest." + k).increment(v));
    }



    public JSONObject getKPIs() throws JSONException {
        logger.debug(registry.getMeters().toString());
        JSONObject recordKPIs = new JSONObject();

        for (Meter item : registry.getMeters()) {
            if (item.getId().getName().startsWith("custom."))
                recordKPIs.put(item.getId().getName(), registry.counter(item.getId().getName()).count());
        }
        return recordKPIs;
    }
}
