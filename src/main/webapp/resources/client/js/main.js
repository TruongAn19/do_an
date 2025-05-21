(function ($) {
    "use strict";

    // Spinner
    var spinner = function () {
        setTimeout(function () {
            if ($('#spinner').length > 0) {
                $('#spinner').removeClass('show');
            }
        }, 1);
    };
    spinner(0);


    // Fixed Navbar
    $(window).scroll(function () {
        if ($(window).width() < 992) {
            if ($(this).scrollTop() > 55) {
                $('.fixed-top').addClass('shadow');
            } else {
                $('.fixed-top').removeClass('shadow');
            }
        } else {
            if ($(this).scrollTop() > 55) {
                $('.fixed-top').addClass('shadow').css('top', 0);
            } else {
                $('.fixed-top').removeClass('shadow').css('top', 0);
            }
        }
    });


    // Back to top button
    $(window).scroll(function () {
        if ($(this).scrollTop() > 300) {
            $('.back-to-top').fadeIn('slow');
        } else {
            $('.back-to-top').fadeOut('slow');
        }
    });
    $('.back-to-top').click(function () {
        $('html, body').animate({ scrollTop: 0 }, 1500, 'easeInOutExpo');
        return false;
    });


    // Testimonial carousel
    $(".testimonial-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 2000,
        center: false,
        dots: true,
        loop: true,
        margin: 25,
        nav: true,
        navText: [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0: {
                items: 1
            },
            576: {
                items: 1
            },
            768: {
                items: 1
            },
            992: {
                items: 2
            },
            1200: {
                items: 2
            }
        }
    });


    // vegetable carousel
    $(".vegetable-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1500,
        center: false,
        dots: true,
        loop: true,
        margin: 25,
        nav: true,
        navText: [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0: {
                items: 1
            },
            576: {
                items: 1
            },
            768: {
                items: 2
            },
            992: {
                items: 3
            },
            1200: {
                items: 4
            }
        }
    });


    // Modal Video
    $(document).ready(function () {
        var $videoSrc;
        $('.btn-play').click(function () {
            $videoSrc = $(this).data("src");
        });
        console.log($videoSrc);

        $('#videoModal').on('shown.bs.modal', function (e) {
            $("#video").attr('src', $videoSrc + "?autoplay=1&amp;modestbranding=1&amp;showinfo=0");
        })

        $('#videoModal').on('hide.bs.modal', function (e) {
            $("#video").attr('src', $videoSrc);
        })

        //add active class to header
        const navElement = $("#navbarCollapse");
        const currentUrl = window.location.pathname;
        navElement.find('a.nav-link').each(function () {
            const link = $(this); // Get the current link in the loop
            const href = link.attr('href'); // Get the href attribute of the link

            if (href === currentUrl) {
                link.addClass('active'); // Add 'active' class if the href matches the current URL
            } else {
                link.removeClass('active'); // Remove 'active' class if the href does not match
            }
        });
    });


    $('.quantity button').on('click', function () {
        let change = 0;

        var button = $(this);
        var oldValue = button.parent().parent().find('input').val();
        if (button.hasClass('btn-plus')) {
            var newVal = parseFloat(oldValue) + 1;
            change = 1;
        } else {
            if (oldValue > 1) {
                var newVal = parseFloat(oldValue) - 1;
                change = -1;
            } else {
                newVal = 1;
            }
        }

        // Cập nhật giá trị vào ô input số lượng
        const input = button.parent().parent().find('input');
        input.val(newVal);

        // Lấy index của sản phẩm
        const index = input.attr("data-cart-detail-index");
        const el = document.getElementById(`cartDetails${index}.quantity`);
        $(el).val(newVal);

        // Lấy thông tin giá và giảm giá của sản phẩm
        const sale = input.attr("data-cart-detail-sale");
        const price = input.attr("data-cart-detail-price");
        const id = input.attr("data-cart-detail-id");

        // Cập nhật giá của từng sản phẩm
        const priceElement = $(`p[data-cart-detail-id='${id}']`);
        if (priceElement) {
            const newPrice = +price * newVal - (+price * newVal * (+sale) / 100);
            priceElement.text(formatCurrency(newPrice.toFixed(2)) + " đ");
        }

        // Cập nhật tổng giá giỏ hàng
        const totalPriceElement = $(`p[data-cart-total-price]`);

        if (totalPriceElement && totalPriceElement.length) {
            let newTotal = 0;

            // Duyệt qua tất cả sản phẩm trong giỏ hàng để tính tổng
            $(".quantity input").each(function () {
                const quantity = +$(this).val();
                const itemPrice = +$(this).attr("data-cart-detail-price");
                const itemSale = +$(this).attr("data-cart-detail-sale");

                const itemTotal = itemPrice * quantity - (itemPrice * quantity * itemSale / 100);
                newTotal += itemTotal;
            });

            // Cập nhật tổng tiền trên giao diện
            totalPriceElement.each(function (index, element) {
                $(element).text(formatCurrency(newTotal.toFixed(2)) + " đ");
                $(element).attr("data-cart-total-price", newTotal);
            });
        }
    });

    function formatCurrency(value) {
        // Use the 'vi-VN' locale to format the number according to Vietnamese currency format
        // and 'VND' as the currency type for Vietnamese đồng
        const formatter = new Intl.NumberFormat('vi-VN', {
            style: 'decimal',
            minimumFractionDigits: 0, // No decimal part for whole numbers
        });

        let formatted = formatter.format(value);
        // Replace dots with commas for thousands separator
        formatted = formatted.replace(/\./g, ',');
        return formatted;
    }



    //handle auto checkbox after page loading
    const params = new URLSearchParams(window.location.search);

    // Set checkboxes for 'address' (sửa từ target)
    if (params.has('address')) {
        const addresses = params.get('address').split(',');
        addresses.forEach(address => {
            $(`#addressFilter .form-check-input[value="${address}"]`).prop('checked', true);
        });
    }

    // Set checkboxes for 'price'
    if (params.has('price')) {
        const prices = params.get('price').split(',');
        prices.forEach(price => {
            $(`#priceFilter .form-check-input[value="${price}"]`).prop('checked', true);
        });
    }

    // Set radio buttons for 'sort'
    if (params.has('sort')) {
        const sort = params.get('sort');
        $(`input[type="radio"][name="radio-sort"][value="${sort}"]`).prop('checked', true);
    }

    // time-handler
    $(document).ready(function () {
        function updateSelectedTimeId() {
            const selectedTimeId = $('#timeSelect').val();
            console.log("Time ID đang chọn: ", selectedTimeId);
            $('#hiddenAvailableTimeId').val(selectedTimeId);
        }

        // Gọi lúc trang load
        updateSelectedTimeId();

        // Gọi khi người dùng đổi dropdown
        $('#timeSelect').on('change', updateSelectedTimeId);
    });

    $(document).ready(function () {
        $("#timeSelect").change(function () {
            $("#hiddenAvailableTimeId").val($(this).val());
        });

        $("#courtSelect").change(function () {
            $("#hiddenCourtId").val($(this).val());
        });

        // Khởi tạo giá trị ban đầu
        $("#hiddenAvailableTimeId").val($("#timeSelect").val());
        $("#hiddenCourtId").val($("#courtSelect").val());
    });

    $(document).ready(function() {
        // Đảm bảo quantity luôn có giá trị
        $('#quantity').val(1);
        
        $('#courtSelect').change(function() {
            if ($(this).val()) {
                $('#quantity').val(1);
                console.log('Quantity set to 1');
            }
        });
    });

    function loadAvailableTimes() {
        const date = $("#bookingDate").val();     // Lấy ngày người dùng chọn
        const courtId = $("#subCourt").val();      // Lấy ID sân người dùng chọn

        if (!date || !courtId) return;             // Nếu thiếu ngày hoặc sân thì không gọi API

        $.get("/api/available-time", { date, courtId }, function (data) {
            const timeSelect = $("#availableTime");
            timeSelect.empty();                   // Xóa hết các option cũ trong select khung giờ

            if (data.length === 0) {
                timeSelect.append('<option disabled selected>Không còn giờ nào</option>');
            } else {
                data.forEach(t => {
                    timeSelect.append(`<option value="${t.id}">${t.time}</option>`);
                });
            }
        });
    }

    $(document).ready(function () {
        $("#bookingDate, #subCourt").on("change", loadAvailableTimes);
    });

    document.addEventListener('DOMContentLoaded', function () {
        const courtSelects = document.querySelectorAll('.court-select');
        const hiddenQuantity = document.getElementById('hiddenQuantity');

        courtSelects.forEach(function (select) {
            select.addEventListener('change', function () {
                hiddenQuantity.value = 1; // Mặc định chọn sân thì số lượng là 1
                document.getElementById('hiddenCourtId').value = this.value; // Cập nhật sân được chọn
            });
        });

        const timeSelect = document.getElementById('timeSelect');
        timeSelect.addEventListener('change', function () {
            document.getElementById('hiddenAvailableTimeId').value = this.value; // Cập nhật giờ được chọn
        });
    });


})(jQuery);

