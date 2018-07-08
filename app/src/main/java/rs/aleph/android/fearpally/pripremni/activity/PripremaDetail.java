package rs.aleph.android.fearpally.pripremni.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.List;

import rs.aleph.android.fearpally.R;
import rs.aleph.android.fearpally.pripremni.db.PripremaORMLightHelper;
import rs.aleph.android.fearpally.pripremni.db.model.Actor;
import rs.aleph.android.fearpally.pripremni.db.model.Movie;

import static rs.aleph.android.fearpally.pripremni.activity.PripremaListActivity.NOTIF_STATUS;
import static rs.aleph.android.fearpally.pripremni.activity.PripremaListActivity.NOTIF_TOAST;

public class PripremaDetail extends AppCompatActivity {

    private PripremaORMLightHelper databaseHelper;
    private SharedPreferences prefs;
    private Actor a;

    private EditText name;
    private EditText bio;
    private EditText birth;
    private RatingBar rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priprema_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int key = getIntent().getExtras().getInt(PripremaListActivity.ACTOR_KEY);

        try {
            a = getDatabaseHelper().getActorDao().queryForId(key);

            name = (EditText) findViewById(R.id.actor_name);
            bio = (EditText) findViewById(R.id.actor_biography);
            birth = (EditText) findViewById(R.id.actor_birth);
            rating = (RatingBar) findViewById(R.id.acrtor_rating);

            name.setText(a.getmName());
            bio.setText(a.getmBiography());
            birth.setText(a.getmBirth());
            rating.setRating(a.getmScore());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final ListView listView = (ListView) findViewById(R.id.priprema_actor_movies);

        try {
            List<Movie> list = getDatabaseHelper().getMovieDao().queryBuilder()
                    .where()
                    .eq(Movie.FIELD_NAME_USER, a.getmId())
                    .query();

            ListAdapter adapter = new ArrayAdapter<>(this, R.layout.list_item, list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie m = (Movie) listView.getItemAtPosition(position);
                    Toast.makeText(PripremaDetail.this, m.getmName()+" "+m.getmGenre()+" "+m.getmYear(), Toast.LENGTH_SHORT).show();

                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refresh() {
        ListView listview = (ListView) findViewById(R.id.priprema_actor_movies);

        if (listview != null){
            ArrayAdapter<Movie> adapter = (ArrayAdapter<Movie>) listview.getAdapter();

            if(adapter!= null)
            {
                try {
                    adapter.clear();
                    List<Movie> list = getDatabaseHelper().getMovieDao().queryBuilder()
                            .where()
                            .eq(Movie.FIELD_NAME_USER, a.getmId())
                            .query();

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

    private void showMessage(String message){
        //provera podesenja
        boolean toast = prefs.getBoolean(NOTIF_TOAST, false);
        boolean status = prefs.getBoolean(NOTIF_STATUS, false);

        if (toast){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        if (status){
            showStatusMesage(message);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.priprema_add_movie:
                //OTVORI SE DIALOG UNESETE INFORMACIJE
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.priprema_add_movie);
                dialog.setCanceledOnTouchOutside(false); //ovo je novo

                Button add = (Button) dialog.findViewById(R.id.add_movie);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText name = (EditText) dialog.findViewById(R.id.movie_name);
                        EditText genre = (EditText) dialog.findViewById(R.id.movie_genre);
                        EditText year = (EditText) dialog.findViewById(R.id.movie_year);

                        Movie m = new Movie();
                        m.setmName(name.getText().toString());
                        m.setmGenre(genre.getText().toString());
                        m.setmYear(year.getText().toString());
                        m.setmUser(a);

                        try {
                            getDatabaseHelper().getMovieDao().create(m);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        //URADITI REFRESH
                        refresh();

                        showMessage("New movie added to actor");

                        dialog.dismiss();
                    }
                });

                dialog.show();

                break;
            case R.id.priprema_edit:
//                //POKUPITE INFORMACIJE SA EDIT POLJA
//                a.setmName(name.getText().toString());
//                a.setmBirth(birth.getText().toString());
//                a.setmBiography(bio.getText().toString());
//                a.setmScore(rating.getRating());
//
//                try {
//                    getDatabaseHelper().getActorDao().update(a);
//
//                    showMessage("Actor detail updated");
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
                final Dialog dialogEdit = new Dialog(this);
                dialogEdit.setContentView(R.layout.edit_actor_layout);
                dialogEdit.setCanceledOnTouchOutside(false); //ovo novo testiraj

                Button edit = (Button) dialogEdit.findViewById(R.id.add_actor_btn_edit);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                        EditText nameA = (EditText) dialogEdit.findViewById(R.id.actor_name_edit);
                        EditText bioA = (EditText) dialogEdit.findViewById(R.id.actor_biography_edit);
                        EditText birthA = (EditText) dialogEdit.findViewById(R.id.actor_birth_edit);
                        RatingBar ratingA = (RatingBar) dialogEdit.findViewById(R.id.actor_rating_edit);


                        a.setmName(nameA.getText().toString());
                        a.setmBiography(bioA.getText().toString());
                        a.setmBirth(birthA.getText().toString());
                        a.setmScore(ratingA.getRating());



                        name.setText(a.getmName());
                        bio.setText(a.getmBiography());
                        birth.setText(a.getmBirth());
                        rating.setRating(a.getmScore());



                            getDatabaseHelper().getActorDao().update(a);
                            refresh();
                            showMessage("Actor detail updated");

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
//                        refresh();
//                        showMessage("Actor detail updated");
                        dialogEdit.dismiss();
                    }
                });

                Button cancel = (Button) dialogEdit.findViewById(R.id.cancel_actor_btn_edit);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });
                dialogEdit.show();

                break;
            case R.id.priprema_remove:
                //OVTVARAMO DIALOG ZA UNOS INFORMACIJA
                final Dialog dialogRemove = new Dialog(this);
                dialogRemove.setContentView(R.layout.remove_actor_layout);
                dialogRemove.setCanceledOnTouchOutside(false);

                TextView textView =(TextView) findViewById(R.id.text_dialog);

                Button deleteDialog = (Button) dialogRemove.findViewById(R.id.delete_actor_btn_dialog);
                deleteDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            getDatabaseHelper().getActorDao().delete(a);
                            showMessage("Actor Deleted");
                            finish();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
//                        showMessage("Actor Deleted");

//                        finish(); //moramo pozvati da bi se vratili na prethodnu aktivnost
                    }
                });
                Button cancelDialog = (Button) dialogRemove.findViewById(R.id.cancel_actor_btn_dialog);
                cancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogRemove.dismiss();

                        finish(); //moramo pozvati da bi se vratili na prethodnu aktivnost
                    }
                });
                dialogRemove.show();
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
