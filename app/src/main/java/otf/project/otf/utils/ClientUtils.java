package otf.project.otf.utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import otf.project.otf.models.OTFClient;

/**
 * Created by denismalcev on 10.06.17.
 */

public class ClientUtils {

    public static void saveClient(final OTFClient client) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(client);
            }
        });
        realm.close();
    }

    public static List<OTFClient> getClients(int group) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<OTFClient> clients = realm.where(OTFClient.class).equalTo("group", group).findAll();
            if (clients != null) {
                return realm.copyFromRealm(clients);
            } else {
                return new ArrayList<>();
            }
        } finally {
            realm.close();
        }
    }

}
