package com.hust.schoolmatechat.DataCenterManagerService;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class CustomMultipartEntity extends MultipartEntity{

	private ProgressListener listener;
	
	public CustomMultipartEntity(HttpMultipartMode mode,ProgressListener listener) {
		super(mode);
		// TODO Auto-generated constructor stub
		this.listener=listener;
	}
	
	
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		// TODO Auto-generated method stub
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}
	

	public interface ProgressListener{
		void transferred(long num); 
	}
	
	public class CountingOutputStream extends FilterOutputStream{

		 private ProgressListener listener;  
	     private long transferred;  
		
		public CountingOutputStream(OutputStream out,ProgressListener listener) {
			super(out);
			// TODO Auto-generated constructor stub
			this.listener=listener;
			
		}
		
		@Override
		public void write(byte[] buffer, int offset, int length)
				throws IOException {
			// TODO Auto-generated method stub
			
			out.write(buffer, offset, length);
			this.transferred+=length;
			this.listener.transferred(transferred);
		}
		
		@Override
		public void write(int oneByte) throws IOException {
			// TODO Auto-generated method stub
			out.write(oneByte);
			this.transferred++;
			this.listener.transferred(transferred);
		}
		
	}
}
