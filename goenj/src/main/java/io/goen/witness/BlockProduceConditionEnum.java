package io.goen.witness;

/**
 * Created by yuanhangzhang1 on 2017/5/7.
 */
public enum BlockProduceConditionEnum {

    PRODUCED(0, "Produce Block Success"),
    NOT_SYNCED(1, "Not Synced"),
    NOT_MY_TURN(2, "Not Turn Myself"),
    NOT_TIME_YET(3, "Not Time Yet"),
    NO_PRIVATE_KEY(4, "No privateKey"),
    LOW_PARTICIPATION(5, "Low Participation"),
    LAG(6, "Lag"),
    CONSECUTIVE(7, "Consecutive"),
    WAIT_FOR_GENESIS(8, "Wait for genesis"),
    EXCEPTION_PRODUCE(9, "Exception Produced");

    private int code;
    private String message;

    private BlockProduceConditionEnum(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
