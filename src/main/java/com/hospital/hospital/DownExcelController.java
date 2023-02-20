package com.hospital.hospital;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hospital.mybatis.MyBatisDAO;
import com.hospital.vo.NoticeToD_2VO;
import com.hospital.vo.NoticeToN_8List;
import com.hospital.vo.NoticeToN_8VO;
import com.hospital.vo.Patient_1VO;
import com.hospital.vo.TestBlood_17VO;
import com.hospital.vo.TestUrine_21VO;

// 파일다운로드 컨트롤러

@Controller
public class DownExcelController {
	
	private static final Logger logger = LoggerFactory.getLogger(DownExcelController.class);

	@Autowired
	private SqlSession sqlSession;

	
// 검사결과보고알림발송 의사 (ajax버젼)
	@ResponseBody
	@RequestMapping (value = "/insertNoticeToDFromPAjax", method = RequestMethod.POST)
	public String insertNoticeToDFromPAjax(HttpServletRequest request, @RequestBody NoticeToD_2VO noticeToD_2VO) {
		logger.info("insertNoticeToDFromPAjax()");
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		mapper.insertNoticeToD(noticeToD_2VO);
		return "success";
	}
//	검사결과보고알림발송 간호사 (ajax버젼)
	@ResponseBody
	@RequestMapping (value = "/insertNoticeToNFromPAjax", method = RequestMethod.POST)
	public String insertNoticeToNFromPAjax(HttpServletRequest request, @RequestBody NoticeToN_8VO noticeToN_8VO) {
		logger.info("insertNoticeToNFromPAjax()");
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		mapper.insertNoticeToN(noticeToN_8VO);
		return "success";
	}

//	혈액검사결과 db에 저장 & 결과보고알림발송 & 혈액검사 엑셀파일 저장 
	@RequestMapping ("/testbloodresult")
	public String testbloodresult(HttpServletRequest request, Model model, HttpServletResponse response, TestBlood_17VO testBloodVO, NoticeToD_2VO noticeToDVO, NoticeToN_8VO noticeToNVO) throws IOException {
		logger.info("testbloodresult()");
		//	직원 정보 받기
		HttpSession session = request.getSession();
		
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		//	환자 정보 받기
		int patientIdx = Integer.parseInt(request.getParameter("patientIdx"));
		int dDay = Integer.parseInt(request.getParameter("dDay"));
		Patient_1VO patientVO = mapper.selectPatient(patientIdx);
		
		// 혈액 검사 결과를 저장
		mapper.insertTestBlood(testBloodVO);
		
/* 검사결과보고 구버젼---------------------------------------------------------		
		//	의사에게 검사 결과 알람
		noticeToDVO.setAlarmD("혈액검사결과");
		noticeToDVO.setFromDP((String)session.getAttribute("dpart"));
		noticeToDVO.setFromName((String)session.getAttribute("employeeName"));
		logger.info("{}", noticeToDVO);
		mapper.insertNoticeToD(noticeToDVO);
		
//		간호사에게 검사 결과 알람
		noticeToNVO.setAlarmN("혈액검사결과");
		noticeToNVO.setFromDP((String)session.getAttribute("dpart"));
		noticeToNVO.setFromName((String)session.getAttribute("employeeName"));
		logger.info("{}", noticeToNVO);
		mapper.insertNoticeToN(noticeToNVO);

		// 알림창 띄우기
		Alert.alertAndGo(response, "To. 의사, 간호사  " + patientVO.getName() +"님의 검사 결과 등록 알림이 발송되었습니다.", "viewPatientDetail?patientIdx=" + patientIdx + "&dDay=" + dDay);
//-----------------------------------------------------------------------------------------	
*/	
		
// < 엑셀파일 저장 >		
    // 파일경로: C:/Upload/testfile/excelfile/ 
    	logger.info("download컨트롤러의 downTestReport");
		
		String name = patientVO.getName();
		int age = patientVO.getAge();
		String gender = patientVO.getGender();
		String diagnosis = patientVO.getDiagnosis();
		
		model.addAttribute("patientIdx", patientIdx);
		model.addAttribute("dDay", dDay);
		
		XSSFWorkbook xssfWb = null; 
		XSSFSheet xssfSheet = null; 
		XSSFRow xssfRow = null; 
		XSSFCell xssfCell = null;
			
		try {
			int rowNo = 0; 
	
			xssfWb = new XSSFWorkbook(); 
			xssfSheet = xssfWb.createSheet("혈액검사결과"); 
			
			XSSFFont font = xssfWb.createFont();
			font.setFontName(HSSFFont.FONT_ARIAL); 
			font.setFontHeightInPoints((short)20); 
			font.setBold(true); 
			font.setColor(new XSSFColor(Color.decode("#323C73"))); 
			
			CellStyle cellStyle = xssfWb.createCellStyle();
			xssfSheet.setColumnWidth(0, (xssfSheet.getColumnWidth(0))+(short)2048); 
			
			cellStyle.setFont(font); 
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); 

			xssfSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 2)); 
			xssfSheet.addMergedRegion(new CellRangeAddress(13, 13, 0, 2)); 
			xssfSheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 2)); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle); 
			xssfCell.setCellValue(" " + name + " (" + age + "/" + gender + ") " + diagnosis); 
			
			xssfSheet.createRow(rowNo++);
			xssfRow = xssfSheet.createRow(rowNo++);  
			
			XSSFFont font2 = xssfWb.createFont();
			font2.setFontName(HSSFFont.FONT_ARIAL); 
			font2.setFontHeightInPoints((short)12); 
			font2.setBold(true); 
			font2.setColor(new XSSFColor(Color.decode("#000000"))); 
			
			CellStyle cellStyle2 = xssfWb.createCellStyle();
			cellStyle2.setFont(font2); 
			cellStyle2.setAlignment(HSSFCellStyle.ALIGN_RIGHT); 
			
			XSSFFont font3 = xssfWb.createFont();
			font3.setFontName(HSSFFont.FONT_ARIAL); 
			font3.setFontHeightInPoints((short)12); 
			font3.setBold(true); 
			font3.setColor(new XSSFColor(Color.decode("#000000"))); 
			
			CellStyle cellStyle3 = xssfWb.createCellStyle();
			cellStyle3.setFont(font3); 
			cellStyle3.setAlignment(HSSFCellStyle.ALIGN_LEFT); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle3); 
			xssfCell.setCellValue("혈액검사 결과"); 
			
			CellStyle tableCellStyle = xssfWb.createCellStyle();
			tableCellStyle.setBorderTop((short) 5);    
			tableCellStyle.setBorderBottom((short) 5); 
			tableCellStyle.setBorderLeft((short) 5);   
			tableCellStyle.setBorderRight((short) 5); 
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("WBC");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getWBC());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mm3");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("Hb");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getHb());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("g/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("Hct");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getHct());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("%");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("RBC");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getRBC());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mm3");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("MCV");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getMCV());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("fl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("MCH");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getMCH());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("pg");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("MCHC");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getMCHC());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("g/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("Platelet");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testBloodVO.getPlatelet());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mm3");
			
			xssfSheet.createRow(rowNo++); 
			
			TestBlood_17VO recentBloodVO = mapper.selectBloodTestIdx(patientIdx);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm");
			
			String reportDate = sdf.format(recentBloodVO.getWriteDate());

			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle2);
			xssfCell.setCellValue(" 검사일 : " + reportDate ); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle2); 
			xssfCell.setCellValue(" 검사자 : " + recentBloodVO.getEmployeeName()); 
			
	    	String uploadPath = "C:" + File.separator + "Upload" + File.separator + "testfile";
	    	
			File dir = new File(uploadPath + File.separator + "excelfile"); 

			if (!dir.exists()) {
				dir.mkdirs();
			}
	    	
	    	String saveFileName = patientIdx + "_blood_" + recentBloodVO.getIdx()  + ".xlsx"; // 엑셀파일명
	    	File downloadFile = new File(dir + File.separator + saveFileName);
			
			FileOutputStream fos = null;
			fos = new FileOutputStream(downloadFile);
			xssfWb.write(fos);
	
			if (fos != null) fos.close();
		}catch(Exception e){
	        	
		}
 	
    	return "redirect:viewTest";
    }    

