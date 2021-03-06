package com.example.weysi.firabaseuserregistration.activitys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.weysi.firabaseuserregistration.informations.CheckInInformation;
import com.example.weysi.firabaseuserregistration.informations.Data;
import com.example.weysi.firabaseuserregistration.informations.PlaceInformation;
import com.example.weysi.firabaseuserregistration.informations.TimeLineCheckInInformation;
import com.example.weysi.firabaseuserregistration.informations.UserInformation;
import com.example.weysi.firabaseuserregistration.parsers.GPSTracker;
import com.example.weysi.firabaseuserregistration.parsers.MyCheckInAddFriendTotalThread;
import com.example.weysi.firabaseuserregistration.parsers.PlaceClass;
import com.example.weysi.firabaseuserregistration.R;
import com.google.android.gms.location.places.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class NearbyPlaceActivity extends AppCompatActivity implements View.OnClickListener{


    // flag for network status
    boolean isNetworkEnabled = false;
    public boolean isGPSEnabled = false;

    private LocationManager konumYoneticisi;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseCheckIn;
    private DatabaseReference databaseReferencePlace;
    private DatabaseReference databaseReferenceUsersID;
    private DatabaseReference  databaseReferencePlaceID;
    private DatabaseReference databaseReferenceUser;

    private ListView lv;
    private EditText mMessage;

    private double x;
    private double y;
    private ProgressDialog pd;
    private List<CheckInInformation> checkInActivityList;
    private String message;
    private String sImage;
    private ImageButton imageButtonBack;
    private Context context;
    GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_place);

        pd=new ProgressDialog(this);
        pd.setMessage("Yakındaki Lokasyonlar Getiriliyor...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
     //   gpsTracker = new GPSTracker(NearbyPlaceActivity.this);
      //  setLocationAddress();


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReferenceUsersID=FirebaseDatabase.getInstance().getReference("UsersCheckIns");
        databaseCheckIn = FirebaseDatabase.getInstance().getReference("check");
        databaseReferencePlace=FirebaseDatabase.getInstance().getReference("place");
        databaseReferenceUser=FirebaseDatabase.getInstance().getReference("Users");
        databaseReferencePlaceID=FirebaseDatabase.getInstance().getReference("InPlaceCheckIns");


        databaseReferenceUser.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue("true");

        konumYoneticisi = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMessage=(EditText)findViewById(R.id.edit_text_checkin_message);
        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(this);
        context=this;

        lv = (ListView) findViewById(R.id.listView1);
        x=(double)getIntent().getSerializableExtra("a");
        y=(double)getIntent().getSerializableExtra("b");
        if(y==0){
            GPSTracker gpsTracker=new GPSTracker(NearbyPlaceActivity.this);

            x=gpsTracker.getLatitude();
            y=gpsTracker.getLongitude();
        }
        PlaceClass p = new PlaceClass(lv, x, y, NearbyPlaceActivity.this, pd);
        p.execute();
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Object o = lv.getItemAtPosition(position);
                final Data data=(Data)o;


                View view2 =(LayoutInflater.from(NearbyPlaceActivity.this)).inflate(R.layout.single_check_in_message_layout,null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(NearbyPlaceActivity.this);
                builder.setView(view2);
                final EditText editTextMessage= view2.findViewById(R.id.edit_text_checkin_message);

                builder.setCancelable(true)
                        .setTitle(data.getName())
                        .setPositiveButton("Check'in", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setTitle(data.getName());
                                progressDialog.setMessage("Check-in işleminiz gerçekleştiriliyor...");
                                progressDialog.show();

                                message=editTextMessage.getText().toString();
                                databaseReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final UserInformation userInformation= dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).getValue(UserInformation.class);
                                        final TimeLineCheckInInformation[] temptlcii = new TimeLineCheckInInformation[1];
                                        final HashMap<String,Object> placeCheckInInfo = new HashMap<>();
                                        sImage=userInformation.getImage();
                                        placeCheckInInfo.put("userID",userInformation.getUserID());
                                        placeCheckInInfo.put("userName",userInformation.getName());
                                        placeCheckInInfo.put("cinsiyet",userInformation.getCinsiyet());


                                        final HashMap<String,Object> checkInInfo=new HashMap<>();
                                        final String id=databaseCheckIn.push().getKey();
                                        checkInInfo.put("placeID",data.getPlaceId());
                                        checkInInfo.put("placeName",data.getName());
                                        checkInInfo.put("userName",userInformation.getName());
                                        checkInInfo.put("userId",userInformation.getUserID());
                                        checkInInfo.put("checkInTime",ServerValue.TIMESTAMP);
                                        checkInInfo.put("message",message);
                                        checkInInfo.put("checkInID",id);
                                        checkInInfo.put("userPhoto",sImage);
                                        databaseCheckIn.child(id).setValue(checkInInfo);
                                        databaseCheckIn.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                temptlcii[0]= dataSnapshot.getValue(TimeLineCheckInInformation.class);
                                                temptlcii[0].setCheckInTime((temptlcii[0].getCheckInTime()*(-1)));
                                                placeCheckInInfo.put("checkInTime",(temptlcii[0].getCheckInTime()));
                                                databaseReferencePlaceID.child(data.getPlaceId()).child(firebaseAuth.getCurrentUser().getUid()).setValue(placeCheckInInfo);
                                                databaseCheckIn.child(id).setValue(temptlcii[0]);
                                                MyCheckInAddFriendTotalThread myCheckInAddFriendTotalThread=new MyCheckInAddFriendTotalThread(
                                                        firebaseAuth.getCurrentUser().getUid(),temptlcii[0],id);
                                                myCheckInAddFriendTotalThread.start();
                                                databaseReferencePlace.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild(data.getPlaceId())){
                                                            PlaceInformation placeInformation = dataSnapshot.child(data.getPlaceId()).getValue(PlaceInformation.class);
                                                            if(userInformation.getCinsiyet().compareTo("Erkek")==0)
                                                            {
                                                                placeInformation.setMaleCount((placeInformation.getMaleCount())+1);
                                                                databaseReferencePlace.child(data.getPlaceId()).setValue(placeInformation);

                                                            }else{
                                                                placeInformation.setFemaleCount((placeInformation.getFemaleCount())+1);
                                                                databaseReferencePlace.child(data.getPlaceId()).setValue(placeInformation);
                                                            }

                                                        }else{

                                                            if(userInformation.getCinsiyet().compareTo("Erkek")==0)
                                                            {
                                                                PlaceInformation placeInformation = new PlaceInformation(data.getName(),data.getPlaceId(),1,0,data.getAddress());
                                                                databaseReferencePlace.child(data.getPlaceId()).setValue(placeInformation);
                                                            }else{
                                                                PlaceInformation placeInformation = new PlaceInformation(data.getName(),data.getPlaceId(),0,1,data.getAddress());
                                                                databaseReferencePlace.child(data.getPlaceId()).setValue(placeInformation);
                                                            }


                                                        }
                                                        progressDialog.dismiss();
                                                        final AlertDialog.Builder builder2= new AlertDialog.Builder(NearbyPlaceActivity.this);
                                                        View view3 =(LayoutInflater.from(NearbyPlaceActivity.this)).inflate(R.layout.single_completed_layout,null);
                                                        builder2.setView(view3);
                                                        builder2.setCancelable(false);
                                                        builder2.setTitle(data.getName());
                                                        builder2.setPositiveButton("Tamam",new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        Dialog dialog2=builder2.create();
                                                        dialog2.show();

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                databaseReferenceUsersID.child(firebaseAuth.getCurrentUser().getUid()).child(id).setValue(temptlcii[0]);

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                Dialog dialog=builder.create();
                dialog.show();



            }
        });


        /*
try {
    isNetworkEnabled = konumYoneticisi
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


    if (isGPSEnabled == false && isNetworkEnabled == false) {
        // no network provider is enabled
    }

    LocationListener konumDinleyicisi = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {

            x = loc.getLatitude();
            y = loc.getLongitude();
            PlaceClass p = new PlaceClass(lv, x, y, NearbyPlaceActivity.this, pd);
            p.execute();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    konumYoneticisi.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, konumDinleyicisi);

}catch (SecurityException e){
    String a="21";
}

  */
    }
    private void setLocationAddress() {
        if (gpsTracker.getLocation() != null) {
            if (gpsTracker.getLatitude() != 0 && gpsTracker.getLongitude() != 0) {

                double x = gpsTracker.getLatitude();
                double y =gpsTracker.getLongitude();
                PlaceClass p = new PlaceClass(lv, x, y, NearbyPlaceActivity.this, pd);
                p.execute();

            } else {
               // buildAlertMessageNoGps();
            }
        } else {
            //buildAlertMessageNoGps();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReferenceUser.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReferenceUser.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }


    @Override
    public void onClick(View v) {
        if(v == imageButtonBack){
            finish();
        }
    }
}
