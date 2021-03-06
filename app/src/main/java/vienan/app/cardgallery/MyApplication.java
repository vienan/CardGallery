package vienan.app.cardgallery;

import android.graphics.Typeface;

import com.activeandroid.ActiveAndroid;

import org.lasque.tusdk.core.TuSdk;

/**
 * Created by vienan on 2015/8/17.
 */
public class MyApplication extends com.activeandroid.app.Application {
    private static final String CANARO_EXTRA_BOLD_PATH = "fonts/canaro_extra_bold.otf";
    public static Typeface canaroExtraBold;

    /**
     * 应用程序创建
     */
    @Override
    public void onCreate() {
        super.onCreate();
    /**
     * ！！！！！！！！！！！！！！！！！！！！！！！！！特别提示信息要长！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
     * 关于TuSDK体积（SDK编译后仅为0.9MB）：
     * 1,如果您不需要使用本地贴纸功能，或仅需要使用在线贴纸功能，请删除/app/assets/TuSDK.bundle/stickers文件夹
     * 2,如果您仅需要几款滤镜，您可以删除/app/assets/TuSDK.bundle/textures下的*.gsce文件
     * 3,如果您不需要使用滤镜功能，请删除/app/assets/TuSDK.bundle/textures文件夹
     * 4,TuSDK在线管理功能请访问：http://tusdk.com/
     *
     * 开发文档:http://tusdk.com/docs/android/api/
     */
    ActiveAndroid.initialize(this);
    // 设置输出状态
    TuSdk.enableDebugLog(true);
    // 初始化SDK (请前往 http://tusdk.com 获取您的APP 开发秘钥)
    TuSdk.init(this.

    getApplicationContext(),

    "b036776d9fc97063-01-shlwn1");

    // 如果不想继承TuApplication，直接在自定义Application.onCreate()方法中调用以下方法
    // 初始化全局变量
    // TuSdk.enableDebugLog(true);
    // 开发ID (请前往 http://tusdk.com 获取您的APP 开发秘钥)
    // TuSdk.init(this.getApplicationContext(),
    // "12aa4847a3a9ce68-04-ewdjn1");
    initTypeface();

}

    private void initTypeface() {
        canaroExtraBold = Typeface.createFromAsset(getAssets(), CANARO_EXTRA_BOLD_PATH);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