//	소변 검사 결과 저장
	@RequestMapping ("/testUrineresult")
	public String testUrineresult(HttpServletRequest request, Model model, HttpServletResponse response, TestUrine_21VO testUrineVO, NoticeToD_2VO noticeToDVO, NoticeToN_8VO noticeToNVO) throws IOException {
		logger.info("testUrineresult()");
		//	직원 정보 받기
		HttpSession session = request.getSession();
		
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		
		//	환자 정보 받기
		int patientIdx = Integer.parseInt(request.getParameter("patientIdx"));
		int dDay = Integer.parseInt(request.getParameter("dDay"));
		model.addAttribute("patientIdx", patientIdx);
		model.addAttribute("dDay", dDay);
		
		Patient_1VO patientVO = mapper.selectPatient(patientIdx);
		
		// 소변 검사 결과를 저장
		mapper.insertTestUrine(testUrineVO);
		
		/* 검사 결과 보고 구버전 ===================================================
		//	의사에게 검사 결과 알람
		noticeToDVO.setAlarmD("소변검사결과");
		noticeToDVO.setFromDP((String)session.getAttribute("dpart"));
		noticeToDVO.setFromName((String)session.getAttribute("employeeName"));
		logger.info("{}", noticeToDVO);
		mapper.insertNoticeToD(noticeToDVO);
		
//		간호사에게 검사 결과 알람
		noticeToNVO.setAlarmN("소변검사결과");
		noticeToNVO.setFromDP((String)session.getAttribute("dpart"));
		noticeToNVO.setFromName((String)session.getAttribute("employeeName"));
		logger.info("{}", noticeToNVO);
		mapper.insertNoticeToN(noticeToNVO);

		// 알림창 띄우기
		Alert.alertAndGo(response, "To. 의사, 간호사  " + patientVO.getName() +"님의 검사 결과 등록 알림이 발송되었습니다.", "viewPatientDetail?patientIdx=" + patientIdx + "&dDay=" + dDay);
		 ============================================================================= */
		
// 엑셀파일저장		
	    // 파일경로: C:/Upload/testfile/excelfile/ 
    	logger.info("download컨트롤러의 downTestReport");
    	
		String name = patientVO.getName();
		int age = patientVO.getAge();
		String gender = patientVO.getGender();
		String diagnosis = patientVO.getDiagnosis();
		
		model.addAttribute("patientIdx", patientIdx);
		model.addAttribute("dDay", dDay);
		
		XSSFWorkbook xssfWb = null; 
		XSSFSheet xssfSheet = null; 
		XSSFRow xssfRow = null; 
		XSSFCell xssfCell = null;
			
		try {
			int rowNo = 0;
	
			xssfWb = new XSSFWorkbook(); 
			xssfSheet = xssfWb.createSheet("소변검사결과"); 
			
			XSSFFont font = xssfWb.createFont();
			font.setFontName(HSSFFont.FONT_ARIAL); 
			font.setFontHeightInPoints((short)20); 
			font.setBold(true); 
			font.setColor(new XSSFColor(Color.decode("#323C73"))); 
			
			CellStyle cellStyle = xssfWb.createCellStyle();
			xssfSheet.setColumnWidth(0, (xssfSheet.getColumnWidth(0))+(short)2048); 
			
			cellStyle.setFont(font); 
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			xssfSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 2)); 
			xssfSheet.addMergedRegion(new CellRangeAddress(17, 17, 0, 2)); 
			xssfSheet.addMergedRegion(new CellRangeAddress(18, 18, 0, 2)); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle); 
			xssfCell.setCellValue(" " + name + " (" + age + "/" + gender + ") " + diagnosis);
			
			xssfSheet.createRow(rowNo++);
			xssfRow = xssfSheet.createRow(rowNo++);  
			
			XSSFFont font2 = xssfWb.createFont();
			font2.setFontName(HSSFFont.FONT_ARIAL); 
			font2.setFontHeightInPoints((short)12); 
			font2.setBold(true);
			font2.setColor(new XSSFColor(Color.decode("#000000"))); 
			
			CellStyle cellStyle2 = xssfWb.createCellStyle();
			cellStyle2.setFont(font2); 
			cellStyle2.setAlignment(HSSFCellStyle.ALIGN_RIGHT); 
			
			XSSFFont font3 = xssfWb.createFont();
			font3.setFontName(HSSFFont.FONT_ARIAL);
			font3.setFontHeightInPoints((short)12); 
			font3.setBold(true); 
			font3.setColor(new XSSFColor(Color.decode("#000000"))); 
			
			CellStyle cellStyle3 = xssfWb.createCellStyle();
			cellStyle3.setFont(font3);
			cellStyle3.setAlignment(HSSFCellStyle.ALIGN_LEFT); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle3);
			xssfCell.setCellValue("소변검사 결과"); 
			
			CellStyle tableCellStyle = xssfWb.createCellStyle();
			tableCellStyle.setBorderTop((short) 5);    
			tableCellStyle.setBorderBottom((short) 5); 
			tableCellStyle.setBorderLeft((short) 5); 
			tableCellStyle.setBorderRight((short) 5);  
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("색깔");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getColor());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("혼탁도");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getTurbidity());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("비중");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getGravity());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("산도");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getAcidity());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("알부민");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getAlbumin());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mg/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("포도당");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getGlucose());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mg/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("케톤");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getKetones());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mg/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("빌리루빈");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getBilirubin());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mg/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("잠혈");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getBlood());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("㎕");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("유로빌리로겐");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getBilinogen());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mg/dl");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("나이트리트");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getNitrite());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("mm3");
			
			xssfRow = xssfSheet.createRow(rowNo++);
			xssfCell = xssfRow.createCell((short) 0);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("백혈구");
			xssfCell = xssfRow.createCell((short) 1);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("" + testUrineVO.getLeukocyte());
			xssfCell = xssfRow.createCell((short) 2);
			xssfCell.setCellStyle(tableCellStyle);
			xssfCell.setCellValue("㎕");
			
			xssfSheet.createRow(rowNo++);
			
			TestUrine_21VO recentUrineVO = mapper.selectUrineTestIdx(patientIdx);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm");
			
			String reportDate = sdf.format(recentUrineVO.getWriteDate());
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle2); 
			xssfCell.setCellValue(" 검사일 : " + reportDate ); 
			
			xssfRow = xssfSheet.createRow(rowNo++); 
			xssfCell = xssfRow.createCell((short) 0); 
			xssfCell.setCellStyle(cellStyle2); 
			xssfCell.setCellValue(" 검사자 : " + recentUrineVO.getEmployeeName()); 
			
	    	String uploadPath = "C:" + File.separator + "Upload" + File.separator + "testfile" + File.separator + "excelfile" + File.separator;
	    	String saveFileName = patientIdx + "_urine_" + recentUrineVO.getIdx()  + ".xlsx"; 
	    	File downloadFile = new File(uploadPath + saveFileName);
			
			FileOutputStream fos = null;
			fos = new FileOutputStream(downloadFile);
			xssfWb.write(fos);
	
			if (fos != null) fos.close();
		}catch(Exception e){
	        	
		}
		
