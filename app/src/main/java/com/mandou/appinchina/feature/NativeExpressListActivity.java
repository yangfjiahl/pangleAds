package com.mandou.appinchina.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mandou.appinchina.AdCodes;
import com.mandou.appinchina.R;
import com.mandou.appinchina.config.TTAdManagerHolder;
import com.mandou.appinchina.utils.TToast;
import com.mandou.appinchina.utils.UIUtils;
import com.mandou.appinchina.view.DislikeDialog;
import com.mandou.appinchina.view.ILoadMoreListener;
import com.mandou.appinchina.view.LoadMoreListView;

/**
 * Feed list Ad
 */
public class NativeExpressListActivity extends AppCompatActivity {
    private static final String TAG = "NativeExpressListActivity";

    private static final int AD_POSITION = 3;
    private static final int LIST_ITEM_COUNT = 30;
    private LoadMoreListView mListView;
    private MyAdapter myAdapter;
    private List<TTNativeExpressAd> mData;
    private EditText mEtWidth;
    private EditText mEtHeight;
    private Button mButtonLoadAd;
    private TTAdNative mTTAdNative;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native_express_listview);
        //step1: initialize sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2: create TTAdNative as request endpoint
        mTTAdNative = ttAdManager.createAdNative(this);
        //step3: test and require permission before Ad is shown
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        initListView();
    }

    private void initListView() {
        mEtHeight = (EditText) findViewById(R.id.express_height);
        mEtWidth = (EditText) findViewById(R.id.express_width);
        mButtonLoadAd = (Button) findViewById(R.id.btn_express_load);
        mButtonLoadAd.setOnClickListener(mClickListener);

        mListView = (LoadMoreListView) findViewById(R.id.my_list);
        mData = new ArrayList<>();
        myAdapter = new MyAdapter(this, mData);
        mListView.setAdapter(myAdapter);
        mListView.setLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadListAd();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListAd();
            }
        }, 500);
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_express_load) {
                if (mData != null) {
                    mData.clear();
                    if (myAdapter != null) {
                        myAdapter.notifyDataSetChanged();
                    }
                }
                loadListAd();
            }
        }
    };

    /**
     * require to load feed list Ad
     */
    private void loadListAd() {
        int width = (int)UIUtils.getScreenWidthDp(this);
        //step4: create AdSlot
        AdSlot adSlot = new AdSlot.Builder()
				.setCodeId(AdCodes.FEEDLIST)
                .setExpressViewAcceptedSize(width, 0) // height = 0 means adaptive
                .setAdCount(3)
                .build();
        //step5: load Ad and setup a listener
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
                // toast error to user
                TToast.show(NativeExpressListActivity.this, message);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }

                if (ads == null || ads.isEmpty()) {
                    TToast.show(NativeExpressListActivity.this, "on FeedAdLoaded: ad is null!");
                    return;
                }

                for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                    mData.add(null);
                }

                // show and bind events for each Ad
                bindAdListener(ads);
            }
        });
    }

	private void bindAdListener(List<TTNativeExpressAd> ads) {
		int count = mData.size();
        for (TTNativeExpressAd ad : ads) {
			TTNativeExpressAd adTmp = ad;
            int random = (int) (Math.random() * LIST_ITEM_COUNT) + count - LIST_ITEM_COUNT;
            mData.set(random, adTmp);
            myAdapter.notifyDataSetChanged();

            adTmp.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int type) {
                    TToast.show(NativeExpressListActivity.this, "Ad clicked");
                }

                @Override
                public void onAdShow(View view, int type) {
                    TToast.show(NativeExpressListActivity.this, "Ad shown");
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    TToast.show(NativeExpressListActivity.this, msg + " code:" + code);
                }

                @Override
                public void onRenderSuccess(View view, float width, float height) {
                    //返回view的宽高 单位 dp
                    TToast.show(NativeExpressListActivity.this, "Ad render");
                    myAdapter.notifyDataSetChanged();
                }
            });
            ad.render();

        }

    }

    private static class MyAdapter extends BaseAdapter {

        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_GROUP_PIC_AD = 1;
        private static final int ITEM_VIEW_TYPE_SMALL_PIC_AD = 2;
        private static final int ITEM_VIEW_TYPE_LARGE_PIC_AD = 3;
        private static final int ITEM_VIEW_TYPE_VIDEO = 4;
        private static final int ITEM_VIEW_TYPE_VERTICAL_IMG = 5;// vertical image
        private static final int ITEM_VIEW_TYPE_VIDEO_VERTICAL = 6;// vertical video

        private int mVideoCount = 0;


        private List<TTNativeExpressAd> mData;
        private Context mContext;

        private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

        public MyAdapter(Context context, List<TTNativeExpressAd> data) {
			mContext = context;
			mData = data;
        }

        @Override
        public int getCount() {
            return mData.size(); // for test
        }

        @Override
        public TTNativeExpressAd getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // several kind of Ad, diff by ad.getImageMode()
        @Override
        public int getItemViewType(int position) {
            TTNativeExpressAd ad = getItem(position);
            if (ad == null) {
                return ITEM_VIEW_TYPE_NORMAL;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
                return ITEM_VIEW_TYPE_SMALL_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
                return ITEM_VIEW_TYPE_LARGE_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
                return ITEM_VIEW_TYPE_GROUP_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
                return ITEM_VIEW_TYPE_VIDEO;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VERTICAL_IMG) {
                return ITEM_VIEW_TYPE_VERTICAL_IMG;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO_VERTICAL) {
                return ITEM_VIEW_TYPE_VIDEO_VERTICAL;
            } else {
                TToast.show(mContext, "invalid type");
                return ITEM_VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 7;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TTNativeExpressAd ad = getItem(position);
            switch (getItemViewType(position)) {
                case ITEM_VIEW_TYPE_SMALL_PIC_AD:
                case ITEM_VIEW_TYPE_LARGE_PIC_AD:
                case ITEM_VIEW_TYPE_GROUP_PIC_AD:
                case ITEM_VIEW_TYPE_VERTICAL_IMG:
                case ITEM_VIEW_TYPE_VIDEO:
                case ITEM_VIEW_TYPE_VIDEO_VERTICAL:
                    return getVideoView(convertView, parent, ad);
                default:
                    return getNormalView(convertView, parent, position);
            }
        }

        // video Ad
		private View getVideoView(View convertView, ViewGroup parent,
				@NonNull TTNativeExpressAd ad) {
			AdViewHolder adViewHolder;
            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_native_express, parent, false);
                    adViewHolder = new AdViewHolder();
                    adViewHolder.videoView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
                    convertView.setTag(adViewHolder);
                } else {
                    adViewHolder = (AdViewHolder) convertView.getTag();
                }

                // bind data and listeners
                bindData(convertView, adViewHolder, ad);
                if (adViewHolder.videoView != null) {
                    // config for video
                    View video = ad.getExpressAdView();
                    if (video != null) {
                        adViewHolder.videoView.removeAllViews();
                        if (video.getParent() == null) {
                            adViewHolder.videoView.addView(video);
//                            ad.render();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        /**
         * 非广告list
         *
         * @param convertView
         * @param parent
         * @param position
         * @return
         */
        @SuppressLint("SetTextI18n")
        private View getNormalView(View convertView, ViewGroup parent, int position) {
            NormalViewHolder normalViewHolder;
            if (convertView == null) {
                normalViewHolder = new NormalViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_normal, parent, false);
                normalViewHolder.idle = (TextView) convertView.findViewById(R.id.text_idle);
                convertView.setTag(normalViewHolder);
            } else {
                normalViewHolder = (NormalViewHolder) convertView.getTag();
            }
            normalViewHolder.idle.setText("ListView item " + position);
            return convertView;
        }

        /**
         * setup dislike for Ad
         *
         * @param ad
         * @param customStyle
         */
		private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
            if (customStyle) {
                List<FilterWord> words = ad.getFilterWords();
                if (words == null || words.isEmpty()) {
                    return;
                }


				DislikeDialog dislikeDialog = new DislikeDialog(mContext, words);
                dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                    @Override
                    public void onItemClick(FilterWord filterWord) {
                        // dislike Ad
                        TToast.show(mContext, "click " + filterWord.getName());
                        // remove Ad
                        mData.remove(ad);
                        notifyDataSetChanged();
                    }
                });
                ad.setDislikeDialog(dislikeDialog);
                return;
            }
            // dislike callback
            ad.setDislikeCallback((Activity) mContext, new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    TToast.show(mContext, "click " + value);
                    // dislike and removed
                    mData.remove(ad);
                    notifyDataSetChanged();
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

		private void bindData(View convertView, AdViewHolder adViewHolder,
				TTNativeExpressAd ad) {
            // dislike dialog
            bindDislike(ad, false);
            switch (ad.getInteractionType()) {
                case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                    bindDownloadListener(adViewHolder, ad);
                    break;
            }
        }


		private void bindDownloadListener(AdViewHolder adViewHolder,
				TTNativeExpressAd ad) {
            TTAppDownloadListener downloadListener = new TTAppDownloadListener() {
                private boolean mHasShowDownloadActive = false;

                @Override
                public void onIdle() {
                    if (!isValid()) {
                        return;
                    }
                    TToast.show(mContext, "Ad downloading");
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    if (!mHasShowDownloadActive) {
                        mHasShowDownloadActive = true;
                        TToast.show(mContext, appName + " downloading", Toast.LENGTH_LONG);
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    TToast.show(mContext, appName + " download stopped", Toast.LENGTH_LONG);

                }

                @Override
                public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    TToast.show(mContext, appName + " download fail", Toast.LENGTH_LONG);
                }

                @Override
                public void onInstalled(String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    TToast.show(mContext, appName + " download and install success", Toast.LENGTH_LONG);
                }

                @Override
                public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    TToast.show(mContext, appName + " download success", Toast.LENGTH_LONG);

                }

                private boolean isValid() {
                    return mTTAppDownloadListenerMap.get(adViewHolder) == this;
                }
            };
            // setup download listener
            ad.setDownloadListener(downloadListener);
            mTTAppDownloadListenerMap.put(adViewHolder, downloadListener);
        }


        private static class AdViewHolder {
            FrameLayout videoView;
        }

        private static class NormalViewHolder {
            TextView idle;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mData != null) {
            for (TTNativeExpressAd ad : mData) {
                if (ad != null) {
                    ad.destroy();
                }
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
