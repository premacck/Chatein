package com.prembros.chatein.injection.module.db;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.injection.scope.DbScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.prembros.chatein.util.Constants.PROFILE_IMAGES;
import static com.prembros.chatein.util.Constants.THUMBS;

@DbScope
@Module
public class StorageModule {

    @Provides @DbScope
    public FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides @DbScope @Named(PROFILE_IMAGES)
    public StorageReference provideProfileImagesRef(FirebaseStorage storage) {
        return storage.getReference().child(PROFILE_IMAGES);
    }

    @Provides @DbScope @Named(THUMBS)
    public StorageReference provideThumbsRef(FirebaseStorage storage) {
        return storage.getReference().child(PROFILE_IMAGES).child(THUMBS);
    }
}