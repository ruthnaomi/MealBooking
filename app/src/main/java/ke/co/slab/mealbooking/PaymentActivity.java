package ke.co.slab.mealbooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidstudy.daraja.Daraja;
import com.androidstudy.daraja.DarajaListener;
import com.androidstudy.daraja.model.AccessToken;
import com.androidstudy.daraja.model.LNMExpress;
import com.androidstudy.daraja.model.LNMResult;
import com.androidstudy.daraja.util.Env;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentActivity extends BaseActivity {
    private EditText editTextMobile;
    private PrefManager prefManager;
    Daraja daraja;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        prefManager=new PrefManager(this);

        setToolBar();

        editTextMobile=findViewById(R.id.mobile);

        //task=new Pay(this);
        // set and init daraja mpesa oauth
        showProgressDialog("Loading payments...");
        daraja = Daraja.with(Apis.app_key, Apis.app_secret, Env.SANDBOX, new DarajaListener<AccessToken>() {
            @Override
            public void onResult(@NonNull AccessToken accessToken) {
                Log.i(PaymentActivity.this.getClass().getSimpleName(), accessToken.getAccess_token());
                hideProgressDialog();
                Toast.makeText(PaymentActivity.this, "Enter your mobile no...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(PaymentActivity.this.getClass().getSimpleName(), error);
                recreate();
            }
        });

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher_round);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PaymentActivity.this,FoodListActivity.class));
            }
        });
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
                finish();
                prefManager.setFirstTimeLaunch(true);
                startActivity(new Intent(this,LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void pay(View view) {
        final String mobile= editTextMobile.getText().toString();
        if (validation(mobile)){
            showProgressDialog("Confirming payments...");
            final LNMExpress lnmExpress = new LNMExpress(
                    Apis.till_number,
                    "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",
                    prefManager.getToal(),
                    mobile,
                    Apis.till_number,
                    mobile,
                    Apis.call_back_url,
                    "MEAL BOOKING",
                    "BUY MEALS"
            );

            daraja.requestMPESAExpress(lnmExpress,
                    new DarajaListener<LNMResult>() {
                        @Override
                        public void onResult(@NonNull final LNMResult lnmResult) {
                            Log.i(PaymentActivity.this.getClass().getSimpleName(), lnmResult.ResponseDescription);
                            hideProgressDialog();
                            // pop up for confirmation payment...
                            AlertDialog.Builder builder=new AlertDialog.Builder(PaymentActivity.this);
                            builder.setTitle("Checkout");
                            builder.setMessage("Wait for the M-Pesa menu to pop up and enter your pin. Click on verify button after completing payment.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressDialog("Verifying payments...");
                                    new Pay(PaymentActivity.this).execute(mobile, prefManager.getToal(),prefManager.getId());
                                }
                            });
                            builder.show();
                        }

                        @Override
                        public void onError(String error) {
                            Log.i(PaymentActivity.this.getClass().getSimpleName(), error);
                            hideProgressDialog();
                            Toast.makeText(PaymentActivity.this, "Error Processing Mpesa Payment.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        }
    }

    private boolean validation(String mobile) {
        if (TextUtils.isEmpty(mobile)){
            Toast.makeText(this, "Fill mobile field", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mobile.length() < 9 || mobile.length() > 14){
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mobile.matches(Validations.number)){
            Toast.makeText(this, "Only digits allowed!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static class Pay extends AsyncTask<String,Void,Void>{
        private String message="Low Internet Connection! Try Again.";
        private boolean isSuccess=false;
        WeakReference<PaymentActivity> reference;

        public Pay(PaymentActivity paymentActivity) {
            reference=new WeakReference<>(paymentActivity);
        }

        @Override
        protected Void doInBackground(String... strings) {
            OkHttpClient okHttpClient=new OkHttpClient();
            RequestBody requestBody=new FormBody.Builder()
                    .add("mobile", strings[0])
                    .add("payment", strings[1])
                    .add("user_id", strings[2])
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.BOOKING)
                    .post(requestBody)
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                if (response.isSuccessful()){
                    JSONObject jsonObject=new JSONObject(response.body().string());
                    message=jsonObject.getString("message");
                    isSuccess=jsonObject.getBoolean("success");
                }else {
                    isSuccess=false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final PaymentActivity paymentActivity=reference.get();
            paymentActivity.hideProgressDialog();
            AlertDialog.Builder builder=new AlertDialog.Builder(paymentActivity);
            builder.setTitle("Payment!");
            if (isSuccess)
            {
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setPositiveButton("Continue Shopping", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(paymentActivity, "Payment Received Successfully", Toast.LENGTH_SHORT).show();
                        paymentActivity.startActivity(new Intent(paymentActivity,FoodListActivity.class));

                    }
                });
            }else {
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setNegativeButton("Try Again!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paymentActivity.recreate();
                    }
                });

            }
            builder.show();
        }
    }
}
