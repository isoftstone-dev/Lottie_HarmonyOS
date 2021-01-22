package com.airbnb.lottietest.slice;

import com.airbnb.lottie.LottieView;
import com.airbnb.lottietest.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;

public class MainAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        LottieView lottieView = (LottieView)findComponentById(ResourceTable.Id_text_helloworld);
        if (lottieView == null) {

        } else {
            lottieView.addDrawTask(lottieView);
        }
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
