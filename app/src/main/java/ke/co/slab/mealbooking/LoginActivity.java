package ke.co.slab.mealbooking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {
    private EditText textUsername,textPassword;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init the EditText fields
        textUsername = findViewById(R.id.username);
        textPassword = findViewById(R.id.password);

        // init prefMnager
        prefManager=new PrefManager(this);

        // first time launch
        if(!prefManager.isFirstTimeLaunch())
        {
            finish();
            if (prefManager.getRole().contentEquals("chef"))
            {
                startActivity(new Intent(this,ChefListActivity.class));
            }else {
                startActivity(new Intent(this,FoodListActivity.class));
            }
        }//else {
            //showTargetHints(this);
       // }

    }

   // private void showTargetHints(LoginActivity activity) {
       // new TapTargetSequence(activity).targets(
      //  );
   // }

    public void login(View view) {
        // getting the input data values
        String username=textUsername.getText().toString();
        String password=textPassword.getText().toString();

        // validation
       if (validation(username,password))
        {
           Log.d("LOGIN","Button Login Here...");
            // run LoginTask...asynctask
            try{
                new LoginTask(this).execute(username,password);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

   private boolean validation(String username, String password)
    {
       if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Username Required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Password Required!", Toast.LENGTH_SHORT).show();
            return false;
        }return true;
    }

    public static class LoginTask extends AsyncTask<String,Void,Void> {
        private String role;
        private boolean isSuccess;
        WeakReference<LoginActivity>weakReference;

        public LoginTask(LoginActivity loginActivity) {
            weakReference=new WeakReference<>(loginActivity);//handles memory leaks
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           LoginActivity activity=weakReference.get();
            activity.showProgressDialog("Signing in...");

        }

        @Override
        protected Void doInBackground(String... strings) {
            LoginActivity activity=weakReference.get();
            OkHttpClient okHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            FormBody body=new FormBody.Builder()
                    .add("username",strings[0])
                    .add("password",strings[1])
                    .build();
            Request request=new Request.Builder()
                    .url(Apis.LOGIN)
                    .post(body)
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                Log.d("RESPONSE", String.valueOf(response.isSuccessful()));
                if (response.isSuccessful())
                {
                    JSONObject jsonObject=new JSONObject(response.body().string());
                    isSuccess=jsonObject.getBoolean("success");
                    role=jsonObject.getString("role");
                    activity.prefManager.setId(jsonObject.getString("user_id"));
                   Log.d("ROLE",role);
                    activity.prefManager.setRole(role);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           final LoginActivity activity=weakReference.get();
            activity.hideProgressDialog();
            if (isSuccess){
                // sign in success...
                // either to chef page or cashier..
                activity.finish();
                if (role.contentEquals("chef"))
                {
                    activity.startActivity(new Intent(activity,ChefListActivity.class));
                } else {
                    activity.startActivity(new Intent(activity,FoodListActivity.class));
                }
            }else {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setMessage("Oops! We didn't recognize you.");
                builder.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.recreate();
                    }
                });
                builder.setPositiveButton("Forgot Password?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }
    }
}
