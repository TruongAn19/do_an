document.addEventListener("DOMContentLoaded", () => {
    const userId = "${sessionScope.id}" // Lấy userId từ session
    if (!userId) return

    const topic = "user-" + userId
    const eventSource = new EventSource("/ntfy-sse/" + topic)

    eventSource.onmessage = async (event) => {
        try {
            const data = JSON.parse(event.data)
            console.log("SSE data nhận được:", data)

            if (data.event === "message") {
                // Nếu có file đính kèm là file text
                if (data.attachment && data.attachment.type.startsWith("text/plain")) {
                    try {
                        const response = await fetch(data.attachment.url)
                        const buffer = await response.arrayBuffer()
                        const decoder = new TextDecoder("iso-8859-1") // hoặc "utf-8" nếu biết chắc
                        const fileContent = decoder.decode(buffer)

                        Swal.fire({
                            icon: "info",
                            title: data.title || "Thông báo",
                            html: `
                <p><strong>${data.message || "Nội dung đính kèm:"}</strong></p>
                <pre style="text-align: left; white-space: pre-wrap; word-wrap: break-word; background: #f8f9fa; padding: 12px; border-radius: 5px; font-family: 'Courier New', monospace;">${fileContent}</pre>
              `,
                            showCloseButton: true,
                            showConfirmButton: false,
                            background: "#f0f8ff",
                            width: 600,
                        })
                    } catch (e) {
                        console.error("Lỗi khi đọc file:", e)
                        Swal.fire({
                            icon: "warning",
                            title: "Không đọc được file đính kèm",
                            text: "Bạn có thể tải file về để xem nội dung.",
                            footer: `<a href="${data.attachment.url}" target="_blank" download="${data.attachment.name}">Tải file: ${data.attachment.name}</a>`,
                            showCloseButton: true,
                            background: "#fff3cd",
                            width: 500,
                        })
                    }
                } else {
                    // Không có file hoặc không phải file text
                    Swal.fire({
                        toast: true,
                        position: "bottom-end",
                        icon: "info",
                        title: data.title || "Thông báo",
                        text: data.message || "",
                        showConfirmButton: false,
                        showCloseButton: true,
                        timer: 5000,
                        timerProgressBar: true,
                        background: "#f0f8ff",
                        iconColor: "#3085d6",
                        customClass: {
                            popup: "custom-toast-popup",
                            title: "custom-toast-title",
                            content: "custom-toast-content",
                            closeButton: "custom-toast-close",
                        },
                        didOpen: (toast) => {
                            toast.addEventListener("mouseenter", Swal.stopTimer)
                            toast.addEventListener("mouseleave", Swal.resumeTimer)
                        },
                    })
                }
            }
        } catch (e) {
            console.error("Lỗi khi xử lý thông báo:", e)
            Swal.fire({
                toast: true,
                position: "bottom-end",
                icon: "error",
                title: "Có lỗi xảy ra!",
                showConfirmButton: false,
                showCloseButton: true,
                timer: 3000,
                timerProgressBar: true,
                background: "#fff0f0",
                customClass: {
                    popup: "custom-toast-popup",
                    title: "custom-toast-title",
                },
            })
        }
    }

    window.addEventListener("beforeunload", () => {
        if (eventSource) {
            eventSource.close()
        }
    })

    // Thêm CSS tùy chỉnh cho toast
    const style = document.createElement("style")
    style.textContent = `
    .custom-toast-popup {
        padding: 12px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        border-left: 4px solid #3085d6;
        margin: 16px;
        animation: slideInRight 0.3s ease-out;
    }

    .custom-toast-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
    }

    .custom-toast-content {
        font-size: 14px;
        color: #555;
    }

    .custom-toast-close {
        color: #888;
    }

    .custom-toast-close:hover {
        color: #333;
    }

    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
  `
    document.head.appendChild(style)
})
