package ke.co.slab.mealbooking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ke.co.slab.mealbooking.adaptors.ChefAdapter;
import ke.co.slab.mealbooking.models.CartModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.DrawableBanner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.events.OnBannerClickListener;
import ss.com.bannerslider.views.BannerSlider;
import ss.com.bannerslider.views.indicators.IndicatorShape;

public class ChefListActivity extends BaseActivity {
    private BannerSlider bannerSlider;
    private List<CartModel> foodModel;
    private ChefAdapter foodAdapter;
    private RecyclerView recyclerView;
    private PrefManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef);
        // model and adapter
        foodModel = new ArrayList<>();
        foodAdapter = new ChefAdapter(this,foodModel);

        // recycler view
        recyclerView=findViewById(R.id.recycler_view_food_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(foodAdapter);

        // set prefManager...
        prefManager=new PrefManager(this);
        //prefManager.setFirstTimeLaunch(false);

        // run the meal task ... through meals api
        try {
            new MealsTask(this).execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(ChefListActivity.this,ChefAddNewMeal.class));
            }
        });
    }

    private void showTargetHints(ChefListActivity activity) {

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher_round);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
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
                prefManager.setFirstTimeLaunch(true);
                startActivity(new Intent(this,LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bannerSlider.removeAllBanners();
        hideProgressDialog();
    }

    private void setupBannerSlider(){
        bannerSlider = findViewById(R.id.banner_slider1);
        //addBanners();
        bannerSlider.setMustAnimateIndicators(true);
        bannerSlider.setDefaultIndicator(IndicatorShape.CIRCLE);
        bannerSlider.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void onClick(int position) {
                //Toast.makeText(MainActivity.this, "Banner with position " + String.valueOf(position) + " clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViews() {
        setToolBar();
        setupBannerSlider();

    }

   /* private void addBanners(){
        List<Banner> drawableBanners=new ArrayList<>();
        //Add banners using image urls

        drawableBanners.add(new DrawableBanner(
                R.drawable.slider01
        ));
        drawableBanners.add(new DrawableBanner(
                R.drawable.slider04
        ));
        drawableBanners.add(new DrawableBanner(
                R.drawable.slider05
        ));
        drawableBanners.add(new DrawableBanner(
                R.drawable.slider06
        ));
        drawableBanners.add(new DrawableBanner(
                R.drawable.slider07
        ));
        drawableBanners.add(new DrawableBanner(
                R.drawable.slider08
        ));
        bannerSlider.setBanners(drawableBanners);
    }
*/



    private static class MealsTask extends AsyncTask<Void,Void,Void> {

        private WeakReference<ChefListActivity> weakReference;
        private boolean isSuccess=false;
        private String message="Meal(s) list not available.";

        public MealsTask(ChefListActivity chefListActivity) {
            weakReference=new WeakReference<>(chefListActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ChefListActivity chefListActivity=weakReference.get();
            chefListActivity.showProgressDialog("Serving Dish List...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ChefListActivity activity=weakReference.get();
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.MEALS)
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                if (response.isSuccessful())
                {
                    JSONArray jsonArray=new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        CartModel model=new CartModel(
                                jsonObject.getString("id"),
                                jsonObject.getString("image"),
                                jsonObject.getString("name"),
                                jsonObject.getString("qty"),
                                jsonObject.getString("price"),
                                null
                        );
                        activity.foodModel.add(model);
                        isSuccess=true;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final ChefListActivity activity=weakReference.get();
            activity.hideProgressDialog();
            if (isSuccess){
                activity.foodAdapter.notifyDataSetChanged();
                if (activity.prefManager.isFirstTimeLaunch())
                {
                    activity.showTargetHints(activity);
                    activity.prefManager.setFirstTimeLaunch(false);
                }
            }else {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setMessage(message);
                builder.setNegativeButton("Come Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                        activity.startActivity(new Intent(activity,LoginActivity.class));
                    }
                });
                builder.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //activity.finish();
                        activity.recreate();
                    }
                });
                builder.show();
            }
        }
    }
}
