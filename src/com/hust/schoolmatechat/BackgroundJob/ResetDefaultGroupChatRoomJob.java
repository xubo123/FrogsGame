package com.hust.schoolmatechat.BackgroundJob;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.GroupChatDao;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public class ResetDefaultGroupChatRoomJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;
    
    private DataCenterManagerService context;
    private GroupChatDao groupChatDao;
    
    public ResetDefaultGroupChatRoomJob(Context context) {
    	//注意：此后台任务的组id和ResetGroupChatRoomInfoJob必须一样。
    	//这样它和ResetGroupChatRoomInfoJob是串行执行的
    	super(new Params(Priority.LOW).requireNetwork().groupBy("reset_groupchat_room_info"));
        this.id = jobCounter.incrementAndGet();
        this.context = (DataCenterManagerService) context;
        this.groupChatDao = this.context.getGroupChatDao();
    }

	@Override
	public void onAdded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRun() throws Throwable {
		ContactsEntity userSelfContactsEntity = context.getUserSelfContactsEntity();
		String classNames[] = userSelfContactsEntity.getClassName().split(",");
		String classIds = "";
		for (int i = 0; i < classNames.length; ++i) {
			String classId = context.getDepartmentDao().getDepartmentId(classNames[i]);
			if(classId != null)
				classIds += classId + ",";
		}
		
		List<GroupChatRoomEntity> defaultRooms = groupChatDao.getGroupChatRoomEntityList(
				context.getTigaseAccount(), classIds);
		String[] classArray = classIds.split(",");
		String groupIds = "";
		if(defaultRooms != null) {
			for(int i=0; i<defaultRooms.size(); ++i){
				if(defaultRooms.get(i) != null)
					groupIds += defaultRooms.get(i).getGroupId() + ",";
			}
		}
		
		for (int i = 0; i < classArray.length; ++i) {
			// 该班级群还未创建
			if (!groupIds.contains(classArray[i])) {
				context.addGroupChatRoom(classNames[i],
						new HashMap<String, Integer>(), true);
			} else {
				context.joinGroupChatRoom(classArray[i]);
			}
		}
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable arg0) {
		// TODO Auto-generated method stub
		return true;
	}
}
