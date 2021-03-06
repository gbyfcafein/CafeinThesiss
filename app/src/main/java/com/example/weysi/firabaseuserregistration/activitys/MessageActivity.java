package com.example.weysi.firabaseuserregistration.activitys;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weysi.firabaseuserregistration.MyApplication;
import com.example.weysi.firabaseuserregistration.R;
import com.example.weysi.firabaseuserregistration.adapters.MessagePagerAdapter;
import com.example.weysi.firabaseuserregistration.fragments.FriendsFragment;
import com.example.weysi.firabaseuserregistration.informations.Friends;
import com.example.weysi.firabaseuserregistration.informations.UserInformation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener{
    private MessagePagerAdapter mMessagePagerAdapter;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageButton aramaBtn;
    private ImageButton back;
    private Context context;

    private EditText editTextSearch;


    private FirebaseUser firebaseUser;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDatabase2;

    private FirebaseAuth firebaseAuth;
    String myUserID;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        firebaseAuth = FirebaseAuth.getInstance();

        LayoutInflater inflater = this.getLayoutInflater();
        editTextSearch = (EditText)findViewById(R.id.editTextSearchAct) ;

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid());
        mUserDatabase.child("online").setValue("true");
        mUserDatabase2 = FirebaseDatabase.getInstance().getReference("Users");
        mUserDatabase2.keepSynced(true);

        //recyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));

        context=this;
        back=(ImageButton) findViewById(R.id.imageButtonBack) ;
        aramaBtn=(ImageButton)findViewById(R.id.imageButtonSearch);
        mTabLayout = (TabLayout) findViewById(R.id.message_mainTabs);
        mViewPager = (ViewPager) findViewById(R.id.message_viewPager);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        myUserID = firebaseUser.getUid();
        mMessagePagerAdapter = new MessagePagerAdapter(getSupportFragmentManager(), myUserID);

        mViewPager.setAdapter(mMessagePagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


        back.setOnClickListener(this);
        aramaBtn.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {

        if(v==back)
            finish();
        if(v==aramaBtn)
        {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.single_search_result_layout);

            dialog.setTitle("Arama Sonuçları");
            RecyclerView rw=(RecyclerView) dialog.findViewById(R.id.recyclerViewSearchAct2);
            rw.setLayoutManager(new LinearLayoutManager(context));

            final String search = editTextSearch.getText().toString();
            Query firebaseQuery=mUserDatabase2.orderByChild("name").startAt(search).endAt(search+"\uf8ff");
            firebaseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildrenCount();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            FirebaseRecyclerAdapter<UserInformation, searchResultViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<UserInformation, searchResultViewHolder>(

                    UserInformation.class,
                    R.layout.users_single_layout,
                    searchResultViewHolder.class,
                    firebaseQuery


            ) {
                @Override
                protected void populateViewHolder(final searchResultViewHolder viewHolder, final UserInformation model, int i) {

                    mUserDatabase2.child(model.getUserID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Bitmap bmp;
                            if(dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);
                            }
                            if(model.getImage().compareTo("default")==0)
                            {
                                bmp= BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);

                            }
                            else
                            {
                                byte []byteArray = Base64.decode(model.getImage(), Base64.DEFAULT);
                                bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                            }
                            viewHolder.setName(model.getName());
                            viewHolder.setUserImage(bmp);
                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(MyApplication.getContext(), AnotherUserProfileActivity.class);
                                    profileIntent.putExtra("UserID", model.getUserID());
                                    startActivity(profileIntent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            };
            rw.setAdapter(friendsRecyclerViewAdapter);

            dialog.show();


        }

    }

    public static class searchResultViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView textViewStatus;

        public searchResultViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            textViewStatus=mView.findViewById(R.id.user_single_status);
            textViewStatus.setVisibility(View.INVISIBLE);

        }

        public void setDate(String date){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(Bitmap bitmap){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            userImageView.setImageBitmap(bitmap);
        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserDatabase.child("online").setValue("true");
    }
}