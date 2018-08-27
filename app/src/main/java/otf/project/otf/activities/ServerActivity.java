package otf.project.otf.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.messages.OTFServicesChangeMessage;
import otf.project.otf.messages.OTFServicesSyncMessage;
import otf.project.otf.messages.OTFSocketCommandMessage;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.protocol.SocketCommand;
import otf.project.otf.protocol.SocketInvitationCommand;
import otf.project.otf.service.ConnectionService;
import otf.project.otf.utils.IntentFilterCreator;

/**
 * Created by denismalcev on 24.05.17.
 */

@Navigator
public class ServerActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView clientsList;
    private ClientsAdapter adapter;
    private Toolbar toolbar;
    private View noWifiConnectionView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.invite_to_group_title);

        clientsList = (RecyclerView) findViewById(R.id.clients_list);
        clientsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientsAdapter(serviceClickListener);
        clientsList.setAdapter(adapter);
        noWifiConnectionView = findViewById(R.id.no_wifi_connection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceStateChanged(OTFServicesChangeMessage message) {
        List<OTFService> services = message.getServices();
        List<OTFService> noGroupServices = new ArrayList<>();
        for (OTFService service : services) {
            if (TextUtils.isEmpty(service.getGroupId()) && service.getPort() > 0 && !TextUtils.isEmpty(service.getId())) {
                noGroupServices.add(service);
            }
        }
        adapter.setClients(noGroupServices);
        adapter.notifyDataSetChanged();
    }

    static class ClientsAdapter extends RecyclerView.Adapter<ClientViewHolder> {

        private List<OTFService> clients = new ArrayList<>();
        private View.OnClickListener serviceClickListener;

        public ClientsAdapter(View.OnClickListener serviceClickListener) {
            this.serviceClickListener = serviceClickListener;
        }

        public void setClients(List<OTFService> clients) {
            this.clients = clients;
        }

        public List<OTFService> getClients() {
            return clients;
        }

        @Override
        public int getItemCount() {
            return clients.size();
        }

        @Override
        public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ClientViewHolder holder = new ClientViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_client, parent, false));
            holder.itemView.setOnClickListener(serviceClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(ClientViewHolder holder, int position) {
            OTFService client = clients.get(position);
            holder.clientName.setText(client.getName());
            holder.itemView.setTag(client);
        }
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {

        private TextView clientName;

        public ClientViewHolder(View itemView) {
            super(itemView);

            clientName = (TextView) itemView.findViewById(R.id.client_name);
        }
    }

    private View.OnClickListener serviceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OTFService service = (OTFService) v.getTag();
            showPendingUserInfoDialog(service);
        }
    };

    private void showPendingUserInfoDialog(final OTFService service) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.invite_to_group_title)
                .setMessage(getString(R.string.invite_to_group_message, service.getName(), service.getIp() + ":" + service.getPort()))
                .setPositiveButton(R.string.group_1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SocketCommand socketCommand = new SocketInvitationCommand(1);
                        EventBus.getDefault().post(new OTFSocketCommandMessage(service, socketCommand));
                    }
                })
                .setNegativeButton(R.string.group_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SocketCommand socketCommand = new SocketInvitationCommand(2);
                        EventBus.getDefault().post(new OTFSocketCommandMessage(service, socketCommand));
                    }
                })
                .setNeutralButton(android.R.string.cancel, null).create().show();
    }

    @Override
    public void onRefresh() {
        adapter.setClients(new ArrayList<OTFService>());
        adapter.notifyDataSetChanged();
        EventBus.getDefault().post(new OTFServicesSyncMessage());

        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        },5000);
    }

    private void sendInvitation() {

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
    }
}
