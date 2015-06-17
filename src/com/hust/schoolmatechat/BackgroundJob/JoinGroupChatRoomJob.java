package com.hust.schoolmatechat.BackgroundJob;

import android.content.Context;
import android.os.Looper;

import com.hust.schoolmatechat.ChatMsgservice.GroupChatFuncStatusEntity;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

import java.util.concurrent.atomic.AtomicInteger;

public class JoinGroupChatRoomJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;

    private DataCenterManagerService context;
    private String groupId;

    public JoinGroupChatRoomJob(Context context, String groupId) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("join_groupchat_room"));
        id = jobCounter.incrementAndGet();
        this.groupId = groupId;
        this.context = (DataCenterManagerService) context;
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
        if(groupId == null || groupId == "") {
        	context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        	return;
        }
        
        // 更新自己的group信息
        context.updateSelfGroupInfoToUserProfile(groupId);
        
        // 开启线程获取群以及群成员信息，broadcast通知获取结果，获取成功以后broadcast通知UI更新
        context.updateGroupChatRoomsFromWeb(context.getTigaseAccount(), context.getTigasePassword(), groupId);
        context.updateGroupChatMembersFromWeb(context.getTigaseAccount(), context.getTigasePassword(), groupId);
        
		// 加入tigase节点，发送自己加入的通知消息
		GroupChatRoomEntity roomEntity = new GroupChatRoomEntity();
		roomEntity.setGroupId(groupId);
		roomEntity.setFunctionType(GroupChatRoomEntity.FUNCTYPE_JOIN);
		// 发送加入订阅聊天室节点消息
		context.setGroupChatFuncStatus(new GroupChatFuncStatusEntity());
		EventBus.getDefault().post(roomEntity);
		// 等待结果
		context.waitEventBusResultOfGroupFunc();

        context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable arg0) {
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        return true;
    }

}
