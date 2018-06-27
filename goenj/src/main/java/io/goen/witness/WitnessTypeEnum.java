package io.goen.witness;


public enum WitnessTypeEnum {
    VOTE_TOPN(1, "Top N"),
    TIMESHARE(2, "Share Time"),
    MINER(3, "Miner");



    private int code;
    private String message;

    private WitnessTypeEnum(int code, String message){
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
