package com.mandou.appinchina.feature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.mandou.appinchina.R;
import com.mandou.appinchina.config.TTAdManagerHolder;
import com.mandou.appinchina.utils.TToast;

/**
 * Full Screen Video
 */
public class FullScreenVideoActivity extends AppCompatActivity {
    private static final String TAG = "FullScreenVideoActivity";
    private Button mLoadAd;
    private Button mLoadAdVertical;
    private Button mShowAd;
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private String mHorizontalCodeId;
    private String mVerticalCodeId;
    private boolean mIsExpress = false;
    private boolean mIsLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_screen_video);
        mLoadAd = (Button) findViewById(R.id.btn_reward_load);
        mLoadAdVertical = (Button) findViewById(R.id.btn_reward_load_vertical);
        mShowAd = (Button) findViewById(R.id.btn_reward_show);
        //step1: initialize sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2: test and require permission before Ad is shown
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3: create TTAdNative as request endpoint
        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());
        getExtraInfo();
        initClickEvent();
    }


    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mHorizontalCodeId = intent.getStringExtra("horizontal_rit");
        mVerticalCodeId = intent.getStringExtra("vertical_rit");
        mIsExpress = intent.getBooleanExtra("is_express", true);
    }

    private void initClickEvent() {
        mLoadAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAd(mHorizontalCodeId, TTAdConstant.HORIZONTAL);
            }
        });
        mLoadAdVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAd(mVerticalCodeId, TTAdConstant.VERTICAL);
            }
        });
        mShowAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mttFullVideoAd != null&&mIsLoaded) {
                    //step6: show Ad
                    mttFullVideoAd.showFullScreenVideoAd(FullScreenVideoActivity.this, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                    mttFullVideoAd = null;
                } else {
                    TToast.show(FullScreenVideoActivity.this, "please load before show");
                }
            }
        });
    }
    private boolean mHasShowDownloadActive = false;

    private void loadAd(String codeId, int orientation) {
        //step4: create AdSlot
        AdSlot adSlot;
        if (mIsExpress) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    // set required width and height in dp
                    .setExpressViewAcceptedSize(500,500)
                    .build();

        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .build();
        }
        //step5: load Ad and setup a listener
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                TToast.show(FullScreenVideoActivity.this, message);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.e(TAG, "Callback --> onFullScreenVideoAdLoad");

                TToast.show(FullScreenVideoActivity.this, "FullVideoAd loaded  Ad：" + getAdType(ad.getFullVideoAdType()));
                mttFullVideoAd = ad;
                mIsLoaded = false;
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(TAG, "Callback --> FullVideoAd show");
                        TToast.show(FullScreenVideoActivity.this, "FullVideoAd show");
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(TAG, "Callback --> FullVideoAd bar click");
                        TToast.show(FullScreenVideoActivity.this, "FullVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(TAG, "Callback --> FullVideoAd close");
                        TToast.show(FullScreenVideoActivity.this, "FullVideoAd close");
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "Callback --> FullVideoAd complete");
                        TToast.show(FullScreenVideoActivity.this, "FullVideoAd complete");
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(TAG, "Callback --> FullVideoAd skipped");
                        TToast.show(FullScreenVideoActivity.this, "FullVideoAd skipped");

                    }

                });


                ad.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);

                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            TToast.show(FullScreenVideoActivity.this, "downloading", Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(FullScreenVideoActivity.this, "download stopped", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(FullScreenVideoActivity.this, "download fail", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(FullScreenVideoActivity.this, "download success", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(FullScreenVideoActivity.this, "install success", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onFullScreenVideoCached() {
                Log.e(TAG, "Callback --> onFullScreenVideoCached");
                mIsLoaded = true;
                TToast.show(FullScreenVideoActivity.this, "FullVideoAd video cached");
            }
        });


    }

    private String getAdType(int type) {
        switch (type) {
            case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                return "Normal Video，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                return "Playable Full Screen Video，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE:
                return "Playable，type=" + type;
        }

        return "unknown+type=" + type;
    }
}
