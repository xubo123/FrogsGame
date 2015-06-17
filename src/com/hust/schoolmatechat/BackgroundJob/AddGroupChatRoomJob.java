package com.hust.schoolmatechat.BackgroundJob;

import android.content.Context;

import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

public class AddGroupChatRoomJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;

    private DataCenterManagerService context;
    private GroupChatRoomEntity room;

    public AddGroupChatRoomJob(Context context, GroupChatRoomEntity room) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("add_groupchat_room"));
        id = jobCounter.incrementAndGet();
        this.room = room;
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
        // 第一步:同步本地信息
        context.updateLocalGroupChatData(room);

        // 第二步:将群信息传到web服务器上
        context.executeGroupInfoSync(room);

        // 第三步：发送邀请消息 账号_姓名_群组id_群组
        ContactsEntity userSelfContactsEntity = context.getUserSelfContactsEntity();
        String userAccount1 = userSelfContactsEntity
                .getUserAccount();
        String name1 = userSelfContactsEntity.getName();
        String groupId1 = room.getGroupId();
        String groupName1 = room.getGroupName();
        StringBuffer buf1 = new StringBuffer();
        buf1.append(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE)
                .append(userAccount1).append("_").append(name1)
                .append("_").append(groupId1).append("_")
                .append(groupName1);
        context.sendGroupNotifyCmdMessage(room,
                buf1.toString());

        context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable arg0) {
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        return true;
    }

}
