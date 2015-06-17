package com.hust.schoolmatechat.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class Request4Image extends Thread {
	private String imageUrl;
	private Bitmap imageBM;
	private ImageView imageView;
	
	

	public ImageView getImageView() {
		return imageView;
	}




	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	


	public Request4Image(String imageUrl, ImageView imageView) {
		super();
		this.imageUrl = imageUrl;
		this.imageView = imageView;
	}




	
	
	

	public String getImageUrl() {
		return imageUrl;
	}




	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	



	public Bitmap getImageBM() {
		return imageBM;
	}




	public void setImageBM(Bitmap imageBM) {
		this.imageBM = imageBM;
	}




	@Override
	public void run() {
		// TODO Auto-generated method stub
		
			try {
				this.imageBM=ImageUtils.getImageFromWeb(imageUrl);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			imageView.setImageBitmap(imageBM);
		
		super.run();
	}

	
	
}
