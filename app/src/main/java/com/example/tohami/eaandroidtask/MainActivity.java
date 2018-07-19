package com.example.tohami.eaandroidtask;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.tohami.eaandroidtask.adapters.CarsOnlineAdapter;
import com.example.tohami.eaandroidtask.pojos.CarsOnlineResponse;
import com.example.tohami.eaandroidtask.system.App;
import com.example.tohami.eaandroidtask.system.Restapi;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Inject
    Retrofit retrofit;

    @BindView(R.id.cars_recycle_view)
    RecyclerView carsRecycleView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private int lang;
    private CarsOnlineAdapter adapter;
    private String ticks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((App) getApplication()).getNetComponent().inject(this);

        ButterKnife.bind(this);

        carsRecycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        String displayLanguage =  Locale.getDefault().getLanguage() ;
        if(displayLanguage.equalsIgnoreCase("ar")) {
            lang = 1 ;
        }else {
            lang = 2 ;
        }
        getCarsOnline() ;

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCarsOnline();
            }
        });
    }

    public void getCarsOnline() {

        Call<CarsOnlineResponse> carsOnlineCall = retrofit.create(Restapi.class).getCarsOnline(ticks);

        carsOnlineCall.enqueue(new Callback<CarsOnlineResponse>() {
            @Override
            public void onResponse(Call<CarsOnlineResponse> call, final Response<CarsOnlineResponse> response) {

                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }

                ticks = response.body().getTicks() ;
                if(adapter == null) {
                    adapter = new CarsOnlineAdapter(MainActivity.this, lang, response.body().getCars());
                    carsRecycleView.setAdapter(adapter);
                }else {
                    adapter.updateList(response.body().getCars());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getCarsOnline();
                    }
                } , response.body().getRefreshInterval()*1000);
            }

            @Override
            public void onFailure(Call<CarsOnlineResponse> call, Throwable t) {
                //Set the error to the textview
//                textView.setText(t.toString());
            }
        });
    }


    @OnClick(R.id.sort_list_tv)
    void displaySortListDialog() {
    }

}
