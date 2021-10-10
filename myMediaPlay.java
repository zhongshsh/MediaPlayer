
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;

public class myMediaPlay extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    private Slider timeSlider;
    private Duration duration;
    private Label playTime;
    private Slider sVol;
    private MediaPlayer mPlayer;
    private String eURL;
    private Stage stage;
    private Media media;
    private MediaView mView;
    private BorderPane bPane;
    private TextArea ta;
    private Scene aboutScene;
    private String backImage;
    private ImageView iView;
    private Image backGround;
    private Button aBut, pBut, rBut, sBut, qBut;
    private Scene scene;
    private MenuBar menuB;

    public void start(Stage stage) {

        iView = new ImageView();
        backImage = this.getClass().getResource("./begin.png").toString();
        backGround = new Image(backImage);
        iView.setImage(backGround);
        iView.setFitWidth(200);
        iView.setFitHeight(200);
        mView = new MediaView();
        mView.setFitWidth(700);
        this.stage = stage;

        // 控制按钮
        setBut();

        // 音量控件
        sVol = new Slider();
        sVol.setMinWidth(30);
        sVol.setPrefWidth(150);
        sVol.setValue(30);
        Label vol = new Label("音量");

        // 视频进度条
        timeSlider = new Slider();
        timeSlider.valueProperty().addListener(new InvalidationListener() {  // 根据鼠标拉动更新
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    if (duration != null) {
                        mPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
                    }
                    updateValues();
                }
            }
        });
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        playTime = new Label("时间");

        // 导航栏
        setMenu();

        // 组装
        HBox hB = new HBox(10);
        hB.setAlignment(Pos.CENTER);
        hB.getChildren().addAll(aBut,pBut,rBut,qBut,sBut,vol,sVol,playTime,timeSlider);
        bPane = new BorderPane();
        bPane.setPadding(new Insets(0, 0, 10, 0));//(上，右，下，左)
        bPane.setTop(menuB);
        bPane.setMargin(menuB, new Insets(0, 0, 6, 0));
        bPane.setCenter(iView);
        bPane.setMargin(iView, new Insets(100, 300, 100, 300));
        bPane.setBottom(hB);
        bPane.setMargin(hB, new Insets(6, 10, 0, 10));
        scene=new Scene(bPane);
        stage.getIcons().add(backGround);
        stage.setTitle("视频播放器");
        stage.setScene(scene);
        stage.show();
    }

    // 设置控制按键
    protected void setBut() {
        aBut = new Button("全屏");
        aBut.setOnAction(e->{
            if(aBut.getText().equals("全屏")) {
                this.stage.setMaximized(true);
                mView.setFitWidth(1200);
                aBut.setText("退出全屏");
            }
            else {
                this.stage.setMaximized(false);
                mView.setFitWidth(700);
                aBut.setText("全屏");
            }

        });

        pBut = new Button("播放");
        pBut.setOnAction(e->{
            if(pBut.getText().equals("播放")) {
                mPlayer.play();
                pBut.setText("暂停");
            }
            else {
                mPlayer.pause();
                pBut.setText("播放");
            }
        });

        rBut = new Button("重播");
        rBut.setOnAction(e->{
            mPlayer.stop();
            setmPlayer();
            mPlayer.play();
            pBut.setText("暂停");

        });

        sBut = new Button("静音");
        sBut.setOnAction(e->{
            if(sBut.getText().equals("静音")){
                mPlayer.setMute(true);
                sBut.setText("恢复");
                sVol.setValue(0);
            }
            else{
                mPlayer.setMute(false);
                sBut.setText("静音");
                sVol.setValue(30);
            }
        });

        qBut = new Button("快进");
        qBut.setOnAction(e->{
            Duration currentTime = mPlayer.getCurrentTime();
            mPlayer.seek(Duration.seconds(currentTime.toSeconds() + 5.0));
        });
    }

    // 设置菜单栏
    protected void setMenu() {
        Menu act = new Menu("文件");

        MenuItem openMI = new MenuItem("打开");
        openMI.setAccelerator(KeyCombination.keyCombination("Ctrl+O")); //快捷键
        openMI.setOnAction(e->{
            openFile();
        });
        iView.setOnMouseClicked(e->{
            openFile();
        });

        MenuItem saveMI = new MenuItem("另存为");
        saveMI.setAccelerator(KeyCombination.keyCombination("Ctrl+S")); //快捷键
        saveMI.setOnAction(e->{
            FileChooser fC=new FileChooser();
            fC.setTitle("另存为……");
            fC.setInitialDirectory(new File("."));
            FileChooser.ExtensionFilter filter=
                    new FileChooser.ExtensionFilter(".mp4", "*.mp4");
            fC.getExtensionFilters().add(filter);
            File file = fC.showSaveDialog(stage);
            if(file != null) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false)));
                    String path = eURL;
                    path = path.substring(6);
                    FileInputStream fileInputStream = new FileInputStream(path);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    writer.write(reader.read());
                    writer.close();
                }
                catch(IOException ioe) {};
            }
        });


        MenuItem initMI = new MenuItem("关闭当前视频");
        initMI.setAccelerator(KeyCombination.keyCombination("Ctrl+X")); //快捷键
        initMI.setOnAction(e->{
            mPlayer.stop();
            pBut.setText("播放");
            playTime.setText("时间");
            bPane.setCenter(iView);
        });

        MenuItem exitMI = new MenuItem("退出");
        exitMI.setAccelerator(KeyCombination.keyCombination("Ctrl+E")); //快捷键
        exitMI.setOnAction(e->{
            Platform.exit();
        });

        act.getItems().addAll(openMI, saveMI, initMI, exitMI);

        Menu abt = new Menu("关于");

        MenuItem au = new MenuItem("作者信息");
        au.setOnAction(e->{
            ta = new TextArea("作者：zhongshsh\n学号：12345678\n班级：XX级XX班\n邮箱：zhongshsh5@mail2.sysu.edu.cn");
            Stage tmpS = new Stage();
            tmpS.getIcons().add(backGround);
            BorderPane tmpBP = new BorderPane();
            tmpBP.setCenter(ta);
            aboutScene=new Scene(tmpBP,300,100);
            tmpS.setScene(aboutScene);
            tmpS.setTitle("作者信息");
            tmpS.show();
        });

        MenuItem exp = new MenuItem("使用说明");
        exp.setOnAction(e->{
            ta = new TextArea("启动Java代码后，点击播放器中间的开始播放图标或者点击“文件”下的“打开”（快捷键ctrl+o）打开本地视频；点击“文件”下的“打开”"
                    + "（快捷键ctrl+o）可以更换视频；点击“文件”下的“关闭”（快捷键ctrl+x）可以关闭当前视频，返回到初始界面；点击“文件”下的“退出”（快捷键ctrl+e）可以退出视频播放器。\n\n"
                    + "(1)文件格式：MP3/flv/MP4等格式的视频、音频文件（不支持图片）\n"
                    + " *    支持调用的音频格式：\n\tMP3；包含非压缩PCM的AIFF；\n\t包含非压缩PCM的WAV；\n\t使用AAC音频的MPEG-4;"
                    + "\n\n\n *    支持调用的视频格式：\n\t包含VP6视频和MP3音频的FLV；\n\t使用H.264/AVC视频压缩的MPEG-4（MP4）\n\n\n"
                    + "(2)播放器支持功能：\n\t暂停、重播、快进、静音&恢复、全屏；\n\t可以直接拖动进度条调整音量和视频进度；\n\t……\n");
            ta.setWrapText(true); // 自动换行
            Stage tmpS = new Stage();
            tmpS.getIcons().add(backGround);
            BorderPane tmpBP = new BorderPane();
            tmpBP.setCenter(ta);
            aboutScene=new Scene(tmpBP,400,400);
            tmpS.setScene(aboutScene);
            tmpS.setTitle("使用说明");
            tmpS.show();
        });

        abt.getItems().addAll(au, exp);

        menuB = new MenuBar();
        menuB.getMenus().addAll(act, abt);
    }

    // 打开文件（用于导航栏和iView）
    protected void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择一个视频/音频文件");
        File file = fileChooser.showOpenDialog(stage);
        eURL = file.toURI().toString();
        media = new Media(eURL);
        pBut.setText("播放");
        try {  // 处理初始化空指针异常
            mPlayer.stop();
        }catch(Exception e) {}
        mPlayer = new MediaPlayer(media);
        mView.setMediaPlayer(mPlayer);
        bPane.setCenter(mView);
        setmPlayer();
    }

    // mediaPlayer进度条
    protected void setmPlayer() {
        mPlayer.volumeProperty().bind(sVol.valueProperty().divide(100));
        mPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() { // 自动更新
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                updateValues();
            }
        });
        mPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mPlayer.getMedia().getDuration();
                updateValues();
            }
        });
    }

    // 更新mediaPlayer进度条和时间
    protected void updateValues() {
        if (playTime != null && timeSlider != null && sVol != null && duration != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mPlayer.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                    }
                    if (!sVol.isValueChanging()) {
                        sVol.setValue((int) Math.round(mPlayer.getVolume() * 100));
                    }
                }
            });
        }
    }

    // 根据视频时长定制时间格式：将两个Duartion参数转化为 hh：mm：ss的形式后输出
    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        int elapsedMinutes = (intElapsed - elapsedHours * 60 * 60) / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;
        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            int durationMinutes = (intDuration - durationHours * 60 * 60) / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%02d:%02d:%02d / %02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d / %02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%02d:%02d:%02d / %02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d / %02d:%02d", elapsedMinutes, elapsedSeconds);
            }
        }
    }

}


