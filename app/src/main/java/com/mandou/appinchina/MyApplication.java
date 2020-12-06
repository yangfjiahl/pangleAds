/*
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 *
 */
package com.mandou.appinchina;

import android.app.Application;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

/**
 * @author calvin
 * @version v1.0
 * @date 2020/12/6 20:47
 * @description
 */
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		TTAdSdk.init(this, new TTAdConfig.Builder().appId("5125367").useTextureView(true) // 默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
				.appName("APP测试媒体").titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)// 落地页主题
				.allowShowNotify(true) // 是否允许sdk展示通知栏提示
				.debug(true) // 测试阶段打开，可以通过日志排查问题，上线时去除该调用
				.directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) // 允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
				.supportMultiProcess(false) // 是否支持多进程，true支持
				.asyncInit(true) // 是否异步初始化sdk,设置为true可以减少SDK初始化耗时
				// .httpStack(new
				// MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
				.build());
	}
}
