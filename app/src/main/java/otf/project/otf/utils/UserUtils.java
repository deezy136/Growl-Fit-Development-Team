package otf.project.otf.utils;

import io.realm.Realm;
import otf.project.otf.models.OTFUser;

/**
 * Created by denismalcev on 04.06.17.
 */

public class UserUtils {

    public static OTFUser getUser() {
        Realm realm = Realm.getDefaultInstance();
        try {
            OTFUser user = realm.where(OTFUser.class).findFirst();
            if (user != null) {
                return realm.copyFromRealm(user);
            }
            return null;
        } finally {
            realm.close();
        }
    }

    public static void updateUser(final OTFUser user) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(user);
                }
            });
        } finally {
            realm.close();
        }
    }

}
