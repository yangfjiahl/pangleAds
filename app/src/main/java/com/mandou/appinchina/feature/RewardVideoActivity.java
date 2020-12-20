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
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.mandou.appinchina.R;
import com.mandou.appinchina.config.TTAdManagerHolder;
import com.mandou.appinchina.utils.TToast;

/**
 * Reward Ad
 */
public class RewardVideoActivity extends AppCompatActivity {
    private static final String TAG = "RewardVideoActivity";
    private Button mLoadAd;
    private Button mLoadAdVertical;
    private Button mShowAd;
    private TTAdNative mTTAdNative;
    private TTRewardVideoAd mttRewardVideoAd;
    private String mHorizontalCodeId;
    private String mVerticalCodeId;
    private boolean mIsExpress = false;
    private boolean mIsLoaded = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reward_video);
        mLoadAd = (Button) findViewById(R.id.btn_reward_load);
        mLoadAdVertical = (Button) findViewById(R.id.btn_reward_load_vertical);
        mShowAd = (Button) findViewById(R.id.btn_reward_show);
        //step1: initialize sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2: create TTAdNative as request endpoint
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3: test and require permission before Ad is shown
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
                if (mttRewardVideoAd != null&&mIsLoaded) {
                    //step6: show Ad
                    mttRewardVideoAd.showRewardVideoAd(RewardVideoActivity.this, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                    mttRewardVideoAd = null;
                } else {
                    TToast.show(RewardVideoActivity.this, "请先加载广告");
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
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                TToast.show(RewardVideoActivity.this, message);
            }

            @Override
            public void onRewardVideoCached() {
                Log.e(TAG, "Callback --> onRewardVideoCached");
                mIsLoaded = true;
                TToast.show(RewardVideoActivity.this, "Callback --> rewardVideoAd video cached");
                mttRewardVideoAd.showRewardVideoAd(RewardVideoActivity.this, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                mttRewardVideoAd = null;
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.e(TAG, "Callback --> onRewardVideoAdLoad");

                TToast.show(RewardVideoActivity.this, "rewardVideoAd loaded 广告类型：" + getAdType(ad.getRewardVideoAdType()));
                mIsLoaded = false;
                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(TAG, "Callback --> rewardVideoAd show");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd show");
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(TAG, "Callback --> rewardVideoAd bar click");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(TAG, "Callback --> rewardVideoAd close");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd close");
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "Callback --> rewardVideoAd complete");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd complete");
                    }

                    @Override
                    public void onVideoError() {
                        Log.e(TAG, "Callback --> rewardVideoAd error");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd error");
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                        String logString = "verify:" + rewardVerify + " amount:" + rewardAmount +
                                " name:" + rewardName + " errorCode:" + errorCode + " errorMsg:" + errorMsg;
                        Log.e(TAG, "Callback --> " + logString);
                        TToast.show(RewardVideoActivity.this, logString);

                        // TODO: your reward logic
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.e(TAG, "Callback --> rewardVideoAd has onSkippedVideo");
                        TToast.show(RewardVideoActivity.this, "rewardVideoAd has onSkippedVideo");
                    }
                });
                mttRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);

                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            TToast.show(RewardVideoActivity.this, "downloading", Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(RewardVideoActivity.this, "download stopped", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(RewardVideoActivity.this, "download fail", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(RewardVideoActivity.this, "download success", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                        TToast.show(RewardVideoActivity.this, "install success", Toast.LENGTH_LONG);
                    }
                });
            }
        });
    }


    private String getAdType(int type) {
        switch (type) {
            case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                return "Normal Reward Video，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                return "Playable Reward Full Screen Video，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE:
                return "Playable，type=" + type;
        }

        return "unknown+type=" + type;
    }
}
