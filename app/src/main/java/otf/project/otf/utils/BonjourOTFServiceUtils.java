package otf.project.otf.utils;

import java.util.Map;

import de.mannodermaus.rxbonjour.BonjourService;

/**
 * Created by denismalcev on 02.11.2017.
 */

public class BonjourOTFServiceUtils {

    public static String getTxtRecord(BonjourService service, String recordName) {
        return getTxtRecord(service, recordName, "");
    }

    public static String getTxtRecord(BonjourService service, String recordName, String defaultValue) {
        Map<String,String> records =  service.getTxtRecords();
        if (records.containsKey(recordName)) {
            return records.get(recordName);
        } else {
            return defaultValue;
        }
    }

}
