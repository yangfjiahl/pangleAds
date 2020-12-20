package com.mandou.appinchina.feature;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mandou.appinchina.AdCodes;
import com.mandou.appinchina.R;
import com.mandou.appinchina.config.TTAdManagerHolder;
import com.mandou.appinchina.utils.TToast;
import com.mandou.appinchina.view.DislikeDialog;

/**
 * Interaction Ad
 */
public class InteractionExpressActivity extends AppCompatActivity implements View.OnClickListener {

    private TTAdNative mTTAdNative;
    private Context mContext;
    private TTAdDislike mTTAdDislike;
    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native_express_intersitial);
        mContext = getApplicationContext();
        initView();
        initTTSDKConfig();
    }


    private void initView() {
        findViewById(R.id.btn_size_1_1).setOnClickListener(this);
        findViewById(R.id.btn_size_2_3).setOnClickListener(this);
        findViewById(R.id.btn_size_3_2).setOnClickListener(this);
        findViewById(R.id.btn_show_ad).setOnClickListener(this);
    }

    private void initTTSDKConfig() {
        //step1: create TTAdNative as request endpoint
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step2: test and require permission before Ad is shown
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_size_1_1:
				loadExpressAd(AdCodes.INTERACTION_1_1, 300, 300);
                break;
            case R.id.btn_size_2_3:
				loadExpressAd(AdCodes.INTERACTION_2_3, 300, 450);
                break;
            case R.id.btn_size_3_2:
				loadExpressAd(AdCodes.INTERACTION_3_2, 450, 300);
                break;
            case R.id.btn_show_ad:
                showAd();
                break;
        }
    }

    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        //step4: create AdSlot
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setAdCount(1)
                .setUserID("calvin")
                // set required width and height in dp
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight)
                .build();
        //step5: load Ad and setup a listener
        mTTAdNative.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                TToast.show(InteractionExpressActivity.this, "load error : " + code + ", " + message);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                TToast.show(mContext, "load success !");
                showAd();
            }
        });
    }

    private void showAd() {
        if (mTTAd != null) {
            mTTAd.render();
        }else {
            TToast.show(mContext,"please load before shown");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
            @Override
            public void onAdDismiss() {
                TToast.show(mContext, "Ad close");
            }

            @Override
            public void onAdClicked(View view, int type) {
                TToast.show(mContext, "Ad click");
            }

            @Override
            public void onAdShow(View view, int type) {
                TToast.show(mContext, "Ad show");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                TToast.show(mContext, msg + " code:" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                TToast.show(mContext, "render success");
                mTTAd.showInteractionExpressAd(InteractionExpressActivity.this);

            }
        });
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                TToast.show(InteractionExpressActivity.this, "click to download", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    TToast.show(InteractionExpressActivity.this, "downloading", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(InteractionExpressActivity.this, "download stopped", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(InteractionExpressActivity.this, "download fail", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                TToast.show(InteractionExpressActivity.this, "install successs", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                TToast.show(InteractionExpressActivity.this, "click to install", Toast.LENGTH_LONG);
            }
        });
    }

    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            List<FilterWord> words = ad.getFilterWords();
            if (words == null || words.isEmpty()) {
                return;
            }

            DislikeDialog dislikeDialog = new DislikeDialog(this, words);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
                    // dislike
                    TToast.show(mContext, "click " + filterWord.getName());
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        ad.setDislikeCallback(InteractionExpressActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                TToast.show(mContext, "dislike: " + value, 3);
            }

            @Override
            public void onCancel() {
                TToast.show(mContext, "cancel ");
            }

            @Override
            public void onRefuse() {
                TToast.show(mContext, "submit success！", 3);
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }


}
