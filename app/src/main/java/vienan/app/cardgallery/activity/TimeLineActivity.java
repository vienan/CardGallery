package vienan.app.cardgallery.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.andexert.library.RippleView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.konifar.fab_transformation.FabTransformation;
import com.melnykov.fab.FloatingActionButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.yalantis.guillotine.animation.GuillotineAnimation;
import com.yalantis.guillotine.interfaces.GuillotineListener;

import org.lasque.tusdk.core.TuSdkResult;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.impl.activity.TuFragment;
import org.lasque.tusdk.impl.components.base.TuSdkComponent;
import org.lasque.tusdk.impl.components.edit.TuEditTurnAndCutFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.greenrobot.event.EventBus;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;
import vienan.app.cardgallery.R;
import vienan.app.cardgallery.adapter.StatusExpandAdapter;
import vienan.app.cardgallery.colorpicker.ColorPickerDialog;
import vienan.app.cardgallery.colorpicker.ColorPickerSwatch;
import vienan.app.cardgallery.model.CardModel;
import vienan.app.cardgallery.model.ChildStatusEntity;
import vienan.app.cardgallery.model.GroupStatusEntity;
import vienan.app.cardgallery.model.MessageEvent;
import vienan.app.cardgallery.view.WheelView;

/**
 * Created by vienan on 2015/9/2.
 */
