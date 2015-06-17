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

        //��ȡ��������ȫ����Ա
        Map<String, Integer> allMembers = new HashMap<String, Integer>();
        allMembers.putAll(room.getOccupantsMap());

        //������ߵ���Ⱥ�������ͬ��ɾȺ�����������˳�Ա���߳�
        if (Arrays.asList(idsArray).contains(room.getCreaterAccount()))
            idsArray = room.getAllMembersStr().split(",");

        //Ҫ�ߵ�������������Լ�����ɾ�����ش�Ⱥ��Ϣ������ɾ��������������������˵ļ�¼
        for (int i = 0; i < idsArray.length; ++i) {
            //�ų�����������Ч��id
            if (idsArray[i] == null || idsArray[i].equals(""))
                continue;

            if (idsArray[i].equals(userAccount)) {
				//���˺�ȡ�����ĸ������ҽڵ�
				//ע�⣺�˺�������ֱ����onEventBackgroundThread��ִ�У��������ܻᵼ��EventBus����
				//������֪ͨUI,Ȼ����UI�е����������
                Bundle bundle = new Bundle();
                bundle.putString("selfMoveOutOfGroup", room.getGroupId() + "_" + room.getGroupName());
                moveOutOfRoomIntent.putExtras(bundle);
                context.sendBroadcast(moveOutOfRoomIntent);

            } else {
                //����Ⱥ���뱻������ص�������Ϣɾ��
                Map<String, Integer> occupantsMap = room.getOccupantsMap();
                if (occupantsMap.containsKey(idsArray[i])) {
                    occupantsMap.remove(idsArray[i]);
                }
                room.setOccupantsMap(occupantsMap);
                groupChatDao.updateGroupChatEntity(room);
                //�����ڴ�
                if (groupChatRoomMap != null) {
                    groupChatRoomMap.remove(groupId);
                    groupChatRoomMap.put(groupId, room);
                    context.sendBroadcast(rosterIntent); // ֪ͨUI����
                }
            }
        }

        //����һ��room����Ŀ���,��������Web������
        GroupChatRoomEntity syncRoomEntity = new GroupChatRoomEntity(room);
        //Ⱥ������Web������
        if (syncRoomEntity.getCreaterAccount().equals(userAccount)) {
            Map<String, Integer> normalMembersMap = new HashMap<String, Integer>();
            Map<String, Integer> adminsMap = new HashMap<String, Integer>();
            for (int j = 0; j < idsArray.length; ++j) {
                //��id�ڴ��������в����ڻ��Ѿ���ɾ��
                if (!allMembers.containsKey(idsArray[j]))
                    continue;

                if (allMembers.get(idsArray[j]) == 0)
                    normalMembersMap.put(idsArray[j], 0);
                else
                    adminsMap.put(idsArray[j], 1);
            }
            syncRoomEntity.setPassword(password);
            //����ͨ��Ա��web��������ɾ��
            if (normalMembersMap.size() > 0) {
                syncRoomEntity.setOccupantsMap(normalMembersMap);
                syncRoomEntity.setSyncType(GroupChatRoomEntity.SYNCTYPE_DEL_NORMAL_MEMBER);
                context.executeGroupInfoSync(syncRoomEntity);
            }
            //������Ա��Web��������ɾ��
            if (adminsMap.size() > 0) {
                if (adminsMap.containsKey(syncRoomEntity.getCreaterAccount())) {
                    //���Ⱥ�����ߣ����ȺҪ�ӷ�������ɾ��
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
