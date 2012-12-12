package com.playup.android.util;

import java.io.BufferedInputStream;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;

public class CacheUtil {

	private static final int SOFT_CACHE_CAPACITY = 400;
	private static final int CACHE_CAPACITY = 200;
	private static final int BUFFER_SIZE = 1024 * 5; // 5kb per image
	private static File cacheFolder;
	private static final HashMap<String, String> requestImageUrls = new HashMap<String, String>();

	public static HashMap<String, List<View>> views = new HashMap<String, List<View>>();

	public static HashMap<String, List<ImageView>> imageViews = new HashMap<String, List<ImageView>>();

	public static HashMap<String, Set<BaseAdapter>> mBaseAdapters = new HashMap<String, Set<BaseAdapter>>();

	// soft cache, with a fixed maximum capacity and a life duration
	private static final HashMap<String, SoftReference<Bitmap>> softCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
			SOFT_CACHE_CAPACITY / 2, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(
				java.util.Map.Entry<String, SoftReference<Bitmap>> eldest) {

			if (size() > SOFT_CACHE_CAPACITY) {

				// Entries push-out of soft reference cache are transferred to
				// cache directory
				SoftReference<Bitmap> bitmapReference = eldest.getValue();

				Bitmap bitmap = bitmapReference.get();
				if (bitmap != null) {

					putInCache(eldest.getKey(), bitmap);
					bitmap.recycle();
					bitmap = null;
				}
				bitmapReference = null;

				return true;

			}
			return false;
		}
	};

	public CacheUtil() {
		cacheFolder = new File(Constants.CACHE_DIR_PATH);

		if (!cacheFolder.exists()) {
			cacheFolder.mkdirs();
		}
	}

	/**
	 * Adds this bitmap to the cache.
	 * 
	 * @param bitmap
	 *            The newly downloaded bitmap.
	 */
	public void addBitmapToSoftCache(final String id, final Bitmap bitmap) {

		try {

			if (bitmap != null) {

				if (id == null) {
					return;
				}

				softCache.put(id, new SoftReference<Bitmap>(bitmap));
				String cachePath = null;
				if (Constants.CACHE_DIR_PATH.endsWith("/")) {
					cachePath = Constants.CACHE_DIR_PATH + id;
				} else {
					cachePath = Constants.CACHE_DIR_PATH + "/" + id;
				}

				if (!(new File(cachePath)).exists()) {
					cachePath = null;
					putInCache(id, bitmap);
				}

			}

		} catch (Exception e) {

			Logs.show(e);

		} catch (Error e) {

			Logs.show(e);

		}

	}

	/**
	 * Removes this bitmap from the cache and File Storage.
	 * 
	 */
	public void removeBitmapFromLocalStorage(final String id) {

		try {

			File f = new File(Constants.CACHE_DIR_PATH + id);
			if (f.exists()) {
				f.delete();
			}
			f = null;
		} catch (Exception e) {
			// TODO: handle exception

		}

	}

	/**
	 * Removes this bitmap from the Soft cache .
	 * 
	 */
	public void removeBitmapFromSoftCache(final String id) {

		try {
			softCache.remove(id);

		} catch (Exception e) {
			// TODO: handle exception

		}

	}

	/**
	 * Interchange this bitmap from the Local Storage .
	 * 
	 */
	public void interchangeBitmapFromLocalStorage(final String firstFileName,
			final String secondFileName) {

		try {
			File firstfile = new File(Constants.CACHE_DIR_PATH + firstFileName);
			File secondfile = new File(Constants.CACHE_DIR_PATH
					+ secondFileName);
			File thirdfile = new File(Constants.CACHE_DIR_PATH + "tempFile");

			if (secondfile.renameTo(thirdfile))

				if (firstfile.renameTo(secondfile))

					if (thirdfile.renameTo(firstfile))

						thirdfile.delete();

			firstfile = null;
			secondfile = null;
			thirdfile = null;

		} catch (Exception e) {

		}

	}

	/**
	 * 
	 * Copy file content
	 */

	public void copyfile(String srFile, String dtFile) {
		try {

			File f1 = null;
			File f2 = null;
			if (Constants.CACHE_DIR_PATH.endsWith("/")) {
				f1 = new File(Constants.CACHE_DIR_PATH + srFile);
				f2 = new File(Constants.CACHE_DIR_PATH + dtFile);
			} else {
				f1 = new File(Constants.CACHE_DIR_PATH + "/" + srFile);
				f2 = new File(Constants.CACHE_DIR_PATH + "/" + dtFile);
			}

			if (f1.exists() && f2.exists()) {

				InputStream in = new FileInputStream(f1);

				Bitmap bm = BitmapFactory.decodeStream(in);
				softCache.put(dtFile, new SoftReference<Bitmap>(bm));
				softCache.put(srFile, new SoftReference<Bitmap>(bm));

				in = new FileInputStream(f1);

				OutputStream out = new FileOutputStream(f2);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

				in = null;
				out = null;

			}
			f1 = null;
			f2 = null;
		} catch (FileNotFoundException ex) {
		} catch (IOException e) {

		}
	}

	/**
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	public void getBitmapFromCache(String key, String url, ImageView imgView,
			ImageDownloader imageDownloader) {

		try {
			if (url == null) {
				return;
			}

			// First try the soft reference cache
			// synchronized (softCache) {

			SoftReference<Bitmap> bitmapReference = softCache.get(key);

			if (bitmapReference != null) {

				Bitmap bitmap = bitmapReference.get();

				if (bitmap != null) {

					if (imgView != null) {

						imgView.setVisibility(View.VISIBLE);
						imgView.setImageBitmap(bitmap);

						imgView = null;
					}
					bitmapReference = null;
					return;
				} else {

					// Soft reference has been Garbage Collected
					softCache.remove(key);
				}
			}

			if (requestImageUrls != null && requestImageUrls.containsKey(url)) {

				if (imageDownloader.mBaseAdapter != null) {

					if (mBaseAdapters != null && mBaseAdapters.containsKey(url)) {

						Set<BaseAdapter> baseAdpaters = mBaseAdapters.get(url);
						if (imageDownloader.mBaseAdapter != null)
							baseAdpaters.add(imageDownloader.mBaseAdapter);

						mBaseAdapters.put(url, baseAdpaters);

					} else {

						Set<BaseAdapter> baseAdpaters = new HashSet<BaseAdapter>();
						if (imageDownloader.mBaseAdapter != null)
							baseAdpaters.add(imageDownloader.mBaseAdapter);

						mBaseAdapters.put(url, baseAdpaters);

					}

				} else {

					if (imageViews.containsKey(url)) {

						List<ImageView> img = imageViews.get(url);

						for (ImageView iView : img) {

							if (iView != null
									&& (iView.getVisibility() == View.INVISIBLE || iView
											.getVisibility() == View.GONE)) {

								img.remove(iView);
							}

						}

						if (imgView != null)
							img.add(imgView);

						imageViews.put(url, img);

					} else {

						List<ImageView> img = new ArrayList<ImageView>();

						if (imgView != null) {
							img.add(imgView);
							imageViews.put(url, img);
						}

					}
				}
			} else {

				requestImageUrls.put(url, url);

				if (imageDownloader.mBaseAdapter != null) {

					if (mBaseAdapters != null && mBaseAdapters.containsKey(url)) {

						Set<BaseAdapter> baseAdpaters = mBaseAdapters.get(url);

						if (imageDownloader.mBaseAdapter != null)
							baseAdpaters.add(imageDownloader.mBaseAdapter);

						mBaseAdapters.put(url, baseAdpaters);

					} else {

						Set<BaseAdapter> baseAdpaters = new HashSet<BaseAdapter>();
						if (imageDownloader.mBaseAdapter != null)
							baseAdpaters.add(imageDownloader.mBaseAdapter);

						mBaseAdapters.put(url, baseAdpaters);

					}

				} else {

					if (imageViews.containsKey(url)) {

						List<ImageView> img = imageViews.get(url);

						for (ImageView iView : img) {

							if (iView != null
									&& (iView.getVisibility() == View.INVISIBLE || iView
											.getVisibility() == View.GONE))
								img.remove(iView);

						}
						if (imgView != null)
							img.add(imgView);

						imageViews.put(url, img);

					} else {

						List<ImageView> img = new ArrayList<ImageView>();

						if (imgView != null) {
							img.add(imgView);
							imageViews.put(url, img);
						}

					}
				}
				try {
					PlayUpActivity.executorPool.execute(new DownloadThread(
							imgView, key, url, imageDownloader, false));
				} catch (RejectedExecutionException e) {
					Logs.show(e);
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	public void getBitmapFromCache(String key, String url, ImageView imgView,
			ImageDownloader imageDownloader, boolean notify) {

		// First try the soft reference cache
		SoftReference<Bitmap> bitmapReference = null;
		if (softCache != null && key != null)
			bitmapReference = softCache.get(key);

		if (bitmapReference != null) {
			Bitmap bitmap = bitmapReference.get();

			if (bitmap != null) {

				if (imgView != null) {
					if (imageDownloader != null) {
						imageDownloader.nullViewDrawablesRecursive(imgView);
					}

					imgView.setImageBitmap(bitmap);
					imgView = null;
				}

				bitmapReference = null;
				return;
			} else {

				// Soft reference has been Garbage Collected
				softCache.remove(key);
			}
		}

		if (requestImageUrls != null && requestImageUrls.containsKey(url)) {

			if (imageDownloader.mBaseAdapter != null) {

				if (mBaseAdapters != null && mBaseAdapters.containsKey(url)) {

					Set<BaseAdapter> baseAdpaters = mBaseAdapters.get(url);
					if (imageDownloader.mBaseAdapter != null)
						baseAdpaters.add(imageDownloader.mBaseAdapter);

					mBaseAdapters.put(url, baseAdpaters);

				} else {

					Set<BaseAdapter> baseAdpaters = new HashSet<BaseAdapter>();
					if (imageDownloader.mBaseAdapter != null)
						baseAdpaters.add(imageDownloader.mBaseAdapter);

					mBaseAdapters.put(url, baseAdpaters);

				}

			}

		} else {

			requestImageUrls.remove(url);
			requestImageUrls.put(url, url);

			if (imageDownloader.mBaseAdapter != null) {

				if (mBaseAdapters != null && mBaseAdapters.containsKey(url)) {

					Set<BaseAdapter> baseAdpaters = mBaseAdapters.get(url);
					if (imageDownloader.mBaseAdapter != null)
						baseAdpaters.add(imageDownloader.mBaseAdapter);

					mBaseAdapters.put(url, baseAdpaters);

				} else {

					Set<BaseAdapter> baseAdpaters = new HashSet<BaseAdapter>();
					if (imageDownloader.mBaseAdapter != null)
						baseAdpaters.add(imageDownloader.mBaseAdapter);

					mBaseAdapters.put(url, baseAdpaters);

				}

			}

			try {
				PlayUpActivity.executorPool.execute(new DownloadThread(imgView,
						key, url, imageDownloader, false, notify));
			} catch (RejectedExecutionException e) {
				Logs.show(e);
			}

		}

	}

	/**
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	public void getBitmapFromCacheForSports(String key, String url, View li,
			ImageDownloaderSports imageDownloader, String bgColor) {
		// First try the soft reference cache

		SoftReference<Bitmap> bitmapReference = null;
		if (softCache != null && key != null)
			bitmapReference = softCache.get(key);

		if (bitmapReference != null) {
			Bitmap bitmap = bitmapReference.get();

			if (bitmap != null) {

				// Bitmap found in soft cache
				// Move element to first position, so that it is removed
				// last
				softCache.remove(key);
				softCache.put(key, bitmapReference);

				if (li != null) {

					if (li instanceof LinearLayout) {

						deSelectSports(li);

						ImageView sportsImage = (ImageView) li
								.findViewById(R.id.mysportsItemImage);
						sportsImage.setTag("1");
						sportsImage.setImageBitmap(bitmap);
						imageDownloader = null;
						sportsImage = null;
						li = null;

					} else if (li instanceof RelativeLayout) {

						if (li.getVisibility() == View.VISIBLE) {

							
							((ImageView) li
									.findViewById(R.id.ImageWithSummaryImage))
									.setImageBitmap(bitmap);

							if (bgColor != null) {

								

								Drawable d = new BitmapDrawable(bitmap);

								if (d != null) {

									if (li.findViewById(
											R.id.ImageWithSummaryImage)
											.getTag() != null
											&& li.findViewById(
													R.id.ImageWithSummaryImage)
													.getTag()
													.toString()
													.equalsIgnoreCase(
															"show_dark")) {
										d.setColorFilter(
												Color.parseColor("#" + bgColor),
												Mode.DST_OVER);
									} else {
										// d.setAlpha(20);
										// d.setColorFilter(Color.parseColor("#77"+
										// bgColor),Mode.DST_OVER);
									}

									((ImageView) li
											.findViewById(R.id.ImageWithSummaryImage))
											.setImageDrawable(d);

								}

							}

						}
					}

				}

				imageDownloader = null;
				return;
			} else {

				// Soft reference has been Garbage Collected
				softCache.remove(key);

			}
		}

		if (requestImageUrls != null && requestImageUrls.containsKey(url)) {

			if (views.containsKey(url)) {

				List<View> view = views.get(url);

				for (View v : view) {

					if (v != null
							&& (v.getVisibility() == View.INVISIBLE || v
									.getVisibility() == View.GONE)) {

						imageDownloader.nullViewDrawablesRecursive(v);
						view.remove(v);
					}

				}

				if (li != null)
					view.add(li);

				views.put(url, view);
			} else {

				List<View> view = new ArrayList<View>();
				if (li != null) {
					view.add(li);
					views.put(url, view);
				}
			}

		} else {
			requestImageUrls.put(url, url);

			if (views.containsKey(url)) {

				List<View> view = views.get(url);

				for (View v : view) {

					if (v != null
							&& (v.getVisibility() == View.INVISIBLE || v
									.getVisibility() == View.GONE)) {
						imageDownloader.nullViewDrawablesRecursive(v);
						view.remove(v);
					}

				}
				if (li != null)
					view.add(li);

				views.put(url, view);
			} else {

				List<View> view = new ArrayList<View>();
				if (li != null) {
					view.add(li);
					views.put(url, view);
				}

			}
			PlayUpActivity.executorPool.execute(new DownloadThread(li, key,
					url, imageDownloader, true));

		}
	}

	/**
* 
* 
*/

	class DownloadThread extends Thread {

		ImageView imgView;

		String key;
		String url;
		ImageDownloader imageDownloader;
		ImageDownloaderSports imageDownloaderSports;
		View linearLayout;
		boolean isBitmapCacheTask = false;
		boolean isRoundCornerReq = false;
		boolean notify = false;

		public DownloadThread(View linearLayout, String key, String url,
				ImageDownloaderSports imageDownloaderSports,
				boolean isBitmapCacheTask) {

			this.linearLayout = linearLayout;
			this.key = key;
			this.url = url;
			this.imageDownloaderSports = imageDownloaderSports;
			this.isBitmapCacheTask = isBitmapCacheTask;
			this.isRoundCornerReq = false;

		}

		public DownloadThread(ImageView imgView, String key, String url,
				ImageDownloader imageDownloader, boolean isBitmapCacheTask) {

			this.imgView = imgView;

			this.key = key;
			this.url = url;
			this.imageDownloader = imageDownloader;
			this.isBitmapCacheTask = isBitmapCacheTask;
			this.isRoundCornerReq = imageDownloader.isRoundCornerReq;

		}

		public DownloadThread(ImageView imgView, String key, String url,
				ImageDownloader imageDownloader, boolean isBitmapCacheTask,
				boolean notify) {

			this.imgView = imgView;
			this.key = key;
			this.url = url;
			this.imageDownloader = imageDownloader;
			this.isBitmapCacheTask = isBitmapCacheTask;
			this.isRoundCornerReq = imageDownloader.isRoundCornerReq;
			this.notify = notify;

		}

		public void run() {
			// TODO Auto-generated method stub
			final Bitmap mBitmap = getImage();

			PlayUpActivity.handler.post(new Runnable() {
				public void run() {

					try {

						if (!isBitmapCacheTask) {

							if (mBaseAdapters != null
									&& mBaseAdapters.containsKey(url)
									&& mBaseAdapters.get(url) != null
									&& mBaseAdapters.get(url).size() > 0) {
								if (mBitmap != null) {
									Set<BaseAdapter> baseAdapters = mBaseAdapters
											.get(url);

									for (BaseAdapter ba : baseAdapters) {

										ba.notifyDataSetChanged();
									}

									if (mBaseAdapters != null) {
										mBaseAdapters.get(url).clear();
										mBaseAdapters.remove(url);
									}

								}
							}

							if (notify) {
								Message msg = new Message();
								msg.obj = "updateImage";
								PlayupLiveApplication
										.callUpdateOnFragmentsNotTopBar(msg);
							}
							if (imageViews.get(url) != null
									&& imageViews.containsKey(url)
									&& imageViews.get(url) != null
									&& imageViews.get(url).size() > 0) {

								try {

									imgView = null;

									List<ImageView> iView = imageViews.get(url);

									for (ImageView img : iView) {
										imgView = img;

										if (imgView.getVisibility() == View.VISIBLE) {
											imgView.setImageBitmap(mBitmap);
											// if (bgColor != null &&
											// bgColor.trim().length() > 0) {
											//
											// Drawable d =
											// imgView.getDrawable();
											// if (d != null) {
											//
											// d.setAlpha(Integer.parseInt("88",
											// 16));
											// d.setColorFilter(Color.parseColor("#"+
											// bgColor),Mode.DST_OVER);
											// imgView.setBackgroundDrawable(d);
											// }
											// }

										}
									}

									imgView = null;
									if (linearLayout != null) {

										if (imageDownloader != null) {
											imageDownloader
													.nullViewDrawablesRecursive(linearLayout);
											linearLayout = null;
										}

										if (imageDownloaderSports != null) {
											imageDownloaderSports
													.nullViewDrawablesRecursive(linearLayout);
											imageDownloaderSports = null;
											linearLayout = null;
										}
									}

									linearLayout = null;

								} catch (Exception e) {
									Logs.show(e);
								}

								if (mBitmap != null) {

									if (imageViews != null) {
										imageViews.get(url).clear();
										imageViews.remove(url);
									}
								}
							}

						}
						if (views != null && views.get(url) != null
								&& views.get(url).size() > 0) {

							List<View> layoutViews = views.get(url);

							for (View lin : layoutViews) {

								if (lin.getVisibility() == View.VISIBLE) {

									linearLayout = lin;

									if (linearLayout instanceof LinearLayout) {

										deSelectSports(linearLayout);

										ImageView sportsImage = (ImageView) linearLayout
												.findViewById(R.id.mysportsItemImage);

										sportsImage.setTag("1");
										sportsImage.setImageBitmap(mBitmap);

										sportsImage = null;
										linearLayout = null;

									} else if (linearLayout instanceof RelativeLayout) {

										if (linearLayout.getVisibility() == View.VISIBLE) {

											if (mBitmap != null) {

												ImageView imageWithSummaryImage = (ImageView) linearLayout
														.findViewById(R.id.ImageWithSummaryImage);

												imageWithSummaryImage
														.setVisibility(View.VISIBLE);
												imageWithSummaryImage
														.setImageBitmap(mBitmap);

												if (linearLayout
														.getTag(R.id.active_users_no) != null
														&& linearLayout
																.getTag(R.id.active_users_no)
																.toString()
																.trim()
																.length() > 0) {

													String bgColor = linearLayout
															.getTag(R.id.active_users_no)
															.toString();

												

													Drawable d = new BitmapDrawable(
															mBitmap);

													if (d != null) {

														if (imageWithSummaryImage
																.getTag() != null
																&& imageWithSummaryImage
																		.getTag()
																		.toString()
																		.equalsIgnoreCase(
																				"show_dark")) {

														
															d.setColorFilter(
																	Color.parseColor("#"
																			+ bgColor),
																	Mode.DST_OVER);
														} else {
															// d.setAlpha(20);
															//
															// Log.e("123","inside after downloades bgcolor != null "
															// + ((ImageView)
															// linearLayout.findViewById(R.id.ImageWithSummaryImage)).getVisibility());
															//
															// d.setColorFilter(Color.parseColor("#77"+
															// bgColor),Mode.DST_OVER);

														}

														imageWithSummaryImage
																.setImageDrawable(d);

													}

												}

												imageWithSummaryImage = null;

											}
										}

										linearLayout = null;

									}

								}

							}

							if (mBitmap != null) {
								if (views != null) {
									views.get(url).clear();
									views.remove(url);

								}
							}

						}

						if (requestImageUrls != null
								&& requestImageUrls.containsKey(url)) {
							requestImageUrls.remove(url);
						}

						key = null;
						url = null;

					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					} finally {
						// mBitmap.recycle();

					}

				}

			});

		}

		private Bitmap getImage() {

			Bitmap mBitmap = getFromCache(key);

			if (mBitmap != null) {

				
				addBitmapToSoftCache(key, mBitmap);
				return mBitmap;
			}
			if (key != null && key.length() > 1) {

				return downloadBitmap(key, url);
			} else {
				return null;
			}
		}

		/**
		 * change the logic here. use connection manager here.
		 */
		private Bitmap downloadBitmap(final String id, final String url) {

			try {

				

				URL aURL = new URL(url);
				URLConnection conn = aURL.openConnection();
				conn.connect();
				BufferedInputStream bis = new BufferedInputStream(
						conn.getInputStream(), 8 * 1024);
				Bitmap bm = BitmapFactory.decodeStream(bis);

				if (bm == null) {

					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpGet httppost = new HttpGet(url);
					HttpResponse response;
					response = httpclient.execute(httppost);
					HttpEntity ht = response.getEntity();
					BufferedHttpEntity buf = new BufferedHttpEntity(ht);
					bm = BitmapFactory.decodeStream(buf.getContent());

					try {
						response = null;
						ht = null;
						buf.consumeContent();

					} catch (Exception e) {
						// TODO: handle exception
						Logs.show(e);
					}

				}

				try {
					if (imgView != null
							&& imgView.getTag() != null
							&& imgView.getTag().toString()
									.equalsIgnoreCase("fullImage")) {
						if (bm != null) {
							int imgViewWidth = imgView.getWidth();
							int bitmapHeight = bm.getHeight();
							int bitmapWidth = bm.getWidth();

							if (imgViewWidth > 0 && bitmapWidth > 0
									&& bitmapWidth < imgViewWidth
									&& bitmapHeight > 0) {
								bitmapHeight = (int) (((float) imgViewWidth / bitmapWidth) * bitmapHeight);
								bm = Bitmap.createScaledBitmap(bm,
										imgViewWidth, bitmapHeight, false);
							}

						}
					}

				} catch (Exception e) {
					Logs.show(e);
				}

				if (isRoundCornerReq) {
					ImageHelper mImageHelper = new ImageHelper();
					bm = mImageHelper.getRoundedCornerImage(bm);
				}
				if (bm != null) {
					addBitmapToSoftCache(id, bm);
				}

				bis.close();
				bis = null;
				conn = null;
				aURL = null;
				return bm;
			} catch (IOException e) {

				

				Logs.show(e);

			} catch (Exception e) {

				

				Logs.show(e);

			} catch (OutOfMemoryError e) {
				Logs.show(e);
			}

			return null;

		}
	}

	/**
	 * 
	 * For Updating the User Interface of All Sports Screen.
	 * 
	 * @param v
	 */
	public void deSelectSports(View v) {
		if (v.getTag(-1) != null
				&& v.getTag(-1).toString().equalsIgnoreCase("live")) {
			v.findViewById(R.id.liveImage).setVisibility(View.VISIBLE);
		} else {
			v.findViewById(R.id.liveImage).setVisibility(View.GONE);
		}
		v.findViewById(R.id.topLayout).setBackgroundResource(
				R.drawable.sport_base);
		((TextView) v.findViewById(R.id.mysportsItemName)).setTextColor(Color
				.parseColor("#404040"));
		v.findViewById(R.id.mysportsItemProgressbar).setVisibility(View.GONE);
	}

	public void clearSofCache() {
		// clearing form soft reference

		if (views != null) {
			views.clear();
		}

		if (imageViews != null) {
			imageViews.clear();
		}
	}

	/**
	 * Clears the image cache used
	 */
	public void clearCache() {

		// clearing form soft reference
		if (softCache != null) {
			softCache.clear();
		}

		// clearing from cache directory
		deleteCacheFiles();

	}

	/**
	 * deletes the files from the cache directory.
	 */
	private void deleteCacheFiles() {

		// get the directory file
		File cache = new File(Constants.CACHE_DIR_PATH);

		// check if we got the correct instance of that directory file.
		if (!cache.exists() || !cache.isDirectory())
			return;

		// gets the list of files in the directory
		File[] files = cache.listFiles();
		// deleting

		deleteFiles(cache);
		files = null;
		cache = null;

	}

	private void deleteFiles(File file) {

		if (file.isDirectory()) {

			File[] files = file.listFiles();

			for (File e : files) {
				deleteFiles(e);
				e = null;
			}
			files = null;
		} else {
			file.delete();
			file = null;
		}
	}

	/**
	 * Saves the image in the cache directory.
	 * 
	 * @param key
	 *            -- filename with which image should be stored.
	 * @param bitmap
	 *            -- bitmap image that needs to be stored.
	 */
	private static void putInCache(final String key, final Bitmap bitmap) {

		BufferedOutputStream output = null;

		try {

			// trying to get the desired file form cache directory.
			File cacheFile = null;
			if (Constants.CACHE_DIR_PATH.endsWith("/")) {
				cacheFile = new File(Constants.CACHE_DIR_PATH + key);
			} else {
				cacheFile = new File(Constants.CACHE_DIR_PATH + "/" + key);
			}
			// no need to save the file if already exists. so return
			if (cacheFile.exists())
				return;

			// copies the bitmap data into the file in cache directory.
			output = new BufferedOutputStream(new FileOutputStream(cacheFile),
					BUFFER_SIZE);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);

			cacheFile = null;

			File weededOut = getWeededOut();

			if (weededOut != null) {

				weededOut.delete();
			}
			weededOut = null;

		} catch (FileNotFoundException e) {

		} finally {
			if (output == null) {
				return;
			}
			try {
				output.close();
				output = null;
			} catch (IOException e) {

			}
		}

	}

	/**
	 * if cache directory has reached a certain amount of memory, delete files
	 * which are old
	 * 
	 * Need to update the logic for better deletion of cached files.
	 */
	private static File getWeededOut() {
		File[] files = cacheFolder.listFiles();
		if (files != null) {
			int count = files.length;
			if (count > CACHE_CAPACITY) {

				File ret = files[0];

				for (File bean : files) {

					if (ret.lastModified() > bean.lastModified())
						ret = bean;
				}
				files = null;
				return ret;
			} else {
				files = null;
				return null;
			}

		}
		files = null;
		return null;
	}

	/**
	 * gets the bitmap from cache.
	 */
	public Bitmap getFromCache(final String key) {

		try {
			if (Constants.CACHE_DIR_PATH.endsWith("/"))
				return BitmapFactory.decodeFile(Constants.CACHE_DIR_PATH + key);
			else
				return BitmapFactory.decodeFile(Constants.CACHE_DIR_PATH + "/"
						+ key);
		} catch (Error e) {

			Logs.show(e);

		}
		return null;
	}

}