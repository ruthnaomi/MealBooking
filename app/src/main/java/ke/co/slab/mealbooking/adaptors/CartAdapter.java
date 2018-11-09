package ke.co.slab.mealbooking.adaptors;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import ke.co.slab.mealbooking.Apis;
import ke.co.slab.mealbooking.CartActivity;
import ke.co.slab.mealbooking.R;
import ke.co.slab.mealbooking.models.CartModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private List<CartModel>cartModels;

    public CartAdapter(Context context, List<CartModel>cartModels) {
        this.context=context;
        this.cartModels=cartModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cart_list,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.viewMealName.setText(cartModels.get(position).getName());
        holder.viewMealQty.setText(String.format("Qty: %s", cartModels.get(position).getQty()));
        holder.viewNetPrice.setText(String.format("Price: Ksh.%s KES", cartModels.get(position).getNet_price()));
        holder.viewGrossPrice.setText(String.format("Total: Ksh. %s KES", cartModels.get(position).getGross_price()));
        Glide.with(context)
                .load(Apis.ASSET_URL + cartModels.get(position).getImage())
                .thumbnail(0.1f)
                .into(holder.imageViewMealImage);
        holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Delete(context).execute(cartModels.get(holder.getAdapterPosition()).getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView viewMealName,viewMealQty,viewNetPrice,viewGrossPrice;
        ImageView imageViewMealImage, imageViewDel;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewMealImage=itemView.findViewById(R.id.meal_image);
            imageViewDel=itemView.findViewById(R.id.delete);
            viewMealName=itemView.findViewById(R.id.meal_name);
            viewMealQty=itemView.findViewById(R.id.meal_qty);
            viewNetPrice=itemView.findViewById(R.id.meal_net_price);
            viewGrossPrice=itemView.findViewById(R.id.meal_gross_price);
        }
    }

    private static class Delete extends AsyncTask<String,Void,Void>{
        WeakReference<Context>reference;
        boolean isSuccess;
        String message;
        ProgressDialog mProgressDialog;

        public Delete(Context context) {
            reference=new WeakReference<>(context);
            mProgressDialog=new ProgressDialog(context);
        }

        public void showProgressDialog() {
            if (mProgressDialog == null) {
                mProgressDialog.setMessage("Removing product ...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
            }

            mProgressDialog.show();
        }

        public void hideProgressDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(String... strings) {
            OkHttpClient okHttpClient=new OkHttpClient();
            Request request=new Request.Builder()
                    .url(Apis.CART + "/" + strings[0])
                    .delete()
                    .build();
            try {
                Response response=okHttpClient.newCall(request).execute();
                if (response.isSuccessful()){
                    JSONObject jsonObject=new JSONObject(response.body().string());
                    isSuccess=jsonObject.getBoolean("success");
                    message=jsonObject.getString("message");
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
            final Context context=reference.get();
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            hideProgressDialog();
            if (isSuccess)
            {
                builder.setTitle("Deleted!");
                builder.setMessage(message);
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context,CartActivity.class));
                    }
                });
            }else
            {
                builder.setTitle("Failed!");
                builder.setMessage("Product failed to be removed. Try again!");
                builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }
            builder.show();
        }
    }
}
