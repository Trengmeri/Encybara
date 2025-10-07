package com.example.test.ui.study;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient; // Thêm import này nếu chưa có
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.test.R;
import com.example.test.api.ApiCallback;
import com.example.test.api.CourseManager;
import com.example.test.model.MediaFile;
import com.example.test.api.BaseApiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MaterialCourseActivity extends AppCompatActivity {

    private WebView webView;
    private CourseManager courseManager;
    private String loadedMarkdownText; // Biến để lưu trữ Markdown đã tải

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_material_course);

        webView = findViewById(R.id.webViewMaterial); // Đảm bảo ID này khớp với layout của bạn

        // Cấu hình WebSettings: RẤT QUAN TRỌNG cho video và JS
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Bật JavaScript
        settings.setDomStorageEnabled(true); // Cần thiết cho một số thư viện JS và bộ nhớ cục bộ
        settings.setDatabaseEnabled(true); // Cần thiết cho một số thư viện JS
        settings.setAllowFileAccess(true); // Cho phép truy cập file (ví dụ: assets)
        settings.setLoadWithOverviewMode(true); // Tải trang ở chế độ overview
        settings.setUseWideViewPort(true); // Cho phép viewport rộng, hữu ích cho responsive design
        settings.setBuiltInZoomControls(true); // Bật điều khiển zoom
        settings.setDisplayZoomControls(false); // Ẩn nút điều khiển zoom
        settings.setMediaPlaybackRequiresUserGesture(false); // Cho phép tự động phát video

        // Thiết lập để WebView có thể xử lý các cảnh báo SSL và video
        // Đây là một cài đặt mạnh mẽ, chỉ nên dùng khi bạn tin tưởng nội dung
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Khi template HTML đã tải xong, inject Markdown vào
                if (loadedMarkdownText != null && url != null && url.contains("markdown_template.html")) {
                    // Escaping backticks và các ký tự đặc biệt khác trong Markdown
                    // Đặc biệt quan trọng cho dấu ` và các ký tự dòng mới
                    String escapedMarkdown = loadedMarkdownText
                            .replace("\\", "\\\\") // Escape backslashes first
                            .replace("`", "\\`") // Escape backticks
                            .replace("\n", "\\n") // Escape newlines
                            .replace("\r", "");   // Remove carriage returns

                    String javascript = "javascript:setMarkdown(`" + escapedMarkdown + "`);";
                    view.evaluateJavascript(javascript, null);
                    Log.d("WebView", "Markdown injected via JavaScript.");
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("WebViewError", "Error: " + description + " at " + failingUrl);
                runOnUiThread(() -> {
                    webView.loadData("<html><body><h1>Error loading content</h1><p>" + description + "</p></body></html>", "text/html", "UTF-8");
                });
            }

            // Mở tất cả các liên kết trong WebView, không mở trình duyệt ngoài
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Quan trọng: Để video chạy, bạn cần WebChromeClient
        // để xử lý các yêu cầu liên quan đến UI của trình duyệt (như fullscreen video)
        webView.setWebChromeClient(new CustomWebChromeClient());


        // Áp dụng padding hệ thống (giữ lại nếu cần cho EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        courseManager = new CourseManager(this);

        int courseId = getIntent().getIntExtra("courseId", 24); // Lấy courseId từ Intent
        fetchMaterial(courseId);
    }

    // Cần thêm WebChromeClient để hỗ trợ video fullscreen và các dialog JavaScript
    private class CustomWebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onShowCustomView(android.view.View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            // Xử lý khi video fullscreen được hiển thị (ví dụ: ẩn các UI khác)
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            // Xử lý khi video fullscreen bị ẩn (ví dụ: hiển thị lại các UI đã ẩn)
        }
    }



    private void fetchMaterial(int courseId) {
        courseManager.fetchMaterialsByCourse(courseId, new ApiCallback<List<MediaFile>>() {
            @Override
            public void onSuccess() {}

            @Override
            public void onSuccess(List<MediaFile> materials) {
                runOnUiThread(() -> {
                    if (materials != null && !materials.isEmpty()) {
                        String link = materials.get(0).getMaterLink();
                        if (link != null && link.endsWith(".md")) {
                            String realUrl = BaseApiManager.replaceHost(link);
                            loadMarkdownToWebView(realUrl);
                        } else {
                            // Xử lý các loại materType khác nếu có, hoặc hiển thị thông báo lỗi
                            webView.loadData("<html><body><h1>Unsupported Material Type</h1><p>The material link does not end with .md or is not recognized.</p></body></html>", "text/html", "UTF-8");
                        }
                    } else {
                        webView.loadData("<html><body><h1>No Material Found</h1><p>There are no learning materials for this course.</p></body></html>", "text/html", "UTF-8");
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FETCH_MATERIAL_ERROR", errorMessage);
                runOnUiThread(() -> {
                    webView.loadData("<html><body><h1>Error</h1><p>Failed to load materials: " + errorMessage + "</p></body></html>", "text/html", "UTF-8");
                });
            }
        });
    }

    private void loadMarkdownToWebView(String fileUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Đọc nội dung Markdown
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder markdownBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    markdownBuilder.append(line).append("\n");
                }
                reader.close();

                loadedMarkdownText = markdownBuilder.toString(); // Lưu Markdown vào biến

                runOnUiThread(() -> {
                    // Tải template HTML cục bộ. Sau khi tải xong, onPageFinished sẽ được gọi.
                    // Đảm bảo "markdown_template.html" nằm trong thư mục "assets"
                    webView.loadUrl("file:///android_asset/markdown_template.html");
                });

            } catch (IOException e) {
                Log.e("LOAD_MD_ERROR", "Error loading markdown from URL: " + e.getMessage());
                runOnUiThread(() -> {
                    webView.loadData("<html><body><h1>Error</h1><p>Could not fetch markdown content from " + fileUrl + ": " + e.getMessage() + "</p></body></html>", "text/html", "UTF-8");
                });
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause(); // Tạm dừng mọi hoạt động trong WebView khi Activity tạm dừng
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume(); // Tiếp tục hoạt động trong WebView khi Activity tiếp tục
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            // Hủy WebView để giải phóng tài nguyên và tránh rò rỉ bộ nhớ
            webView.destroy();
        }
    }
}