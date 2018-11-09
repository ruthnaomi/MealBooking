package ke.co.slab.mealbooking;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class LogoutAsync extends AsyncTask<Void,Void,Void>{
    private WeakReference<Context>weakReference;
    private boolean isSuccess;
    private String message;
    public LogoutAsync(Context context) {
        weakReference=new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder()
                .url(Apis.LOGOUT)
                .build();
        try {
            Response response=okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                JSONObject jsonObject=new JSONObject(response.body().string());
                isSuccess=jsonObject.getBoolean("success");
                message=jsonObject.getString("message");
            }else {
                isSuccess=false;
                message="Logout failed! Couldn't connect to the server at this time.";
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
        Context context=weakReference.get();
        if (isSuccess)
        {
            context.startActivity(new Intent(context,LoginActivity.class));
        }else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
