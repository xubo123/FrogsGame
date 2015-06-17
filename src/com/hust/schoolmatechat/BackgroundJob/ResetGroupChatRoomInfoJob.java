package com.hust.schoolmatechat.BackgroundJob;

import android.content.Context;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.GroupChatDao;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

public class ResetGroupChatRoomInfoJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;

    private DataCenterManagerService context;
    private String groupIds;
    private GroupChatDao groupChatDao;

    public ResetGroupChatRoomInfoJob(Context context, String groupIds) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("reset_groupchat_room_info"));
        this.id = jobCounter.incrementAndGet();
        this.context = (DataCenterManagerService) context;
        this.groupIds = groupIds;
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
        context.checkGroupChatRoomMap();

        if (groupIds == null || groupIds.equals("")) {
            context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
            return;
        }
        if (context.getGroupChatDao() == null) {
            groupChatDao = new GroupChatDao(context);
        }

        String groupIdArray[] = groupIds.split(",");
        int groupCount = groupChatDao.getGroupChatRoomEntityCount(context.getTigaseAccount());
        if (groupIdArray.length == groupCount) {
            context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
            return;
        }
        context.updateGroupChatRoomsFromWeb(context.getTigaseAccount(), context.getTigasePassword(), groupIds);
        for (int i = 0; i < groupIdArray.length; ++i) {
            if (groupIdArray[i] != null && groupIdArray[i] != "")
                context.updateGroupChatMembersFromWeb(context.getTigaseAccount(), context.getTigasePassword(), groupIdArray[i]);
        }
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable arg0) {
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        return true;
    }

}
