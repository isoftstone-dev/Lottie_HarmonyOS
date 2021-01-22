package com.airbnb.lottie;

import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.LayerParser;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.utils.Matrix;
import ohos.app.Context;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;

import java.io.IOException;

import static okio.Okio.buffer;
import static okio.Okio.source;

public class LottieView extends Component implements Component.DrawTask {

    //lottie解析器
    private LottieComposition lottieComposition;

    //
    private CompositionLayer compositionLayer;


    // 动画
    private AnimatorValue animatorValue;

    public LottieView(Context context) {
        super(context);
        init();
    }

    public LottieView(Context context, AttrSet attrSet) {
        super(context, attrSet);
        init();
    }

    public LottieView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init();
    }

    public LottieView(Context context, AttrSet attrSet, int resId) {
        super(context, attrSet, resId);
        init();
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        final Matrix matrix = new Matrix();
        int alpha = 255;
        matrix.reset();
        compositionLayer.draw(canvas, matrix, alpha);
    }

    // 动画侦听函数
    private final AnimatorValue.ValueUpdateListener mAnimatorUpdateListener
            = new AnimatorValue.ValueUpdateListener() {
        @Override
        public void onUpdate(AnimatorValue animatorValue, float v) {
            if (v >= 0.8f) {
              v = 0.8f;
            }
            compositionLayer.setProgress(v);
            invalidate();
        }
    };

    private void init()  {
        // 解析json文件，获取InputStream
        ResourceManager resourceManager = getContext().getResourceManager();
        RawFileEntry rawFileEntry = resourceManager.getRawFileEntry("resources/rawfile/bullseye.json");
        Resource resource = null;
        try {
            resource = rawFileEntry.openRawFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析json文件,返回lottieComposition
        JsonReader reader = JsonReader.of(buffer(source(resource)));
        try {
            lottieComposition = LottieCompositionMoshiParser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 构造compositionLayer
        compositionLayer = new CompositionLayer(
                null,
                LayerParser.parse(lottieComposition),
                lottieComposition.getLayers(),
                lottieComposition);


        // 启动动画
        animatorValue = new AnimatorValue();
        animatorValue.setCurveType(Animator.CurveType.LINEAR);
        animatorValue.setDelay(100);
        animatorValue.setLoopedCount(Animator.INFINITE);
        animatorValue.setDuration(4000);
        animatorValue.setValueUpdateListener(mAnimatorUpdateListener);
        animatorValue.start();
    }
}
