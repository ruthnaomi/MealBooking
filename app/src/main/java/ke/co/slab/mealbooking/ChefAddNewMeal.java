package ke.co.slab.mealbooking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChefAddNewMeal extends BaseActivity {

    // set the edit texts
    EditText textName,textPrice,textQty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_add_new_meal);

        setToolBar();

        // init the fields
        textName=findViewById(R.id.name);
        textPrice=findViewById(R.id.price);
        textQty=findViewById(R.id.qty);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher_round);
    }

    public void add(View view) {
        // get the text data
        String name = textName.getText().toString();
        String price = textPrice.getText().toString().trim();
        String qty = textQty.getText().toString().trim();

        // set validation rules
        if (validation(name,price,qty)){
            showProgressDialog("Adding meal...");
            new AddMeal(this).execute(name,price,qty);
        }
    }

    private boolean validation(String name, String price, String qty) {
        // Name Validation
        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Meal Name Required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!name.matches(Apis.textPattern))
        {
            Toast.makeText(this, "Invalid Meal Name!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (name.length() < 3)
        {
            Toast.makeText(this, "Meal Name Should be More Than 3 Chars!", Toast.LENGTH_LONG).show();
            return false;
        }

        // Price Validation
        if (TextUtils.isEmpty(price))
        {
            Toast.makeText(this, "Price Required!", Toast.LENGTH_SHORT).show();
            return false;
        }
       /* if (!price.matches(Apis.number))
        {
            Toast.makeText(this, "Invalid Price!", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        if (price.length() < 1)
        {
            Toast.makeText(this, "Price should be greater than one!", Toast.LENGTH_LONG).show();
            return false;
        }

        // Qty Validation
        if (TextUtils.isEmpty(qty))
        {
            Toast.makeText(this, "Qty Required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        /*if (!qty.matches(Apis.number))
        {
            Toast.makeText(this, "Invalid Qty!", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        if (qty.length() < 1)
        {
            Toast.makeText(this, "Qty Should be greater than 0!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private static class AddMeal extends AsyncTask<String,Void,Void>{

        WeakReference<ChefAddNewMeal>weakReference;
        String message;
        boolean isSuccess;
        public AddMeal(ChefAddNewMeal chefAddNewMeal) {
            weakReference=new WeakReference<>(chefAddNewMeal);
        }

        @Override
        protected Void doInBackground(String... strings) {
            ChefAddNewMeal activity=weakReference.get();
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            RequestBody body = new FormBody.Builder()
                    .add("name",strings[0])
                    .add("price",strings[1])
                    .add("qty",strings[2])
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.MEALS)
                    .post(body)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful())
                {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    isSuccess = jsonObject.getBoolean("success");
                    message = jsonObject.getString("message");
                }else {
                    isSuccess=false;
                    message="Meal Not Created!";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final ChefAddNewMeal activity=weakReference.get();
            activity.hideProgressDialog();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            if (isSuccess){
                builder.setMessage(message);
                builder.setPositiveButton("Add New", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.recreate();
                    }
                });
                builder.setNegativeButton("Meal List", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.startActivity(new Intent(activity,ChefListActivity.class));
                    }
                });
            }else {
                builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }
            builder.setCancelable(false);
            builder.show();
        }
    }
}
