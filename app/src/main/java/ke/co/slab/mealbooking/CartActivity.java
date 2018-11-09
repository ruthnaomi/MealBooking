package ke.co.slab.mealbooking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ke.co.slab.mealbooking.adaptors.CartAdapter;
import ke.co.slab.mealbooking.models.CartModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class  CartActivity extends BaseActivity {
    private List<CartModel>list;
    private CartAdapter adapter;
    private PrefManager prefManager;
    private TextView viewTotal;
    public int total=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //setToolBar
        setToolBar();

        // set
        list=new ArrayList<>();
        adapter=new CartAdapter(this,list);

        // pref
        prefManager=new PrefManager(this);

        // recycler view
        RecyclerView recyclerView=findViewById(R.id.recycler_view_cart_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // set view total
        viewTotal=findViewById(R.id.total);

        // run task cart
        try{
            new CartTask(this).execute(prefManager.getId());
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this,FoodListActivity.class));
            }
        });
    }

    public void checkout(View view) {
        startActivity(new Intent(this,PaymentActivity.class));
    }

    private static class CartTask extends AsyncTask<String,Void,Void>{
        WeakReference<CartActivity> weakReference;
        boolean isSuccess=false;

        public CartTask(CartActivity cartActivity) {
            weakReference=new WeakReference<>(cartActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CartActivity activity=weakReference.get();
            activity.showProgressDialog("Loading cart...");
        }

        @Override
        protected Void doInBackground(String... strings) {
            CartActivity activity=weakReference.get();
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.CART + "/" + strings[0])
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                if (response.isSuccessful())
                {
                    JSONArray jsonArray=new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        activity.total += jsonObject.getInt("gross_price");
                        CartModel model=new CartModel(
                                jsonObject.getString("id"),
                                jsonObject.getString("image"),
                                jsonObject.getString("name"),
                                jsonObject.getString("qty"),
                                jsonObject.getString("net_price"),
                                jsonObject.getString("gross_price")
                        );
                        activity.list.add(model);
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
            final CartActivity activity=weakReference.get();
            activity.hideProgressDialog();
            if (isSuccess){
                activity.adapter.notifyDataSetChanged();
                String grand_total= String.valueOf(activity.total);
                activity.viewTotal.setText(String.format("Ksh.%s KES", grand_total));
                activity.prefManager.setTotal(grand_total);
            }else {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setMessage("Cart is Empty!");
                builder.setPositiveButton("Shop Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(activity,FoodListActivity.class));
                    }
                });
                builder.setNegativeButton("Refresh", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.recreate();
                    }
                });
                builder.show();
            }
        }
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

}
