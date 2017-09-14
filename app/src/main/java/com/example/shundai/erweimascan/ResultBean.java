package com.example.shundai.erweimascan;
import android.graphics.Bitmap;
/**
 * Created by win7 on 2017/9/12.
 */

public class ResultBean {

    private String resultString;
    private Bitmap bitmap;

    public ResultBean(String resultString, Bitmap bitmap) {
        this.resultString = resultString;
        this.bitmap = bitmap;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
