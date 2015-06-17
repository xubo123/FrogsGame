package com.hust.schoolmatechat.FaceInput;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hust.schoolmatechat.R;

/**
 * 
 ******************************************
 * @author 
 * @�ļ�����	:  FaceRelativeLayout.java
 * @����ʱ��	: 2013-1-27 ����02:34:17
 * @�ļ�����	: ��������Զ��������
 ******************************************
 */
public class FaceRelativeLayout extends RelativeLayout implements
		OnItemClickListener, OnClickListener {

	private Context context;

	/** ����ҳ�ļ����¼� */
	private OnCorpusSelectedListener mListener;

	/** ��ʾ����ҳ��viewpager */
	private ViewPager vp_face;

	/** ����ҳ���漯�� */
	private ArrayList<View> pageViews;

	/** �α���ʾ���� */
	private LinearLayout layout_point;

	/** �α�㼯�� */
	private ArrayList<ImageView> pointViews;

	/** ���鼯�� */
	private List<List<ChatEmoji>> emojis;

	/** �������� */
	private View view;

	/** ����� */
	private EditText et_sendmessage;

	/** ������������� */
	private List<FaceAdapter> faceAdapters;

	/** ��ǰ����ҳ */
	private int current = 0;

	public FaceRelativeLayout(Context context) {
		super(context);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	/**
	 * ����ѡ�����
	 * 
	 * @author naibo-liao
	 * @ʱ�䣺 2013-1-15����04:32:54
	 */
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		emojis = FaceConversionUtil.getInstace().emojiLists;
		onCreate();
	}

	private void onCreate() {
		Init_View();
		Init_viewPager();
		Init_Point();
		Init_Data();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_face:
			// ���ر���ѡ���
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
			break;
		default:
			// ���ر���ѡ���
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			}
			break;

		}
	}

	/**
	 * ���ر���ѡ���
	 */
	public boolean hideFaceView() {
		// ���ر���ѡ���
		if (view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	/**
	 * ��ʼ���ؼ�
	 */
	private void Init_View() {
		vp_face = (ViewPager) findViewById(R.id.vp_contains);
		et_sendmessage = (EditText) findViewById(R.id.editText_chat_chatcontent);
		layout_point = (LinearLayout) findViewById(R.id.iv_image);
		et_sendmessage.setOnClickListener(this);
		findViewById(R.id.btn_face).setOnClickListener(this);
		view = findViewById(R.id.ll_facechoose);

	}

	/**
	 * ��ʼ����ʾ�����viewpager
	 */
	private void Init_viewPager() {
		pageViews = new ArrayList<View>();
		// �����ӿ�ҳ
		View nullView1 = new View(context);
		// ����͸������
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView1);

		// �м���ӱ���ҳ

		faceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < emojis.size(); i++) {
			GridView view = new GridView(context);
			FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));
			view.setAdapter(adapter);
			faceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			pageViews.add(view);
		}

		// �Ҳ���ӿ�ҳ��
		View nullView2 = new View(context);
		// ����͸������
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView2);
	}

	/**
	 * ��ʼ���α�
	 */
	private void Init_Point() {

		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			layout_point.addView(imageView, layoutParams);
			if (i == 0 || i == pageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViews.add(imageView);

		}
	}

	/**
	 * �������
	 */
	private void Init_Data() {
		vp_face.setAdapter(new ViewPagerAdapter(pageViews));

		vp_face.setCurrentItem(1);
		current = 0;
		vp_face.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// ����ҳ��
				draw_Point(arg0);
				// ����ǵ�һ�����������һ����ֹ��������ʵ����ʵ�ֵ�������������ǵ�һ������ת���ڶ�������������һ������ת�������ڶ���.
				if (arg0 == pointViews.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						vp_face.setCurrentItem(arg0 + 1);// �ڶ��� ���ٴ�ʵ�ָûص�����ʵ����ת.
						pointViews.get(1).setBackgroundResource(R.drawable.d2);
					} else {
						vp_face.setCurrentItem(arg0 - 1);// �����ڶ���
						pointViews.get(arg0 - 1).setBackgroundResource(
								R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	/**
	 * �����α걳��
	 */
	public void draw_Point(int index) {
		for (int i = 1; i < pointViews.size(); i++) {
			if (index == i) {
				pointViews.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				pointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon) {
			int selection = et_sendmessage.getSelectionStart();
			String text = et_sendmessage.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					et_sendmessage.getText().delete(start, end);
					return;
				}
				et_sendmessage.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = FaceConversionUtil.getInstace()
					.addFace(getContext(), emoji.getId(), emoji.getCharacter());
			et_sendmessage.append(spannableString);
		}

	}
}
