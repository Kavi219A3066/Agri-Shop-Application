package com.example.cultureshops;

public class CartImpl extends Cart {
    public CartImpl(String name, String qty, String price, String poster_id, String post_id, String time, String post_image) {
        super(name, qty, price, poster_id, post_id, time, post_image);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
