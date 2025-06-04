<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>--%>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">
    <title>${post.area} - ${post.playDate}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link href="/client/css/chat.css" rel="stylesheet">


</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<div class="main-container">
    <!-- Post Detail Section -->
    <div class="post-detail-card" style="margin-top: 100px">
        <div class="post-header">
            <h2><i class="fas fa-map-marker-alt"></i> ${post.area}</h2>
            <div class="post-status-badge ${post.status == 'open' ? 'bg-success' : 'bg-secondary'}">
                <i class="fas ${post.status == 'open' ? 'fa-check-circle' : 'fa-times-circle'}"></i>
                ${post.status == 'open' ? 'Đang mở' : 'Đã đủ người'}
            </div>
        </div>

        <div class="post-meta">
            <span class="post-meta-item">
                <i class="far fa-calendar-alt"></i>
                ${post.playDateStr}
            </span>
            <span class="post-meta-item">
                <i class="far fa-clock"></i>
                ${post.timeSlot}
            </span>
            <span class="post-meta-item">
                <i class="fas fa-trophy"></i>
                ${post.skillLevel}
            </span>
            <span class="post-meta-item">
                <i class="fas fa-users"></i>
                ${post.currentParticipants}/${post.maxParticipants} người
            </span>
        </div>

        <div class="post-content">
            <div class="post-section">
                <h4><i class="fas fa-align-left"></i> Mô tả</h4>
                <p>${post.description}</p>
            </div>

            <div class="post-section">
                <h4><i class="fas fa-user"></i> Người đăng</h4>
                <div class="user-info">
                    <%--                    <img src="${post.user.avatar}" alt="Avatar" class="user-avatar">--%>
                    <span class="user-name">${post.user.fullName}</span>
                </div>
            </div>

            <div class="post-section">
                <h4><i class="fas fa-users"></i> Danh sách tham gia (${participants.size()} người)</h4>
                <c:choose>
                    <c:when test="${not empty participants}">
                        <div class="participants-list">
                            <c:forEach items="${participants}" var="participant">
                                <div class="participant-item">
                                    <span class="participant-name">${participant.user.fullName}</span>
                                    <c:if test="${post.user.id == currentUser.id || participant.user.id == currentUser.id}">
                                        <form action="/match-posts/${post.id}/leave" method="post">
                                            <input type="hidden" name="userId" value="${participant.user.id}">
                                            <button type="submit" class="btn btn-outline-danger btn-sm">
                                                <i class="fas fa-user-minus"></i> Rời
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="fas fa-users"></i>
                            <p>Chưa có người tham gia</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="post-actions">
            <c:choose>
                <c:when test="${post.user.id == currentUser.id}">
                    <form action="/match-posts/${post.id}/cancel" method="post" class="d-inline">
                        <button type="submit" class="btn btn-danger">
                            <i class="fas fa-times"></i> Hủy bài đăng
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${alreadyJoined}">
                            <form action="/match-posts/${post.id}/leave" method="post" class="d-inline">
                                <button type="submit" class="btn btn-warning">
                                    <i class="fas fa-user-minus"></i> Rời khỏi
                                </button>
                            </form>
                        </c:when>
                        <c:when test="${post.status == 'open'}">
                            <form action="/match-posts/${post.id}/join" method="post" class="d-inline">
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-user-plus"></i> Tham gia
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <button class="btn btn-secondary" disabled>
                                <i class="fas fa-user-times"></i> Đã đủ người
                            </button>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
            <a href="/match-posts" class="btn btn-outline-primary">
                <i class="fas fa-arrow-left"></i> Quay lại
            </a>
        </div>
    </div>

    <!-- Chat Section -->
    <div class="chat-section" style="margin-top: 100px">
        <div class="chat-title">
            <i class="fas fa-comments"></i> Tin nhắn
        </div>

        <div id="chat-messages" class="chat-container">
            <c:choose>
                <c:when test="${not empty messages}">
                    <c:forEach items="${messages}" var="message">
                        <div class="message ${message.senderId == currentUser.id ? 'user-message' : 'other-message'}" id="message-${message.id}">
                            <div class="message-sender">${message.senderName}</div>
                            <div class="message-bubble">
                                <div class="message-content">${message.content}</div>
                                <div class="message-time">
                                    <fmt:formatDate value="${message.sentAt}" pattern="HH:mm dd/MM/yyyy"/>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
<%--                <c:otherwise>--%>
<%--                    <div class="empty-state">--%>
<%--                        <i class="fas fa-comments"></i>--%>
<%--                        <p>Chưa có tin nhắn nào</p>--%>
<%--                    </div>--%>
<%--                </c:otherwise>--%>
            </c:choose>
        </div>

        <div class="chat-input">
            <form id="chat-form">
                <textarea id="message-content" class="form-control" rows="2" placeholder="Nhập tin nhắn..." required></textarea>
                <button type="button" id="send-button" class="btn btn-primary">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.5.0/dist/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<script>
    const postId = ${post.id};
    const currentUserId = ${currentUser.id};
    const currentUserName = "${currentUser.fullName}";
    let stompClient = null;

    function connect() {
        const socket = new SockJS("/ws");
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            stompClient.subscribe('/topic/chat/' + postId, function (messageOutput) {
                const message = JSON.parse(messageOutput.body);
                if (message.senderId != currentUserId || message.id) {
                    showNewMessage(message);
                }
            });
        });
    }

    function sendMessage() {
        const content = $('#message-content').val().trim();
        if (!content) return;

        if (stompClient && stompClient.connected) {
            stompClient.send("/app/chat/" + postId + "/send", {},
                JSON.stringify({
                    content: content
                }));
        }

        $('#message-content').val('').focus();
    }

    function showNewMessage(message) {
        if (!message || !message.content) return;

        if (message.id && $('#message-' + message.id).length > 0) {
            return;
        }

        const isCurrentUser = Number(message.senderId) === Number(currentUserId);
        const messageClass = isCurrentUser ? 'user-message' : 'other-message';

        const messageElement = $('<div>')
            .addClass('message ' + messageClass)
            .attr('id', message.id ? 'message-' + message.id : '');

        const senderElement = $('<div>')
            .addClass('message-sender')
            .text(message.senderName || 'Người dùng');

        const bubbleElement = $('<div>')
            .addClass('message-bubble');

        const contentElement = $('<div>')
            .addClass('message-content')
            .text(message.content);

        bubbleElement.append(contentElement);

        if (message.sentAt) {
            const timeElement = $('<div>')
                .addClass('message-time')
                .text(message.sentAt);
            bubbleElement.append(timeElement);
        }

        messageElement.append(senderElement);
        messageElement.append(bubbleElement);

        $('#chat-messages').append(messageElement);
        $('#chat-messages').scrollTop($('#chat-messages')[0].scrollHeight);
    }

    $(document).ready(function () {
        connect();

        $('#send-button').click(sendMessage);

        $('#message-content').keypress(function (e) {
            if (e.which === 13 && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        $('#chat-messages').scrollTop($('#chat-messages')[0].scrollHeight);
    });
</script>
</body>
</html>