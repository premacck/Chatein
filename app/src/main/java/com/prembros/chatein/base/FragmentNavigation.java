package com.prembros.chatein.base;

import android.support.v4.app.Fragment;

public interface FragmentNavigation {
    void pushFragment(Fragment fragment);
    void popFragment();
}