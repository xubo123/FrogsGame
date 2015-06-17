package com.hust.schoolmatechat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/**
 * Bitmap������,�����ù���ָ��������ͼƬ,ʹ�ô˹�����,������Ҫ�ֶ�����Bitmap�ڴ� ԭ��:
 * ��һ�����б���ʹ��Bitmap��˳��,ÿ��ʹ��Bitmap�������ƶ�������ͷ ���ڴ治��,���ߴﵽ�ƶ��Ļ���������ʱ��,���ն���β��ͼƬ
 * ��֤��ǰʹ������ͼƬ�õ��ʱ��Ļ���,����ٶ�
 * 
 * @author 
 * 
 */
public final class BitmapUtil {

	private static final Size ZERO_SIZE = new Size(0, 0);
	private static final Options OPTIONS_GET_SIZE = new Options();
	private static final Options OPTIONS_DECODE = new Options();
	private static final byte[] LOCKED = new byte[0];

	private static final LinkedList<String> CACHE_ENTRIES = new LinkedList<String>(); // �˶�����������Bitmap�Ļ���˳��,��֤���ʹ�õ�ͼƬ������
	private static final Queue<QueueEntry> TASK_QUEUE = new LinkedList<QueueEntry>(); // �߳����󴴽�ͼƬ�Ķ���
	private static final Set<String> TASK_QUEUE_INDEX = new HashSet<String>(); // ������������ڴ����ͼƬ��key,��Ч��ֹ�ظ���ӵ����󴴽�����

	private static final Map<String, Bitmap> IMG_CACHE_INDEX = new HashMap<String, Bitmap>(); // ����Bitmap
																								// ͨ��ͼƬ·��,ͼƬ��С

//	private static int CACHE_SIZE = 200; // ����ͼƬ����
	private static int CACHE_SIZE = 0; // ����ͼƬ����

