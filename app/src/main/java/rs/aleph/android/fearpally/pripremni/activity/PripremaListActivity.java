package rs.aleph.android.fearpally.pripremni.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.List;

import rs.aleph.android.fearpally.R;
import rs.aleph.android.fearpally.pripremni.db.PripremaORMLightHelper;
import rs.aleph.android.fearpally.pripremni.db.model.Actor;
import rs.aleph.android.fearpally.pripremni.dilog.AboutDialog;
import rs.aleph.android.fearpally.pripremni.preferences.PripremaPrefererences;
//ovaj7/8
public class PripremaListActivity extends AppCompatActivity {

    private PripremaORMLightHelper databaseHelper;
    private SharedPreferences prefs;

    public static String ACTOR_KEY = "ACTOR_KEY";
    public static String NOTIF_TOAST = "notif_toast";
    public static String NOTIF_STATUS = "notif_statis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priprema_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final ListView listView = (ListView) findViewById(R.id.priprema_glumci_list);

        try {
            List<Actor> list = getDatabaseHelper().getActorDao().queryForAll();

            ListAdapter adapter = new ArrayAdapter<>(PripremaListActivity.this, R.layout.list_item, list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Actor p = (Actor) listView.getItemAtPosition(position);

                    Intent intent = new Intent(PripremaListActivity.this, PripremaDetail.class);
                    intent.putExtra(ACTOR_KEY, p.getmId());
                    startActivity(intent);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        ListView listview = (ListView) findViewById(R.id.priprema_glumci_list);

        if (listview != null){
            ArrayAdapter<Actor> adapter = (ArrayAdapter<Actor>) listview.getAdapter();

            if(adapter!= null)
            {
                try {
                    adapter.clear();
                    List<Actor> list = getDatabaseHelper().getActorDao().queryForAll();

                    adapter.addAll(list);

                    adapter.notifyDataSetChanged();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showStatusMesage(String message){
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Pripremni test");
        mBuilder.setContentText(message);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_add);

        mBuilder.setLargeIcon(bm);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.priprema_add_new_actor:
                //DIALOG ZA UNOS PODATAKA
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.priprema_add_actor_layout);

                Button add = (Button) dialog.findViewById(R.id.add_actor);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText name = (EditText) dialog.findViewById(R.id.actor_name);
                        EditText bio = (EditText) dialog.findViewById(R.id.actor_biography);
                        RatingBar rating = (RatingBar) dialog.findViewById(R.id.acrtor_rating);
                        EditText birth = (EditText) dialog.findViewById(R.id.actor_birth);

                        Actor a = new Actor();
                        a.setmName(name.getText().toString());
                        a.setmBiography(bio.getText().toString());
                        a.setmBirth(birth.getText().toString());
                        a.setmScore(rating.getRating());

                        try {
                            getDatabaseHelper().getActorDao().create(a);

                            //provera podesenja
                            boolean toast = prefs.getBoolean(NOTIF_TOAST, false);
                            boolean status = prefs.getBoolean(NOTIF_STATUS, false);

                            if (toast){
                                Toast.makeText(PripremaListActivity.this, "Added new actor", Toast.LENGTH_SHORT).show();
                            }

                            if (status){
                                showStatusMesage("Added new actor");
                            }

                            //REFRESH
                            refresh();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();

                    }
                });

                dialog.show();

                break;
            case R.id.priprema_about:

                AlertDialog alertDialog = new AboutDialog(this).prepareDialog();
                alertDialog.show();
                break;
            case R.id.priprema_preferences:
                startActivity(new Intent(PripremaListActivity.this, PripremaPrefererences.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Metoda koja komunicira sa bazom podataka
    public PripremaORMLightHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, PripremaORMLightHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // nakon rada sa bazo podataka potrebno je obavezno
        //osloboditi resurse!
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}
