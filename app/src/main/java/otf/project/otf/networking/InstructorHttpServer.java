package otf.project.otf.networking;

import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import otf.project.otf.utils.ConnectionUtils;

/**
 * Created by denismalcev on 05.06.17.
 */

public class InstructorHttpServer {

    private AsyncHttpServer server;
    private int port;

    public InstructorHttpServer() {

        server = new AsyncHttpServer();
        configureEndpoints();
        port = ConnectionUtils.findFreePort();
        server.listen(port);
    }

    public int getPort() {
        return port;
    }

    private void configureEndpoints() {
        server.get("/select_group", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Multimap query = request.getQuery();
                String groupId = query.getString("group_id");
                String userId = query.getString("user_id");
                response.send("Group: " + groupId);
            }
        });
    }

    private void getUser() {

    }
}
