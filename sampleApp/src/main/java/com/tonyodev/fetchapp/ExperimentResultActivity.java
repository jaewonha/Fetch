package com.tonyodev.fetchapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.DefaultFetchNotificationManager;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/*
    · (고정측정) 코엑스 실내외 특정 구간(예시: 파르나스 몰 입구 진입구간, 별마당 도서관, 메가박스 영화관)에서 고정상태 측정

    · (이동측정) 이동상태(파르나스 ~ 별마당, 별마당 ~ 메가박스, 메가박스 ~ 파르나스)에서 이동상태 측정 (기지국이오버랩되지 않은 위치 확인 필요)

   ① (속도측정) 이통3사 MEC 서버에 저장된 속도 측정용 콘텐츠*를 이통사별 단말기 10대**가 동시 접속하여 콘텐츠를 스트리밍 받으면서 5G 네트워크 속도 측정. 영상을 고화질로 재생하기 위한 속도가 일정하게 유지되어야 함
 */
public class ExperimentResultActivity extends AppCompatActivity  {

    final String TAG = ExperimentResultActivity.class.getSimpleName();
    private static final int STORAGE_PERMISSION_CODE = 100;

    SharedPreferences sharedpreferences;
    TextView tvExpResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_result);

        sharedpreferences = getSharedPreferences(DownloadInfo.PREF_NAME, Context.MODE_PRIVATE);

        checkStoragePermissions();

        tvExpResult = findViewById(R.id.tvExpResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            loadExpData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadExpData();
        } else {
            Snackbar.make(findViewById(R.id.activity_main), R.string.permission_not_enabled, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    public void btnClickSave(View v) {
        String expResult = tvExpResult.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("실험 이름을 입력해주세요");

        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setPadding(
                getResources().getDimensionPixelOffset(R.dimen.dp_19), // if you look at android alert_dialog.xml, you will see the message textview have margin 14dp and padding 5dp. This is the reason why I use 19 here
                0,
                getResources().getDimensionPixelOffset(R.dimen.dp_19),
                0
        );
        EditText input = new EditText(this);
        //textInputLayout.hint = "Email"
        textInputLayout.addView(input);

//        // Set up the input
//        final EditText input = new EditText(this);
//        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(textInputLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String expName = input.getText().toString();


                Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

                //String fileName = "exp_" + expName + "_" + dateFormat.format(date) + ".txt";
                String fileName = "exp_" + expName + "_" + dateFormat.format(date) + ".xls";

                try {
                    //File file = writeToFile(fileName, expResult);
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    WritableWorkbook wb = createWorkbook(file);
                    WritableSheet sheet = createSheet(wb, "sheet0", 0);

                    writeCell(0, 0, "test", true, sheet);
                    writeCell(0, 1, "value", false, sheet);
                    wb.write();
                    wb.close();

                    Toast.makeText(ExperimentResultActivity.this,
                            "파일 저장 성공:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                    openFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ExperimentResultActivity.this,
                            "파일 저장 실패:" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }

    public void openFile(File writtenFile) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", writtenFile);
        //Uri fileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", writtenFile);
        intent.setData(fileUri);

        startActivity(intent);
    }

    private File writeToFile(String fileName, String content) throws IOException {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();

            return file;
    }

    private void loadExpData() {
        String[] expIDList = {
                "expExtFix1",
                "expExtFix2",
                "expExtFix3",
                "expExtMove1",
                "expExtMove2",
                "expExtMove3",
                "expIntFix1",
                "expIntFix2",
                "expIntFix3",
                "expIntMove1",
                "expIntMove2",
                "expIntMove3",
                "socketExpResult",
                "pingExpResult"
        };

        String[] expDescList = {
                "고정1(삼성역 5출)",
                "고정2(봉은사역 7출)",
                "고정3(삼성중앙역 5출)",
                "이동1(삼성역5출~봉은사역7출)",
                "이동2(봉은사역7출~삼성중앙역5출)",
                "이동3(삼성중앙역5출~삼성역5출)",
                "고정1(별마당도서관)",
                "고정2(초계국수 앞)",
                "고정3(알도 매장 앞)",
                "이동1(별마당~초계국수)",
                "이동2(초계국수~초계국수)",
                "이동3(초계국수~알도)",
                "소켓 응답속도 측정 결과",
                "핑 응답속도 측정 결과"
        };

        for(int i=0; i<expIDList.length; i++) {
            String expId    = expIDList[i];
            String expDesc  = expDescList[i];

            String expData = sharedpreferences.getString(expId, null);
            System.err.println(expId + ":" + expData);

            tvExpResult.append("*"  + expDesc + "\n\n");

            if(expData!=null) {
                if( expId.compareTo("socketExpResult")==0 ||
                        expId.compareTo("pingExpResult")==0 )
                {
                    LatencyInfo latencyInfo = new Gson().fromJson(expData, LatencyInfo.class);
                    tvExpResult.append(latencyInfo.statsWithJudge(true)+ "\n");
                } else {
                    DownloadInfo downloadInfo = new Gson().fromJson(expData, DownloadInfo.class);
                    tvExpResult.append(downloadInfo.toString() + "\n");
                }

            } else {
                tvExpResult.append("None\n\n");
            }

            tvExpResult.append("\n");
        }
    }

    public WritableWorkbook createWorkbook(File file){
        //exports must use a temp file while writing to avoid memory hogging
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setUseTemporaryFileDuringWrite(true);

        WritableWorkbook wb = null;

        try{
            //create a new WritableWorkbook using the java.io.File and
            //WorkbookSettings from above
            wb = Workbook.createWorkbook(file,wbSettings);
        }catch(IOException ex){
            Log.e(TAG,ex.getStackTrace().toString());
            Log.e(TAG, ex.getMessage());
        }

        return wb;
    }

    /**
     *
     * @param wb - WritableWorkbook to create new sheet in
     * @param sheetName - name to be given to new sheet
     * @param sheetIndex - position in sheet tabs at bottom of workbook
     * @return - a new WritableSheet in given WritableWorkbook
     */
    public WritableSheet createSheet(WritableWorkbook wb,
                                     String sheetName, int sheetIndex){
        //create a new WritableSheet and return it
        return wb.createSheet(sheetName, sheetIndex);
    }


    /**
     *
     * @param columnPosition - column to place new cell in
     * @param rowPosition - row to place new cell in
     * @param contents - string value to place in cell
     * @param headerCell - whether to give this cell special formatting
     * @param sheet - WritableSheet to place cell in
     * @throws RowsExceededException - thrown if adding cell exceeds .xls row limit
     * @throws WriteException - Idunno, might be thrown
     */
    public void writeCell(int columnPosition, int rowPosition, String contents, boolean headerCell,
                          WritableSheet sheet) throws RowsExceededException, WriteException{
        //create a new cell with contents at position
        Label newCell = new Label(columnPosition,rowPosition,contents);

        if (headerCell){
            //give header cells size 10 Arial bolded
            WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
            //center align the cells' contents
            headerFormat.setAlignment(Alignment.CENTRE);
            newCell.setCellFormat(headerFormat);
        }

        sheet.addCell(newCell);
    }
}