//		return "viewPatientDetail";
		return "redirect:viewTest";

	}	
	
    
    // 파일경로: C:/Upload/testfile/excelfile/ 
    @RequestMapping("/downTestReport")
    public String downTestReport(HttpServletRequest request, HttpServletResponse response, Model model) {
    	logger.info("download컨트롤러의 downTestReport");
    	
    	
		int patientIdx = Integer.parseInt(request.getParameter("patientIdx"));
		int dDay = Integer.parseInt(request.getParameter("dDay"));
		
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		Patient_1VO patientVO = mapper.selectPatient(patientIdx);
		
		String name = patientVO.getName();
		int age = patientVO.getAge();
		String gender = patientVO.getGender();
		String diagnosis = patientVO.getDiagnosis();
		
		model.addAttribute("patientIdx", patientIdx);
		model.addAttribute("dDay", dDay);
    			
    			XSSFWorkbook xssfWb = null; 
    			XSSFSheet xssfSheet = null; 
    			XSSFRow xssfRow = null; 
    			XSSFCell xssfCell = null;
    				
    			try {
    				int rowNo = 0; 
    		
    				xssfWb = new XSSFWorkbook(); 
    				xssfSheet = xssfWb.createSheet("워크 시트1"); 
    				
    				XSSFFont font = xssfWb.createFont();
    				font.setFontName(HSSFFont.FONT_ARIAL); 
    				font.setFontHeightInPoints((short)20); 
    				font.setBold(true); 
    				font.setColor(new XSSFColor(Color.decode("#457ba2")));
    				
    				CellStyle cellStyle = xssfWb.createCellStyle();
    				xssfSheet.setColumnWidth(0, (xssfSheet.getColumnWidth(0))+(short)2048); 
    				
    				cellStyle.setFont(font); 
    				cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); 

    				xssfSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 4)); 
    				
    				xssfRow = xssfSheet.createRow(rowNo++); 
    				xssfCell = xssfRow.createCell((short) 0); 
    				xssfCell.setCellStyle(cellStyle); 
    				xssfCell.setCellValue(" " + name + " (" + age + "/" + gender + ") " + diagnosis); 
    				
    				xssfSheet.createRow(rowNo++);
    				xssfRow = xssfSheet.createRow(rowNo++); 
    				
    				CellStyle tableCellStyle = xssfWb.createCellStyle();
    				tableCellStyle.setBorderTop((short) 5);    
    				tableCellStyle.setBorderBottom((short) 5); 
    				tableCellStyle.setBorderLeft((short) 5);  
    				tableCellStyle.setBorderRight((short) 5); 
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("mm3");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("g/dl");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("%");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("mm3");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("fl");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("pg");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("g/dl");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
    				xssfRow = xssfSheet.createRow(rowNo++);
    				xssfCell = xssfRow.createCell((short) 0);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀1");
    				xssfCell = xssfRow.createCell((short) 1);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀2");
    				xssfCell = xssfRow.createCell((short) 2);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("mm3");
    				xssfCell = xssfRow.createCell((short) 3);
    				xssfCell.setCellStyle(tableCellStyle);
    				xssfCell.setCellValue("셀4");
    				xssfCell = xssfRow.createCell((short) 4);
    				xssfCell.setCellStyle(tableCellStyle);
    				
        	    	String uploadPath = "C:" + File.separator + "Upload" + File.separator + "testfile" + File.separator + "excelfile" + File.separator;
        	    	String saveFileName = patientIdx + ".xlsx"; 
        	    	File downloadFile = new File(uploadPath + saveFileName);
    				
    				FileOutputStream fos = null;
    				fos = new FileOutputStream(downloadFile);
    				xssfWb.write(fos);
    		
    				if (fos != null) fos.close();
    			}catch(Exception e){
    		        	
    			}
    	
    	return "redirect:viewTest";
    }    
    
    
    
    
}
