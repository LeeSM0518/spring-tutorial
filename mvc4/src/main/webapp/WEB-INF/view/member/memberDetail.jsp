<%--
  Created by IntelliJ IDEA.
  User: soengwon
  Date: 2020/03/26
  Time: 6:50 오후
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="tf" tagdir="/WEB-INF/tags" %>
<html>
<head>
    <title>회원 정보</title>
</head>
<body>
<p>아이디: ${member.id}</p>
<p>이메일: ${member.email}</p>
<p>이름: ${member.name}</p>
<p>가입일: <tf:formatDateTime value="${member.registerDateTime}" pattern="yyyy-MM-dd HH:mm"/> </p>
</body>
</html>
