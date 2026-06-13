<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>MeTA Greeting App</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 40px auto; padding: 20px; }
        h1 { color: #2563eb; }
        input, button { padding: 8px; font-size: 16px; }
        button { background: #2563eb; color: white; border: none; cursor: pointer; border-radius: 4px; }
        button:hover { background: #1d4ed8; }
        .greeting { background: #f0f9ff; padding: 15px; margin-top: 20px; border-radius: 6px; border: 1px solid #bfdbfe; }
        label { display: block; margin-bottom: 6px; }
    </style>
</head>
<body>
    <h1 id="pageTitle">MeTA Corporate Greeting App</h1>
    <p>DevOps Final Project &mdash; MTA 2026 Semester B</p>
    <p id="version" style="color:#16a34a;font-weight:bold;">v4 &mdash; deploy + selenium chained pipeline</p>

    <form method="post" action="index.jsp">
        <label for="name">Your name:</label>
        <input type="text" id="name" name="name" placeholder="Type your name" />
        <button type="submit" id="greetBtn">Greet me</button>
    </form>

    <%
        String name = request.getParameter("name");
        if (name != null && !name.trim().isEmpty()) {
    %>
        <div class="greeting" id="greeting">
            Hello, <strong id="greetingName"><%= name %></strong>! Welcome to MeTA.
        </div>
    <%
        }
    %>

    <p>
        <a id="repoLink" href="https://github.com/roisol144/devops-final-mta-2026" target="_blank">
            View source on GitHub
        </a>
    </p>
</body>
</html>
