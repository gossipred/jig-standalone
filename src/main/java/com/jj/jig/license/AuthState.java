package com.jj.jig.license;

public enum AuthState {
    MASTER,       // 萬用金鑰，無任何限制
    LICENSED,     // 正式授權（買斷 or 訂閱有效中）
    TRIAL,        // 試用期內
    EXPIRED,      // 試用到期 or 訂閱到期
    INVALID       // .lic 簽名錯誤 or 機器 ID 不符
}
