package com.example.tohami.eaandroidtask.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tohami.eaandroidtask.R;
import com.example.tohami.eaandroidtask.pojos.Car;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CarsOnlineAdapter extends RecyclerView.Adapter<CarsOnlineAdapter.ViewHolder> {

    private List<Car> mData;
    private LayoutInflater mInflater;
    private Context context ;

    private static final String FORMAT = "%02d:%02d:%02d";
    private static final int ARGENT_TIME = 60*5 ;

    private int seconds = 0 , lang;

    private ArrayList<BroadCastTick> receivers ;
    private ScheduledFuture updateFuture;

    // data is passed into the constructor
    public CarsOnlineAdapter(Context context ,int lang , List<Car> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context ;
        this.lang = lang ;

        receivers = new ArrayList<>() ;
        tick() ;
    }

    private void tick(){
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        updateFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        seconds ++ ;
                        if(receivers.size()>0){
                            for (int i =0 ; i<receivers.size() ; i++){
                                receivers.get(i).onTick(seconds);
                            }
                        }
                    }
                });
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    private void stopTick() {
        if(updateFuture!=null){
            updateFuture.cancel(true);
            updateFuture = null;
        }
        seconds = 0 ;
    }

    public void updateList(List<Car> data) {

        for(Car car : data) {
            int index = mData.indexOf(car) ;
            if(index == -1){
                mData.add(car) ;
                notifyItemChanged(mData.size()-1);
            }else {
                mData.set(index , car) ;
                notifyItemChanged(index);
            }
        }
    }
    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Car item = mData.get(position);


        holder.bidsValue.setText(String.valueOf(item.getAuctionInfo().getBids()));
        holder.lotValue.setText(String.valueOf(item.getAuctionInfo().getLot()));
        holder.carePrice.setText(String.valueOf(item.getAuctionInfo().getCurrentPrice()));
        //arabic
        if(lang == 1){
            holder.carTitle.setText(String.format("%s %s %d", item.getMakeAr(), item.getModelAr(), item.getYear()));
            holder.priceCurrency.setText(item.getAuctionInfo().getCurrencyAr());

        }else {
            holder.carTitle.setText(String.format("%s %s %d", item.getMakeEn(), item.getModelEn(), item.getYear()));
            holder.priceCurrency.setText(item.getAuctionInfo().getCurrencyEn());
        }
        Glide.with(context).load(item.getImage().
                replace("[w]" , "150").replace("[h]" , "150")).into(holder.itemImage);


        holder.setTicks(new BroadCastTick() {
            @Override
            public void onTick(long seconds) {
               long remaining =  item.getAuctionInfo().getEndDate() - seconds ;
               if(remaining==0){
                   receivers.remove(holder.getTicks()) ;
                   mData.remove(position) ;
                   notifyDataSetChanged();
               }else if(remaining < ARGENT_TIME){
                   holder.timeLeftValue.setTextColor(Color.RED);
               }else {
                   holder.timeLeftValue.setTextColor(Color.DKGRAY);
               }
                holder.timeLeftValue.setText(String.format("%s", String.format(Locale.ENGLISH, FORMAT,
                        TimeUnit.SECONDS.toHours(remaining),
                        TimeUnit.SECONDS.toMinutes(remaining) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.SECONDS.toHours(remaining)),
                        TimeUnit.SECONDS.toSeconds(remaining) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.SECONDS.toMinutes(remaining)))));
            }
        });

        receivers.add(holder.getTicks()) ;
    }

//    public String getInArabicLocale(String text) {
//        return String.format(Locale.getDefault() , "%s" , text) ;
//    }
//
//    public String getInArabicLocale(int text) {
//        return String.format(Locale.getDefault() , "%d" , text) ;
//    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getCarID();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }
    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        receivers.remove(holder.getTicks()) ;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_image_iv) ImageView itemImage;
        @BindView(R.id.item_car_title_tv) TextView carTitle;
        @BindView(R.id.item_car_price_tv) TextView carePrice;
        @BindView(R.id.item_car_currency_tv) TextView priceCurrency;
        @BindView(R.id.lot_value_tv) TextView lotValue;
        @BindView(R.id.bids_value_tv) TextView bidsValue;
        @BindView(R.id.time_left_value_tv) TextView timeLeftValue;

        private BroadCastTick ticks  ;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public BroadCastTick getTicks() {
            return ticks;
        }

        void setTicks(BroadCastTick ticks) {
            this.ticks = ticks;
        }
    }

    public interface BroadCastTick {
        void onTick(long milliSeconds) ;
    }
}