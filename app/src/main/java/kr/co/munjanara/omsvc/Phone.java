package kr.co.munjanara.omsvc;

public class Phone {
    private String mName;
    private String mPhone;

    public Phone(String name, String phone) {
        mName = name;
        mPhone = phone;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }
}
