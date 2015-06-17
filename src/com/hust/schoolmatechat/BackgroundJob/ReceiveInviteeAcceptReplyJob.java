package com.hust.schoolmatechat.BackgroundJob;

import android.content.Context;

import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.GroupChatDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiveInviteeAcceptReplyJob extends Job {
    private static final String TAG = "ReceiveInviteeAcceptReplyJob";
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;

    private DataCenterManagerService context;
    private String groupId;
    private String message;

    public ReceiveInviteeAcceptReplyJob(Context context, String groupId, String message) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("receive_invitee_accept_reply"));
        id = jobCounter.incrementAndGet();
        this.context = (DataCenterManagerService) context;
        this.groupId = groupId;
        this.message = message;
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
        String filterMsg = message
                .substring(APPConstant.CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE
                        .length());
        int i1 = filterMsg.indexOf("_");
        String friendAccount = filterMsg.substring(0,
                i1);
        String filterMsg2 = filterMsg.substring(i1 + 1);
        int i2 = filterMsg2.indexOf("_");
        String name = filterMsg2.substring(0,
                i2);
        String picture = filterMsg2.substring(i2 + 1);
        String userAccount = context.getTigaseAccount();
        String password = context.getTigasePassword();

        Map<String, GroupChatRoomEntity> groupChatRoomMap = context.getGroupChatRoomMap();
        GroupChatDao groupChatDao = context.getGroupChatDao();

        // 更新本地群组表的成员
        GroupChatRoomEntity groupChatRoomEntity = groupChatRoomMap
                .get(groupId);
        int syncType = -1;
        if (groupChatRoomEntity == null) {
            // 本地没有群组记录，需要到web获取处理
            context.updateGroupChatRoomsFromWeb(userAccount, password, groupId);
            groupChatRoomEntity = groupChatRoomMap.get(groupId);
            if (groupChatRoomEntity == null) {
                CYLog.i(TAG, "服务器上不存在该聊天室信息!");
                context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
                return;
            }
        }
        Map<String, Integer> syncOccupantsMap = groupChatRoomEntity.getOccupantsMap();

		// 创建者发送的同意信息，创建者加为管理员+++lqg+++ 只支持一个管理员，其他的管理员只能通过提升
		//权限来操作，登陆用户自己发送的同意命令，且登陆用户是群组创建者，把登陆用户加为管理员
        if (friendAccount.equals(userAccount) &&
                groupChatRoomEntity.getCreaterAccount().equals(userAccount)) {
            groupChatRoomEntity.addAdministrater(friendAccount);
            syncType = GroupChatRoomEntity.SYNCTYPE_ADD_ADMIN;
            syncOccupantsMap.put(friendAccount, 1);
        } else {
            syncType = GroupChatRoomEntity.SYNCTYPE_ADD_NORMAL_MEMBER;
            syncOccupantsMap.put(friendAccount, 0);
            groupChatRoomEntity.addNormalMember(friendAccount);

            // 非好友且本地没有好友信息需要从网络获取信息
            if (!friendAccount.equals(userAccount) && !context.isMyFriend(friendAccount)
                    && !groupChatDao.isContactsEntityExisted(
                    userAccount, friendAccount)) {
                context.updateGroupChatMembersFromWeb(userAccount, password, friendAccount);
            }
        }
        // 重新设置membersAccount或者AdminsAccount
        groupChatRoomEntity.setOccupantsMap(syncOccupantsMap);
        groupChatDao.updateGroupChatEntity(groupChatRoomEntity);

        // 如果本账号是创建者，则更新web服务器
        if (userAccount.equals(groupChatRoomEntity
                .getCreaterAccount())) {
            // 需要预先设定密码
            groupChatRoomEntity.setPassword(password);
            groupChatRoomEntity.setSyncType(syncType);
            context.executeGroupInfoSync(groupChatRoomEntity);
        }

        context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable arg0) {
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        return true;
    }

}
