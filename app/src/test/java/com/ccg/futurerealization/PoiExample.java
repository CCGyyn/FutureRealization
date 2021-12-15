package com.ccg.futurerealization;

import android.content.Context;

import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.PoiUtils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-15 下午6:46
 * @Version: 1.0
 */
public class PoiExample {

    /**
     * 读取解析Poi数据
     */
    private void readPoiData() {
        LogUtils.d("poi test start");
        Workbook wb = null;
        Sheet sheet = null;
        Row row = null;
        String cellData = null;
        //有权限问题,放入的时候也要把权限设置为777,默认600 getContext().getExternalFilesDir(null)
        String path = "/storage/emulated/0/Android/data/com.ccg.futurerealization/files/test.xls";
        wb = PoiUtils.readExcel(path);
        LogUtils.d("wb == null ? " + (wb == null));
        if (wb != null) {
            //获取第一个sheet
            sheet = wb.getSheetAt(0);
            //获取最大行数
            int rownum = sheet.getPhysicalNumberOfRows();
            //获取第一行
            row = sheet.getRow(0);
            //获取最大列数
            int colnum = row.getPhysicalNumberOfCells();
            LogUtils.d("rownum = " + rownum + ", colnum=" + colnum);
            for (int i = 1; i < rownum; i++) {
                Map<String,String> map = new LinkedHashMap<>();
                row = sheet.getRow(i);
                if(row != null){
                    for (int j = 0;j < colnum;j++){
                        cellData = (String) PoiUtils.getCellFormatValue(row.getCell(j));
                        LogUtils.d("poi = " + cellData);
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 写入Poi数据保存
     * @param context
     */
    private void writePoi(Context context) {
        //写入
        // 创建工作薄 xlsx
        XSSFWorkbook xssWorkbook = new XSSFWorkbook();
        // 创建工作表
        XSSFSheet sheetF = xssWorkbook.createSheet("test");

        for (int i = 0; i < 10; i++)
        {
            XSSFRow rows = sheetF.createRow(i);
            for (int col = 0; col < 10; col++)
            {
                // 向工作表中添加数据
                rows.createCell(col).setCellValue("data" + i + col);
            }
        }
        File xlsFile = new File(context.getExternalFilesDir(null), "poi.xlsx");
        FileOutputStream xlsStream = null;
        try {
            xlsStream = new FileOutputStream(xlsFile);
            xssWorkbook.write(xlsStream);
        } catch (FileNotFoundException e) {
            LogUtils.e(e.getMessage());
        } catch (IOException e) {
            LogUtils.e(e.getMessage());
        }
    }
}
