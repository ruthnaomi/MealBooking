package ke.co.slab.mealbooking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.DrawableBanner;
import ss.com.bannerslider.events.OnBannerClickListener;
import ss.com.bannerslider.views.BannerSlider;
import ss.com.bannerslider.views.indicators.IndicatorShape;

public class DishDetailsActivity extends BaseActivity {
    private BannerSlider bannerSlider;
    private String id,name,price,qty;
    private TextView viewName,viewPrice,viewQty;
    private PrefManager prefManager;
    Object object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_details);

        setToolBar();

        //set pref
        prefManager=new PrefManager(this);

        // get the data from intent...
        id=getIntent().getStringExtra("id");
        name=getIntent().getStringExtra("name");
        price=getIntent().getStringExtra("price");
        qty=getIntent().getStringExtra("qty");

        // View initialization...
        viewName = findViewById(R.id.detailName);
        viewName.setText(name);
        viewPrice = findViewById(R.id.detailPrice);
        viewPrice.setText(String.format("Ksh.%sKES", price));
        viewQty = findViewById(R.id.detailQty);
        viewQty.setText(String.format("Dish Rem. For %s People", qty));

        // qty spinner dropdown
        // spinner dropdown for schools
        Spinner spinner = findViewById(R.id.spinnerQty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.qty, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                object = parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // floating action button...
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dishQty = (String) object;
                if (validation(dishQty))
                {
                    try {
                        new CartTask(DishDetailsActivity.this).execute(id,prefManager.getId(),price,dishQty);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean validation(String dishQty) {
        if (TextUtils.equals(dishQty,"Choose Quantity"))
        {
            Toast.makeText(this, "Choose a valid Quantity!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DishDetailsActivity.this,FoodListActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart:
                startActivity(new Intent(this,CartActivity.class));
                return true;
            case R.id.logout:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }


    private static class CartTask extends AsyncTask<String,Void,Void>{
        WeakReference<DishDetailsActivity>weakReference;
        private boolean isSuccess=false;
        public CartTask(DishDetailsActivity dishDetailsActivity) {
            weakReference=new WeakReference<>(dishDetailsActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DishDetailsActivity activity=weakReference.get();
            activity.showProgressDialog("Adding to cart...");
        }

        @Override
        protected Void doInBackground(String... strings) {
            DishDetailsActivity activity=weakReference.get();
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            FormBody body=new FormBody.Builder()
                    .add("meal_id",strings[0])
                    .add("user_id",strings[1])
                    .add("price",strings[2])
                    .add("qty",strings[3])
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.CART)
                    .post(body)
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                if (response.isSuccessful())
                {
                    JSONObject jsonObject=new JSONObject(response.body().string());
                    isSuccess=jsonObject.getBoolean("success");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            DishDetailsActivity activity=weakReference.get();
            activity.hideProgressDialog();
            if (isSuccess)
            {
                Toast.makeText(activity, "Added to Cart!", Toast.LENGTH_SHORT).show();
                if (activity.prefManager.isFirstTimeCart())
                {
                    activity.showTargetHints(activity);
                    activity.prefManager.setFirstTimeCart(false);
                }
            }else {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setMessage("Oops! We couldn't add the meal to cart.");
                builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }
    }

    private void showTargetHints(DishDetailsActivity activity) {
        new TapTargetSequence(activity).targets(/*
                TapTarget.forView(activity.findViewById(R.id.cart),"Shopping Cart","Your meal has been added here.\nIn Future: Find all meals placed to cart by touching here :) ")
                        .cancelable(false)
        */);
    }


}
