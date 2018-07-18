package com.example.tohami.eaandroidtask.dagger.component;


import com.example.tohami.eaandroidtask.MainActivity;
import com.example.tohami.eaandroidtask.dagger.module.AppModule;
import com.example.tohami.eaandroidtask.dagger.module.NetModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {
    void inject(MainActivity activity);
}