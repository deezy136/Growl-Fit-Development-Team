package otf.project.otf.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.messages.OTFServiceConfigMessage;
import otf.project.otf.messages.OTFServiceConnectMessage;
import otf.project.otf.messages.OTFServicesChangeMessage;
import otf.project.otf.messages.OTFServicesSyncCompletedMessage;
import otf.project.otf.messages.OTFServicesSyncMessage;
import otf.project.otf.messages.OTFSocketCommandMessage;
import otf.project.otf.messages.OTFSocketIncomingCommandMessage;
import otf.project.otf.models.OTFServiceImpl;
import otf.project.otf.models.OTFUser;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.protocol.SocketCommand;
import otf.project.otf.protocol.SocketInvitationAcceptCommand;
import otf.project.otf.protocol.SocketInvitationCommand;
import otf.project.otf.service.ClientConnectionService;
import otf.project.otf.service.ConnectionService;
import otf.project.otf.utils.UserUtils;

/**
 * Created by denismalcev on 24.05.17.
 */

@Navigator
public class ClientActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView clientsList;
    private ClientsAdapter adapter;
    private Toolbar toolbar;

    private LinearLayout userGroupLayout;
    private TextView userGroupView;
    private TextView userInvitationGroupView;
    private TextView userConfigView;

    private ProgressDialog loadingDialog;
    private Handler restartServiceHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Client");

        userConfigView = (TextView) findViewById(R.id.user_config_view);
        userGroupView = (TextView) findViewById(R.id.user_group_view);
        userInvitationGroupView = (TextView) findViewById(R.id.no_group_description);
        userGroupLayout = (LinearLayout) findViewById(R.id.user_group_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        clientsList = (RecyclerView) findViewById(R.id.clients_list);
        clientsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientsAdapter(serviceClickListener);
        clientsList.setAdapter(adapter);


        startService(new Intent(this, ClientConnectionService.class));

        OTFUser user = UserUtils.getUser();
        if (!TextUtils.isEmpty(user.getGroupName())) {
            userGroupView.setText(user.getGroupName());
            userInvitationGroupView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceStateChanged(OTFServicesChangeMessage message) {
        List<OTFService> services = message.getServices();
        adapter.setClients(services);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceSyncCompleted(OTFServicesSyncCompletedMessage message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionDataChanged(OTFServiceConfigMessage message) {
        userConfigView.setText(message.getIpAddress() + ":" + message.getSocketPort());
        userConfigView.setVisibility(View.VISIBLE);
    }

    static class ClientsAdapter extends RecyclerView.Adapter<ClientViewHolder> {

        private View.OnClickListener serviceClickListener;

        private List<OTFService> clients = new ArrayList<>();

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
                .setMessage(service.getName())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new OTFServiceConnectMessage(service));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).create().show();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIncomingCommand(OTFSocketIncomingCommandMessage message) {
        final SocketCommand command = message.getCommand();
        if (command instanceof SocketInvitationCommand) {
            final SocketInvitationCommand invitationCommand = (SocketInvitationCommand) command;
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.invitation_to_group_message, invitationCommand.getSender().getName(),
                            String.valueOf(invitationCommand.getGroup())))
                    .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            acceptGroup(invitationCommand);
                        }
                    })
                    .setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
        }
    }

    private void acceptGroup(SocketInvitationCommand command) {
        OTFUser user = UserUtils.getUser();
        user.setGroup(command.getGroup());
        user.setGroupName(command.getSender().getName());
        UserUtils.updateUser(user);

        userGroupView.setText(user.getGroupName());
        userInvitationGroupView.setVisibility(View.GONE);

        SocketInvitationAcceptCommand acceptCommand = new SocketInvitationAcceptCommand(command.getGroup());
        OTFUser sender = acceptCommand.getSender();
        OTFServiceImpl service = new OTFServiceImpl(sender.getId(), sender.getName(), command.getIp(), command.getPort(), System.currentTimeMillis());
        EventBus.getDefault().post(new OTFSocketCommandMessage(service, acceptCommand));

        Intent intent = new Intent(this, ClientConnectionService.class);
        intent.setAction(ClientConnectionService.RESTART_CLIENT_BROADCAST);
        startService(intent);
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
        Intent intent = new Intent(ClientActivity.this, ClientConnectionService.class);
        intent.setAction(ConnectionService.STOP);
        startService(intent);

        restartServiceHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
                Intent intent = new Intent(ClientActivity.this, ClientConnectionService.class);
                startService(intent);
            }
        }, 3000);
    }
}
