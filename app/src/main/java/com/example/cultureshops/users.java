package com.example.cultureshops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link users#newInstance} factory method to
 * create an instance of this fragment.
 */
public class users extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public users() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment users.
     */
    // TODO: Rename and change types and number of parameters
    public static users newInstance(String param1, String param2) {
        users fragment = new users();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public class UsersFragment extends Fragment {

        View v;
        private RecyclerView cartList;
        private DatabaseReference myCartDatabase;
        private FirebaseAuth mAuth;
        private String userId;
        private DatabaseReference myUsersDatabase;
        private final int CALL_PERMISSION_CODE = 345;

        public UsersFragment() {
            // Required empty public constructor
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mParam1 = getArguments().getString(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            v = inflater.inflate(R.layout.fragment_users, container, false);
            cartList = v.findViewById(R.id.cartList);
            mAuth = FirebaseAuth.getInstance();
            userId = mAuth.getCurrentUser().getUid();
            myCartDatabase = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Cart").child(userId);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            cartList.setLayoutManager(linearLayoutManager);


            return v;


        }

        @Override
        public void onStart() {
            super.onStart();
            FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                    .setQuery(myCartDatabase, Cart.class)
                    .build();

            FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final CartViewHolder holder, int position, @NonNull Cart model) {

                    holder.cart_tvName.setText(model.getName());
                    holder.cart_tvQty.setText(model.getQty() + " Kgs ");
                    holder.cart_tvPrice.setText("Ksh :" + model.getPrice());

                    Picasso.get().load(model.getPost_image()).placeholder(R.drawable.ic_photo_library_black_24dp).into(holder.cart_product_imageView);


                    myUsersDatabase = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");
                    myUsersDatabase.child(model.getPoster_id()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String name = dataSnapshot.child("name").getValue().toString();
                            final String phone = dataSnapshot.child("phone").getValue().toString();
                            String county = dataSnapshot.child("county").getValue().toString();
                            String subCounty = dataSnapshot.child("subcounty").getValue().toString();

                            holder.tvCounty.setText(county);
                            holder.tvSuCounty.setText(subCounty);
                            holder.tvProductUploader.setText("Sold by: " + name);

                            holder.btnCall.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Toast.makeText(getContext(), "Call: "+phone, Toast.LENGTH_SHORT).show();

                                    if (isCallAllowed()) {

                                        dial(phone);
                                    } else {
                                        requestCallPermission();
                                    }

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

                @NonNull
                @Override
                public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_cart, viewGroup, false);
                    CartViewHolder viewHolder = new CartViewHolder(view);

                    return viewHolder;
                }
            };


            cartList.setAdapter(adapter);
            adapter.startListening();
            adapter.notifyDataSetChanged();

        }

        private void dial(String phone) {

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        }

        public static class CartViewHolder extends RecyclerView.ViewHolder {
            private ImageView cart_product_imageView;
            private TextView cart_tvName, cart_tvPrice, cart_tvQty, tvCounty, tvSuCounty, tvProductUploader;
            private Button btnCall;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);

                cart_product_imageView = itemView.findViewById(R.id.cart_product_image_view);
                cart_tvName = itemView.findViewById(R.id.cart_name_tv);
                cart_tvPrice = itemView.findViewById(R.id.cart_price_tv);
                cart_tvQty = itemView.findViewById(R.id.cart_qty_tv);
                tvCounty = itemView.findViewById(R.id.cart_county);
                tvSuCounty = itemView.findViewById(R.id.cart_sub_county);
                btnCall = itemView.findViewById(R.id.btnCall);
                tvProductUploader = itemView.findViewById(R.id.product_cart_uploader);


            }
        }

        private void requestCallPermission() {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
                //If the user has denied the permission previously your code will come to this block
                //Here you can explain why you need this permission
                //Explain here why you need this permission
            }

            //And finally ask for the permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == CALL_PERMISSION_CODE) {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getContext(), "Oops you just denied the permission", Toast.LENGTH_LONG).show();
                }
            }
        }

        private boolean isCallAllowed() {
            //Getting the permission status
            int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE);

            //If permission is granted returning true
            if (result == PackageManager.PERMISSION_GRANTED)
                return true;

            //If permission is not granted returning false
            return false;
        }
    }
}