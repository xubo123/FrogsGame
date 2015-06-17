package com.hust.schoolmatechat.utils;

import android.graphics.Bitmap;

public class GetImage extends Thread {
	private String imageURl;
	private Bitmap imBitmap;
	private boolean finished=false;
	
	
	

	public GetImage(String imageURl) {
		super();
		this.imageURl = imageURl;
	}




	public String getImageURl() {
		return imageURl;
	}




	public void setImageURl(String imageURl) {
		this.imageURl = imageURl;
	}




	public Bitmap getImBitmap() {
		return imBitmap;
	}




	public void setImBitmap(Bitmap imBitmap) {
		this.imBitmap = imBitmap;
	}




	public boolean isFinished() {
		return finished;
	}




	public void setFinished(boolean finished) {
		this.finished = finished;
	}




	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.imBitmap=ImageUtils.getImageFromWeb(imageURl);
			if(this.imBitmap!=null){
				this.finished=true;
			}else{
				this.finished=false;
				System.out.println("get bitmap failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("get bitmap failed");
		} finally{
			this.finished=true;
		}
		super.run();
	}

	
	
}
