package ke.co.slab.mealbooking.adaptors;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;

import com.bumptech.glide.Glide;

import java.util.List;

import ke.co.slab.mealbooking.Apis;
import ke.co.slab.mealbooking.DishDetailsActivity;
import ke.co.slab.mealbooking.FoodListActivity;
import ke.co.slab.mealbooking.R;
import ke.co.slab.mealbooking.models.FoodModel;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder>{

    Context context;
    List<FoodModel> foodModels;

    public FoodAdapter(Context context, List<FoodModel> foodModel) {
        this.context=context;
        this.foodModels=foodModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String id=foodModels.get(position).getId();
        final String name=foodModels.get(position).getName();
        final String price=foodModels.get(position).getPrice();
        final String qty=foodModels.get(position).getQty();

        holder.viewName.setText(name);
        Glide.with(context)
                .load(Apis.ASSET_URL + foodModels.get(position).getImage())
                .thumbnail(0.1f)
                .into(holder.imageViewDish);

        holder.viewPrice.setText(String.format("Ksh.%s KES", price));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //...
                Intent intent=new Intent(context, DishDetailsActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("name",name);
                intent.putExtra("price",price);
                intent.putExtra("qty",qty);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView viewName,viewPrice;
        ImageView imageViewDish;
        public ViewHolder(View itemView) {
            super(itemView);
            viewName=itemView.findViewById(R.id.name_text_view);
            viewPrice=itemView.findViewById(R.id.price_text_view);
            imageViewDish=itemView.findViewById(R.id.dishImage);
        }
    }
}