	static {
		OPTIONS_GET_SIZE.inJustDecodeBounds = true;
		// ��ʼ������ͼƬ�߳�,���ȴ�����
		new Thread() {
			{
				setDaemon(true);
			}

			public void run() {
				while (true) {
					synchronized (TASK_QUEUE) {
						if (TASK_QUEUE.isEmpty()) {
							try {
								TASK_QUEUE.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					QueueEntry entry = TASK_QUEUE.poll();
					String key = createKey(entry.path, entry.width,
							entry.height);
					TASK_QUEUE_INDEX.remove(key);
					//createBitmap(entry.path, entry.width, entry.height);
					//�������Ĵ���
					getBitmap(entry.path,entry.width,entry.height);
				}
			}
		}.start();

	}

	/**
	 * ����һ��ͼƬ ����������Ѿ�����,�򷵻ػ����е�ͼ,���򴴽�һ���µĶ���,�����뻺��
	 * ���,�߶�,Ϊ������ԭͼ�����ڴ��,�������Ŀ�,��,��ԭͼ��,����ԭͼ
	 * 
	 * @param path		ͼƬ����·�� (�����Ǳ���·��,����������·��)
	 * @param width		��Ҫ�Ŀ��
	 * @param height	��Ҫ�ĸ߶�
	 * @return
	 */
	public static Bitmap getBitmap(String path, int width, int height) {
		Bitmap bitMap = null;
		try {
			if (CACHE_ENTRIES.size() >= CACHE_SIZE) {
				destoryLast();
			}
			bitMap = useBitmap(path, width, height);
			if (bitMap != null && !bitMap.isRecycled()) {
				return bitMap;
			}
			bitMap = createBitmap(path, width, height);
			String key = createKey(path, width, height);
			synchronized (LOCKED) {
				IMG_CACHE_INDEX.put(key, bitMap);
				CACHE_ENTRIES.addFirst(key);
			}
		} catch (OutOfMemoryError err) {
			destoryLast();
			System.out.println(CACHE_SIZE);
			//return createBitmap(path, width, height);
			//�������Ĵ���
			return getBitmap(path, width, height);
		}
		return bitMap;
	}

	/**
	 * ���û���ͼƬ���� ������븺��,������쳣
	 * 
	 * @param size
	 */
	public static void setCacheSize(int size) {
		if (size <= 0) {
			throw new RuntimeException("size :" + size);
		}
		while (size < CACHE_ENTRIES.size()) {
			destoryLast();
		}
		CACHE_SIZE = size;
	}

	/**
	 * ����һ��ͼƬ��������ͼƬ��������
	 * 
	 * @param path
	 *            ͼƬ·��(����)
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 */
	public static void addTask(String path, int width, int height) {
		QueueEntry entry = new QueueEntry();
		entry.path = path;
		entry.width = width;
		entry.height = height;
		synchronized (TASK_QUEUE) {
			String key = createKey(path, width, height);
			if (!TASK_QUEUE_INDEX.contains(key)
					&& !IMG_CACHE_INDEX.containsKey(key)) {
				TASK_QUEUE.add(entry);
				TASK_QUEUE_INDEX.add(key);
				TASK_QUEUE.notify();
			}
		}
	}
	
	/**
	 * ͨ��ͼƬ·������ͼƬʵ�ʴ�С
	 * @param path		ͼƬ����·��
	 * @return
	 */
	public static Size getBitMapSize(String path) {
		File file = new File(path);
		if (file.exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				BitmapFactory.decodeStream(in, null, OPTIONS_GET_SIZE);
				return new Size(OPTIONS_GET_SIZE.outWidth,
						OPTIONS_GET_SIZE.outHeight);
			} catch (FileNotFoundException e) {
				return ZERO_SIZE;
			} finally {
				closeInputStream(in);
			}
		}
		return ZERO_SIZE;
	}

	// ------------------------------------------------------------------ private Methods
	// ��ͼƬ�������ͷ
	private static Bitmap useBitmap(String path, int width, int height) {
		Bitmap bitMap = null;
		String key = createKey(path, width, height);
		synchronized (LOCKED) {
			bitMap = IMG_CACHE_INDEX.get(key);
			if (null != bitMap) {
				if (CACHE_ENTRIES.remove(key)) {
					CACHE_ENTRIES.addFirst(key);
				}
			}
		}
		return bitMap;
	}

	// �������һ��ͼƬ
	private static void destoryLast() {
		synchronized (LOCKED) {
			String key = CACHE_ENTRIES.removeLast();
			if (key.length() > 0) {
				Bitmap bitMap = IMG_CACHE_INDEX.remove(key);
				if (bitMap != null && !bitMap.isRecycled()) {
					bitMap.recycle();
					bitMap = null;
				}
			}
		}
	}

	// ������
	private static String createKey(String path, int width, int height) {
		if (null == path || path.length() == 0) {
			return "";
		}
		return path + "_" + width + "_" + height;
	}

	// ͨ��ͼƬ·��,��ȸ߶ȴ���һ��Bitmap����
	private static Bitmap createBitmap(String path, int width, int height) {
		File file = new File(path);
		if (file.exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				Size size = getBitMapSize(path);
				if (size.equals(ZERO_SIZE)) {
					return null;
				}
				int scale = 1;
				int a = size.getWidth() / width;
				int b = size.getHeight() / height;
				scale = Math.max(a, b);
				synchronized (OPTIONS_DECODE) {
					OPTIONS_DECODE.inSampleSize = scale;
					Bitmap bitMap = BitmapFactory.decodeStream(in, null,
							OPTIONS_DECODE);
					return bitMap;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				closeInputStream(in);
			}
		}
		return null;
	}
	
	// �ر�������
	private static void closeInputStream(InputStream in) {
		if (null != in) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ͼƬ��С
	static class Size {
		private int width, height;

		Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}

	// ���л����������
	static class QueueEntry {
		public String path;
		public int width;
		public int height;
	}
}
