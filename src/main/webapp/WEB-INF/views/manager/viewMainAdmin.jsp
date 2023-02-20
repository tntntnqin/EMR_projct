<%@page import="com.hospital.calendar.SolaToLunar"%>
<%@page import="com.hospital.calendar.LunarDate"%>
<%@page import="com.hospital.calendar.MyCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="com.hospital.vo.Employee_20VO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.hospital.vo.Dpart_23List"%>
<%@page import="com.hospital.vo.Employee_20List"%>
<%-- <%@page import="com.hospital.service.PatientService"%> --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자</title>

<meta name="viewpport" content="width=device-width, initial-scale=1">
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.1/jquery.min.js"></script>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.1/dist/css/bootstrap.min.css"
	rel="stylesheet">
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.1/dist/js/bootstrap.bundle.min.js"></script>
<link rel="shortcut icon" type="image/x-icon" href="./images/logo.png" />


</head>
<body>

	<jsp:include page="../header/header.jsp"></jsp:include>
	
<ul class="nav nav-tabs">
  <li class="nav-item">
    <a class="nav-link active" aria-current="page" href="viewMainAdmin">관리자 HOME</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" href="viewAdmin">사원관리</a>
  </li>  
    <c:if test="${admin == '팀장'}">
	  <c:if test="${dpart == '간호사' or dpart == '의사'}">
			  <li class="nav-item">
			    <a class="nav-link" href="viewTeamCalendar">팀스케줄 관리</a>
			  </li>
		</c:if>
		<c:if test="${dpart == '간호사'}">
			  <li class="nav-item">
			    <a class="nav-link" href="viewTeamCalendarNInsert">팀스케줄 등록</a>
			  </li>
		</c:if>
  </c:if>
</ul>
  

	<div style="width: 900px; margin-left: auto; margin-right: auto; margin-top: 20px; border: solid 1px;">
		<br>
		<table style="border: solid 1px; margin-top: 0px; margin-left: 0px; position: relative;">
			 <h4 align="center">승인 대기중</h4>
			<tr>
				<th style="width: 130px; text-align: center;">사번</th>
				<th style="width: 130px; text-align: center;">성명</th>
				<th style="width: 130px; text-align: center;">부서</th>
				<th style="width: 130px; text-align: center;">팀</th>
				<th style="width: 140px; text-align: center;">내선번호</th>
				<th style="width: 150px; text-align: center;">개인연락처</th>
				<th style="width: 90px; text-align: center;">&nbsp;</th>
			</tr>
		</table>
		<c:forEach var="employeeVO" items="${employeeList.employeeList}">
			<c:if test="${employeeVO.sign == null}">
				<button onclick="location.href='viewAdminUpdate?employeeIdx=${employeeVO.employeeIdx}'" class="btn" style="border-color: black;"
					data-bs-eidx="${employeeVO.employeeIdx}"
					data-bs-name="${employeeVO.name}"
					data-bs-dnum="${employeeVO.dnumber}"
					data-bs-enum="${employeeVO.enumber}">
					<table style="border: solid 1px; margin-top: 0px; margin-left: 0px; position: relative;">
						<tr>
							<td style="width: 125px; text-align: center;">${employeeVO.employeeIdx}</td>
							<td style="width: 125px; text-align: center;">${employeeVO.name}</td>
							<c:if test="${employeeVO.dpart == '의사'}">
								<td id="eDpart${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">의무과</td>
								<td id="eTeam${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">${employeeVO.doctorT}팀</td>
							</c:if>
							<c:if test="${employeeVO.dpart == '간호사'}">
								<td id="eDpart${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">간호과</td>
								<td id="eTeam${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">${employeeVO.nurseT}팀</td>
							</c:if>
							<c:if test="${employeeVO.dpart == '병리사'}">
								<td id="eDpart${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">병리과</td>
								<td id="eTeam${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">병리팀</td>
							</c:if>
							<c:if test="${employeeVO.dpart == '원무과'}">
								<td id="eDpart${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">원무과</td>
								<td id="eTeam${employeeVO.employeeIdx}" style="width: 125px; text-align: center;">원무팀</td>
							</c:if>
							<td style="width: 150px; text-align: center;">${employeeVO.dnumber}</td>
							<td style="width: 150px; text-align: center;">${employeeVO.enumber}</td>
							<td></td>
						</tr>
					</table>
				</button>
				
				<c:if test="${employeeVO.dpart == dpart}">
				<input type="button" class="update" onclick="location.href='updateSign?employeeIdx=${employeeVO.employeeIdx}'" value="승인" />
				</c:if>
				
			</c:if>

		</c:forEach>
	</div>



	<div style="width: 900px; margin-left: auto; margin-right: auto; margin-top: 20px; border: solid 1px;">
	<div><h4 align="center">병원 일정</h4></div>
	<jsp:include page="../manager/calendar.jsp"></jsp:include>

	</div>


	<script type="text/javascript">

onload = () => {
	let item = document.querySelector('#item');
	item.focus();
	
}

function checkOnly(obj) {
	
	if (obj == 1) {
		item.value = '';
		item.focus();
		
	} else if (obj == 2) {
		
		item.value = '';
		item.focus();
		
	} else if (obj == 3) {
		let item = document.querySelector('#item');
		
		if (item.type == 'text') {
			item.type = 'date';
		} else {
			item.type = 'text';
			item.value = '';
			item.focus();
		}
		
	}
}

function update() {
	$('.update').click(function() {		
		location.href = 'updateSign'
	})
}
</script>
</body>
</html>