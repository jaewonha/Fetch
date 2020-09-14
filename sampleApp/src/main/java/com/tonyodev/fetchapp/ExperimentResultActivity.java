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

    String TAG = ExperimentResultActivity.class.getSimpleName();
    static int STORAGE_PERMISSION_CODE = 100;

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

                String fileName = "exp_" + expName + "_" + dateFormat.format(date);
                try {
                    File txtFile = writeToFile(fileName + ".txt", expResult);
                    File excelFile = writeToExcelFile(fileName + ".xls");

                    Toast.makeText(ExperimentResultActivity.this,
                            "파일 저장 성공:" + excelFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                    openFile(excelFile);

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


    private File writeToExcelFile(String fileName) throws Exception {
        File excelFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
        if (!excelFile.exists()) {
            excelFile.createNewFile();
        }

        //expFileDownIDList
        //expLatencyIDList

        WritableWorkbook wb = createWorkbook(excelFile);

        //download test sheet
        WritableSheet downloadTestSheet = createSheet(wb, "다운로드테스트", 0);
        int c=0;
        writeCell(c++, 0, "이름", true, downloadTestSheet);
        writeCell(c++, 0, "URL", true, downloadTestSheet);
        writeCell(c++, 0, "해시값", true, downloadTestSheet);
        writeCell(c++, 0, "원본해시값", true, downloadTestSheet);
        writeCell(c++, 0, "시작시간(ms)", true, downloadTestSheet);
        writeCell(c++, 0, "종료시간(ms)", true, downloadTestSheet);
        writeCell(c++, 0, "걸린시간(ms)", true, downloadTestSheet);
        writeCell(c++, 0, "파일크기(ms)", true, downloadTestSheet);
        writeCell(c++, 0, "다운로드속도(Byte/s)", true, downloadTestSheet);
        writeCell(c++, 0, "무결성테스트", true, downloadTestSheet);
        writeCell(c++, 0, "속도테스트", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(시작)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(종료)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(1분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(2분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(3분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(4분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(5분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(6분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(7분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(8분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(9분)", true, downloadTestSheet);
        writeCell(c++, 0, "GPS(10분)", true, downloadTestSheet);

        for(int r=1; r<=expFileDownIDList.length; r++) {
            c=0;
            DownloadInfo expInfo = getExpFileDownResult(expFileDownIDList[r-1]);
            if(expInfo==null) {
                writeCell(c++, r, getExpName(expFileDownIDList[r-1]), false, downloadTestSheet);
                continue;
            }
            writeCell(c++, r, expInfo.name, false, downloadTestSheet);
            writeCell(c++, r, expInfo.url, false, downloadTestSheet);
            writeCell(c++, r, expInfo.hash, false, downloadTestSheet);
            writeCell(c++, r, expInfo.correctHash, false, downloadTestSheet);
            writeCell(c++, r, ""+expInfo.startMs, false, downloadTestSheet);
            writeCell(c++, r, ""+expInfo.endMs, false, downloadTestSheet);
            writeCell(c++, r, ""+expInfo.durMs, false, downloadTestSheet);
            writeCell(c++, r, ""+expInfo.size, false, downloadTestSheet);
            writeCell(c++, r, ""+expInfo.bytePerSec, false, downloadTestSheet);
            writeCell(c++, r, expInfo.passIntegrityTest() ? "PASS" : "FAIL", false, downloadTestSheet);
            writeCell(c++, r, expInfo.passLatencyTest() ? "PASS" : "FAIL", false, downloadTestSheet);
            writeCell(c++, r, expInfo.gpsDataList.get(0).getGPSString(), false, downloadTestSheet);
            writeCell(c++, r, expInfo.gpsDataList.get(expInfo.gpsDataList.size()-1).getGPSString(), false, downloadTestSheet);
            for(int i=1; i<expInfo.gpsDataList.size()-1 && i<=10; i++) {
                writeCell(c++, r, expInfo.gpsDataList.get(i).getGPSString(), false, downloadTestSheet);
            }
        }

        //latency test sheet
        WritableSheet latencyTestSheet = createSheet(wb, "응답속도테스트", 1);
        c=0;
        writeCell(c++, 0, "이름", true, latencyTestSheet);
        writeCell(c++, 0, "시도횟수", true, latencyTestSheet);
        writeCell(c++, 0, "성공횟수", true, latencyTestSheet);
        writeCell(c++, 0, "실패횟수", true, latencyTestSheet);
        writeCell(c++, 0, "평균속도(ms)", true, latencyTestSheet);
        writeCell(c++, 0, "최소속도(ms)", true, latencyTestSheet);
        writeCell(c++, 0, "최대속도(ms)", true, latencyTestSheet);
        writeCell(c++, 0, "응답속도테스트", true, latencyTestSheet);

        for(int r=1; r<=expLatencyIDList.length; r++) {
            c=0;
            LatencyInfo expInfo = getExpLatencyResult(expLatencyIDList[r-1]);
            if(expInfo==null) {
                writeCell(c++, r, getExpName(expLatencyIDList[r-1]), false, latencyTestSheet);
                continue;
            }
            writeCell(c++, r, ""+expInfo.name, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.cnt, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.ok, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.fail, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.avg, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.min, false, latencyTestSheet);
            writeCell(c++, r, ""+expInfo.max, false, latencyTestSheet);
            writeCell(c++, r, expInfo.passLatencyTest() ? "PASS" : "FAIL", false, latencyTestSheet);
        }

        //test
        //writeCell(0, 0, "test", true, downloadTestSheet);
        //writeCell(0, 1, "value", false, downloadTestSheet);
        wb.write();
        wb.close();

        return excelFile;
    }

    String[] expFileDownIDList = {
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
    };

    String[] expLatencyIDList = {
            "socketExpResult",
            "pingExpResult",
    };


    String getExpName(String expId) {
        switch(expId) {
            case "expExtFix1": return "고정1(삼성역 5출)";
            case "expExtFix2": return "고정2(봉은사역 7출)";
            case "expExtFix3": return "고정3(삼성중앙역 5출)";
            case "expExtMove1": return "이동1(삼성역5출~봉은사역7출)";
            case "expExtMove2": return "이동2(봉은사역7출~삼성중앙역5출)";
            case "expExtMove3": return "이동3(삼성중앙역5출~삼성역5출)";
            case "expIntFix1": return "고정1(별마당도서관)";
            case "expIntFix2": return "고정2(초계국수 앞)";
            case "expIntFix3": return "고정3(알도 매장 앞)";
            case "expIntMove1": return "이동1(별마당~초계국수)";
            case "expIntMove2": return "이동2(초계국수~초계국수)";
            case "expIntMove3": return "이동3(초계국수~알도)";
            case "socketExpResult": return "소켓 응답속도 측정 결과";
            case "pingExpResult":   return "핑 응답속도 측정 결과";
            default:                return "None";
        }
    }

    DownloadInfo getExpFileDownResult(String expFileDownID) {
        String expData = sharedpreferences.getString(expFileDownID, null);
        return new Gson().fromJson(expData, DownloadInfo.class);
    }

    LatencyInfo getExpLatencyResult(String expLatencyID) {
        String expData = sharedpreferences.getString(expLatencyID, null);
        return new Gson().fromJson(expData, LatencyInfo.class);
    }

    void loadExpData() {

        for(String expId : expFileDownIDList) {
            DownloadInfo expInfo = getExpFileDownResult(expId);

            if(expInfo==null) {

                tvExpResult.append("*"  + getExpName(expId) + "\n\n");
                tvExpResult.append("실험결과없음\n\n");
            } else {
                tvExpResult.append("*"  + expInfo.name + "\n\n");
                tvExpResult.append(expInfo.toString() + "\n");
            }

        }

        for(String expId : expLatencyIDList) {
            LatencyInfo expInfo = getExpLatencyResult(expId);

            if(expInfo==null) {
                tvExpResult.append("*"  + getExpName(expId) + "\n\n");
                tvExpResult.append("실험결과없음\n\n");
            } else {
                tvExpResult.append("*"  + expInfo.name + "\n\n");
                tvExpResult.append(expInfo.statsWithJudge(true) + "\n");
            }
        }

        tvExpResult.append("\n\n");
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
