package com.hust.schoolmatechat.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.CYLog;

public class ImageUtils {
	private static final String TAG = "ImageUtils";
	private static ExecutorService executorService = Executors
			.newFixedThreadPool(5);

	/** �û�Ĭ��ͼ����Դ */
	public static final int localImageResourceIds[] = { R.drawable.picture_0,
			R.drawable.picture_1, R.drawable.picture_2, R.drawable.picture_3,
			R.drawable.picture_4, R.drawable.picture_5, R.drawable.picture_6,
			R.drawable.picture_7, R.drawable.picture_8, R.drawable.picture_9,
			R.drawable.picture_10, R.drawable.picture_11,
			R.drawable.picture_12, R.drawable.picture_13,
			R.drawable.picture_14, R.drawable.picture_15,
			R.drawable.picture_16, R.drawable.picture_17,
			R.drawable.picture_18, R.drawable.picture_19,
			R.drawable.ic_launcher};

	/**
	 * ����url�������ϻ�ȡͼƬ
	 */
	public static Bitmap getImageFromWeb(String url) {

		try {
			HttpGet httpRequest = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
					entity);
			InputStream in = bufferedHttpEntity.getContent();

			int start = url.lastIndexOf("/");
			String iconname = url.substring(start + 1, url.length());

			String baseDir = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			String targetDir = baseDir + File.separator + "chuangyou"
					+ File.separator + "icon";
			File dirFile = new File(targetDir);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			File file = new File(dirFile, iconname);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] result = StreamUtils.getBytes(in);
			// ��ͼƬ��Ϣ ���浽sd��
			fos.write(result);
			fos.flush();
			fos.close();

