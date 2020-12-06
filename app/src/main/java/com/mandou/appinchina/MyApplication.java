/*
 * Copyright (C) 2019 ~ 2020 itech.com. All Rights Reserved.
 *
 */
package com.mandou.appinchina;

import android.app.Application;

import com.mandou.appinchina.config.TTAdManagerHolder;

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

		TTAdManagerHolder.init(this);
	}
}
