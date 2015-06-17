package com.hust.schoolmatechat.login;

public class LoginService {
	
	
	
	public void loginOnMainServer(final String accountNum,final String password){
		
		
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String loginJResult=null;
					loginJResult=LoginUtils.loginOnMainServer(accountNum, password);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				super.run();
			}
			
			
		}.start();
		
	}

}
