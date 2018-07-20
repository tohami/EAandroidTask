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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.tohami.eaandroidtask.R;
import com.example.tohami.eaandroidtask.pojos.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    //five minutes => used to change the color of timer to red
    private static final int ARGENT_TIME = 60*5 ;

    private int seconds = 0 , lang;

    private ArrayList<OnTimeChangeListener> receivers ;
    private ScheduledFuture updateFuture;

    public final static int SORTEDBY_END_DATE = 0 ;
    public final static int SORTEDBY_PRICE = 1 ;
    public final static int SORTEDBY_YEAR = 2 ;

    public final static int SORT_ASC = 1 ;
    public final static int SORT_DESC = -1 ;

    private int sortedby ;
    private int asc_decs ;

    public CarsOnlineAdapter(Context context ,int lang , int sortedby , int asc_decs , List<Car> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context ;
        this.lang = lang ;
        this.sortedby = sortedby ;
        this.asc_decs = asc_decs ;
        //sort the data
        Collections.sort(mData , getComparator());
        receivers = new ArrayList<>() ;
        tick() ;
    }



    //this method responsibility is broadcast time change event every second to listener views
    //the idea here is we dont need to broadcast the time change to hole recycle view elements
    //only the visible and active items will receive the time change event every second
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
                                //receivers is the views that listen to the time change event
                                receivers.get(i).onTimeChange(seconds);
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
        //after sync with api , if data change we update the list
        for(Car car : data) {
            //get the index of the item that changed
            int index = mData.indexOf(car) ;
            Log.e("new Car" , mData.get(index).getModelAr()+ " - " + mData.get(index).getMakeAr() + " - "+
                    mData.get(index).getAuctionInfo().getBids() + " - " + mData.get(index).getAuctionInfo().getCurrentPrice() ) ;
            //if the item is not fount at the list , add it as new item
            if(index == -1){
                //we need to add the item to the list in the index whitch keep the list sorted
                //Collections.binarySearch return the index of the element and if the element is not fount
                //it return the (-(insertion point) - 1). The insertion point is defined as the
                // point at which the car would be inserted into the list
                int positionToAdd = Collections.binarySearch(mData , car , getComparator()) ;
                if(positionToAdd<0){
                    positionToAdd = Math.abs(positionToAdd+1) ;
                }
                mData.add(positionToAdd , car);
                notifyItemChanged(positionToAdd);
            }else {
                //if found replace the car with new car data
                Log.e("new Car" , car.getModelAr() + " - " +  car.getMakeAr() + " - " +
                        car.getAuctionInfo().getBids() + " - " + car.getAuctionInfo().getCurrentPrice() ) ;
                mData.set(index , car) ;
                notifyItemChanged(index);
            }
        }
    }

    public void setSortedby(int sortedby) {
        setSortedby(sortedby , SORT_ASC);
    }

    public void setSortedby(int sortedby , int asc_decs){
        this.sortedby = sortedby ;
        this.asc_decs =asc_decs ;
        //sort the list and notify the adapter
        Collections.sort(mData , getComparator());
        notifyDataSetChanged();
    }

    //get the comparator implementation that used to sort the list
    //asc_desc = 1 or -1 , i use to revert the order
    private Comparator<Car> getComparator() {
        switch (sortedby) {
            case SORTEDBY_END_DATE:
                return new Comparator<Car>() {
                    @Override
                    public int compare(Car car, Car t1) {
                        return asc_decs * car.getAuctionInfo().getEndDate().compareTo(t1.getAuctionInfo().getEndDate());
                    }
                };
            case SORTEDBY_PRICE:
                return new Comparator<Car>() {
                    @Override
                    public int compare(Car car, Car t1) {
                        return asc_decs * car.getAuctionInfo().getCurrentPrice().compareTo(t1.getAuctionInfo().getCurrentPrice());
                    }
                };
            case SORTEDBY_YEAR:
                return new Comparator<Car>() {
                    @Override
                    public int compare(Car car, Car t1) {
                        return asc_decs * car.getYear().compareTo(t1.getYear());
                    }
                };
            default:
                return new Comparator<Car>() {
                    @Override
                    public int compare(Car car, Car t1) {
                        return asc_decs * car.getAuctionInfo().getEndDate().compareTo(t1.getAuctionInfo().getEndDate());
                    }
                };
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

        holder.favToggleButton.setChecked(item.isFav());
        holder.favToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mData.get(holder.getAdapterPosition()).setFav(b);
            }
        });

        //listem to time change event and update the view each second
        holder.setOnTimeChangeListener(new OnTimeChangeListener() {
            @Override
            public void onTimeChange(long seconds) {
               long remaining =  item.getAuctionInfo().getEndDate() - seconds ;
               if(remaining==0){
                   receivers.remove(holder.getOnTimeChangeListener()) ;
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

        //add this view as listener to time event
        receivers.add(holder.getOnTimeChangeListener()) ;
    }

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
        // when the view recycled we do not need to receive the time updates any more
        //so remove the listner fro the recievers list
        receivers.remove(holder.getOnTimeChangeListener()) ;
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
        @BindView(R.id.item_fav_tb) ToggleButton favToggleButton;

        private OnTimeChangeListener onTimeChangeListener;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public OnTimeChangeListener getOnTimeChangeListener() {
            return onTimeChangeListener;
        }

        void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
            this.onTimeChangeListener = onTimeChangeListener;
        }
    }

    public interface OnTimeChangeListener {
        void onTimeChange(long milliSeconds) ;
    }
}