			return BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			CYLog.e(TAG, e.toString());
			return null;
		}

	}

	/**
	 * ����ͼƬ����
	 * 
	 * @param imageView
	 * @param imageUrl
	 * @param handler
	 */
	public static void setIcon(final ImageView imageView,
			final String imageUrl, final Handler handler) {
		if (imageUrl == null || imageUrl.equals("")) {
			CYLog.i("ImageUtils", "imageUrl == null");
			return;
		}

		int start = imageUrl.lastIndexOf("/");
		String iconname = imageUrl.substring(start + 1, imageUrl.length());
		String baseDir = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File file = new File(baseDir + File.separator + "chuangyou"
				+ File.separator + "icon" + File.separator + iconname);
		if (file.exists() && file.length() > 1024) {
			CYLog.i("ImageUtils", "using cache");
			imageView.setImageURI(Uri.fromFile(file));
			return;
		}

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					final Bitmap bitmap = getImageFromWeb(imageUrl);
					CYLog.i("ImageUtils", "downloading image");

					handler.post(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					CYLog.e("ImageUtils", e.toString());
				}

			}
		});
	}

	/**
	 * ���û�ͼ��imageView����ͼƬ
	 * 
	 * @param imageView
	 * @param imageUrl
	 * @param handler
	 */
	public static void setUserHeadIcon(final ImageView imageView,
			final String imageUrl, final Handler handler) {
		try {
			if (imageUrl == null || imageUrl.equals("")) {
				CYLog.i("ImageUtils", "setUserHeadIcon imageUrl == null");
				return;
			}

			if (!imageUrl.startsWith("http")) {// ʹ�õ���Ĭ��ͼ��
				int num = Integer.parseInt(imageUrl) % 21;
				imageView.setImageResource(localImageResourceIds[num]);
				return;
			} else {// ʹ�õ����û��ϴ���ͼ��
				String temp[] = imageUrl.split("/");
				String baseDir = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				// ���ش洢·��
				String targetDir = baseDir + File.separator + "chuangyou"
						+ File.separator + "icon" + File.separator
						+ temp[temp.length - 2];
				File dirFile = new File(targetDir);
				if (!dirFile.exists()) {
					dirFile.mkdirs();
				}

				// �����Ѿ��洢����ͼ��
				final File file = new File(dirFile, temp[temp.length - 1]);
				if (file.exists()) {
					imageView.setImageURI(Uri.fromFile(file));
					return;
				}

				// ����û���û��ϴ�ͼ���������̴߳���������
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						try {
							final Bitmap bitmap = getImageFromWeb(imageUrl,
									file);
							CYLog.i("ImageUtils", "downloading image");

							handler.post(new Runnable() {
								@Override
								public void run() {
									imageView.setImageBitmap(bitmap);
								}
							});
							
							Message msg = new Message();
							msg.what = 0;
							handler.sendMessage(msg);
						} catch (Exception e) {
							e.printStackTrace();
							CYLog.e("ImageUtils", e.toString());
						}

					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ���û�ͼ��imageView����ͼƬ
	 * 
	 * @param imageView
	 * @param imageUrl
	 * @param handler
	 */
	public static void setWelcomeLogo(final ImageView imageView,
			final String imageUrl, final Handler handler) {
		try {
			if (imageUrl == null || imageUrl.equals("")||!imageUrl.startsWith("http")) {// ʹ�õ���Ĭ��ͼ��
				imageView.setImageResource(R.drawable.loading);
				CYLog.i(TAG, "Ĭ��ͼ��"+imageUrl== null?"null":imageUrl);
				return;
			} else {// ʹ�õ����û��ϴ���ͼ��
				String temp[] = imageUrl.split("/");
				String baseDir = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				// ���ش洢·��
				String targetDir = baseDir + File.separator + "chuangyou"
						+ File.separator + "icon" + File.separator
						+ temp[temp.length - 2];
				File dirFile = new File(targetDir);
				if (!dirFile.exists()) {
					dirFile.mkdirs();
				}

				// �����Ѿ��洢����ͼ��
				final File file = new File(dirFile, temp[temp.length - 1]);
				if (file.exists()) {
					imageView.setImageURI(Uri.fromFile(file));
					return;
				}

				// ����û���û��ϴ�ͼ���������̴߳���������
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						try {
							final Bitmap bitmap = getImageFromWeb(imageUrl,
									file);
							CYLog.i("ImageUtils", "downloading image");

							handler.post(new Runnable() {
								@Override
								public void run() {
									imageView.setImageBitmap(bitmap);
								}
							});
							
							Message msg = new Message();
							msg.what = 0;
							handler.sendMessage(msg);
						} catch (Exception e) {
							e.printStackTrace();
							CYLog.e("ImageUtils", e.toString());
						}

					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����url�������ϻ�ȡͼƬ,�洢������file��
	 */
	private static Bitmap getImageFromWeb(String url, final File file) {
		try {
			HttpGet httpRequest = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
					entity);
			InputStream in = bufferedHttpEntity.getContent();

			FileOutputStream fos = new FileOutputStream(file);
			byte[] result = StreamUtils.getBytes(in);
			// ��ͼƬ��Ϣ ���浽sd��
			fos.write(result);
			fos.flush();
			fos.close();

			return BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			CYLog.e(TAG, e.toString());
			return null;
		}

	}

	// public static void setlocalIcon(final ImageView imageView,
	// final String imageUrl) {
	// try {
	// int num = Integer.parseInt(imageUrl) % 20;
	// imageView.setImageResource(localImageResourceIds[num % 20]);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// �����ļ�
	public static void copyFile(File sourceFile, File targetFile) {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// �½��ļ����������������л���
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// �½��ļ���������������л���
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// ��������
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// ˢ�´˻���������
			outBuff.flush();
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "copyFile " + e.toString());
		} finally {
			// �ر���
			if (inBuff != null)
				try {
					inBuff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (outBuff != null)
				try {
					outBuff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 
	 * @param srcFileName
	 * @param destFileName
	 * @param srcCoding
	 * @param destCoding
	 * @throws IOException
	 */
	public static void copyFile(File srcFileName, File destFileName,
			String srcCoding, String destCoding) throws IOException {// ���ļ�ת��ΪGBK�ļ�
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					srcFileName), srcCoding));
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(destFileName), destCoding));
			char[] cbuf = new char[1024 * 5];
			int len = cbuf.length;
			int off = 0;
			int ret = 0;
			while ((ret = br.read(cbuf, off, len)) > 0) {
				off += ret;
				len -= ret;
			}
			bw.write(cbuf, 0, off);
			bw.flush();
		} finally {
			if (br != null)
				br.close();
			if (bw != null)
				bw.close();
		}
	}
	/**
	 * ����uri��ȡͼƬ ���out of memory����
	 * 
	 * @param uri
	 * @param size
	 *            ����size������Ϊ�ǿ�͸ߵ����ֵ
	 * @return
	 */
	public static Bitmap getThumbnail(Context context ,Uri uri, int size) {
		try {
			InputStream input = context.getContentResolver().openInputStream(uri);
			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither = true;// optional
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();
			if ((onlyBoundsOptions.outWidth == -1)
					|| (onlyBoundsOptions.outHeight == -1))
				return null;
			int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
					: onlyBoundsOptions.outWidth;
			double ratio = (originalSize > size) ? (originalSize / size) : 1.0;
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			bitmapOptions.inDither = true;// optional
			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			input = context.getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(input, null,
					bitmapOptions);
			input.close();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0)
			return 1;
		else
			return k;
	}
}
