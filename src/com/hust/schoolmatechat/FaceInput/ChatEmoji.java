package com.hust.schoolmatechat.FaceInput;


/**
 * 
 ******************************************
 * @author 
 * @�ļ�����	:  ChatEmoji.java
 * @����ʱ��	: 2013-1-27 ����02:33:43
 * @�ļ�����	: �������ʵ��
 ******************************************
 */
public class ChatEmoji {

    /** ������ԴͼƬ��Ӧ��ID */
    private int id;

    /** ������Դ��Ӧ���������� */
    private String character;

    /** ������Դ���ļ��� */
    private String faceName;

    /** ������ԴͼƬ��Ӧ��ID */
    public int getId() {
        return id;
    }

    /** ������ԴͼƬ��Ӧ��ID */
    public void setId(int id) {
        this.id=id;
    }

    /** ������Դ��Ӧ���������� */
    public String getCharacter() {
        return character;
    }

    /** ������Դ��Ӧ���������� */
    public void setCharacter(String character) {
        this.character=character;
    }

    /** ������Դ���ļ��� */
    public String getFaceName() {
        return faceName;
    }

    /** ������Դ���ļ��� */
    public void setFaceName(String faceName) {
        this.faceName=faceName;
    }
}
