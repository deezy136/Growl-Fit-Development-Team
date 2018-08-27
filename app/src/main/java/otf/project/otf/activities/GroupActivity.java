package otf.project.otf.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcase.RectangularShape;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapes.CircularShape;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.services.network.NetworkUtils;
import io.github.kobakei.grenade.annotation.Navigator;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import otf.project.otf.OTFApp;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.messages.OTFServicesChangeMessage;
import otf.project.otf.models.OTFClient;
import otf.project.otf.models.OTFRole;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.service.ConnectionService;
import otf.project.otf.utils.ConnectionUtils;
import otf.project.otf.utils.Constants;
import otf.project.otf.utils.IntentFilterCreator;
import otf.project.otf.utils.RoleUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@Navigator
@RuntimePermissions
public class GroupActivity extends BaseActivity implements RealmChangeListener<RealmResults<OTFClient>>{

    private RecyclerView groupList;
    private Toolbar toolbar;
    private ClientsAdapter adapter;

    private Realm realm;
    private RealmResults<OTFClient> clients;

    private Map<String, InetSocketAddress> clientAddress;

    private Handler restartServiceHandler = new Handler();

    private ProgressDialog loadingDialog;

    private TextView localIpAddress;

    private View noWifiConnectionView;
    private View noUsers;
    private FloatingActionButton inviteUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        inviteUserButton = (FloatingActionButton) findViewById(R.id.fab);
        noWifiConnectionView = findViewById(R.id.no_wifi_connection);
        noUsers = findViewById(R.id.no_users);
        localIpAddress = (TextView) findViewById(R.id.local_ip_address);
        clientAddress = new HashMap<>();
        realm = Realm.getDefaultInstance();
        clients = realm.where(OTFClient.class).findAll();
        clients.addChangeListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.your_group_screen_title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new ServerActivityNavigator().build(GroupActivity.this));
            }
        });

        FloatingActionButton mic = (FloatingActionButton) findViewById(R.id.mic);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            GroupActivityPermissionsDispatcher.startRecordingWithCheck(GroupActivity.this);
            }
        });

        groupList = (RecyclerView) findViewById(R.id.group_list);
        groupList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientsAdapter(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        groupList.setAdapter(adapter);

        RoleUtils.setRole(OTFRole.INSTRUCTOR);

        onChange(clients);

        groupList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                groupList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                showAddClientHint();
            }
        });
        updateConnectionView();
    }

    private void showAddClientHint() {
        boolean showedHint1 = PreferenceManager.getDefaultSharedPreferences(OTFApp.instance).getBoolean(Constants.PREF_SHOWED_HINT_1, false);
        if (!showedHint1) {
            SimpleHintContentHolder hintBlock = new SimpleHintContentHolder.Builder(this)
                    .setContentText(R.string.hint_1)
                    .setContentStyle(R.style.hint_style)
                    .build();
            View decorView = getWindow().getDecorView();
            new HintCase(decorView)
                    .setTarget(findViewById(R.id.fab), new CircularShape(), HintCase.TARGET_IS_NOT_CLICKABLE)
                    .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                    .setHintBlock(hintBlock)
                    .show();
            PreferenceManager.getDefaultSharedPreferences(OTFApp.instance).edit().putBoolean(Constants.PREF_SHOWED_HINT_1, true).commit();
        }
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecording() {
        Intent intent = new Intent(GroupActivity.this, ConnectionService.class);
        intent.setAction(ConnectionService.START_RECORD);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(connectionChangedReceiver,
                IntentFilterCreator.create(ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.WIFI_STATE_CHANGED_ACTION));

        Intent intent = new Intent(this, ConnectionService.class);
        intent.setAction(ConnectionService.RESTART_DISCOVERY);
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionChangedReceiver);
    }

    @Override
    public void onChange(RealmResults<OTFClient> otfClients) {
        for (OTFClient client : otfClients) {
            if (!clientAddress.containsKey(client.getId())) {
                clientAddress.put(client.getId(), null);
            }
        }
        adapter.setClients(realm.copyFromRealm(otfClients));
        adapter.notifyDataSetChanged();

        noUsers.setVisibility(ConnectionUtils.isWifiConnectionEnabled() && clients.size() == 0 ? View.VISIBLE : View.INVISIBLE);

        localIpAddress.setText(ConnectionUtils.getIPAddress());
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceStateChanged(OTFServicesChangeMessage message) {
        List<OTFService> services = message.getServices();
        for (OTFService service : services) {
            if (clientAddress.containsKey(service.getId())) {
                String ip = service.getIp();
                int port = service.getPort();
                clientAddress.put(service.getId(), new InetSocketAddress(ip, port));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class ClientsAdapter extends RecyclerView.Adapter<ClientViewHolder> {

        private View.OnClickListener serviceClickListener;

        private List<OTFClient> clients = new ArrayList<>();

        public ClientsAdapter(View.OnClickListener serviceClickListener) {
            this.serviceClickListener = serviceClickListener;
        }

        public void setClients(List<OTFClient> clients) {
            this.clients = clients;
            Collections.sort(clients, new Comparator<OTFClient>() {
                @Override
                public int compare(OTFClient o1, OTFClient o2) {
                    return o1.getGroup() > o2.getGroup() ? 1 : 0;
                }
            });
        }

        public List<OTFClient> getClients() {
            return clients;
        }

        @Override
        public int getItemCount() {
            return clients.size();
        }

        @Override
        public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ClientViewHolder holder = new ClientViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_group_client, parent, false));
            holder.itemView.setOnClickListener(serviceClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(ClientViewHolder holder, int position) {
            OTFClient client = clients.get(position);
            holder.clientName.setText(client.getName() + " #" + client.getGroup());
            holder.itemView.setTag(client);

            if (clientAddress.containsKey(client.getId()) && clientAddress.get(client.getId()) != null) {
                InetSocketAddress address = clientAddress.get(client.getId());
                holder.onlineLogo.setImageResource(R.drawable.ic_connection_ok);
                holder.clientMeta.setText(address.getAddress().getHostAddress() + ":" + address.getPort());
            } else {
                holder.onlineLogo.setImageResource(R.drawable.ic_connection_failed);
                holder.clientMeta.setText("");
            }
        }
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {

        private ImageView onlineLogo;
        private TextView clientName;
        private TextView clientMeta;

        public ClientViewHolder(View itemView) {
            super(itemView);

            onlineLogo = (ImageView) itemView.findViewById(R.id.online_logo);
            clientName = (TextView) itemView.findViewById(R.id.client_name);
            clientMeta = (TextView) itemView.findViewById(R.id.client_meta);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.restart_service) {
            restartService();
        }
        return super.onOptionsItemSelected(item);
    }

    private void restartService() {
        loadingDialog = ProgressDialog.show(this, null, getString(R.string.please_wait));
        Intent intent = new Intent(GroupActivity.this, ConnectionService.class);
        intent.setAction(ConnectionService.STOP);
        startService(intent);

        restartServiceHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
                Intent intent = new Intent(GroupActivity.this, ConnectionService.class);
                startService(intent);
            }
        }, 3000);
    }


    private BroadcastReceiver connectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnectionView();
        }
    };

    private void updateConnectionView() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        noWifiConnectionView.setVisibility(wifiNetwork.isConnectedOrConnecting() ? View.INVISIBLE : View.VISIBLE);
        inviteUserButton.setVisibility(wifiNetwork.isConnectedOrConnecting() ? View.VISIBLE : View.INVISIBLE);
        noUsers.setVisibility(wifiNetwork.isConnectedOrConnecting() && clients.size() == 0 ? View.VISIBLE : View.INVISIBLE);
    }
}
