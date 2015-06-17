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
 * Bitmap工具类,缓存用过的指定数量的图片,使用此工具类,不再需要手动管理Bitmap内存 原理:
 * 用一个队列保存使用Bitmap的顺序,每次使用Bitmap将对象移动到队列头 当内存不够,或者达到制定的缓存数量的时候,回收队列尾部图片
 * 保证当前使用最多的图片得到最长时间的缓存,提高速度
 * 
 * @author 
 * 
 */
public final class BitmapUtil {

	private static final Size ZERO_SIZE = new Size(0, 0);
	private static final Options OPTIONS_GET_SIZE = new Options();
	private static final Options OPTIONS_DECODE = new Options();
	private static final byte[] LOCKED = new byte[0];

	private static final LinkedList<String> CACHE_ENTRIES = new LinkedList<String>(); // 此对象用来保持Bitmap的回收顺序,保证最后使用的图片被回收
	private static final Queue<QueueEntry> TASK_QUEUE = new LinkedList<QueueEntry>(); // 线程请求创建图片的队列
	private static final Set<String> TASK_QUEUE_INDEX = new HashSet<String>(); // 保存队列中正在处理的图片的key,有效防止重复添加到请求创建队列

	private static final Map<String, Bitmap> IMG_CACHE_INDEX = new HashMap<String, Bitmap>(); // 缓存Bitmap
																								// 通过图片路径,图片大小

//	private static int CACHE_SIZE = 200; // 缓存图片数量
	private static int CACHE_SIZE = 0; // 缓存图片数量

	static {
		OPTIONS_GET_SIZE.inJustDecodeBounds = true;
		// 初始化创建图片线程,并等待处理
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
					//修正过的代码
					getBitmap(entry.path,entry.width,entry.height);
				}
			}
		}.start();

	}

	/**
	 * 创建一张图片 如果缓存中已经存在,则返回缓存中的图,否则创建一个新的对象,并加入缓存
	 * 宽度,高度,为了缩放原图减少内存的,如果输入的宽,高,比原图大,返回原图
	 * 
	 * @param path		图片物理路径 (必须是本地路径,不能是网络路径)
	 * @param width		需要的宽度
	 * @param height	需要的高度
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
			//修正过的代码
			return getBitmap(path, width, height);
		}
		return bitMap;
	}

	/**
	 * 设置缓存图片数量 如果输入负数,会产生异常
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
	 * 加入一个图片处理请求到图片创建队列
	 * 
	 * @param path
	 *            图片路径(本地)
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
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
	 * 通过图片路径返回图片实际大小
	 * @param path		图片物理路径
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
	// 将图片加入队列头
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

	// 回收最后一张图片
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

	// 创建键
	private static String createKey(String path, int width, int height) {
		if (null == path || path.length() == 0) {
			return "";
		}
		return path + "_" + width + "_" + height;
	}

	// 通过图片路径,宽度高度创建一个Bitmap对象
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
	
	// 关闭输入流
	private static void closeInputStream(InputStream in) {
		if (null != in) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 图片大小
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

	// 队列缓存参数对象
	static class QueueEntry {
		public String path;
		public int width;
		public int height;
	}
}