public class TimeLineActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long RIPPLE_DURATION = 250;
    private final static int TO_EDIT = 1;
    private final static int TO_SWIPE=2;
    private static final String[] STYLES = new String[]{"STANDARD", "GROW", "CARDS", "CURL",
            "WAVE", "FLIP", "FLY", "REVERSE_FLY", "HELIX", "FAN", "TILT", "ZIPPER", "FADE", "TWIRL",
            "SLIDE_IN"};
    @Bind(R.id.more)
    RippleView rippleView;
    @Bind(R.id.group_tiao)
    View groupTiao;
    private int[] mColor;
    @Bind(R.id.content_hamburger)
    ImageView contentHamburger;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.root)
    FrameLayout root;
    @Bind(R.id.view_helper)
    View viewHelper;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.toolbar_footer)
    Toolbar toolbarFooter;
    @Bind(R.id.header_view_helper)
    View header_helper;
    private ExpandableListView expandlistView;
    private StatusExpandAdapter statusAdapter;
    private Context context;
    SystemBarTintManager tintManager;
    SharedPreferences preferences;
    private int selectedStyle;
    public  static int mSelectedColor;
    private int mSelectedIndex;
    private View guillotineMenu;
    GuillotineAnimation guillotineAnimation;
    List<GroupStatusEntity> lists;
    ImageButton ib_capture, ib_album;
    TourGuide mTourGuideHandler;
    // 组件委托
    TuSdkComponent.TuSdkComponentDelegate delegate = new TuSdkComponent.TuSdkComponentDelegate() {
        @Override
        public void onComponentFinished(TuSdkResult result, Error error,
                                        TuFragment lastFragment) {
            toEditActivity(result);
            TLog.d("onEditMultipleComponentReaded: %s | %s", result, error);
        }
    };
    TuEditTurnAndCutFragment.TuEditTurnAndCutFragmentDelegate mDelegate =
            new TuEditTurnAndCutFragment.TuEditTurnAndCutFragmentDelegate() {
                /**
                 * 图片编辑完成
                 *
                 * @param fragment 旋转和裁剪视图控制器
                 * @param result   旋转和裁剪视图控制器处理结果
                 */
                @Override
                public void onTuEditTurnAndCutFragmentEdited(
                        TuEditTurnAndCutFragment fragment, TuSdkResult result) {
                    if (!fragment.isShowResultPreview()) {
                        fragment.hubDismissRightNow();
                        fragment.dismissActivityWithAnim();
                    }
                    toEditActivity(result);
                    TLog.d("onTuEditTurnAndCutFragmentEdited: %s", result);
                }

                /**
                 * 图片编辑完成 (异步方法)
                 *
                 * @param fragment 旋转和裁剪视图控制器
                 * @param result   旋转和裁剪视图控制器处理结果
                 * @return 是否截断默认处理逻辑 (默认: false, 设置为True时使用自定义处理逻辑)
                 */
                @Override
                public boolean onTuEditTurnAndCutFragmentEditedAsync(
                        TuEditTurnAndCutFragment fragment, TuSdkResult result) {

                    return false;
                }

                @Override
                public void onComponentError(TuFragment fragment, TuSdkResult result,
                                             Error error) {
                    TLog.d("onComponentError: fragment - %s, result - %s, error - %s",
                            fragment, result, error);
                }
            };



    private void toEditActivity(TuSdkResult result) {
        Log.i("image2", result.toString());
        Log.d("img2", result.imageSqlInfo.path);
        Calendar c = result.imageSqlInfo.createDate;
        Intent toEditIntent = new Intent(TimeLineActivity.this, EditCardActivity.class);
        String createDate = c.get(Calendar.MONTH) + 1 + "月"
                + c.get(Calendar.DAY_OF_MONTH) + "日";
        toEditIntent.putExtra("createDate", createDate);
        toEditIntent.putExtra("imgPath", result.imageSqlInfo.path);
        toEditIntent.putExtra("theme", mSelectedColor);
        startActivityForResult(toEditIntent, TO_EDIT);
    }

    @OnClick(R.id.fab)
    public void addImg() {
        if (guillotineMenu.getVisibility() == View.VISIBLE) {
            return;
        }
        if (mTourGuideHandler!=null) {
            mTourGuideHandler.cleanUp();
            contentHamburger.setClickable(true);
        }

        FabTransformation.with(fab)
                .transformTo(toolbarFooter);
        if (ib_capture == null || ib_album == null) {
            ib_capture = (ImageButton) toolbarFooter.findViewById(R.id.capture_img);
            ib_album = (ImageButton) toolbarFooter.findViewById(R.id.add_from_album);
        }
        ib_capture.setOnClickListener(this);
        ib_album.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        listViewAnim();
        Log.d("model", "onResume");
        mSelectedIndex=preferences.getInt("mSelectedIndex",1);

    }

    private void listViewAnim() {
        if (lists.size() == 0) {
            groupTiao.setVisibility(View.GONE);
        } else {
            groupTiao.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInRight).playOn(expandlistView);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(MessageEvent event){
        if(event!=null){
            if (event.hasChanged){
                statusAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        preferences = getSharedPreferences("config", MODE_PRIVATE);
        ButterKnife.bind(this);
        if (mColor == null) {
            mColor = getColorArray();
        }
        mSelectedColor = preferences.getInt("theme", mColor[1]);
        selectedStyle = preferences.getInt("style", JazzyHelper.GROW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        EventBus.getDefault().register(this);
        context = this;
        expandlistView = (ExpandableListView) findViewById(R.id.expandlist);
        initView();
        initExpandListView();

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void initView() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
        }

        guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        loadThem(mSelectedColor);
        initGuillotineMenuItem();
        root.addView(guillotineMenu);
        ImageView guillotine_hamburger = (ImageView) guillotineMenu.findViewById(R.id.guillotine_hamburger);
        guillotineAnimation = new GuillotineAnimation.GuillotineBuilder(guillotineMenu,
                guillotine_hamburger, contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setRightToLeftLayout(true)
                .setGuillotineListener(new GuillotineListener() {
                    @Override
                    public void onGuillotineOpened() {
                        viewHelper.setVisibility(View.VISIBLE);
                        expandlistView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onGuillotineClosed() {
                        if (toolbarFooter.getVisibility() == View.VISIBLE) {
                            FabTransformation.with(fab).transformFrom(toolbarFooter);
                        }
                        if (viewHelper.getVisibility() == View.VISIBLE) {
                            viewHelper.setVisibility(View.GONE);
                            expandlistView.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.BounceInDown).playOn(expandlistView);
                        }
                    }
                })
                .build();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHelp();
                if (lists.size() == 0) {
                mTourGuideHandler = TourGuide.init(TimeLineActivity.this).with(TourGuide.Technique.Click)
                        .setPointer(new Pointer())
                        .setToolTip(new ToolTip().setTitle("欢迎!")
                                .setDescription("点击按钮来丰富应用...")
                                .setBackgroundColor(mSelectedColor)
                                .setGravity(Gravity.TOP | Gravity.LEFT))
                        .setOverlay(new Overlay())
                        .playOn(fab);
                if (mTourGuideHandler!=null){
                    contentHamburger.setClickable(false);
                }
                }
            }
        }, 800);
        rippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (!contentHamburger.isClickable()){
                    return;
                }
                Intent addNoteIntent = new Intent(TimeLineActivity.this, EditCardActivity.class);
                addNoteIntent.putExtra("theme", mSelectedColor);
                addNoteIntent.putExtra("style", selectedStyle);
                startActivityForResult(addNoteIntent, TO_EDIT);
            }
        });

    }

    /**
     * 初始化GuillotineMenuItem
     */
    private void initGuillotineMenuItem() {
        LinearLayout profile_group = (LinearLayout) guillotineMenu.findViewById(R.id.profile_group);
        LinearLayout feed_group = (LinearLayout) guillotineMenu.findViewById(R.id.feed_group);
        LinearLayout style_group = (LinearLayout) guillotineMenu.findViewById(R.id.style_group);
        LinearLayout allCard_group = (LinearLayout) guillotineMenu.findViewById(R.id.all_group);
        LinearLayout settings_group = (LinearLayout) guillotineMenu.findViewById(R.id.settings_group);
        profile_group.setOnClickListener(this);
        feed_group.setOnClickListener(this);
        style_group.setOnClickListener(this);
        allCard_group.setOnClickListener(this);
        settings_group.setOnClickListener(this);

    }

    /**
     * 初始化可拓展列表
     */
    private void initExpandListView() {
        lists = getListData();
        statusAdapter = new StatusExpandAdapter(context, lists);
        expandlistView.setAdapter(statusAdapter);
        expandlistView.setGroupIndicator(null); // 去掉默认带的箭头
        expandlistView.setSelection(0);// 设置默认选中项
        // 遍历所有group,将所有项设置成默认展开
        int groupCount = expandlistView.getCount();
        for (int i = 0; i < groupCount; i++) {
            expandlistView.expandGroup(i);
        }

        expandlistView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // TODO Auto-generated method stub
                if (guillotineMenu.getVisibility() == View.VISIBLE) {
                    return true;
                } else if (mTourGuideHandler!=null){
                    mTourGuideHandler.cleanUp();
                }
                String date = statusAdapter.getGroupList().get(groupPosition).getGroupName();
                Intent intent = new Intent(TimeLineActivity.this, MainActivity.class);
                intent.putExtra("theme", mSelectedColor);
                intent.putExtra("style", selectedStyle);
                intent.putExtra("date", date);
                intent.putExtra("mSelectedIndex", mSelectedIndex);
                startActivity(intent);
                return true;
            }
        });
    }

    /**
     * 获取编辑结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TO_EDIT:
                if (resultCode == RESULT_OK) {
                    CardModel model = (CardModel) data.getSerializableExtra("cardModel");
                    model.save();
                    if (lists.size() > 0 && lists.get(0).getGroupName().equals(model.date)) {
                        ChildStatusEntity childStatusEntity = addChildStatusEntity(model);
                        lists.get(0).getChildList().add(0,childStatusEntity);
                    } else {
                        GroupStatusEntity group = new GroupStatusEntity();
                        group.setGroupName(model.date);
                        List<ChildStatusEntity> childList = new ArrayList<ChildStatusEntity>();
                        childList.add(addChildStatusEntity(model));
                        group.setChildList(childList);
                        lists.add(0,group);
                        expandlistView.expandGroup(0);
                        expandlistView.expandGroup(lists.size() - 1);
                    }
                    statusAdapter.notifyDataSetChanged();

                }
                break;
        }
    }

    @NonNull
    private ChildStatusEntity addChildStatusEntity(CardModel model) {
        ChildStatusEntity childStatusEntity = new ChildStatusEntity(model);
        return childStatusEntity;
    }

    private List<GroupStatusEntity> getListData() {
        List<GroupStatusEntity> groupList;
        String[] strArray = getCreateDate();
        List<CardModel> titles = null;

        groupList = new ArrayList<GroupStatusEntity>();
        for (int i = strArray.length - 1; i >= 0; i--) {
            GroupStatusEntity groupStatusEntity = new GroupStatusEntity();
            groupStatusEntity.setGroupName(strArray[i]);
            titles = new Select().from(CardModel.class).where("date=?", new Object[]{strArray[i]}).execute();
            List<ChildStatusEntity> childList = new ArrayList<ChildStatusEntity>();
            for (int j = titles.size()-1; j >=0; j--) {
                ChildStatusEntity childStatusEntity = addChildStatusEntity(titles.get(j));
                childList.add(childStatusEntity);
            }

            groupStatusEntity.setChildList(childList);
            groupList.add(groupStatusEntity);
        }
        return groupList;
    }

    private void toast(String msg) {
        Toast.makeText(TimeLineActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private List<CardModel> getAll() {
        return new Select()
                .from(CardModel.class)
                .execute();
    }

    public String[] getCreateDate() {

        List<CardModel> lists = new Select()
                .from(CardModel.class)
                .groupBy("date").orderBy("Id").execute();
        String[] dates = new String[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            dates[i] = lists.get(i).date;
            Log.i("date",dates[i]);
        }

        return dates;
    }

    /**
     * 点击返回键退出程序
     */
    private static Boolean isExit = false;
    private Handler mHandler = new Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (guillotineMenu.getVisibility() == View.VISIBLE) {
                viewHelp();
            } else if (toolbarFooter.getVisibility() == View.VISIBLE) {
                FabTransformation.with(fab).transformFrom(toolbarFooter);
            } else if (isExit == false) {
                isExit = true;
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
            } else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("style", selectedStyle);
                editor.putInt("theme", mSelectedColor);
                editor.commit();
                finish();
                System.exit(0);
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_group:
                viewHelp();
                Intent toSwipeIntent = new Intent(TimeLineActivity.this, SwipeAbleCardsActivity.class);
                toSwipeIntent.putExtra("fromWhere","TimeLine");
                toSwipeIntent.putExtra("theme", mSelectedColor);
                startActivityForResult(toSwipeIntent,TO_SWIPE);
                break;
            case R.id.feed_group:
                final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(R.string.color_picker_default_title, mColor, mSelectedColor, 4, 0);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int color) {
                        mSelectedColor = color;
                        loadThem(color);
                        viewHelp();
                    }
                });
                colorPickerDialog.show(getSupportFragmentManager(), "THEME");
                break;
            case R.id.style_group:
                View outerView = LayoutInflater.from(this).inflate(R.layout.wheel_view, null);
                WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
                wv.setOffset(2);
                wv.setItems(Arrays.asList(STYLES));
                wv.setSeletion(selectedStyle);
                wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        Log.i("index", "" + selectedIndex);
                        selectedStyle = selectedIndex - 2;
                    }
                });
                new AlertDialog.Builder(this)
                        .setTitle("STYLE")
                        .setView(outerView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                viewHelp();
                            }
                        })
                        .show();
                break;
            case R.id.all_group:
                guillotineAnimation.close();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent toMyGalleryIntent=new Intent(TimeLineActivity.this,MyGalleryActivity.class);
                        toMyGalleryIntent.putExtra("theme",mSelectedColor);
                        startActivity(toMyGalleryIntent);
                    }
                },1000);
                break;
            case R.id.settings_group:
                new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("卡片日记~！")
                        .setContentText("STYLE中主要定义的cardGallery中滚动的样式,您可以查看滚动样式，选择自己喜欢的style")
                        .setCustomImage(R.mipmap.ic_menu_36pt_3x)
                        .setConfirmText("查看style")
                        .setCancelText("知道了")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("http://lab.hakim.se/scroll-effects/"));
                                startActivity(intent);
                                sweetAlertDialog.cancel();
                                viewHelp();
                            }
                        })
                        .showCancelButton(true)
                        .setCancelClickListener(null)
                        .show();

                break;
            case R.id.capture_img:
                if (guillotineMenu.getVisibility() == View.VISIBLE) {
                    return;
                }
                CameraAndEditCutSimple cameraAndEditCutSimple = new CameraAndEditCutSimple();
                cameraAndEditCutSimple.mDelegate = mDelegate;
                cameraAndEditCutSimple.showSimple(TimeLineActivity.this);
                break;
            case R.id.add_from_album:
                if (guillotineMenu.getVisibility() == View.VISIBLE) {
                    return;
                }
                EditMultipleComponentSimple multipleComponentSimple = new EditMultipleComponentSimple();
                multipleComponentSimple.delegate = delegate;
                multipleComponentSimple.showSimple(TimeLineActivity.this);
                break;
            default:
                if (mTourGuideHandler!=null){
                    mTourGuideHandler.cleanUp();
                }
                break;
        }
    }

    private void viewHelp() {
        guillotineAnimation.close();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHelper.setVisibility(View.GONE);
                expandlistView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomInRight).playOn(expandlistView);
            }
        }, 300);

    }

    private int[] getColorArray() {
        int[] mColorChoices = null;
        String[] color_array = getResources().getStringArray(R.array.default_color_choice_values);
        if (color_array != null && color_array.length > 0) {
            mColorChoices = new int[color_array.length];

            for (int i = 0; i < color_array.length; ++i) {
                mColorChoices[i] = Color.parseColor(color_array[i]);
                Log.i("color", color_array[i]);
            }
        }
        return mColorChoices;
    }

    private void loadThem(int color) {
        viewHelper.setBackgroundColor(color);
        toolbar.setBackgroundColor(color);
        tintManager.setTintColor(color);
        fab.setColorNormal(color);
        fab.setColorPressed(color);
        toolbarFooter.setBackgroundColor(color);
        guillotineMenu.setBackgroundColor(color);
        guillotineMenu.findViewById(R.id.menu_toolbar).setBackgroundColor(color);
        header_helper.setBackgroundColor(color);
    }

}