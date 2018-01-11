package com.emotibot.multiroundTemplateTest.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;
import com.emotibot.middleware.utils.JsonUtils;
import com.emotibot.multiroundTemplateTest.constants.Constants;
import com.emotibot.multiroundTemplateTest.utils.FileUtils;
import com.google.gson.JsonObject;

@Controller
public class MultiroundTemplateTestController
{
    private static Logger logger = Logger.getLogger(MultiroundTemplateTestController.class);
    
    @RequestMapping("/index")
    public String index() 
    {
        return "index";
    }
    
    @RequestMapping(value="/test", method = RequestMethod.POST)
    public void test(@RequestParam("file") MultipartFile file, HttpServletResponse resp) 
    {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        System.out.println("fileName-->" + fileName);
        System.out.println("getContentType-->" + contentType);
        BufferedInputStream bis = null;
        OutputStream os = null;
        try 
        {
            FileUtils.uploadFile(file.getBytes(), ConfigManager.INSTANCE.getPropertyString(Constants.UPLOAD_FILE_PATH_KEY), fileName);
            
            List<List<String>> xlsContents = readXls(ConfigManager.INSTANCE.getPropertyString(Constants.UPLOAD_FILE_PATH_KEY) + fileName);
            String outputFile = ConfigManager.INSTANCE.getPropertyString(Constants.UPLOAD_FILE_PATH_KEY) + "测试结果-" + fileName;
            boolean ret = writeXls(xlsContents, outputFile);
            if (!ret)
            {
                return;
            }
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment;filename=" + new String(("测试结果-" + fileName).getBytes("GB2312"), "ISO_8859_1"));
            byte[] buff = new byte[1024];
            
            os = resp.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(outputFile));
            int i = bis.read(buff);
            while (i != -1) 
            {
                os.write(buff, 0, i);
                os.flush();
                i = bis.read(buff);
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bis != null)
                {
                    bis.close();
                }
                if (os != null)
                {
                    os.close();
                }
            }
            catch(Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }
    
    private List<List<String>> readXls(String fileName)
    {
        InputStream is = null;
        XSSFWorkbook xssfWorkbook = null;
        try
        {
            is = new FileInputStream(fileName);
            xssfWorkbook = new XSSFWorkbook(is);
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
            if (xssfSheet == null)
            {
                return null;
            }
            List<List<String>> ret = new ArrayList<List<String>>();
            for (int i = 0; i <= xssfSheet.getLastRowNum(); i ++)
            {
                List<String> cellList = new ArrayList<String>();
                XSSFRow xssfRow = xssfSheet.getRow(i);
                if (xssfRow != null)
                {
                    String sentence = xssfRow.getCell(0).getStringCellValue();
                    if (sentence.startsWith("场景"))
                    {
                        cellList.add(sentence);
                        ret.add(cellList);
                        continue;
                    }
                    String intent = xssfRow.getCell(1).getStringCellValue();
                    String semantic = xssfRow.getCell(2).getStringCellValue();
                    cellList.add(sentence);
                    cellList.add(intent);
                    cellList.add(semantic);
                    ret.add(cellList);
                }
            }
            return ret;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                is.close();
            } 
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private boolean writeXls(List<List<String>> cellLists, String fileName)
    {
        if (cellLists == null || cellLists.isEmpty())
        {
            return false;
        }
        for (List<String> cellList : cellLists)
        {
            if (cellList.size() != 3)
            {
                continue;
            }
            String sentence = cellList.get(0).trim();
            JsonObject body = new JsonObject();
            body.addProperty("user_id", ConfigManager.INSTANCE.getPropertyString(Constants.USER_ID_KEY));
            body.addProperty("text", sentence);
            body.addProperty("app_id", ConfigManager.INSTANCE.getPropertyString(Constants.APP_ID_KEY));
            HttpRequest request = new HttpRequest(ConfigManager.INSTANCE.getPropertyString(Constants.CHIQ_PARSER_URL_KEY), body.toString(), HttpRequestType.POST);
            HttpResponse response = HttpUtils.call(request, 10000);
            String result = response.getResponse();
            logger.info("");
            logger.info("sentence is: " + sentence);
            logger.info("result is: " + result);
            logger.info("");
            JsonObject retObj = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
            if (retObj.has("msg_response"))
            {
                JsonObject msgObj = retObj.get("msg_response").getAsJsonObject();
                if (msgObj.has("update"))
                {
                    JsonObject updateObj = msgObj.get("update").getAsJsonObject();
                    if (updateObj.has("matchSentenceType"))
                    {
                        boolean isMatch = updateObj.get("matchSentenceType").getAsBoolean();
                        if (isMatch)
                        {
                            continue;
                        }
                    }
                }
                cellList.add("错误");
            }
        }
        File file = new File(fileName);
        if (file.exists())
        {
            file.delete();
        }
        OutputStream os = null;
        XSSFWorkbook xssfWorkbook = null;
        try
        {
            os = new FileOutputStream(fileName);
            xssfWorkbook = new XSSFWorkbook();
            Sheet sheet = xssfWorkbook.createSheet();
            int rowCount = 0;
            for (List<String> cellList : cellLists)
            {
                Row row = sheet.createRow(rowCount);
                for (int i = 0; i < cellList.size(); i ++)
                {                    
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cellList.get(i));
                }
                rowCount ++;
            }
            xssfWorkbook.write(os);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                os.close();
            }
            catch (Exception e)
            {
                
            }
        }
        return true;
    }
}
