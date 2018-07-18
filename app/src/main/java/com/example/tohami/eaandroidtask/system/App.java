package com.example.tohami.eaandroidtask.system;

import android.app.Application;

import com.example.tohami.eaandroidtask.dagger.component.DaggerNetComponent;
import com.example.tohami.eaandroidtask.dagger.component.NetComponent;
import com.example.tohami.eaandroidtask.dagger.module.AppModule;
import com.example.tohami.eaandroidtask.dagger.module.NetModule;

public class App extends Application {

    private NetComponent mNetComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule(STATICS.BASE_URLS))
                .build();
    }


    public NetComponent getNetComponent() {
        return mNetComponent;
    }
}