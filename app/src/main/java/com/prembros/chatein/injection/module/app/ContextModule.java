package com.prembros.chatein.injection.module.app;

import android.content.Context;

import com.prembros.chatein.injection.component.DbComponent;
import com.prembros.chatein.injection.scope.AppScope;

import dagger.Module;
import dagger.Provides;

/**
 *
 * Created by Prem$ on 3/7/2018.
 */

@AppScope
@Module(subcomponents = {DbComponent.class})
public class ContextModule {

    private final Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides @AppScope public Context getContext() {
        return context;
    }
}