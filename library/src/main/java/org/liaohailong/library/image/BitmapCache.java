package org.liaohailong.library.image;

import android.graphics.Bitmap;

public interface BitmapCache {

	boolean containsKey(String url);

	Bitmap put(String url, Bitmap bitmap);

	Bitmap get(String url);

	void clear();

	Bitmap remove(String url);
	
	int getCacheType();

}
