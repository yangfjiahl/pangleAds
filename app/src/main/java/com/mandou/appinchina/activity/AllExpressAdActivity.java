package com.mandou.appinchina.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.mandou.appinchina.AdCodes;
import com.mandou.appinchina.R;
import com.mandou.appinchina.feature.BannerExpressActivity;
import com.mandou.appinchina.feature.FullScreenVideoActivity;
import com.mandou.appinchina.feature.NativeExpressListActivity;
import com.mandou.appinchina.feature.RewardVideoActivity;

/**
 * created by wuzejian on 2019-12-19
 */
public class AllExpressAdActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_express_ad);
        bindButton(R.id.express_native_ad, NativeExpressActivity.class);
        bindButton(R.id.express_native_ad_list, NativeExpressListActivity.class);
        bindButton(R.id.express_banner_ad, BannerExpressActivity.class);

        bindButton(R.id.express_splash_ad, SplashActivity.class);

        bindButton(R.id.express_full_screen_video_ad, FullScreenVideoActivity.class);
        bindButton(R.id.express_draw_video_ad, DrawNativeExpressVideoActivity.class);

    }

    private void bindButton(@IdRes int id, Class clz) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllExpressAdActivity.this, clz);
                //全屏模板视频代码位id
                if (v.getId() == R.id.express_full_screen_video_ad) {
					intent.putExtra("horizontal_rit", AdCodes.HORIZONTAL_VIDEO);
					intent.putExtra("vertical_rit", AdCodes.VERTICAL_VIDEO);
                    intent.putExtra("is_express", true);
                }
                //激励模板视频代码位id
                if (v.getId() == R.id.express_rewarded_video_ad) {
					intent.putExtra("horizontal_rit", AdCodes.HORIZONTAL_REWARD);
					intent.putExtra("vertical_rit", AdCodes.VERTICAL_REWARD);
                    intent.putExtra("is_express", true);
                }
                //开屏模板代码位id
                if (v.getId() == R.id.express_splash_ad) {
					intent.putExtra("splash_rit", AdCodes.SPLASH_ID);
                    intent.putExtra("is_express", true);
                }
                startActivity(intent);
            }
        });
    }
}
