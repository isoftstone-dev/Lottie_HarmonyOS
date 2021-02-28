package com.airbnb.lottietest.slice;

import com.airbnb.lottie.LottieView;
import com.airbnb.lottietest.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentParent;
import ohos.agp.components.LayoutScatter;
import ohos.agp.text.Layout;
import ohos.agp.utils.Point;
import ohos.agp.window.service.*;
import ohos.media.image.Image;

import java.util.Optional;

public class MainAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
    }


    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
