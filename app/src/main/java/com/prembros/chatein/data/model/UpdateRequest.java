package com.prembros.chatein.data.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class UpdateRequest {

    private DatabaseReference reference;
    private HashMap<String, Object> map;

    private UpdateRequest() {
        map = new HashMap<>();
    }

    private UpdateRequest(DatabaseReference reference) {
        this.reference = reference;
        map = new HashMap<>();
    }

    @NonNull public static UpdateRequest forMapOnly() {
        return new UpdateRequest();
    }

    @NonNull public static UpdateRequest forDatabase(DatabaseReference reference) {
        return new UpdateRequest(reference);
    }

    public UpdateRequest put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Map<String, Object> get() {
        return map;
    }

    public void update(DatabaseReference.CompletionListener listener) {
        reference.updateChildren(map, listener);
    }
}