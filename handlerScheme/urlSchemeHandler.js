// urlSchemeHandler.js

class URLSchemeHandler {
    constructor(options) {
        this.scheme = options.scheme;          // URL Scheme (e.g., "line://")
        this.packageName = options.packageName; // Package Name (e.g., "jp.naver.line.android")
        this.timeout = options.timeout || 500;  // 超時時間，預設 500ms
    }

    // 開啟應用程式
    openApp() {
        // 記錄當前時間
        const startTime = Date.now();
        
        // 監聽頁面可見性變化
        const handleVisibilityChange = () => {
            // 如果頁面變為隱藏，表示成功打開應用程式
            if (document.hidden) {
                clearTimeout(timer);
                document.removeEventListener('visibilitychange', handleVisibilityChange);
            }
        };
        
        // 設定超時計時器，若超時則開啟商店
        const timer = setTimeout(() => {
            // 移除監聽器
            document.removeEventListener('visibilitychange', handleVisibilityChange);
            
            // 如果經過的時間小於兩倍的超時時間，表示應用程式未打開
            if (Date.now() - startTime < (this.timeout * 2)) {
                // 開啟 Play Store
                window.location.href = `market://details?id=${this.packageName}`;
                
                // 如果無法開啟 market:// 協議，則使用網頁版
                setTimeout(() => {
                    window.location.href = `https://play.google.com/store/apps/details?id=${this.packageName}`;
                }, 100);
            }
        }, this.timeout);

        // 監聽頁面可見性變化
        document.addEventListener('visibilitychange', handleVisibilityChange);

        // 嘗試開啟應用程式
        window.location.href = this.scheme;
    }
}

// 使用範例
const openLine = new URLSchemeHandler({
    scheme: 'https://cobeco.github.io',
    packageName: 'com.bok.cordova',
    timeout: 500
});

// HTML 按鈕點擊事件處理
document.getElementById('openLineBtn').onclick = () => {
    openLine.openApp();
};
