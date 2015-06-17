package com.hust.schoolmatechat.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

/**
 * @Title: CYLog.java
 * @Package com.hust.schoolmatechat.engine
 * @author luoguangzhen
 * @date 2014-10-28 ����03:36:18
 * @version V1.0
 */
public final class CYLog {

	private static final String LOG_NAME = "log.txt";

	public static final int LOG_NO = 0;
	public static final int LOG_LOGCAT = 1;
	public static final int LOG_FILE = 2;
	public static final int LOG_BOTH = 3;

	private static int mLogLevel = LOG_BOTH;

	private static SimpleDateFormat mSdf = null;

	private CYLog() {
	}

	public static void i(String tag, String msg) {
		// if (!APPConstant.DEBUG_MODE) {
		// return;
		// }

		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				Log.i(tag, msg);
			}
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				write("Info", tag, msg);
			}
		}
	}

	public static void d(String tag, String msg) {
		// if (!APPConstant.DEBUG_MODE) {
		// return;
		// }

		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				Log.d(tag, msg);
			}
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				write("Debug", tag, msg);
			}
		}
	}

	public static void v(String tag, String msg) {
		// if (!APPConstant.DEBUG_MODE) {
		// return;
		// }

		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				Log.v(tag, msg);
			}
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				write("Verbose", tag, msg);
			}
		}
	}

	public static void w(String tag, String msg) {
		// if (!APPConstant.DEBUG_MODE) {
		// return;
		// }

		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				Log.w(tag, msg);
			}
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				write("Warn", tag, msg);
			}
		}
	}

	public static void e(String tag, String msg) {
		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				Log.e(tag, msg);
			}
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (tag != null && msg != null) {
				write("Error", tag, msg);
			}
		}
	}

	public static void e(String tag, Throwable throwable) {
		if (throwable == null) {
			return;
		}

		if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
			throwable.printStackTrace();
		}

		if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
			if (!APPConstant.DEBUG_MODE) {
				StackTraceElement[] stacks = new Throwable().getStackTrace();
				for (StackTraceElement stack : stacks) {
					StringBuilder sb = new StringBuilder();
					sb.append("class:").append(stack.getClassName())
							.append(";line:").append(stack.getLineNumber());
					Log.e(tag, sb.toString());
				}
			}
		}
	}

	/**
	 * ��ӡ�ڴ���Ϣ���������Ѵ�С���ڴ�ռ�á�ϵͳ�����ڴ��
	 * 
	 * @param tag
	 * @param context
	 */
	public static void logHeapStats(String tag, Context context) {
		ActivityManager.MemoryInfo sysMemInfo = new ActivityManager.MemoryInfo();
		((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryInfo(sysMemInfo);

		Debug.MemoryInfo proMemInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(proMemInfo);

		long heapTotalSize = Debug.getNativeHeapSize();
		long heapAllocatedSize = Debug.getNativeHeapAllocatedSize();
		long heapFreeSize = Debug.getNativeHeapFreeSize();

		DecimalFormat df = new DecimalFormat("0.000");

		Log.d(tag,
				"heap_stats " + "heap_size="
						+ df.format(heapTotalSize / (1024 * 1024f))
						+ "M allocated="
						+ df.format(heapAllocatedSize / (1024 * 1024f))
						+ "M free=" + df.format(heapFreeSize / (1024 * 1024f))
						+ "M " + "memory_stats " + "memory_usage="
						+ df.format(proMemInfo.getTotalPss() / 1024f)
						+ "M dalvik_usage="
						+ df.format(proMemInfo.dalvikPss / 1024f)
						+ "M native_usage="
						+ df.format(proMemInfo.nativePss / 1024f)
						+ "M other_usage="
						+ df.format(proMemInfo.otherPss / 1024f) + "M "
						+ "system_stats " + "system_available="
						+ df.format(sysMemInfo.availMem / (1024 * 1024f)) + "M");
	}

	/**
	 * ��ӡ��ǰ�̵߳Ķ�ջ��Ϣ
	 * 
	 * @param tag
	 */
	public static void logStackTrace(String tag) {
		Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();
		StackTraceElement[] ste = ts.get(Thread.currentThread());
		for (StackTraceElement s : ste) {
			Log.d(tag, s.toString());
		}
	}

	/**
	 * ��Logд����־�ļ���
	 * 
	 * @param text
	 * @param level
	 */
	private static synchronized void write(String level, String tag,
			String content) {
		if (mSdf == null) {
			mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		}

		StringBuilder buf = new StringBuilder();
		buf.append(mSdf.format(Calendar.getInstance().getTime())).append(" [");
		buf.append("Thread-").append(Thread.currentThread().getId())
				.append("] ");
		buf.append(level.toUpperCase()).append(" ");
		buf.append(tag).append(" : ").append(content);
		File mLogFile;
		if (isExternalStorageAvailable()) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator + "chuangyou" + File.separator + "Log";
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String fileName = dir.getAbsolutePath() + File.separator + level + LOG_NAME;
			mLogFile = new File(fileName);
			
			//��־�ļ���С���� 10M ������ɾ�����´�ӡ
//			FileInputStream fis;
			try {
//				fis = new FileInputStream(mLogFile);
//				FileChannel fc = fis.getChannel();

				if (!APPConstant.DEBUG_MODE && mLogFile.length() > 1024 * 1024 * 10) {
					mLogFile.delete();
					mLogFile = new File(fileName);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// ��������ķ����ļ����κεط�
			RandomAccessFile raf = null;
			try {
				if (!mLogFile.exists()) {
					try {
						mLogFile.createNewFile();
					} catch (IOException e) {
						mLogFile = null;
					}
				}
				raf = new RandomAccessFile(fileName, "rw");
				raf.seek(raf.length());// ���ļ���¼ָ�붨λ��posλ�á�
			//	raf.writeUTF(buf.toString() + "\r\n");
				try {
				      OutputStreamWriter write = null;
				      BufferedWriter out = null;
				      if (fileName != null) {
				        try {   // new FileOutputStream(fileName, true) �ڶ���������ʾ׷��д��
				          write = new OutputStreamWriter(new FileOutputStream(
				              fileName,true),Charset.forName("gbk"));//һ��Ҫʹ��gbk��ʽ
				          out = new BufferedWriter(write, buf.toString().length()+2);
				        } catch (Exception e) {
				        }
				      }
				  out.write(buf.toString()+ "\r\n");
				      out.flush();
				      out.close();
				    } catch (Exception e) {
				      Log.e(tag, e.getMessage(), e);
				    }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException e) {
					}
				}
			}

		} else {
			mLogFile = null;
		}

	}

	/**
	 * ���SD���Ƿ����
	 * 
	 * @return boolean
	 */
	public static boolean isExternalStorageAvailable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return true;
		}
		return false;
	}

}
