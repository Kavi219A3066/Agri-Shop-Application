package com.example.cultureshops;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telecom.Call;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profile<FirebaseRecyclerOptions> extends Fragment {
        private static final int GALLERY_REQUEST_CODE = 2345;
        private FloatingActionButton btnUpdate;
        private EditText productName,productCategory,productQuantity,productPrice;
        private RecyclerView productsList;
        private FirebaseAuth mAuth;
        private String userId;

        private View v;
        private Dialog d;
        private Uri imageUri=null;
        private ImageView profileImage;
        private DatabaseReference myProductsDatabase;
        private StorageReference mStorageReference;
        private ProgressDialog pd;


        // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment profile.
     */
    // TODO: Rename and change types and number of parameters
    public static profile newInstance(String param1, String param2) {
        profile fragment = new profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        v = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth=FirebaseAuth.getInstance();
        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");

        mStorageReference= FirebaseStorage.getInstance().getReference().child("ENACTUS UOE").child("Product Images");

        btnUpdate=v.findViewById(R.id.fb);
        d=new Dialog(getContext());
        d.setContentView(R.layout.activity_post_dialog);
        profileImage=d.findViewById(R.id.productImage);
        productName=d.findViewById(R.id.product_title);
        productCategory=d.findViewById(R.id.product_category);
        productQuantity=d.findViewById(R.id.product_quantity);
        productPrice=d.findViewById(R.id.product_price);
        productsList=v.findViewById(R.id.productsList);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2);
        productsList.setLayoutManager(gridLayoutManager);
        userId=mAuth.getCurrentUser().getUid();
        pd=new ProgressDialog(getContext());
        pd.setTitle("Uploading Product");
        pd.setMessage("Please wait...");
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bringPostDialog();
            }
        });
        return v;
    }

    private void bringPostDialog() {
        Button btnPost=d.findViewById(R.id.btnUpload);




        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST_CODE);
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=productName.getText().toString().trim().trim();
                String category=productCategory.getText().toString().trim();
                String quantity=productQuantity.getText().toString().trim();
                String price=productPrice.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(category) && !TextUtils.isEmpty(quantity) &&!TextUtils.isEmpty(price))
                {

                    if (imageUri!=null)
                    {

                        //toast("Data is ready");
                        pd.show();
                        postData(name,category,quantity,price,imageUri);
                    }else {
                        toast("Tap the avatar to select product Image...");
                    }
                }else {
                    toast("Check your Product Inputs...some fileds are blank...");
                }
            }
        });


        d.show();






    }

    private void postData(final String name, final String category, final String quantity, final String price, final Uri myUri) {

        final StorageReference filePath=mStorageReference.child(myUri.getLastPathSegment()+".jpg");
        filePath.putFile(myUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadLink=uri.toString();

                            DateFormat dateFormat=new SimpleDateFormat("HH:mm:ss ");
                            Date date=new Date();
                            String time=dateFormat.format(date);
                            String productKey=myProductsDatabase.push().getKey();
                            DatabaseReference newProduct=myProductsDatabase.child(productKey);
                            HashMap<String,Object> myMap=new HashMap<>();
                            myMap.put("post_id",productKey);
                            myMap.put("product_name",name);
                            myMap.put("product_category",category);
                            myMap.put("product_quantity",quantity);
                            myMap.put("product_price",price);
                            myMap.put("post_time",time);
                            myMap.put("product_poster",userId);
                            myMap.put("product_views","0");
                            myMap.put("product_likes","0");

                            myMap.put("post_image",downloadLink);

                            newProduct.updateChildren(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        toast("Uploaded successfully");
                                        pd.dismiss();
                                        d.dismiss();
                                        productName.setText("");
                                        productCategory.setText("");
                                        productQuantity.setText("");
                                        productPrice.setText("");
                                        //imageUri=null;
                                        profileImage.setImageURI(null);

                                    }else {
                                        pd.dismiss();

                                        toast(task.getException().getMessage());
                                    }
                                }
                            });



                        }
                    });
                }else {
                    toast(task.getException().getMessage());
                }
            }
        });






    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GALLERY_REQUEST_CODE==requestCode && resultCode==RESULT_OK)
        {
            imageUri=data.getData();
            profileImage.setImageURI(imageUri);
        }


    }
    private void toast(String s) {
        Toast.makeText(getContext(), "Message: "+s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public <FirebaseRecyclerAdapter> void onStart() {
        super.onStart();
        myProductsDatabase.keepSynced(true);
        FirebaseRecyclerOptions<Product> options=new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(myProductsDatabase,Product.class)
                .build();

        final FirebaseRecyclerAdapter<Product,productsViewHolder> adapter=new FirebaseRecyclerAdapter<Product, productsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull productsViewHolder holder, int position, @NonNull final Product model) {


                holder.tvName.setText(model.getProduct_name());
                holder.tvQty.setText(model.getProduct_quantity()+" Kgs");
                holder.tvPrice.setText("Ksh:"+model.getProduct_price());
                Picasso.get().load(model.getPost_image()).resize(180,150).placeholder(R.drawable.ic_photo_library_black_24dp).into(holder.product_imageView);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent in=new Intent(getContext(), detail.class);
                        in.putExtra("name",model.getProduct_name());
                        in.putExtra("qty",model.getProduct_quantity());
                        in.putExtra("price",model.getProduct_price());
                        in.putExtra("image",model.getPost_image());
                        in.putExtra("poster",model.getProduct_poster());
                        in.putExtra("time",model.getPost_time());
                        in.putExtra("postId",model.getPost_id());
                        in.putExtra("category",model.getProduct_category());
                        startActivity(in);









                    }
                });





            }

            @NonNull
            @Override
            public productsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_single_product,viewGroup,false);
                productsViewHolder viewHolder=new productsViewHolder(view);


                return viewHolder;
            }
        };

        productsList.setAdapter((RecyclerView.Adapter) adapter);
        adapter.startListening();

        productsList.smoothScrollToPosition(adapter.getItemCount()+1);


    }
    public static class productsViewHolder extends RecyclerView.ViewHolder{
        private ImageView product_imageView;
        private TextView tvName,tvPrice,tvQty;


        public productsViewHolder(@NonNull View itemView) {
            super(itemView);
            product_imageView=itemView.findViewById(R.id.product_image_view);
            tvName=itemView.findViewById(R.id.name_tv);
            tvPrice=itemView.findViewById(R.id.price_tv);
            tvQty=itemView.findViewById(R.id.qty_tv);






        }
    }

}
