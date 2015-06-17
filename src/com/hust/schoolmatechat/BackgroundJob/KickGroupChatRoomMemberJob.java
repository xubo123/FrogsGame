package com.hust.schoolmatechat.BackgroundJob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.GroupChatDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class KickGroupChatRoomMemberJob extends Job {
    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;

    private DataCenterManagerService context;
    private String groupId;
    private String message;

    public KickGroupChatRoomMemberJob(Context context, String groupId, String message) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("kick_groupchat_room_member"));
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
        String ids = message.substring(APPConstant.CMD_PREFIX_GROUPCHAT_KICK.length());
        String[] idsArray = ids.split("_");
        String userAccount = context.getTigaseAccount();
        String password = context.getTigasePassword();
        GroupChatDao groupChatDao = context.getGroupChatDao();
        GroupChatRoomEntity room = groupChatDao.getGroupChatRoomByGroupId(userAccount, groupId);
        Intent moveOutOfRoomIntent = new Intent("com.schoolmatechat.moveOutOfRoomBroadcastReceiver");
        Intent rosterIntent = new Intent("com.schoolmatechat.onRosterChanged");
        Map<String, GroupChatRoomEntity> groupChatRoomMap = context.getGroupChatRoomMap();
        if(room == null){
        	return;
        }

        //获取此聊天室全部成员
        Map<String, Integer> allMembers = new HashMap<String, Integer>();
        allMembers.putAll(room.getOccupantsMap());

        //如果被踢的有群主，则等同于删群。即将所有人成员都踢除
        if (Arrays.asList(idsArray).contains(room.getCreaterAccount()))
            idsArray = room.getAllMembersStr().split(",");

        //要踢的名单中如果有自己，则删除本地此群信息，否则删除本地这个名单中所有人的记录
        for (int i = 0; i < idsArray.length; ++i) {
            //排除掉名单中无效的id
            if (idsArray[i] == null || idsArray[i].equals(""))
                continue;

            if (idsArray[i].equals(userAccount)) {
				//本账号取消订阅该聊天室节点
				//注意：此函数不能直接在onEventBackgroundThread中执行，这样可能会导致EventBus环回
				//必须先通知UI,然后在UI中调用这个函数
                Bundle bundle = new Bundle();
                bundle.putString("selfMoveOutOfGroup", room.getGroupId() + "_" + room.getGroupName());
                moveOutOfRoomIntent.putExtras(bundle);
                context.sendBroadcast(moveOutOfRoomIntent);

            } else {
                //将此群中与被踢人相关的所有信息删除
                Map<String, Integer> occupantsMap = room.getOccupantsMap();
                if (occupantsMap.containsKey(idsArray[i])) {
                    occupantsMap.remove(idsArray[i]);
                }
                room.setOccupantsMap(occupantsMap);
                groupChatDao.updateGroupChatEntity(room);
                //更新内存
                if (groupChatRoomMap != null) {
                    groupChatRoomMap.remove(groupId);
                    groupChatRoomMap.put(groupId, room);
                    context.sendBroadcast(rosterIntent); // 通知UI更新
                }
            }
        }

        //产生一个room对象的拷贝,用来更新Web服务器
        GroupChatRoomEntity syncRoomEntity = new GroupChatRoomEntity(room);
        //群主更新Web服务器
        if (syncRoomEntity.getCreaterAccount().equals(userAccount)) {
            Map<String, Integer> normalMembersMap = new HashMap<String, Integer>();
            Map<String, Integer> adminsMap = new HashMap<String, Integer>();
            for (int j = 0; j < idsArray.length; ++j) {
                //该id在此聊天室中不存在或已经被删除
                if (!allMembers.containsKey(idsArray[j]))
                    continue;

                if (allMembers.get(idsArray[j]) == 0)
                    normalMembersMap.put(idsArray[j], 0);
                else
                    adminsMap.put(idsArray[j], 1);
            }
            syncRoomEntity.setPassword(password);
            //将普通成员从web服务器上删除
            if (normalMembersMap.size() > 0) {
                syncRoomEntity.setOccupantsMap(normalMembersMap);
                syncRoomEntity.setSyncType(GroupChatRoomEntity.SYNCTYPE_DEL_NORMAL_MEMBER);
                context.executeGroupInfoSync(syncRoomEntity);
            }
            //将管理员从Web服务器上删除
            if (adminsMap.size() > 0) {
                if (adminsMap.containsKey(syncRoomEntity.getCreaterAccount())) {
                    //如果群主被踢，则该群要从服务器上删除
                    syncRoomEntity.setSyncType(GroupChatRoomEntity.SYNCTYPE_DEL_ROOM);
                } else {
                    syncRoomEntity.setOccupantsMap(adminsMap);
                    syncRoomEntity.setSyncType(GroupChatRoomEntity.SYNCTYPE_DEL_ADMIN);
                }
                context.executeGroupInfoSync(syncRoomEntity);
            }

        }

        context.sendHttpStatusMessage(HttpJobStatus.HTTP_SUCCESS);

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable arg0) {
        context.sendHttpStatusMessage(HttpJobStatus.HTTP_FAIL);
        return true;
    }

}
