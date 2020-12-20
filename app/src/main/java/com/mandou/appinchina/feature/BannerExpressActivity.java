package com.mandou.appinchina.feature;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.mandou.appinchina.utils.UIUtils;
import com.mandou.appinchina.view.DislikeDialog;
import com.mandou.appinchina.view.LoadMoreRecyclerView;

/**
 * Banner Ad
 */
public class BannerExpressActivity extends AppCompatActivity {

    private TTAdNative mTTAdNative;
    private FrameLayout mExpressContainer;
    private Context mContext;
    private TTAdDislike mTTAdDislike;
    private TTNativeExpressAd mTTAd;
    private LoadMoreRecyclerView mListView;
    private List<AdSizeModel> mBannerAdSizeModelList;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native_express_banner);
        mContext = getApplicationContext();
        initView();
        initData();
        initRecycleView();
        initTTSDKConfig();

    }

    private void initTTSDKConfig() {
        //step1: initialize sdk
        //step2: create TTAdNative as request endpoint
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step3: test and require permission before Ad is shown
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
    }

    private void initRecycleView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        mListView.setLayoutManager(layoutManager);
        AdapterForBannerType adapterForBannerType = new AdapterForBannerType(this, mBannerAdSizeModelList);
        mListView.setAdapter(adapterForBannerType);

    }

    private void initView() {
        mExpressContainer = (FrameLayout) findViewById(R.id.express_container);
        mListView = findViewById(R.id.my_list);
        findViewById(R.id.showBanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickShowBanner();
            }
        });
    }

    private void initData() {
        mBannerAdSizeModelList = new ArrayList<>();
        int width = (int)UIUtils.getScreenWidthDp(this);
		mBannerAdSizeModelList
				.add(new AdSizeModel("600*150", width, width / 4, AdCodes.BANNER_600_150));
		mBannerAdSizeModelList
				.add(new AdSizeModel("600*300", width, width / 2, AdCodes.BANNER_600_300));
    }


    public static class AdapterForBannerType extends RecyclerView.Adapter<AdapterForBannerType.ViewHolder> {
        private List<AdSizeModel> mBannerSizeList;
        private BannerExpressActivity mActivity;

        public AdapterForBannerType(BannerExpressActivity activity, List<AdSizeModel> bannerSize) {
            mActivity = activity;
            mBannerSizeList = bannerSize;
        }

        @NonNull
        @Override
        public AdapterForBannerType.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.express_banner_list_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterForBannerType.ViewHolder viewHolder, int i) {
            AdSizeModel bannerSize = mBannerSizeList == null ? null : mBannerSizeList.get(i);
            if (bannerSize != null) {
                viewHolder.btnSize.setText(bannerSize.adSizeName);
                viewHolder.btnSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // request for banner Ad
                        mActivity.loadExpressAd(bannerSize.codeId, bannerSize.width, bannerSize.height);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mBannerSizeList != null ? mBannerSizeList.size() : 0;
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            private Button btnSize;

            public ViewHolder(View view) {
                super(view);
                btnSize = view.findViewById(R.id.btn_banner_size);
            }

        }
    }

    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        mExpressContainer.removeAllViews();
        //step4: create AdSlot
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5: load Ad and setup a listener
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                TToast.show(BannerExpressActivity.this, "load error : " + code + ", " + message);
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                mTTAd.setSlideIntervalTime(30 * 1000);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                TToast.show(mContext,"load success!");
                mTTAd.render();
            }
        });
    }

    public void onClickShowBanner() {
        if (mTTAd != null) {
            mTTAd.render();
        } else {
            TToast.show(mContext, "please load before show ..");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
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
                mExpressContainer.removeAllViews();
                mExpressContainer.addView(view);
            }
        });
        // dislike
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                TToast.show(BannerExpressActivity.this, "download", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    TToast.show(BannerExpressActivity.this, "downloading", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(BannerExpressActivity.this, "download stopped", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(BannerExpressActivity.this, "down fail", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                TToast.show(BannerExpressActivity.this, "download and install success", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                TToast.show(BannerExpressActivity.this, "click to install", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * setup dislike
     *
     * @param ad
     * @param customStyle
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            //使用自定义样式
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
                    // remove Ad
                    mExpressContainer.removeAllViews();
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        ad.setDislikeCallback(BannerExpressActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                TToast.show(mContext, "click " + value);
                // dislike and remove Ad
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onCancel() {
                TToast.show(mContext, "cancel ");
            }

            @Override
            public void onRefuse() {

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


    public static class AdSizeModel {
        public AdSizeModel(String adSizeName, int width, int height, String codeId) {
            this.adSizeName = adSizeName;
            this.width = width;
            this.height = height;
            this.codeId = codeId;
        }

        public String adSizeName;
        public int width;
        public int height;
        public String codeId;
    }
}
